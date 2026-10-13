package com.example.csv_project;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@AllArgsConstructor
public class CsvService {

    //TODO zip 파일 분석 : bear
    /**
     * 특정 CSV 파일을 열었을 때 글자가 깨지는지 확인하고 수정하는 단독 테스트 메서드
     * @param targetCsvPath 문제가 되는 CSV 파일의 실제 컴퓨터 경로
     */
    public void testAndFixEncoding(String targetCsvPath) {
        Path filePath = Paths.get(targetCsvPath);

        // 💡 윈도우/일본어 환경에서 글자 깨짐을 방지하는 MS932 인코딩 적용
        Charset charset = Charset.forName("MS932");

        System.out.println("=== 파일 인코딩 읽기 시작: " + filePath.getFileName() + " ===");

        try (BufferedReader br = Files.newBufferedReader(filePath, charset)) {
            String line;
            int lineCount = 0;

            // 한 줄씩 읽으면서 콘솔에 깨짐 없이 출력되는지 확인
            while ((line = br.readLine()) != null) {
                lineCount++;
                if (lineCount <= 5) { // 처음 5줄만 샘플로 출력
                    System.out.println("Line " + lineCount + ": " + line);
                }
            }
            System.out.println("=== 파일 읽기 성공! (총 " + lineCount + "줄) ===");

        } catch (IOException e) {
            System.err.println("파일 읽기 실패 또는 인코딩 에러 발생: " + e.getMessage());
        }
    }











































































































    //TODO csv >> 엑셀로 전환





}
