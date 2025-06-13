package ru.stayyhydratedd.wbot.ShiftSheet.services;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.sheets.v4.Sheets;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.stayyhydratedd.wbot.ShiftSheet.context.SessionContext;
import ru.stayyhydratedd.wbot.ShiftSheet.enums.MimeType;
import ru.stayyhydratedd.wbot.ShiftSheet.models.AppFolder;
import ru.stayyhydratedd.wbot.ShiftSheet.repositories.AppFolderRepository;
import ru.stayyhydratedd.wbot.ShiftSheet.services.google.GoogleService;
import ru.stayyhydratedd.wbot.ShiftSheet.util.GoogleFileWorkerUtil;
import ru.stayyhydratedd.wbot.ShiftSheet.util.InputOutputUtil;
import ru.stayyhydratedd.wbot.ShiftSheet.util.JColorUtil;

import java.io.IOException;
import java.util.*;

@Service
public class AppFolderService {

    private final AppFolderRepository appFolderRepository;
    private final SessionContext sessionContext;
    private final GoogleFileWorkerUtil googleFileWorkerUtil;
    private final AppSettingsService appSettingsService;
    private final RootFolderService rootFolderService;
    private final Sheets sheetsService;
    private final Drive driveService;
    private final UserService userService;
    private final InputOutputUtil inputUtil;
    private final JColorUtil jColorUtil;

    @Autowired
    public AppFolderService(GoogleService googleService, AppFolderRepository appFolderRepository,
                            SessionContext sessionContext, GoogleFileWorkerUtil googleFileWorkerUtil,
                            RootFolderService rootFolderService, UserService userService, InputOutputUtil inputUtil,
                            JColorUtil jColorUtil, AppSettingsService appSettingsService) {
        this.appFolderRepository = appFolderRepository;
        this.sessionContext = sessionContext;
        this.googleFileWorkerUtil = googleFileWorkerUtil;
        this.appSettingsService = appSettingsService;
        this.rootFolderService = rootFolderService;
        this.sheetsService = googleService.getSheetsService();
        this.driveService = googleService.getDriveService();
        this.userService = userService;
        this.inputUtil = inputUtil;
        this.jColorUtil = jColorUtil;
    }

    public static final int SINGLETON_ID = 1;

    public AppFolder getAppFolder() {
        return appFolderRepository.findById(SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("AppFolder must be initialized manually in the DB"));
    }

    @PostConstruct
    private void initializeAppFolder() {
        AppFolder appFolder = getAppFolder();
        FileList fileList = googleFileWorkerUtil.getDriveFoldersList();
        Optional<File> foundAppFolderInGoogleDisk = fileList.getFiles()
                .stream()
                .filter(f -> f.getName().equals("ShiftSheet"))
                .findAny();
        if (foundAppFolderInGoogleDisk.isEmpty()) {

            File appFolderInGoogleDisk = googleFileWorkerUtil
                    .createFile(appFolder.getTitle(), "Google Disk", MimeType.FOLDER);

            File update = new File().setFolderColorRgb("#80CDA3");  // изменение цвета папки
            try {
                driveService.files().update(appFolderInGoogleDisk.getId(), update)
                        .setFields("id, name, folderColorRgb")
                        .execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            File usersFolder = googleFileWorkerUtil
                    .createFile("users", appFolder.getTitle(), MimeType.FOLDER, appFolderInGoogleDisk.getId());
            File rootsFolder = googleFileWorkerUtil
                    .createFile("roots", appFolder.getTitle(), MimeType.FOLDER, appFolderInGoogleDisk.getId());
            File userDataSpreadsheet = googleFileWorkerUtil
                    .createFile("userData", usersFolder.getName(), MimeType.SPREADSHEET, usersFolder.getId());

            appFolder.setGoogleId(appFolderInGoogleDisk.getId());
            appFolder.setUsersFolderGoogleId(usersFolder.getId());
            appFolder.setRootsFolderGoogleId(rootsFolder.getId());
            appFolder.setUserDataSpreadsheetGoogleId(userDataSpreadsheet.getId());

            int sheetId = userService.executeFormatUserDataSheet(userDataSpreadsheet.getId());
            appFolder.setUserDetailsSheetGoogleId(sheetId);

            appFolderRepository.saveAndFlush(appFolder);
            appSettingsService.updateSettings(settings -> settings.setAppFolder(appFolder));

        } else if(rootFolderService.findAll().isEmpty() && userService.findAll().isEmpty()) {
//            AppSettings appSettings = appSettingsService.getSettings();
//            File appFolderFile;
//            File rootsFolderFile;
//            File usersFilderFile;
//            File userDataSpreadsheetFile;
//            Sheet userDetailsSheet;
//            if(appSettings.getAppFolder().isEmpty()){
//                appFolderFile = googleFileWorkerUtil.getFileByNameAndParents("ShiftSheet", null);
//                rootsFolderFile = googleFileWorkerUtil.getFileByNameAndParents("roots", appFolderFile.getId());
//                usersFilderFile = googleFileWorkerUtil.getFileByNameAndParents("users", appFolderFile.getId());
//                userDataSpreadsheetFile = googleFileWorkerUtil.getFileByNameAndParents("userData", usersFilderFile.getId());
//                userDetailsSheet = googleFileWorkerUtil.getSheets(userDataSpreadsheetFile.getId()).getFirst();
//                appFolder.setGoogleId(appFolderFile.getId());
//                appFolder.setRootsFolderGoogleId(rootsFolderFile.getId());
//                appFolder.setUsersFolderGoogleId(usersFilderFile.getId());
//                appFolder.setUserDataSpreadsheetGoogleId(userDataSpreadsheetFile.getId());
//                appFolder.setUserDetailsSheetGoogleId(userDetailsSheet.getProperties().getSheetId());
//                appFolderRepository.saveAndFlush(appFolder);
//                appSettingsService.updateSettings(settings -> settings.setAppFolder(appFolder));
//            } else {
//
//            }



            if (inputUtil.askYesOrNo("Хотите синхронизировать данные", "", JColorUtil.COLOR.INFO)){
                synchronizeData();
            }

        }
        Optional<AppFolder> appFolderFromAppSettingsOpt = appSettingsService.getSettings().getAppFolder();
        appFolderFromAppSettingsOpt.ifPresent(sessionContext::setAppFolder);
    }

    private void synchronizeData() {

    }
}
