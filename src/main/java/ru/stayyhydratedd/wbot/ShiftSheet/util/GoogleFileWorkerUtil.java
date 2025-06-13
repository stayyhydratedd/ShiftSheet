package ru.stayyhydratedd.wbot.ShiftSheet.util;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import org.springframework.stereotype.Component;
import ru.stayyhydratedd.wbot.ShiftSheet.enums.MimeType;
import ru.stayyhydratedd.wbot.ShiftSheet.services.google.GoogleService;

import java.io.IOException;
import java.util.*;

@Component
public class GoogleFileWorkerUtil {

    private final Sheets sheetsService;
    private final Drive driveService;
    private final JColorUtil jColorUtil;

    public GoogleFileWorkerUtil(GoogleService googleService, JColorUtil jColorUtil) {
        sheetsService = googleService.getSheetsService();
        driveService = googleService.getDriveService();
        this.jColorUtil = jColorUtil;
    }

    public File createFile(String fileName, String destination, MimeType mimeType, String... parentsFolderIds) {

        File file = new File();
        file.setName(fileName);
        file.setMimeType("application/vnd.google-apps." + mimeType.toString().toLowerCase());
        
        if (Arrays.stream(parentsFolderIds).count() == 1){
            file.setParents(Collections.singletonList(Arrays.stream(parentsFolderIds).findAny().get()));
        } else if (Arrays.stream(parentsFolderIds).count() > 1){
            file.setParents(Arrays.stream(parentsFolderIds).toList());
        }
        try {
            File createdFile = driveService.files().create(file)
                    .setFields("id, name, createdTime")
                    .execute();
            String fileType = getFileType(mimeType);
            System.out.printf("""
                    %s%s '%s' успешно создана в '%s'!
                    """, jColorUtil.SUCCESS, fileType,
                    jColorUtil.turnTextIntoColor(fileName, JColorUtil.COLOR.SUCCESS),
                    jColorUtil.turnTextIntoColor(destination, JColorUtil.COLOR.SUCCESS));

            return createdFile;
        } catch (GoogleJsonResponseException e) {
            System.err.printf(jColorUtil.ERROR + "Не удалось создать %s: " + e.getDetails(), getFileType(mimeType));
        } catch (IOException e) {
            System.err.printf(jColorUtil.ERROR + "Не удалось создать %s: " + e.getMessage(), getFileType(mimeType));
        }
        return null;
    }

    public String getFileType(MimeType mimeType) {
        if(mimeType == MimeType.FOLDER)
            return "Папка";
        else if (mimeType == MimeType.SPREADSHEET)
            return "Таблица";
        else
            return null;
    }

    public void deleteFileById(String fileId, String fileName) {
        try {
            driveService.files().delete(fileId).execute();
            System.out.printf("%sФайл '%s' успешно удалён. ID: %s\n", jColorUtil.SUCCESS,
                    jColorUtil.turnTextIntoColor(fileName, JColorUtil.COLOR.SUCCESS),
                    jColorUtil.turnTextIntoColor(fileId, JColorUtil.COLOR.SUCCESS));
        } catch (IOException e) {
            System.err.printf("%sОшибка при удалении файла '%s'. ID: %s",
                    jColorUtil.ERROR, jColorUtil.turnTextIntoColor(fileName, JColorUtil.COLOR.ERROR),
                    jColorUtil.turnTextIntoColor(fileId, JColorUtil.COLOR.ERROR));
            e.printStackTrace();
        }
    }

    public void renameFileById(String fileId, String oldName, String newName) {
        try{
            File fileMetadata = new File();
            fileMetadata.setName(newName);
            driveService.files().update(fileId, fileMetadata)
                    .setFields("name")
                    .execute();
            System.out.printf("%sИмя папки '%s' успешно изменено на '%s'\n", jColorUtil.SUCCESS,
                    jColorUtil.turnTextIntoColor(oldName, JColorUtil.COLOR.SUCCESS),
                    jColorUtil.turnTextIntoColor(newName, JColorUtil.COLOR.SUCCESS));
        } catch (IOException e) {
            System.out.printf("%sИмя папки '%s' не удалось изменить\n", jColorUtil.ERROR,
                    jColorUtil.turnTextIntoColor(oldName, JColorUtil.COLOR.ERROR));
            e.printStackTrace();
        }
    }

    //используется, чтобы вернуть список файлов в папке с указанным id
    public Optional<FileList> getDirectoryFileList(String folderId){
        try{
            return Optional.of(driveService.files().list()
                    .setQ("'" + folderId + "' in parents and trashed = false")
                    .setFields("nextPageToken, files(id, name, createdTime)")
                    .execute());
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    //перегруженный метод, используется, чтобы вернуть список доступных файлов в google drive
     public FileList getDriveFoldersList() {
        try{
            return driveService.files().list()
                    .setFields("nextPageToken, files(id, name, createdTime)")
                    .setQ("mimeType = 'application/vnd.google-apps.folder' and trashed = false")
                    .execute();
        } catch (IOException e) {
            return null;
        }
    }

    public List<Sheet> getSheets(String spreadsheetGoogleId) {
        try {
            Spreadsheet spreadsheet = sheetsService.spreadsheets().get(spreadsheetGoogleId).execute();
            return spreadsheet.getSheets();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File getFileByNameAndParents(String fileName, String parentFolderId) {
        try {
            String query = String.format("name = '%s' and trashed = false", fileName);

            if (parentFolderId != null && !parentFolderId.isEmpty()) {
                query += String.format(" and '%s' in parents", parentFolderId);
            }

            FileList result = driveService.files().list()
                    .setQ(query)
                    .setSpaces("drive")
                    .setFields("files(id, name, createdTime, mimeType, parents)")
                    .setPageSize(1)
                    .execute();

            List<File> files = result.getFiles();
            if (files != null && !files.isEmpty()) {
                return files.getFirst();  // Возвращаем первый найденный файл
            } else {
                return null;  // Файл не найден
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void executeRequestsBody(List<Request> requests, String spreadsheetId) throws IOException {
        BatchUpdateSpreadsheetRequest body =
                new BatchUpdateSpreadsheetRequest()
                        .setRequests(requests);

        sheetsService.spreadsheets()
                .batchUpdate(spreadsheetId, body)
                .execute();
    }

    public void executeDataBody(List<ValueRange> data, String spreadsheetId) throws IOException {
        BatchUpdateValuesRequest body = new BatchUpdateValuesRequest()
                .setValueInputOption("USER_ENTERED")
                .setData(data);

        BatchUpdateValuesResponse result =
                sheetsService.spreadsheets()
                        .values()
                        .batchUpdate(spreadsheetId, body)
                        .execute();
    }

    protected LinkedHashMap<String, String> getDirectoryDataMap(FileList fileList){
        LinkedHashMap<String, String> spreadsheetData = new LinkedHashMap<>();
        List<File> files = fileList.getFiles();
        for(File file: files){
            spreadsheetData.put(file.getName(), file.getId());
        }
        return spreadsheetData;
    }
}
