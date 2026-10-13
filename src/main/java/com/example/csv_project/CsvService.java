package com.example.csv_project;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@AllArgsConstructor
public class CsvService {

    //TODO zip 파일 분석 : bear
    /**
     * 특정 CSV 파일을 열었을 때 글자가 깨지는지 확인하고 수정하는 단독 테스트 메서드
     * @param targetCsvPath 문제가 되는 CSV 파일
     */
    public void testAndFixEncoding(MultipartFile targetCsvPath) {

        // 💡 1. [요구사항 반영] 2번째 폴더 이름을 추출하여 새로운 파일명으로 확정
        String originalFilename = targetCsvPath.getOriginalFilename();
        String finalFileName = "unknown_file"; // 기본 이름

        if (originalFilename != null) {
            // 경로 구분자 표준화 (윈도우의 \ 기호를 / 로 변경)
            String normalizedPath = originalFilename.replace("\\", "/");
            String[] pathSegments = normalizedPath.split("/");

            // 2번째 폴더 이름을 추출해서 파일 이름으로 지정
            if (pathSegments.length >= 3) {
                // 구조에 따라 2번째 폴더 위치 추출 (예: root / 2번째폴더 / 파일명.csv)
                finalFileName = pathSegments[pathSegments.length - 2];
            } else if (pathSegments.length == 2) {
                finalFileName = pathSegments[0];
            }
        }

        // 👉 이제 이 변수(finalFileName)가 파일의 새로운 이름이 됩니다! (.xlsx나 확장자 필요 시 붙여서 사용)
        System.out.println("=== 2번째 폴더 이름으로 바뀐 최종 파일명: " + finalFileName + ".xlsx ===");

        // 윈도우/일본어 환경의 MS932 인코딩 적용
        Charset charset = Charset.forName("MS932");

        System.out.println("=== 파일 인코딩 읽기 시작: "
                + targetCsvPath.getOriginalFilename() + " ===");

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(targetCsvPath.getInputStream(), charset))) {

            String line;
            int lineCount = 0;

            // 한 줄씩 읽으면서 처음 5줄만 출력
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











































































































    //TODO csv >> 엑셀로 전환





}