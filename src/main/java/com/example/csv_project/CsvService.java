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

@Service
@AllArgsConstructor
public class CsvService {

    //TODO zip 파일 분석 : bear
    /**
     * 특정 CSV 파일을 열었을 때 글자가 깨지는지 확인하고 수정하는 단독 테스트 메서드 (기존 유지)
     * @param targetCsvPath 문제가 되는 CSV 파일
     */
    public void testAndFixEncoding(MultipartFile targetCsvPath) {
        String originalFilename = targetCsvPath.getOriginalFilename();
        String finalFileName = "unknown_file"; // 기본 이름

        if (originalFilename != null) {
            // 경로 구분자 표준화 (윈도우의 \ 기호를 / 로 변경)
            String normalizedPath = originalFilename.replace("\\", "/");
            String[] pathSegments = normalizedPath.split("/");

            // 2번째 폴더 이름을 추출해서 파일 이름으로 지정 (마이너스 기호 줄바꿈 이슈 방지)
            if (pathSegments.length >= 3) {
                int targetIndex = pathSegments.length - 2;
                finalFileName = pathSegments[targetIndex];
            } else if (pathSegments.length == 2) {
                finalFileName = pathSegments[0];
            }
        }

        System.out.println("=== 2번째 폴더 이름으로 바뀐 최종 파일명: " + finalFileName + ".xlsx ===");

        // 윈도우/일본어 환경의 MS932 인코딩 적용 (기존 유지)
        Charset charset = Charset.forName("MS932");

        System.out.println("=== 파일 인코딩 읽기 시작: "
                + targetCsvPath.getOriginalFilename() + " ===");

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(targetCsvPath.getInputStream(), charset))) {

            String line;
            int lineCount = 0;

            // 한 줄씩 읽으면서 처음 5줄만 출력 (기존 유지)
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
     * 💡 최상위 집 폴더 경로를 입력받아 내부를 순회하며
     * CSV 파일이 있는 폴더만 찾아내고, 2번째 폴더 이름을 추출해 처리하는 메서드
     * @param rootDirectoryPath 최상위 집 폴더 경로 (예: "C:/my_root_folder")
     */
    public void processAllCsvFiles(String rootDirectoryPath) {
        Path rootPath = Paths.get(rootDirectoryPath);

        if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
            System.err.println("유효하지 않은 디렉토리 경로입니다: " + rootDirectoryPath);
            return;
        }

        // 폴더 내부를 깊이 탐색하며 파일들을 순회
        try (Stream<Path> pathStream = Files.walk(rootPath)) {
            pathStream
                    .filter(Files::isRegularFile) // 파일인 것만 선택 (폴더 자체는 제외)
                    .filter(path -> path.toString().toLowerCase().endsWith(".csv")) // 확장자가 .csv인 파일만 골라냄
                    .forEach(csvPath -> {
                        try {
                            String secondFolderName = "unknown_folder";

                            // 루트 경로와의 상대적 위치를 고려하여 상위 폴더명 추출
                            Path relativePath = rootPath.relativize(csvPath);
                            if (relativePath.getNameCount() >= 2) {
                                secondFolderName = relativePath.getName(0).toString();
                            }

                            // 동일한 파일명(messages.csv 등)을 2번째 폴더 이름으로 대체
                            String finalExcelName = secondFolderName + ".xlsx";
                            System.out.println("\n[처리 중] 발견된 파일: " + csvPath.getFileName()
                                    + " -> 2번째 폴더명 적용 엑셀 이름: " + finalExcelName);

                            // 기존에 만들었던 인코딩 및 읽기 로직 수행 (MS932 적용)
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
                if (lineCount <= 3) { // 샘플로 3줄만 확인
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