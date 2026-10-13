package com.example.csv_project;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@AllArgsConstructor
public class CsvService {

    /**
     * 특정 CSV 파일을 열었을 때 글자가 깨지는지 확인하고 수정하는 단독 테스트 메서드 (기존 유지)
     * @param targetCsvPath 문제가 되는 CSV 파일
     */
    public void testAndFixEncoding(MultipartFile targetCsvPath) {
        String originalFilename = targetCsvPath.getOriginalFilename();
        String finalFileName = "unknown_file"; // 기본 이름

        if (originalFilename != null) {
            String normalizedPath = originalFilename.replace("\\", "/");
            String[] pathSegments = normalizedPath.split("/");

            if (pathSegments.length >= 3) {
                int targetIndex = pathSegments.length - 2;
                finalFileName = pathSegments[targetIndex];
            } else if (pathSegments.length == 2) {
                finalFileName = pathSegments[0];
            }
        }

        System.out.println("=== 2번째 폴더 이름으로 바뀐 최종 파일명: " + finalFileName + ".xlsx ===");

        Charset charset = Charset.forName("MS932");

        System.out.println("=== 파일 인코딩 읽기 시작: "
                + targetCsvPath.getOriginalFilename() + " ===");

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(targetCsvPath.getInputStream(), charset))) {

            String line;
            int lineCount = 0;

            while ((line = br.readLine()) != null) {
                lineCount++;

                if (lineCount <= 5) {
                    System.out.println("Line " + lineCount + ": " + line);
                }
            }

            System.out.println("=== 파일 읽기 성공! (총 "
                    + lineCount + "줄) ===");

        } catch (IOException e) {
            System.err.println("파일 읽기 실패: " + e.getMessage());
        }
    }


    /**
     * 💡 [추가된 기능] 사용자가 업로드한 ZIP 파일을 서버의 임시 폴더에 안전하게 해제한 뒤,
     * 내부 폴더를 순회하며 2번째 폴더 이름으로 CSV 파일을 식별하여 처리하는 메인 메서드
     * @param zipFile 사용자가 업로드한 짚(ZIP) 압축 파일
     */
    public void processZipFile(MultipartFile zipFile) {
        if (zipFile == null || zipFile.isEmpty()) {
            System.err.println("업로드된 ZIP 파일이 비어 있거나 존재하지 않습니다.");
            return;
        }

        try {
            // 1. 서버에 임시 디렉토리 생성
            Path tempDir = Files.createTempDirectory("csv_unzip_");
            System.out.println("=== ZIP 압축 해제 시작: " + zipFile.getOriginalFilename() + " ===");

            // 2. ZIP 스트림을 열어 압축 해제 (일본어 파일명 깨짐 방지를 위해 MS932 또는 UTF-8 대응)
            try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream(), Charset.forName("MS932"))) {
                ZipEntry zipEntry;

                while ((zipEntry = zis.getNextEntry()) != null) {
                    Path resolvedPath = tempDir.resolve(zipEntry.getName()).normalize();

                    // 보안 취약점(Zip Slip) 방어 코드
                    if (!resolvedPath.startsWith(tempDir)) {
                        throw new IOException("잘못된 경로의 ZIP 엔트리가 감지되었습니다: " + zipEntry.getName());
                    }

                    if (zipEntry.isDirectory()) {
                        Files.createDirectories(resolvedPath);
                    } else {
                        if (resolvedPath.getParent() != null) {
                            Files.createDirectories(resolvedPath.getParent());
                        }
                        Files.copy(zis, resolvedPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                    zis.closeEntry();
                }
            }

            System.out.println("=== ZIP 압축 해제 완료. 내부 CSV 파일 탐색 및 변환 시작 ===");

            // 3. 압축이 해제된 임시 폴더 경로를 기존의 디렉토리 순회 메서드로 전달!
            processAllCsvFiles(tempDir.toString());

        } catch (IOException e) {
            System.err.println("ZIP 파일 처리 중 에러 발생: " + e.getMessage());
        }
    }


    /**
     * 최상위 집 폴더 경로를 입력받아 내부를 순회하며
     * CSV 파일이 있는 폴더만 찾아내고, 2번째 폴더 이름을 추출해 처리하는 메서드
     * @param rootDirectoryPath 최상위 집 폴더 경로
     */
    public void processAllCsvFiles(String rootDirectoryPath) {
        Path rootPath = Paths.get(rootDirectoryPath);

        if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
            System.err.println("유효하지 않은 디렉토리 경로입니다: " + rootDirectoryPath);
            return;
        }

        try (Stream<Path> pathStream = Files.walk(rootPath)) {
            pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".csv"))
                    .forEach(csvPath -> {
                        try {
                            String secondFolderName = "unknown_folder";

                            Path relativePath = rootPath.relativize(csvPath);
                            if (relativePath.getNameCount() >= 2) {
                                secondFolderName = relativePath.getName(0).toString();
                            }

                            String finalExcelName = secondFolderName + ".xlsx";
                            System.out.println("\n[처리 중] 발견된 파일: " + csvPath.getFileName()
                                    + " -> 2번째 폴더명 적용 엑셀 이름: " + finalExcelName);

                            readCsvWithEncoding(csvPath);

                        } catch (Exception e) {
                            System.err.println("파일 처리 중 에러 발생: " + csvPath + " / 원인: " + e.getMessage());
                        }
                    });

        } catch (IOException e) {
            System.err.println("디렉토리 순회 중 에러 발생: " + e.getMessage());
        }
    }

    /**
     * 개별 경로(Path)의 파일을 MS932 인코딩으로 안전하게 읽어 처리하는 내부 공통 메서드
     */
    private void readCsvWithEncoding(Path filePath) {
        Charset charset = Charset.forName("MS932");

        try (BufferedReader br = Files.newBufferedReader(filePath, charset)) {
            String line;
            int lineCount = 0;

            while ((line = br.readLine()) != null) {
                lineCount++;
                if (lineCount <= 3) {
                    System.out.println("  > 샘플 내용 [" + lineCount + "]: " + line);
                }
            }
            System.out.println("  ✔ 읽기 완료 (총 " + lineCount + "줄)");

        } catch (IOException e) {
            System.err.println("  ❌ 파일 읽기 실패: " + e.getMessage());
        }
    }

    //TODO csv >> 엑셀로 전환

}