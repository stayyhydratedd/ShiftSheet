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

    public String createFile(String fileName, String destination, MimeType mimeType, String... parentsFolderIds) {

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
                    .setFields("id")
                    .execute();
            String fileType = getFileType(mimeType);
            System.out.printf("""
                    %s%s '%s' успешно создана в '%s'!
                    """, jColorUtil.SUCCESS, fileType,
                    jColorUtil.turnTextIntoColor(fileName, JColorUtil.COLOR.SUCCESS),
                    jColorUtil.turnTextIntoColor(destination, JColorUtil.COLOR.SUCCESS));

            return createdFile.getId();
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
    //используется, чтобы вернуть список файлов в папке с указанным id
    public Optional<FileList> getDirectoryFileList(String folderId){
        try{
            return Optional.of(driveService.files().list()
                    .setQ("'" + folderId + "' in parents and trashed = false")
                    .setFields("nextPageToken, files(id, name)")
                    .execute());
        } catch (IOException e) {
            return Optional.empty();
        }
    }
    //перегруженный метод, используется, чтобы вернуть список доступных файлов в google drive
     public FileList getDriveFileList() throws IOException {
        return driveService.files().list()
                .setFields("nextPageToken, files(id, name)")
                .execute();
    }

    public List<Sheet> getSheets(String pwzGoogleId) {
        try {
            Spreadsheet spreadsheet = sheetsService.spreadsheets().get(pwzGoogleId).execute();
            return spreadsheet.getSheets();
        } catch (IOException e) {
            throw new RuntimeException(e);
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
