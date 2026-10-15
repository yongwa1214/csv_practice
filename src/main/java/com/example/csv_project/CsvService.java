package com.example.csv_project;

import com.example.csv_project.common.MyFileUtils;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@AllArgsConstructor
public class CsvService {
    private final ConvertExcelService convertExcelService;
    private final MyFileUtils   myFileUtils;

    /**
     * ZIP 파일을 받아 서버 임시 공간에 압축을 풀고, 내부 폴더 순회 로직으로 연결하는 메인 메서드
     */
    public void processZipFile(MultipartFile zipFile) throws IOException {
        if (zipFile == null || zipFile.isEmpty()) {
            System.err.println("업로드된 ZIP 파일이 비어 있거나 존재하지 않습니다.");
            return;
        }

        // 💡 finally 블록에서도 참조할 수 있도록 try 블록 바깥에 미리 선언
        Path tempDir = null;

        try {
            // 폴더 생성
            String originName = zipFile.getOriginalFilename();
            if (originName != null) {
                String filename = originName.substring(0, originName.lastIndexOf("."));
                Path savePath = myFileUtils.makeFolders(filename);

                // ZIP 파일 압축 해제 경로 생성
                tempDir = Files.createTempDirectory("csv_unzip_");
                System.out.println("=== ZIP 압축 해제 시작: " + zipFile.getOriginalFilename() + " ===");

                try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream(), Charset.forName("MS932"))) {
                    ZipEntry zipEntry;

                    while ((zipEntry = zis.getNextEntry()) != null) {
                        Path resolvedPath = tempDir.resolve(zipEntry.getName()).normalize();

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

                int result = processAllCsvFiles(tempDir, savePath);
            }

        } catch (IOException e) {
            System.err.println("ZIP 파일 처리 중 에러 발생: " + e.getMessage());
        } finally {
            // 💡 성공하든, 중간에 예외가 터져서 catch로 빠지든 작업이 끝나면 무조건 임시 폴더 삭제
            if (tempDir != null) {
                FileSystemUtils.deleteRecursively(tempDir);
                System.out.println("=== 임시 디렉토리 정리 완료 ===");
            }
        }
    }


    /**
     * 2. 압축 해제된 폴더 내부를 탐색하며 처리하는 메서드
     */
    private int processAllCsvFiles(Path rootPath, Path savePath) {
/*        if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
            System.err.println("유효하지 않은 디렉토리 경로입니다: " + rootDirectoryPath);
            return;
        }
*/
        //중복 폴더명 체크를 위한 HashSet 선언부
        Set<String> processedFolders = new HashSet<>();
        //

        try (Stream<Path> pathStream = Files.walk(rootPath)) {
            pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".csv"))
                    .forEach(csvPath -> {
                        try {
                            //1. 상위폴더명 저장 변수
                            // CSV 파일의 바로 상위 폴더 이름 가져와서 최종 엑셀 파일명 지정
                            String parentFolderName = "unknown_folder";
                            if (csvPath.getParent() != null && csvPath.getParent().getFileName() != null) {
                                parentFolderName = csvPath.getParent().getFileName().toString();
                            }

                            //2. set add >> 중복 검사 true >> 진행 false >> 건너뛰기
                            if (!processedFolders.add(parentFolderName)) {
                                System.out.println("  ⚠️ 중복된 상위 폴더명 감지됨. 이미 처리되었으므로 건너뜁니다.");
                                return; // 중복이면 건너뜀
                            }
                            // 3. 매개변수는 .csv 파일

                            //convertExcelService 호출부

                            Workbook workbook = convertExcelService.convertCsvToExcel(csvPath);

                            //TODO : workbook 저장
                            //저장 경로 + 변경 파일명.xlsx | changeTitle을 선언할 것
                            Path excelPath = savePath.resolve(parentFolderName + ".xlsx");

                            OutputStream outputStream = Files.newOutputStream(excelPath);
                            workbook.write(outputStream);
                            outputStream.close();
                            workbook.close();


                        } catch (Exception e) {
                            System.err.println("파일 처리 중 에러 발생: " + csvPath + " / 원인: " + e.getMessage());
                        }
                    });
            return 0;

        } catch (IOException e) {
            System.err.println("디렉토리 순회 중 에러 발생: " + e.getMessage());
            return 1;
        }
    }



}