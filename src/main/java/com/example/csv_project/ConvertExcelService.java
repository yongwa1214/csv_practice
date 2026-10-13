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
@AllArgsConstructor
public class ConvertExcelService {
    private final MyFileUtils myFileUtils;

    public void convertCsvToExcel(MultipartFile csvFile) throws IOException {

        //Charset charset = Charset.forName("MS932");

        String originalFileName = csvFile.getOriginalFilename();
        String excelFileName = originalFileName.replaceFirst("(?i)\\.csv$", ".xlsx");

        Path excelPath = myFileUtils.getPath(excelFileName);

        // CSV 파일을 UTF-8로 읽기
        String csvContent = new String(
                csvFile.getBytes(),
                StandardCharsets.UTF_8
        );

        try (
                CSVParser parser = CSVParser.parse(csvContent, CSVFormat.DEFAULT);
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

            try (OutputStream out = Files.newOutputStream(excelPath)) {
                workbook.write(out);
            }
        }
    }
}


