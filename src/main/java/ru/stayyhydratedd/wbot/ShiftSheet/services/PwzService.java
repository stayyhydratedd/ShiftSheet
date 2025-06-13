package ru.stayyhydratedd.wbot.ShiftSheet.services;

import com.google.api.services.drive.model.File;
import com.google.api.services.sheets.v4.model.Sheet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.stayyhydratedd.wbot.ShiftSheet.context.ConditionContextManager;
import ru.stayyhydratedd.wbot.ShiftSheet.context.SessionContext;
import ru.stayyhydratedd.wbot.ShiftSheet.enums.ConditionContext;
import ru.stayyhydratedd.wbot.ShiftSheet.enums.MimeType;
import ru.stayyhydratedd.wbot.ShiftSheet.models.AppSettings;
import ru.stayyhydratedd.wbot.ShiftSheet.models.Pwz;
import ru.stayyhydratedd.wbot.ShiftSheet.models.RootFolder;
import ru.stayyhydratedd.wbot.ShiftSheet.repositories.PwzRepository;
import ru.stayyhydratedd.wbot.ShiftSheet.util.*;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PwzService {

    private final PwzRepository pwzRepository;
    private final GoogleFileWorkerUtil googleFileWorkerUtil;
    private final SessionContext sessionContext;
    private final HelperUtil helper;
    private final PrinterUtil printer;
    private final JColorUtil jColorUtil;
    private final InputOutputUtil inputUtil;
    private final ConditionContextManager contextManager;
    private final AppSettingsService appSettingsService;
    private final RootFolderService rootFolderService;

    public List<Pwz> findAll() {
        return pwzRepository.findAll();
    }

    public Optional<Pwz> findById(int id) {
        return pwzRepository.findByIdWithMonthSheets(id);
    }

    public void save(Pwz pwz) {
        pwzRepository.save(pwz);
    }

    public String createPwzFolder(String folderName) {
        return googleFileWorkerUtil.createFile(
                folderName, "pwzs", MimeType.FOLDER, sessionContext.getCurrentPwzsFolderId().get()).getId();
    }

    public Pwz createPwz(String pwzAddress){
        String pwzFolderId = createPwzFolder("ПВЗ " + pwzAddress);
        String employeeWorkSchedule = "График работы сотрудников ул. " + pwzAddress;

        String pwzId = googleFileWorkerUtil.createFile(
                employeeWorkSchedule, "ПВЗ " + pwzAddress,
                MimeType.SPREADSHEET, pwzFolderId).getId();

        System.out.printf("""
                        %sСтавка по умолчанию для текущего ПВЗ = %s
                        %sЭто значение можно будет изменить позже
                        """, jColorUtil.INFO,
                jColorUtil.turnTextIntoColor(
                        sessionContext.getCurrentRootFolder().get().getPayRate().toString(), JColorUtil.COLOR.INFO),
                jColorUtil.INFO);

        Pwz pwz = Pwz.builder()
                .address(pwzAddress)
                .googleId(pwzId)
                .folderGoogleId(pwzFolderId)
                .payRate(sessionContext.getCurrentRootFolder().get().getPayRate())
                .build();

        pwzRepository.save(pwz);
        return pwz;
    }

//    todo @PreAuthorize
    public void createNewPwz(){

        AppSettings appSettings = sessionContext.getAppSettings();

        if(appSettings.getLastRootFolder().isEmpty()) {
            System.out.printf("%sНеобходимо указать корневую папку, прежде чем создавать ПВЗ\n",
                    jColorUtil.WARN);
        } else {
            Optional<RootFolder> rootFolderOpt = rootFolderService.findById(appSettings.getLastRootFolder().get().getId());
            if(rootFolderOpt.isEmpty()){
                System.out.printf("%sВозникла непредвиденная ошибка\n", jColorUtil.ERROR);
                return;
            }
            RootFolder rootFolder = rootFolderOpt.get();
            contextManager.enterContext(ConditionContext.CREATE_NEW_PWZ);
            System.out.printf("%sУкажите адрес вашего пвз:\n", jColorUtil.INFO);
            while (true){
                Optional<String> parsed = inputUtil.parseInput(
                        "^[А-ЯЁа-яё\\s]+\\s\\d+[а-яА-Я]?(([к/])?\\d+)?$", "/help");
                if (parsed.isEmpty()) {
                    System.out.printf("""
                        %sУказанный адрес имеет неверный формат
                        Введите '%s' для получения справки:
                        """, jColorUtil.ERROR, jColorUtil.turnTextIntoColor("/help", JColorUtil.COLOR.INFO));
                    continue;
                }
                if (parsed.get().equals("/help")) {
                    helper.getHelp(contextManager.getCurrentContext());
                    continue;
                }

                System.out.printf("""
                        %sСтавка по умолчанию для текущего ПВЗ = %s
                        %sЭто значение можно будет изменить позже
                        """, jColorUtil.INFO,
                        jColorUtil.turnTextIntoColor(rootFolder.getPayRate().toString(), JColorUtil.COLOR.INFO),
                        jColorUtil.INFO
                );

                String pwzAddress = parsed.get();
                String pwzFolderId = createPwzFolder("ПВЗ " + pwzAddress);
                String employeeWorkSchedule = "График работы сотрудников ул. " + pwzAddress;

                File pwzFile = googleFileWorkerUtil.createFile(employeeWorkSchedule, "ПВЗ " + pwzAddress,
                        MimeType.SPREADSHEET, pwzFolderId);

                Pwz pwz = Pwz.builder()
                        .address(pwzAddress)
                        .googleId(pwzFile.getId())
                        .payRate(rootFolder.getPayRate())
                        .rootFolder(rootFolder)
                        .build();

                save(pwz);
                rootFolder.getPwzs().add(pwz);
                rootFolderService.save(rootFolder);
                sessionContext.setCurrentPwz(pwz);
                appSettingsService.updateSettings(settings -> settings.setLastPwz(pwz));
                contextManager.exitContext();
                return;
            }
        }
    }

    public List<Sheet> getMonthSheets(String pwzGoogleId) {
        return googleFileWorkerUtil.getSheets(pwzGoogleId);
    }
}
