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
     * 특정 단건 CSV 파일을 열었을 때 글자가 깨지는지 확인하고 수정하는 단독 테스트 메서드 (기존 유지)
     */
    public void testAndFixEncoding(MultipartFile targetCsvPath) {
        String originalFilename = targetCsvPath.getOriginalFilename();
        String finalFileName = "unknown_file";

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

        System.out.println("=== 파일 인코딩 읽기 시작: " + targetCsvPath.getOriginalFilename() + " ===");

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

            System.out.println("=== 파일 읽기 성공! (총 " + lineCount + "줄) ===");

        } catch (IOException e) {
            System.err.println("파일 읽기 실패: " + e.getMessage());
        }
    }


    /**
     * 1. ZIP 파일을 받아 서버 임시 공간에 압축을 풀고, 내부 폴더 순회 로직으로 연결하는 메인 메서드
     */
    public void processZipFile(MultipartFile zipFile) {
        if (zipFile == null || zipFile.isEmpty()) {
            System.err.println("업로드된 ZIP 파일이 비어 있거나 존재하지 않습니다.");
            return;
        }

        try {
            // 서버에 임시 디렉토리 생성
            Path tempDir = Files.createTempDirectory("csv_unzip_");
            System.out.println("=== ZIP 압축 해제 시작: " + zipFile.getOriginalFilename() + " ===");

            // ZIP 압축 해제 (일본어 파일명 깨짐 방지를 위해 MS932 적용)
            try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream(), Charset.forName("MS932"))) {
                ZipEntry zipEntry;

                while ((zipEntry = zis.getNextEntry()) != null) {
                    Path resolvedPath = tempDir.resolve(zipEntry.getName()).normalize();

                    // 보안 취약점(Zip Slip) 방어
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

            System.out.println("=== ZIP 압축 해제 완료. 내부 구조 분석 및 CSV 탐색 시작 ===");

            // 해제된 임시 폴더 경로를 순회 메서드로 전달
            processAllCsvFiles(tempDir.toString());

        } catch (IOException e) {
            System.err.println("ZIP 파일 처리 중 에러 발생: " + e.getMessage());
        }
    }


    /**
     * 2. 압축 해제된 폴더 내부를 깊이 탐색하며:
     *    - CSV가 없는 빈 폴더나 다른 폴더는 에러 없이 안전하게 건너뜀
     *    - 계층 구조상 순서적 '2번째 폴더 이름'을 기억하여 고유한 파일명으로 매칭함
     */
    public void processAllCsvFiles(String rootDirectoryPath) {
        Path rootPath = Paths.get(rootDirectoryPath);

        if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
            System.err.println("유효하지 않은 디렉토리 경로입니다: " + rootDirectoryPath);
            return;
        }

        try (Stream<Path> pathStream = Files.walk(rootPath)) {
            pathStream
                    .filter(Files::isRegularFile) // 파일인 것만 선택
                    .filter(path -> path.toString().toLowerCase().endsWith(".csv")) // 확장자가 .csv인 파일만 골라냄
                    .forEach(csvPath -> {
                        try {
                            String secondFolderName = "unknown_folder";

                            // 루트 경로 기준 상대 경로 추출
                            Path relativePath = rootPath.relativize(csvPath);

                            // 구조: [0번째: 1단계 폴더] / [1번째: 계층상 2번째 폴더] / [파일명.csv]
                            if (relativePath.getNameCount() >= 3) {
                                secondFolderName = relativePath.getName(1).toString(); // 💡 순서상 2번째 폴더 이름 획득!
                            } else if (relativePath.getNameCount() == 2) {
                                secondFolderName = relativePath.getName(0).toString();
                            }

                            // 동일한 파일명을 계층상 2번째 폴더 이름으로 대체하여 고유 이름 지정
                            String finalExcelName = secondFolderName + ".xlsx";
                            System.out.println("\n[발견된 파일] " + csvPath.getFileName()
                                    + " 👉 기억한 2번째 폴더명 적용 (저장될 파일명): " + finalExcelName);

                            // MS932 인코딩을 적용해 내용 읽기 및 글자 깨짐 해결
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
     * 3. 개별 CSV 파일을 MS932 인코딩으로 안전하게 읽어오는 공통 메서드
     */
    private void readCsvWithEncoding(Path filePath) {
        Charset charset = Charset.forName("MS932");

        try (BufferedReader br = Files.newBufferedReader(filePath, charset)) {
            String line;
            int lineCount = 0;

            while ((line = br.readLine()) != null) {
                lineCount++;
                if (lineCount <= 3) { // 샘플로 상단 내용 확인
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