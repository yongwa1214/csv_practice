
const fileInput = document.getElementById("fileInput");
const selectButton = document.getElementById("selectButton");
const dropZone = document.getElementById("dropZone");
const fileList = document.getElementById("fileList");
const fileCount = document.getElementById("fileCount");
const uploadButton = document.getElementById("uploadButton");

const progressSection = document.getElementById("progressSection");
const progressBar = document.getElementById("progressBar");
const progressText = document.getElementById("progressText");
const progressPercent = document.getElementById("progressPercent");
const statusMessage = document.getElementById("statusMessage");

const UPLOAD_URL = "/api/files/upload";

let selectedFile = null;
let uploading = false;

// 파일 크기 변환
const formatSize = (bytes) => {
    if (bytes < 1024) {
        return `${bytes} B`;
    }

    if (bytes < 1024 * 1024) {
        return `${(bytes / 1024).toFixed(1)} KB`;
    }

    if (bytes < 1024 * 1024 * 1024) {
        return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
    }

    return `${(bytes / (1024 * 1024 * 1024)).toFixed(2)} GB`;
};

// 상태 메시지 표시
const showStatus = (message, type = "") => {
    statusMessage.textContent = message;
    statusMessage.className = `status-message ${type}`;
};

// 진행 표시창 초기화
const resetProgress = () => {
    progressSection.hidden = true;
    progressBar.style.width = "0%";
    progressPercent.textContent = "0%";
    progressText.textContent = "업로드 준비 중...";
};

// ZIP 확장자 검사
const isZipFile = (file) => {
    return file.name.toLowerCase().endsWith(".zip");
};

// 파일 목록 표시
const renderFile = () => {
    fileList.replaceChildren();

    fileCount.textContent = selectedFile ? "1개 선택" : "0개 선택";
    uploadButton.disabled = !selectedFile || uploading;

    if (!selectedFile) {
        const empty = document.createElement("li");

        empty.className = "empty-state";
        empty.textContent = "선택한 파일이 없습니다.";

        fileList.appendChild(empty);
        return;
    }

    const item = document.createElement("li");
    item.className = "file-item";

    const icon = document.createElement("div");
    icon.className = "file-icon";
    icon.textContent = "ZIP";

    const info = document.createElement("div");
    info.className = "file-info";

    const name = document.createElement("div");
    name.className = "file-name";
    name.textContent = selectedFile.name;
    name.title = selectedFile.name;

    const size = document.createElement("div");
    size.className = "file-size";
    size.textContent = formatSize(selectedFile.size);

    info.append(name, size);

    const removeButton = document.createElement("button");
    removeButton.type = "button";
    removeButton.className = "remove-button";
    removeButton.textContent = "×";
    removeButton.setAttribute(
        "aria-label",
        `${selectedFile.name} 제거`
    );
    removeButton.disabled = uploading;

    // 선택 파일 제거
    removeButton.addEventListener("click", () => {
        if (uploading) return;

        selectedFile = null;
        fileInput.value = "";

        renderFile();
        showStatus("");
        resetProgress();
    });

    item.append(icon, info, removeButton);
    fileList.appendChild(item);
};

// 파일 선택 및 교체
const selectFile = (file) => {
    if (uploading || !file) return;

    // ZIP 확장자 검사
    if (!isZipFile(file)) {
        fileInput.value = "";
        showStatus("ZIP 파일만 선택할 수 있습니다.", "error");
        return;
    }

    // 새 파일 선택 시 이전 결과 초기화
    selectedFile = file;

    showStatus("");
    resetProgress();
    renderFile();
};

// 파일 선택 버튼
selectButton.addEventListener("click", () => {
    if (!uploading) {
        fileInput.click();
    }
});

// 파일 선택 이벤트
fileInput.addEventListener("change", (event) => {
    const file = event.target.files[0];

    if (file) {
        selectFile(file);
    }
});

// 드래그 진입
dropZone.addEventListener("dragover", (event) => {
    event.preventDefault();

    if (!uploading) {
        dropZone.classList.add("drag-over");
    }
});

// 드래그 영역 이탈
dropZone.addEventListener("dragleave", (event) => {
    if (!dropZone.contains(event.relatedTarget)) {
        dropZone.classList.remove("drag-over");
    }
});

// 파일 드롭
dropZone.addEventListener("drop", (event) => {
    event.preventDefault();
    dropZone.classList.remove("drag-over");

    if (uploading) return;

    const file = event.dataTransfer.files[0];

    if (file) {
        selectFile(file);
    }
});

// 파일 업로드
const uploadFile = async () => {
    if (uploading || !selectedFile) return;

    // 업로드 직전 ZIP 확장자 검사
    if (!isZipFile(selectedFile)) {
        showStatus("ZIP 파일만 업로드할 수 있습니다.", "error");
        return;
    }

    const formData = new FormData();
    formData.append("file", selectedFile);

    uploading = true;

    uploadButton.disabled = true;
    fileInput.disabled = true;
    selectButton.disabled = true;

    renderFile();

    // 업로드 중 표시
    progressSection.hidden = false;
    progressBar.style.width = "0%";
    progressPercent.textContent = "0%";
    progressText.textContent = "업로드 중...";

    showStatus("");

    try {
        // 백엔드에 파일 전송 후 응답 대기
        const response = await fetch(UPLOAD_URL, {
            method: "POST",
            body: formData
        });

        // 백엔드 응답 본문 읽기
        const responseText = await response.text();

        // HTTP 오류 처리
        if (!response.ok) {
            throw new Error(
                responseText || `요청 실패 (HTTP ${response.status})`
            );
        }

        // 업로드 성공
        progressBar.style.width = "100%";
        progressPercent.textContent = "100%";
        progressText.textContent = "업로드 완료";

        showStatus(
            responseText || "파일 업로드가 완료되었습니다.",
            "success"
        );

        // 성공하면 선택 파일 제거
        selectedFile = null;
        fileInput.value = "";

    } catch (error) {
        // 업로드 실패: 파일과 실패 결과 유지
        progressText.textContent = "업로드 실패";

        showStatus(
            error.message || "파일 업로드 중 오류가 발생했습니다.",
            "error"
        );

    } finally {
        // 요청 종료 후 버튼 복구
        uploading = false;

        fileInput.disabled = false;
        selectButton.disabled = false;

        renderFile();

        // 진행 표시창은 여기서 초기화하지 않음
        // 새 파일을 선택할 때 resetProgress() 실행
    }
};

// 업로드 버튼
uploadButton.addEventListener("click", uploadFile);

// 최초 화면 초기화
renderFile();
resetProgress();
