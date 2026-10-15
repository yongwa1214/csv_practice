package com.example.csv_project;


import com.example.csv_project.common.MyFileUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ConvertExcelService {

    /**
     * UTF-8 인코딩의 .csv 파일 하나를 Workbook으로 변환하여 반환한다.
     * - CSV 읽기와 파싱은 이 메서드가 전부 담당한다.
     * - Workbook의 저장과 close()는 호출한 쪽에서 처리한다.
     * - 변환 중 예외는 호출부로 throw한다.
     */
    public Workbook convertCsvToExcel(Path csvPath) throws IOException {

        try (CSVParser parser = CSVParser.parse(csvPath, StandardCharsets.UTF_8, CSVFormat.DEFAULT)) {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Sheet1");
            int rowNum = 0;

            for (CSVRecord record : parser) {
                Row row = sheet.createRow(rowNum++);

                for (int colNum = 0; colNum < record.size(); colNum++) {
                    String value = record.get(colNum);

                    if (colNum == record.size() - 1 && value.length() > 32767) {
                        value = value.substring(0, 32767);
                    }

                    row.createCell(colNum).setCellValue(value);
                }
            }
            return workbook;
        }
    }
}

