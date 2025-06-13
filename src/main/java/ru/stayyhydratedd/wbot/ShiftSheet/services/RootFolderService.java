package ru.stayyhydratedd.wbot.ShiftSheet.services;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import ru.stayyhydratedd.wbot.ShiftSheet.context.SessionContext;
import ru.stayyhydratedd.wbot.ShiftSheet.enums.ChangeRootMethod;
import ru.stayyhydratedd.wbot.ShiftSheet.enums.MimeType;
import ru.stayyhydratedd.wbot.ShiftSheet.models.AppFolder;
import ru.stayyhydratedd.wbot.ShiftSheet.models.AppSettings;
import ru.stayyhydratedd.wbot.ShiftSheet.models.RootFolder;
import ru.stayyhydratedd.wbot.ShiftSheet.models.User;
import ru.stayyhydratedd.wbot.ShiftSheet.repositories.RootFolderRepository;
import ru.stayyhydratedd.wbot.ShiftSheet.util.GoogleFileWorkerUtil;
import ru.stayyhydratedd.wbot.ShiftSheet.util.InputOutputUtil;
import ru.stayyhydratedd.wbot.ShiftSheet.util.JColorUtil;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RootFolderService {

    private final AppSettingsService appSettingsService;
    private final UserService userService;
    private final RootFolderRepository rootFolderRepository;
    private final GoogleFileWorkerUtil googleFileWorkerUtil;
    private final SessionContext sessionContext;
    private final JColorUtil jColorUtil;
    private final InputOutputUtil inputUtil;

    public List<RootFolder> findAll(){
        return rootFolderRepository.findAll();
    }

    public Optional<RootFolder> findById(int id){
        return rootFolderRepository.findByIdWithPwzsAndUsers(id);
    }

    public void save(RootFolder rootFolder) {
        rootFolderRepository.save(rootFolder);
    }

    public void delete(RootFolder rootFolder) {
        rootFolderRepository.delete(rootFolder);
    }

    public Optional<RootFolder> findByGoogleId(String googleId) {
        return rootFolderRepository.findByGoogleIdWithPwzsAndUsers(googleId);
    }

    public Optional<File> createRootFolder(String folderName) {

        AppFolder appFolder = sessionContext.getAppFolder();
        File rootsFolderFile = googleFileWorkerUtil.getFileByNameAndParents("roots", appFolder.getGoogleId());
        return Optional.ofNullable(
                googleFileWorkerUtil.createFile(folderName, rootsFolderFile.getName(), MimeType.FOLDER, rootsFolderFile.getId()));
    }

    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public void changeCurrentRootFolderOnExist(ChangeRootMethod changeRootMethod) {
        while (true) {
            if (changeRootMethod.equals(ChangeRootMethod.GOOGLE_ID)) {
                System.out.printf("%sВведите %s папки на вашем Google Disk:\n", jColorUtil.INFO,
                        jColorUtil.turnTextIntoColor("google id", JColorUtil.COLOR.INFO));
            } else {
                System.out.printf("%sВведите %s папки на вашем Google Disk:\n", jColorUtil.INFO,
                        jColorUtil.turnTextIntoColor("название", JColorUtil.COLOR.INFO));
            }
            Optional<String> parsed = inputUtil.parseInput();

            if (parsed.isEmpty()){
                continue;
            }
            List<File> files = findRootFolder(changeRootMethod, parsed.get());

            if (changeRootMethod.equals(ChangeRootMethod.GOOGLE_ID)) {
                if (files.isEmpty()){
                    System.out.printf("%sНе найдено папок с указанным %s\n", jColorUtil.WARN,
                            jColorUtil.turnTextIntoColor("google id", JColorUtil.COLOR.INFO));
                    return;
                } else {
                    File file = files.getFirst();
                    RootFolder.RootFolderBuilder rootFolderBuilder = RootFolder.builder()
                            .title(file.getName())
                            .googleId(file.getId())
                            .createdTime(file.getCreatedTime())
                            .appSettings(sessionContext.getAppSettings());

                    File pwzsFolder = googleFileWorkerUtil.getFileByNameAndParents("pwzs", file.getId());

                    rootFolderBuilder.pwzsFolderId(pwzsFolder.getId());

                    RootFolder newRootFolder = rootFolderBuilder.build();

                    Optional<RootFolder> lastRootFolder = sessionContext.getAppSettings().getLastRootFolder();
                    if (lastRootFolder.isPresent() && lastRootFolder.get().equals(newRootFolder)){
                        System.out.printf("%sЭта папка уже выбрана\n", jColorUtil.WARN);
                        return;
                    }
                    findRootFolderByGoogleIdAndSaveIfItNotExist(newRootFolder);
                    return;
                }
            } else {
                RootFolder.RootFolderBuilder rootFolderBuilder = RootFolder.builder();
                RootFolder newRootFolder = null;
                if (files.isEmpty()){
                    System.out.printf("%sНе найдено папок с указанным %s\n", jColorUtil.WARN,
                            jColorUtil.turnTextIntoColor("названием", JColorUtil.COLOR.INFO));
                    return;
                } else if (files.size() == 1){
                    File file = files.getFirst();
                    rootFolderBuilder
                            .title(file.getName())
                            .googleId(file.getId())
                            .createdTime(file.getCreatedTime())
                            .appSettings(sessionContext.getAppSettings());

                    File pwzsFolder = googleFileWorkerUtil.getFileByNameAndParents("pwzs", file.getId());

                    rootFolderBuilder.pwzsFolderId(pwzsFolder.getId());

                    newRootFolder = rootFolderBuilder.build();
                } else {
                    boolean printFolders = true;
                    boolean newRootFolderInitialized = false;
                    while (!newRootFolderInitialized) {
                        if (printFolders) {
                            System.out.printf("%sНайдено несколько папок с таким названием:\n", jColorUtil.INFO);
                            int folderNum = 1;
                            for (File file : files) {
                                System.out.printf("%s. %s (Дата создания: %s)\n",
                                        folderNum++,
                                        jColorUtil.turnTextIntoColor(file.getName(), JColorUtil.COLOR.INFO),
                                        jColorUtil.turnTextIntoColor(
                                                file.getCreatedTime().toString(), JColorUtil.COLOR.INFO));
                            }
                        }
                        printFolders = false;
                        Optional<String> parsedFolderNum = inputUtil.parseInput("\\d+");
                        if (parsedFolderNum.isEmpty()){
                            System.out.printf("%sУказано неверное значение\n", jColorUtil.ERROR);
                            continue;
                        }
                        int folderNum = Integer.parseInt(parsedFolderNum.get());
                        if (folderNum < 1 || folderNum > files.size()){
                            System.out.printf("%sВыберите цифрой от %s до %s\n",
                                    jColorUtil.WARN, jColorUtil.turnTextIntoColor("1", JColorUtil.COLOR.INFO),
                                    jColorUtil.turnTextIntoColor(
                                            Integer.toString(files.size()), JColorUtil.COLOR.INFO));
                            continue;
                        }
                        File file = files.get(folderNum - 1);

                        rootFolderBuilder
                                .title(file.getName())
                                .googleId(file.getId())
                                .createdTime(file.getCreatedTime())
                                .appSettings(sessionContext.getAppSettings());

                        File pwzsFolder = googleFileWorkerUtil.getFileByNameAndParents("pwzs", file.getId());

                        rootFolderBuilder.pwzsFolderId(pwzsFolder.getId());

                        newRootFolder = rootFolderBuilder.build();
                        newRootFolderInitialized = true;
                    }
                }
                Optional<RootFolder> lastRootFolder = sessionContext.getAppSettings().getLastRootFolder();
                if (lastRootFolder.isPresent() && lastRootFolder.get().equals(newRootFolder)){
                    System.out.printf("%sЭта папка уже выбрана\n", jColorUtil.WARN);
                    return;
                }

                findRootFolderByGoogleIdAndSaveIfItNotExist(newRootFolder);
                checkRootFolderForInternalFolders(newRootFolder);
                return;
            }
        }
    }

    public void findRootFolderByGoogleIdAndSaveIfItNotExist(RootFolder newRootFolder) {
        Optional<RootFolder> foundRootByGoogleId = findByGoogleId(newRootFolder.getGoogleId());

        if (foundRootByGoogleId.isPresent()){
            System.out.printf("%sПапка '%s' успешно выбрана\n", jColorUtil.SUCCESS,
                    jColorUtil.turnTextIntoColor(newRootFolder.getTitle(), JColorUtil.COLOR.INFO));
            AppSettings updAppSettings = appSettingsService.updateSettings(settings ->
                    settings.setLastRootFolder(foundRootByGoogleId.get()));
            sessionContext.setAppSettings(updAppSettings);
            sessionContext.setCurrentRootFolder(foundRootByGoogleId.get());
        } else {
            System.out.printf("%sЗначение ставки '%s' для этой папки выбрано по умолчанию\n", jColorUtil.INFO,
                    jColorUtil.turnTextIntoColor(sessionContext.getAppSettings().getPayRate().toString(),
                            JColorUtil.COLOR.INFO));
            newRootFolder.setPayRate(sessionContext.getAppSettings().getPayRate());

            save(newRootFolder);

            System.out.printf("%sПапка '%s' успешно сохранена и выбрана\n", jColorUtil.SUCCESS,
                    jColorUtil.turnTextIntoColor(newRootFolder.getTitle(), JColorUtil.COLOR.INFO));

            sessionContext.setCurrentRootFolder(newRootFolder);

            AppSettings updAppSettings = appSettingsService.updateSettings(settings ->
                    settings.setLastRootFolder(newRootFolder));

            sessionContext.setAppSettings(updAppSettings);
        }
    }

    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public void createNewRootFolderInteractive() {
        System.out.printf("""
                %sДайте название для рабочей папки на вашем Google Disk:
                """, jColorUtil.INFO);

        String rootFolderTitle = inputUtil.parseInput().orElseThrow();

        Optional<File> createdRootFolderOpt = createRootFolder(rootFolderTitle);

        if (createdRootFolderOpt.isPresent()) {
            File createdRootFolder = createdRootFolderOpt.get();
            AppSettings appSettings = sessionContext.getAppSettings();

            RootFolder.RootFolderBuilder rootFolderBuilder = RootFolder.builder()
                    .googleId(createdRootFolder.getId())
                    .title(createdRootFolder.getName())
                    .payRate(appSettings.getPayRate())
                    .appSettings(appSettings)
                    .createdTime(createdRootFolder.getCreatedTime());

            String pwzsFolderId = checkRootFolderForInternalFolders(rootFolderBuilder.build());

            rootFolderBuilder.pwzsFolderId(pwzsFolderId);

            RootFolder newRootFolder = rootFolderBuilder.build();

            save(newRootFolder);

            Optional<User> currentUserOpt = sessionContext.getCurrentUser();

            if (currentUserOpt.isPresent()){
                User currentUser = userService.findById(currentUserOpt.get().getId()).orElseThrow();
                currentUser.addRootFolder(newRootFolder);
                userService.saveWithoutPasswordEncoding(currentUser);
            }

            appSettings = appSettingsService.updateSettings(settings ->
                    settings.setLastRootFolder(newRootFolder));

            sessionContext.setAppSettings(appSettings);
            sessionContext.setCurrentRootFolder(newRootFolder);
        }
    }

    public String checkRootFolderForInternalFolders(RootFolder rootFolder) {

        Optional<FileList> foundFiles = googleFileWorkerUtil.getDirectoryFileList(rootFolder.getGoogleId());
        if (foundFiles.isPresent()) {
            FileList fileList = foundFiles.get();
            List<File> files = fileList.getFiles();
            boolean pwzsFolderNotFound = true;
            String pwzsFolderId = null;

            for (File file : files) {
                if (file.getName().equals("pwzs")){
                    pwzsFolderId = file.getId();
                    pwzsFolderNotFound = false;
                    break;
                }
            }
            if (pwzsFolderNotFound) {
                pwzsFolderId = googleFileWorkerUtil.createFile(
                        "pwzs", rootFolder.getTitle(), MimeType.FOLDER, rootFolder.getGoogleId()).getId();

            }
            sessionContext.setCurrentPwzsFolderId(pwzsFolderId);
            return pwzsFolderId;
        } else {
            System.out.printf("Root folder does not exist: %s\n", rootFolder.getGoogleId());
            return null;
        }
    }

    public List<File> findRootFolder(ChangeRootMethod changeRootMethod, String rootIdOrTitle) {
        AppFolder appFolder = sessionContext.getAppFolder();

        Optional<FileList> filesOpt = googleFileWorkerUtil.getDirectoryFileList(appFolder.getRootsFolderGoogleId());
        if(filesOpt.isEmpty()){
            System.out.printf("%sВозникла непредвиденная ошибка\n", jColorUtil.ERROR);
            return null;
        }
        FileList files = filesOpt.get();

        if(changeRootMethod.equals(ChangeRootMethod.GOOGLE_ID)) {
            return files.getFiles()
                    .stream()
                    .filter(file -> file.getId().equals(rootIdOrTitle))
                    .findFirst()
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
        } else {
            return files.getFiles()
                    .stream()
                    .filter(file -> file.getName().equals(rootIdOrTitle))
                    .toList();
        }
    }

    public void deleteRootFolder() {
        if (sessionContext.getCurrentRootFolder().isEmpty()){
            System.out.printf("%sНе выбрана корневая папка, выберите ее, перед тем как производить удаление\n",
                    jColorUtil.WARN);
        } else {
            RootFolder currentRootFolder = findById(sessionContext.getCurrentRootFolder().get().getId()).orElseThrow();

            String firstQuestion = "Вы действительно хотите удалить текущую корневую папку";
            if(inputUtil.askYesOrNo(firstQuestion, "", JColorUtil.COLOR.WARN)){
                String secondQuestion = "Вы точно уверены в своем решении";
                if (inputUtil.askYesOrNo(secondQuestion, "", JColorUtil.COLOR.WARN)){


                    for(User user: userService.findAllByRootFoldersContaining(currentRootFolder)){
                        user = userService.findById(user.getId()).orElseThrow();
                        user.removeRootFolder(currentRootFolder);
                        userService.saveWithoutPasswordEncoding(user);
                    }

                    AppSettings updAppSettings = appSettingsService.updateSettings(settings -> {
                        settings.setLastRootFolder(null);
                        settings.setLastPwz(null);
                        settings.setLastMonthSheet(null);
                        settings.getRootFolders().remove(currentRootFolder);
                    });

                    delete(currentRootFolder);

                    googleFileWorkerUtil.deleteFileById(currentRootFolder.getGoogleId(), currentRootFolder.getTitle());

                    sessionContext.setAppSettings(updAppSettings);
                    sessionContext.setCurrentRootFolder(null);
                    sessionContext.setCurrentPwz(null);
                    sessionContext.setCurrentPwzsFolderId(null );
                    sessionContext.setCurrentMonthSheet(null);
                }
            }
        }
    }

    public void renameRootFolder() {
        Optional<RootFolder> currentRootFolderOpt = sessionContext.getCurrentRootFolder();
        if (currentRootFolderOpt.isEmpty()) {
            System.out.printf("%sВозникла непредвиденная ошибка\n", jColorUtil.ERROR);
        } else {
            RootFolder rootFolder = currentRootFolderOpt.get();
            if (inputUtil.askYesOrNo("Вы уверены, что хотите изменить имя текущей корневой папки",
                    "'" + rootFolder.getTitle() + "'", JColorUtil.COLOR.INFO)) {
                Optional<RootFolder> foundRootFolder = findById(rootFolder.getId());
                String oldName;
                String googleId;
                if (foundRootFolder.isPresent()) {
                    rootFolder = foundRootFolder.get();
                    oldName = rootFolder.getTitle();
                    googleId = rootFolder.getGoogleId();
                } else {
                    System.out.printf("%sВозникла непредвиденная ошибка\n", jColorUtil.ERROR);
                    return;
                }

                System.out.printf("%sВведите новое имя папки:\n", jColorUtil.INFO);

                Optional<String> parsed = inputUtil.parseInput();
                String newName = parsed.orElseThrow();

                if (newName.equals(oldName)) {
                    System.out.printf("%sИмя не было изменено, так как это имя уже указано\n",
                            jColorUtil.WARN);
                    return;
                }

                googleFileWorkerUtil.renameFileById(googleId, oldName, newName);
                rootFolder.setTitle(newName);
                save(rootFolder);
                sessionContext.setCurrentRootFolder(rootFolder);
            }
        }
    }

    public void changePayRate() {
        Optional<RootFolder> currentRootFolderOpt = sessionContext.getCurrentRootFolder();
        if (currentRootFolderOpt.isEmpty()) {
            System.out.printf("%sВозникла непредвиденная ошибка\n", jColorUtil.ERROR);
        } else {
            RootFolder rootFolder = currentRootFolderOpt.get();
            Double oldPayRate = rootFolder.getPayRate();
            if (inputUtil.askYesOrNo("Вы уверены, что хотите изменить ставку",
                    "'" + oldPayRate + "'", JColorUtil.COLOR.INFO)){
                Optional<RootFolder> foundRootFolder = findById(rootFolder.getId());

                if (foundRootFolder.isEmpty()){
                    System.out.printf("%sВозникла непредвиденная ошибка\n", jColorUtil.ERROR);
                } else {
                    rootFolder = foundRootFolder.get();
                    boolean isWrongPayRate = false;
                    while(true) {
                        if(!isWrongPayRate) {
                            System.out.printf("%sВведите новую ставку:\n", jColorUtil.INFO);
                        }

                        Optional<String> parsed = inputUtil.parseInput("\\d+(\\.\\d+)?");

                        if (parsed.isEmpty() || Double.parseDouble(parsed.get()) <= 0) {
                            System.out.printf("%sУказано неверное значение\n", jColorUtil.ERROR);
                            isWrongPayRate = true;
                            continue;
                        }

                        Double newPayRate = Double.parseDouble(parsed.get());

                        if (newPayRate.equals(oldPayRate)) {
                            System.out.printf("%sСтавка не была изменена, так как эта ставка уже указана\n",
                                    jColorUtil.WARN);
                            return;
                        }

                        rootFolder.setPayRate(newPayRate);
                        save(rootFolder);
                        sessionContext.setCurrentRootFolder(rootFolder);

                        System.out.printf("%sСтавка '%s' успешно изменена на '%s'\n", jColorUtil.SUCCESS,
                                jColorUtil.turnTextIntoColor(oldPayRate.toString(), JColorUtil.COLOR.SUCCESS),
                                jColorUtil.turnTextIntoColor(newPayRate.toString(), JColorUtil.COLOR.SUCCESS));
                        return;
                    }
                }
            }
        }
    }
}