package com.example.csv_project;

import com.example.csv_project.common.MyFileUtils;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    //클래스 맨 상단에 추가된 바로 상위 폴더 이름 추출 및 HashSet 중복 검사 메서드
    public boolean checkAndRegisterParentFolder(Path csvPath, Set<String> processedFolders) {
        String parentFolderName = "unknown_folder";

        // CSV 파일의 바로 상위 폴더(직속 부모) 객체 획득
        Path parentPath = csvPath.getParent();
        if (parentPath != null && parentPath.getFileName() != null) {
            parentFolderName = parentPath.getFileName().toString();
        }

        // HashSet에 추가 시도 (새로운 값이면 true, 이미 존재하면 false 반환)
        return processedFolders.add(parentFolderName);
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
            // 폴더 생성
            String originName = zipFile.getOriginalFilename();
            String filename = zipFile.getOriginalFilename().substring(0,originName.lastIndexOf("."));

            myFileUtils.makeFolders(filename);

            //집파일 열고

            Path tempDir = Files.createTempDirectory("csv_unzip_");
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

            processAllCsvFiles(tempDir.toString());

        } catch (IOException e) {
            System.err.println("ZIP 파일 처리 중 에러 발생: " + e.getMessage());
        }
    }


    /**
     * 2. 압축 해제된 폴더 내부를 탐색하며 처리하는 메서드
     */
    public void processAllCsvFiles(String rootDirectoryPath) {
        Path rootPath = Paths.get(rootDirectoryPath);

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
                            Workbook workbook =  convertExcelService.convertCsvToExcel();

                            //4. 저장
                            System.out.println("\n[발견된 파일] " + csvPath.getFileName()
                                    + " 👉 기억한 바로 상위 폴더명 적용 (저장될 엑셀명): " + finalExcelName);

                            //convertExcelService 호출부
                            // 매개변수는 .csv 파일을 전달해주세요
                            Workbook workbook =  convertExcelService.convertCsvToExcel(csvPath, finalExcelName);
                            //

                        } catch (Exception e) {
                            System.err.println("파일 처리 중 에러 발생: " + csvPath + " / 원인: " + e.getMessage());
                        }
                    });

        } catch (IOException e) {
            System.err.println("디렉토리 순회 중 에러 발생: " + e.getMessage());
        }
    }



}