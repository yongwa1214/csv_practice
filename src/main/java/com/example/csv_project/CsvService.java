package com.example.csv_project;

import com.example.csv_project.common.MyFileUtils;
import com.example.csv_project.common.error.CustomException;
import com.example.csv_project.common.error.ErrorCode;
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
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@AllArgsConstructor
public class CsvService {
    private final ConvertExcelService convertExcelService;
    private final MyFileUtils  myFileUtils;



    /**
     * ZIP 파일을 받아 서버 임시 공간에 압축을 풀고, 내부 폴더 순회 로직으로 연결하는 메인 메서드
     */
    public void processZipFile(MultipartFile zipFile) throws IOException {
        //파일 검증
        // 1. 파일을 보내지 않았을때
        if (zipFile == null || zipFile.isEmpty()) {
            throw new CustomException(ErrorCode.FILE_EMPTY);
        }

        //2. 파일 형식이 올바르지 않음
        String originName = zipFile.getOriginalFilename();
        if (originName == null || !originName.toLowerCase().endsWith(".zip")) {
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
        }

        // 파일명 및 저장 경로
        String fileName = originName.substring(0, originName.lastIndexOf("."));
        Path tempDir = null;
        Path savePath = null;

        try {
            tempDir = Files.createTempDirectory("csv_unzip_");

            // zip 압축 해제
            try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream(), Charset.forName("MS932"))) {

                ZipEntry zipEntry;
                while ((zipEntry = zis.getNextEntry()) != null) {
                    Path resolvedPath = tempDir.resolve(zipEntry.getName()).normalize();

                    if (!resolvedPath.startsWith(tempDir)) {
                        throw new CustomException(ErrorCode.ZIP_INVALID_PATH);
                    }

                    if (zipEntry.isDirectory()) {
                        Files.createDirectories(resolvedPath);
                    } else {
                        Files.createDirectories(resolvedPath.getParent());
                        Files.copy(zis, resolvedPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                    zis.closeEntry();
                }
            }

            // 내부 탐색
            savePath =  myFileUtils.makeFolders(fileName);
            processAllCsvFiles(tempDir, savePath);

        }catch (Exception e) {
            if (savePath != null) {
                FileSystemUtils.deleteRecursively(savePath.toFile());
            }
            throw e;
        }finally {
            // 💡 작업이 끝나면 무조건 임시 폴더 삭제
            if (tempDir != null) {
                FileSystemUtils.deleteRecursively(tempDir);
            }
        }
    }


    /**
     * 2. 압축 해제된 폴더 내부를 탐색하며 처리하는 메서드
     */
    private void processAllCsvFiles(Path rootPath, Path savePath) throws IOException {
        // 중복 폴더명 체크
        Set<String> processedFolders = new HashSet<>();

        try (Stream<Path> pathStream = Files.walk(rootPath)) {

            for (Path csvPath : (Iterable<Path>) () -> pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".csv"))
                    .iterator()) {

                // 1. 상위 폴더명 추출
                String parentFolderName = "unknown_folder";
                if (csvPath.getParent() != null && csvPath.getParent().getFileName() != null) {
                    parentFolderName = csvPath.getParent().getFileName().toString();
                }

                // 2. 중복 폴더명 검사
                if (!processedFolders.add(parentFolderName)) {
                    continue;
                }

                // 3. CSV를 Excel로 변환 및 저장
                try (Workbook workbook = convertExcelService.convertCsvToExcel(csvPath);
                     OutputStream outputStream = Files.newOutputStream(
                             savePath.resolve(parentFolderName + ".xlsx"))) {

                    workbook.write(outputStream);
                }
            }
        }
    }
}