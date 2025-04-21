package ru.stayyhydratedd.wbot.ShiftSheet.services;

import com.google.api.services.sheets.v4.model.Sheet;
import lombok.RequiredArgsConstructor;
import org.intellij.lang.annotations.Language;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.stayyhydratedd.wbot.ShiftSheet.context.SessionContext;
import ru.stayyhydratedd.wbot.ShiftSheet.dtos.OwnerDTO;
import ru.stayyhydratedd.wbot.ShiftSheet.models.*;
import ru.stayyhydratedd.wbot.ShiftSheet.util.DateUtil;
import ru.stayyhydratedd.wbot.ShiftSheet.util.InputOutputUtil;
import ru.stayyhydratedd.wbot.ShiftSheet.util.JColorUtil;

import java.io.Console;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ConsoleService {

    private final OwnerService ownerService;
    private final AppSettingsService appSettingsService;
    private final RootFolderService rootFolderService;
    private final PwzService pwzService;
    private final MonthSheetService monthSheetService;
    private final SessionContext sessionContext;
    private final PasswordEncoder passwordEncoder;
    private final JColorUtil jColorUtil;
    private final InputOutputUtil inputOutputUtil;
    private final DateUtil dateUtil;
    private final Scanner scanner = new Scanner(System.in);

    private static boolean pwzIsMissing = false;
    private static boolean monthSheetIsMissing = false;

    public void showControlPanel(){

        Map<String, String> commands = new HashMap<>(){{

        }};

        while(true){
            System.out.printf("""
                    =========================ГЛАВНАЯ=========================
                    1. Работа с корневой папкой
                    2. Работа с ПВЗ
                    3. Работа с листами
                    """);
            String parsed = parseInput("\\d");
        }
    }
//    =============================================================================
    public void ownerAuthenticationStage(List<Owner> owners){
        if(owners.isEmpty()){
            System.out.printf("%sНе удалось обнаружить владельцев, создайте нового, чтобы продолжить работу\n",
                    jColorUtil.INFO);
            createOwnerInteractive(new OwnerDTO());
        } else if(owners.size() == 1){
            authenticateOwnerInteractive(owners.getFirst());
        } else {
            System.out.printf("%sВыберите владельца для входа:\n", jColorUtil.INFO);
            Owner owner = selectOwnerInteractive(owners);
            authenticateOwnerInteractive(owner);
        }
    }

    public void createOwnerInteractive(OwnerDTO ownerDTO) {
        boolean nameAlreadyExists = true;
        boolean firstNameAttempt = true;

        do {
            if (firstNameAttempt)
                System.out.printf("%sВведите имя нового владельца:\n", jColorUtil.INFO);
            else
                System.out.printf("%sВведите другое имя:\n", jColorUtil.INFO);
            String name = parseInput();
            Optional<Owner> foundOwner = ownerService.findByName(name);
            if (foundOwner.isPresent()) {
                System.out.printf("%sВладелец с таким именем уже существует\n", jColorUtil.WARN);
                firstNameAttempt = false;
            } else {
                nameAlreadyExists = false;
                ownerDTO.setName(name);
            }
        } while (nameAlreadyExists);

        boolean passwordsDoNotMatch = true;

        do{
            System.out.printf("%sВведите новый пароль для входа:\n", jColorUtil.INFO);
            ownerDTO.setPassword(parseInput());

            System.out.printf("%sПодтвердите пароль:\n", jColorUtil.INFO);
            ownerDTO.setConfirmPassword(parseInput());

            if (!ownerDTO.getPassword().equals(ownerDTO.getConfirmPassword())) {
                System.out.printf("%sВведенные пароли не совпадают\n", jColorUtil.ERROR);
            } else {
                passwordsDoNotMatch = false;
                Owner owner = ownerService.ownerDtoToOwner(ownerDTO);
                ownerService.save(owner);
                System.out.printf("%sПользователь '%s' успешно добавлен\n", jColorUtil.INFO,
                        jColorUtil.turnTextIntoColor(ownerDTO.getName(), JColorUtil.COLOR.INFO));
            }
        } while (passwordsDoNotMatch);
    }

    public void authenticateOwnerInteractive(Owner owner){
        boolean incorrectPassword = true;
        do {
            System.out.printf("%sВведите пароль для входа:\n", jColorUtil.INFO);
            String password = parseInput();
            if(passwordEncoder.matches(password, owner.getPassword())){
                System.out.printf("Привет, %s!\n",
                        jColorUtil.turnTextIntoColor(owner.getName(), JColorUtil.COLOR.INFO));
                sessionContext.setCurrentOwner(owner);
                incorrectPassword = false;
            } else{
                System.out.printf("%sНеправильный пароль\n", jColorUtil.ERROR);
            }
        } while (incorrectPassword);
    }

    public Owner selectOwnerInteractive(List<Owner> owners){
        int ownerNumber = 1;
        for(Owner owner : owners) {
            System.out.printf("%d. %s\n", ownerNumber++, owner.getName());
        }
        while (true) {
            String parsed = parseInput("\\d+");
            if (parsed == null){
                continue;
            }

            int choiceNum = Integer.parseInt(parsed);

            if (choiceNum <= 0 || choiceNum > owners.size()){
                System.out.println("Владельца под таким номером нет");
            } else {
                System.out.printf("Владелец '%s' успешно выбран.\n", owners.get(choiceNum - 1).getName());
                return owners.get(choiceNum - 1);
            }
        }
    }
//    =============================================================================
    public void rootIdentityStage(AppSettings appSettings) {
        if (appSettings.getLastRootFolder() != null) {
            System.out.printf("%sРабочая папка '%s' была автоматически выбрана с прошлой сессии\n",
                    jColorUtil.INFO,
                    jColorUtil.turnTextIntoColor(appSettings.getLastRootFolder().getTitle(), JColorUtil.COLOR.INFO));
            sessionContext.setCurrentRootFolder(appSettings.getLastRootFolder());
        } else {
            List<RootFolder> rootFolders = appSettings.getRootFolders();
            if (rootFolders.isEmpty()) {
                createNewRootFolderInteractive();
            } else if (rootFolders.size() == 1) {
                RootFolder rootFolder = rootFolders.getFirst();
                System.out.printf("%sРабочая папка '%s' была выбрана автоматически\n",
                        jColorUtil.INFO,
                        jColorUtil.turnTextIntoColor(rootFolder.getTitle(), JColorUtil.COLOR.INFO));
                sessionContext.setCurrentRootFolder(rootFolder);
                appSettingsService.updateSettings(settings -> settings.setLastRootFolder(rootFolder));
            } else {
                selectRootFolderInteractive(rootFolders);
            }
        }
    }

    public void createNewRootFolderInteractive() {
        System.out.printf("""
        %sНе указано рабочее пространство на вашем Google Disk'е
        %sДайте название для рабочей папки:
        """, jColorUtil.INFO, jColorUtil.INFO);
        String rootFolderTitle = parseInput(".+");
        String rootId = rootFolderService.createRootFolder(rootFolderTitle);

        setPayRateForAppSettingsInteractive();

        if (rootId != null) {
            AppSettings appSettings = sessionContext.getAppSettings();

            RootFolder rootFolder = new RootFolder(rootId, rootFolderTitle,
                    appSettings.getPayRate(), appSettings);

            System.out.println(rootFolder);

            rootFolderService.save(rootFolder);

            appSettingsService.updateSettings(settings -> {
                settings.setRootFolders(Collections.singletonList(rootFolder));
            });
            sessionContext.setCurrentRootFolder(rootFolder);
        }
    }

    public void selectRootFolderInteractive(List<RootFolder> rootFolders) {
        boolean rootFolderSelected = false;
        boolean firstAttempt = true;
        do {
            if (firstAttempt) {
                System.out.println("Выберите рабочую папку для продолжения работы: ");
                int rootFolderNumber = 1;
                for (RootFolder rootFolder : rootFolders) {
                    System.out.println(rootFolderNumber + ". " + rootFolder.getTitle());
                    rootFolderNumber++;
                }
            }
            firstAttempt = false;
            int choiceNum = Integer.parseInt(parseInput("\\d"));

            if (choiceNum > rootFolders.size() || choiceNum < 1) {
                System.out.printf("Выберите цифрой от 1 до %d\n", rootFolders.size());
            } else {
                RootFolder rootFolder = rootFolders.get(choiceNum - 1);
                System.out.printf("Рабочая папка '%s' выбрана успешно\n",
                        rootFolder.getTitle());
                appSettingsService.updateSettings(settings -> settings.setLastRootFolder(rootFolder));
                sessionContext.setCurrentRootFolder(rootFolder);
                rootFolderSelected = true;
            }
        } while (!rootFolderSelected);
    }
//    =============================================================================
    public void pwzIdentityStage(AppSettings appSettings){
        if(appSettings.getLastPwz() != null) {
            Pwz lastPwz = appSettings.getLastPwz();
            sessionContext.setCurrentPwz(appSettings.getLastPwz());
            System.out.printf("%sПвз '%s' был автоматически выбран с прошлой сессии\n", jColorUtil.INFO,
                    jColorUtil.turnTextIntoColor(lastPwz.getAddress(), JColorUtil.COLOR.INFO));
        } else {
            List<Pwz> pwzs = pwzService.findAll();
            //todo
            if(pwzs.isEmpty()){
                createNewPwzInteractive();
            } else if (pwzs.size() == 1) {
                Pwz pwz = pwzs.getFirst();
                System.out.printf("%sПвз '%s' был выбран автоматически\n", jColorUtil.INFO,
                        jColorUtil.turnTextIntoColor(pwz.getAddress(), JColorUtil.COLOR.INFO));
                sessionContext.setCurrentPwz(pwz);
                appSettingsService.updateSettings(settings -> settings.setLastPwz(pwz));
            } else {
                selectPwzInteractive(pwzs);
            }
        }
    }

    public void createNewPwzInteractive() {
        boolean pwzAddressIsValid = false;
        System.out.printf("%sУкажите адрес вашего пвз:\n", jColorUtil.INFO);
        do{
            String parsed = parseInput("^[А-ЯЁа-яё\\s]+\\s\\d+[а-яА-Я]?(([к/])?\\d+)?$", "(help|/help)");
            if (parsed == null) {
                System.out.printf("""
                        %sУказанный адрес имеет неверный формат
                        Введите '%s' для получения справки:
                        """, jColorUtil.ERROR, jColorUtil.turnTextIntoColor("/help", JColorUtil.COLOR.INFO));
            } else {
                if(parsed.matches("(help|/help)")){
                    //todo help info с форматами
                } else{
                    pwzAddressIsValid = true;
                    Pwz pwz = pwzService.createPwz(parsed);
                    sessionContext.setCurrentPwz(pwz);
                }
            }
        } while (!pwzAddressIsValid);
    }

    public void selectPwzInteractive(List<Pwz> pwzs) {
        boolean pwzSelected = false;
        boolean firstAttempt = true;
        do {
            if (firstAttempt) {
                System.out.println("Выберите ПВЗ для продолжения работы: ");
                int pwzNumber = 1;
                for (Pwz pwz : pwzs) {
                    System.out.println(pwzNumber + ". " + pwz.getAddress());
                    pwzNumber++;
                }
            }
            firstAttempt = false;
            int choiceNum = Integer.parseInt(parseInput("\\d"));

            if (choiceNum > pwzs.size() || choiceNum < 1) {
                System.out.printf("Выберите цифрой от 1 до %d\n", pwzs.size());
            } else {
                Pwz pwz = pwzs.get(choiceNum - 1);
                System.out.printf("ПВЗ '%s' успешно выбран\n", //todo
                        pwz.getAddress());
                sessionContext.setCurrentPwz(pwz);
                appSettingsService.updateSettings(settings -> settings.setLastPwz(pwz));
                pwzSelected = true;
            }
        } while (!pwzSelected);
    }
//    =============================================================================
    public void monthSheetIdentityStage(AppSettings appSettings) {

        if(appSettings.getLastMonthSheet() != null){

            MonthSheet lastMonthSheet = appSettings.getLastMonthSheet();
            sessionContext.setCurrentMonthSheet(lastMonthSheet);

            System.out.printf("%sЛист '%s' был автоматически выбран с прошлой сессии", jColorUtil.INFO,
                    jColorUtil.turnTextIntoColor(
                            dateUtil.getFullMonthSheetDate(lastMonthSheet), JColorUtil.COLOR.INFO));
        } else{
            List<MonthSheet> monthSheets = monthSheetService.findAll();
            if(monthSheets.isEmpty()){
                createNewMonthSheetInteractive(sessionContext.getCurrentPwz().getGoogleId());
            } else if(monthSheets.size() == 1){
                MonthSheet monthSheet = monthSheets.getFirst();
                System.out.printf("%sЛист '%s' был выбран автоматически", jColorUtil.INFO,
                        jColorUtil.turnTextIntoColor(
                                dateUtil.getFullMonthSheetDate(monthSheet), JColorUtil.COLOR.INFO));

                sessionContext.setCurrentMonthSheet(monthSheet);
                appSettingsService.updateSettings(settings ->
                        settings.setLastMonthSheet(monthSheet));
            } else {
                selectMonthSheetInteractive(monthSheets);
            }
        }
    }

    public void createNewMonthSheetInteractive(String pwzGoogleId) {

        MonthSheet monthSheet = monthSheetService.createMonthSheet(pwzGoogleId);

        sessionContext.setCurrentMonthSheet(monthSheet);

        monthSheetService.executeCreateMonthSheetInPwzSpreadsheet(monthSheet);

        monthSheetService.executeFormatMonthSheetInPwzSpreadsheet(monthSheet);

        List<Sheet> sheets = pwzService.getMonthSheets(pwzGoogleId);
        for(Sheet sheet : sheets){
            if(sheet.getProperties().getSheetId() == 0){
                monthSheetService.executeDeleteZeroIdSheet(pwzGoogleId);
            }
        }
    }

    public void selectMonthSheetInteractive(List<MonthSheet> monthSheets) {
        boolean monthSheetSelected = false;
        boolean firstAttempt = true;
        do {
            if (firstAttempt) {
                System.out.println("Выберите лист для продолжения работы: ");
                int monthSheetNumber = 1;
                for (MonthSheet monthSheet : monthSheets) {
                    System.out.println(monthSheetNumber + ". " + dateUtil.getFullMonthSheetDate(monthSheet));
                    monthSheetNumber++;
                }
            }
            firstAttempt = false;
            int choiceNum = Integer.parseInt(parseInput("\\d"));

            if (choiceNum > monthSheets.size() || choiceNum < 1) {
                System.out.printf("Выберите цифрой от 1 до %d\n", monthSheets.size());
            } else {
                MonthSheet monthSheet = monthSheets.get(choiceNum - 1);
                System.out.printf("Лист '%s' успешно выбран\n", //todo
                        dateUtil.getFullMonthSheetDate(monthSheet));
                sessionContext.setCurrentMonthSheet(monthSheet);
                appSettingsService.updateSettings(settings -> settings.setLastMonthSheet(monthSheet));
                monthSheetSelected = true;
            }
        } while (!monthSheetSelected);
    }
//    =============================================================================
    public void setPayRateForAppSettingsInteractive() {
        System.out.printf("""
                %sУкажите ставку по которой будет производиться подсчет зарплаты
                %sЭто значение можно будет изменить позже
                """, jColorUtil.INFO, jColorUtil.INFO);
        boolean payRateIsValid = false;
        double payRate;
        do {
            String parsed = parseInput("\\d+");
            if (parsed != null) {
                payRate = Double.parseDouble(parsed);
                if (payRate <= 0) {
                    System.out.printf("%sСтавка не может быть нулевой или отрицательной\n", jColorUtil.ERROR);
                } else {
                    payRateIsValid = true;
                    final double payRateConst = payRate;
                     AppSettings appSettings = appSettingsService.updateSettings(settings ->
                            settings.setPayRate(payRateConst));
                     sessionContext.setAppSettings(appSettings);
                    System.out.printf("%sСтавка успешно указана\n", jColorUtil.SUCCESS);
                }
            } else {
                System.out.printf("%sУказано неверное значение для ставки\n", jColorUtil.ERROR);
            }
        } while (!payRateIsValid);
    }
//    =============================================================================
    public String parseInput(@Language("RegExp") String... regexps) {
        System.out.print(jColorUtil.turnTextIntoColor(">", JColorUtil.COLOR.INFO));
        return inputOutputUtil.parseInput(scanner, regexps);
    }
}