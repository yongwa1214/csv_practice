package com.example.csv_project;


import lombok.AllArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
    public void testAndFixEncoding(MultipartFile targetCsvPath) {
        //Path filePath = Paths.get(targetCsvPath);

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
    public void convertCsvToExcel(MultipartFile csvFile, Path xlsxPath) throws IOException {
        Charset charset = Charset.forName("MS932");

        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(csvFile.getInputStream(), charset)
                );
                CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT);
                Workbook workbook = new XSSFWorkbook()
        ) {
            Sheet sheet = workbook.createSheet("Sheet1");
            int rowNum = 0;

            for (CSVRecord record : parser) {
                Row row = sheet.createRow(rowNum++);

                for (int colNum = 0; colNum < record.size(); colNum++) {
                    row.createCell(colNum).setCellValue(record.get(colNum));
                }
            }

            try (OutputStream out = Files.newOutputStream(xlsxPath)) {
                workbook.write(out);
            }
        }
    }





}
