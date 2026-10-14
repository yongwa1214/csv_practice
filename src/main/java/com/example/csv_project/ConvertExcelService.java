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
     * 다음 함수는 .csv 파일을 .xlsx로 반환합니다.
     * 호출한 쪽에서 파일명을 설정하고 폴더에 저장하시면 됩니다.
    * */
    public Workbook convertCsvToExcel(MultipartFile csvFile) throws IOException {

        // UTF-8의 csv파일은 아래로 하면 해당 안된다.
        //Charset charset = Charset.forName("MS932");

        // CSV 파일을 UTF-8로 읽기
        String csvContent = new String(
                csvFile.getBytes(),
                StandardCharsets.UTF_8
        );

        Workbook workbook = new XSSFWorkbook();

        try (CSVParser parser = CSVParser.parse(csvContent, CSVFormat.DEFAULT)) {
            Sheet sheet = workbook.createSheet("Sheet1");
            int rowNum = 0;

            for (CSVRecord record : parser) {
                Row row = sheet.createRow(rowNum++);

                for (int colNum = 0; colNum < record.size(); colNum++) {
                    row.createCell(colNum).setCellValue(record.get(colNum));
                }
            }
            return workbook;
        } catch (IOException | RuntimeException e) {
            // 변환 도중 실패하면 workbook이 누수되지 않도록 닫는다
            workbook.close();
            throw new RuntimeException("변환 실패",e);
        }
    }
}

