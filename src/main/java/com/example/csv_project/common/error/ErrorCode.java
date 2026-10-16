package com.example.csv_project.common.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    FILE_EMPTY("FILE_001", HttpStatus.BAD_REQUEST, "ファイルが空です。"),
    INVALID_FILE_TYPE("FILE_002", HttpStatus.BAD_REQUEST, "サポートされていないファイル形式です。"),
    FILE_PROCESS_ERROR("FILE_003", HttpStatus.INTERNAL_SERVER_ERROR, "ファイルの処理に失敗しました。しばらくしてからもう一度お試しください。"),
    ZIP_INVALID_PATH("FILE_004", HttpStatus.BAD_REQUEST, "圧縮ファイルを処理できません。"),

    FILE_READ_ERROR("FILE_005", HttpStatus.INTERNAL_SERVER_ERROR, "ファイルの内容を確認できません。ファイルの状態を確認してください。"),
    FOLDER_ALREADY_EXISTS("FILE_006", HttpStatus.BAD_REQUEST, "同じ名前のファイルがすでに存在します。")
    ;

    private final String code;
    private final HttpStatus status;
    private final String message;
}