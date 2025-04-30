package ru.stayyhydratedd.wbot.ShiftSheet.services;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import ru.stayyhydratedd.wbot.ShiftSheet.context.SessionContext;
import ru.stayyhydratedd.wbot.ShiftSheet.enums.ChangeRootMethod;
import ru.stayyhydratedd.wbot.ShiftSheet.enums.MimeType;
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
        return Optional.ofNullable(
                googleFileWorkerUtil.createFile(folderName, "Google Disk", MimeType.FOLDER));
    }

    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public void changeCurrentRootFolderOnExist(ChangeRootMethod changeRootMethod) {
        while (true) {
            if (changeRootMethod.equals(ChangeRootMethod.GOOGLE_ID)) {
                System.out.printf("%sВведите %s папки на вашем Google Disk:\n", jColorUtil.INFO,
                        jColorUtil.turnTextIntoColor("google_id", JColorUtil.COLOR.INFO));
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
                            jColorUtil.turnTextIntoColor("google_id", JColorUtil.COLOR.INFO));
                    return;
                } else {
                    File file = files.getFirst();
                    RootFolder newRootFolder = RootFolder.builder()
                            .title(file.getName())
                            .googleId(file.getId())
                            .appSettings(sessionContext.getAppSettings())
                            .build();

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
                    newRootFolder = rootFolderBuilder
                            .title(file.getName())
                            .googleId(file.getId())
                            .appSettings(sessionContext.getAppSettings())
                            .build();
                } else {
                    boolean printFolders = true;
                    boolean newRootFolderInitialized = false;
                    while (!newRootFolderInitialized) {
                        if (printFolders) {
                            System.out.printf("%sНайдено несколько папок с таким названием:\n", jColorUtil.INFO);
                            int folderNum = 1;
                            for (File file : files) {
                                System.out.printf("%s. %s, дата создания(%s)\n",
                                        jColorUtil.turnTextIntoColor(
                                                Integer.toString(folderNum++), JColorUtil.COLOR.INFO),
                                        file.getName(),
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
                        newRootFolder = rootFolderBuilder
                                .title(file.getName())
                                .googleId(file.getId())
                                .appSettings(sessionContext.getAppSettings())
                                .build();
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
            appSettingsService.updateSettings(settings ->
                    settings.setLastRootFolder(foundRootByGoogleId.get()));
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
            appSettingsService.updateSettings(settings ->
                    settings.setLastRootFolder(newRootFolder));

            checkRootFolderForInternalFolders(newRootFolder);
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

            RootFolder newRootFolder = RootFolder.builder()
                    .googleId(createdRootFolder.getId())
                    .title(createdRootFolder.getName())
                    .payRate(appSettings.getPayRate())
                    .appSettings(appSettings)
                    .createdTime(createdRootFolder.getCreatedTime())
                    .build();

            save(newRootFolder);

            Optional<User> currentUserOpt = sessionContext.getCurrentUser();

            if (currentUserOpt.isPresent()){
                System.out.println(currentUserOpt.get().getPassword());
                User currentUser = userService.findById(currentUserOpt.get().getId()).orElseThrow();
                System.out.println(currentUser.getPassword());
                currentUser.addRootFolder(newRootFolder);
                userService.saveWithoutPasswordEncoding(currentUser);
            }

            appSettingsService.updateSettings(settings ->
                    settings.setLastRootFolder(newRootFolder));

            sessionContext.setCurrentRootFolder(newRootFolder);

            checkRootFolderForInternalFolders(newRootFolder);
        }
    }

    public void checkRootFolderForInternalFolders(RootFolder rootFolder) {
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
            String finalPwzFolderId = pwzsFolderId;
            appSettingsService.updateSettings(settings -> settings.setPwzsFolderId(finalPwzFolderId));
            sessionContext.setCurrentPwzsFolderId(pwzsFolderId);
        } else {
            System.out.printf("Root folder does not exist: %s\n", rootFolder.getGoogleId());
        }
    }

    public List<File> findRootFolder(ChangeRootMethod changeRootMethod, String idOrTitle) {
        FileList files = googleFileWorkerUtil.getDriveFoldersList();
        if(changeRootMethod.equals(ChangeRootMethod.GOOGLE_ID)) {
            return files.getFiles()
                    .stream()
                    .filter(file -> file.getId().equals(idOrTitle))
                    .findFirst()
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
        } else {
            return files.getFiles()
                    .stream()
                    .filter(file -> file.getName().equals(idOrTitle))
                    .toList();
        }
    }

    public void deleteRootFolder() {
        if (sessionContext.getCurrentRootFolder().isEmpty()){
            System.out.printf("%sНе выбрана корневая папка, выберите ее, перед тем как производить удаление\n",
                    jColorUtil.WARN);
        } else {
            RootFolder currentRootFolder = sessionContext.getCurrentRootFolder().get();

            String firstQuestion = "Вы действительно хотите удалить текущую корневую папку";
            if(inputUtil.askYesOrNo(firstQuestion, "", JColorUtil.COLOR.WARN)){
                String secondQuestion = "Вы точно уверены в своем решении";
                if (inputUtil.askYesOrNo(secondQuestion, "", JColorUtil.COLOR.WARN)){


                    for(User user: userService.findAllByRootFoldersContaining(currentRootFolder)){
                        user = userService.findById(user.getId()).orElseThrow();
                        user.removeRootFolder(currentRootFolder);
                        userService.saveWithoutPasswordEncoding(user);
                    }

                    appSettingsService.updateSettings(settings -> {
                        settings.setLastRootFolder(null);
                        settings.getRootFolders().remove(currentRootFolder);
                    });

                    delete(currentRootFolder);

                    googleFileWorkerUtil.deleteFileById(currentRootFolder.getGoogleId(), currentRootFolder.getTitle());

                    sessionContext.setCurrentRootFolder(null);
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