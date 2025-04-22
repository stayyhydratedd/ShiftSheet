package ru.stayyhydratedd.wbot.ShiftSheet.services;

import com.google.api.services.sheets.v4.model.Sheet;
import lombok.RequiredArgsConstructor;
import org.intellij.lang.annotations.Language;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.stayyhydratedd.wbot.ShiftSheet.context.ConditionContextManager;
import ru.stayyhydratedd.wbot.ShiftSheet.context.SessionContext;
import ru.stayyhydratedd.wbot.ShiftSheet.dtos.OwnerDTO;
import ru.stayyhydratedd.wbot.ShiftSheet.enums.ConditionContext;
import ru.stayyhydratedd.wbot.ShiftSheet.models.*;
import ru.stayyhydratedd.wbot.ShiftSheet.util.DateUtil;
import ru.stayyhydratedd.wbot.ShiftSheet.util.ContextPrinterUtil;
import ru.stayyhydratedd.wbot.ShiftSheet.util.InputOutputUtil;
import ru.stayyhydratedd.wbot.ShiftSheet.util.JColorUtil;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ConsoleService {

    private final OwnerService ownerService;
    private final AppSettingsService appSettingsService;
    private final RootFolderService rootFolderService;
    private final PwzService pwzService;
    private final MonthSheetService monthSheetService;
    private final EmployeeService employeeService;
    private final SessionContext sessionContext;
    private final ConditionContextManager contextManager;
    private final PasswordEncoder passwordEncoder;
    private final JColorUtil jColorUtil;
    private final InputOutputUtil inputOutputUtil;
    private final DateUtil dateUtil;
    private final ContextPrinterUtil printer;
    private final Scanner scanner = new Scanner(System.in);

    private static final Map<String, String> COMMANDS = new HashMap<>(){{
        put("help", "/help");
        put("back", "/back");
    }};

//    =============================================================================
    public void showMainMenu(){
        contextManager.enterContext(ConditionContext.MAIN_MENU);
        while(true){
            System.out.printf("""
                    =========================%s=========================
                    %s. Работа с корневой папкой %s
                    %s. Работа с ПВЗ %s
                    %s. Работа с листом графика %s
                    %s. Работа с сотрудниками %s
                    %s. Вернуться к выбору владельца %s
                    """, jColorUtil.turnTextIntoColor("ГЛАВНАЯ", JColorUtil.COLOR.SUCCESS),
                    jColorUtil.turnTextIntoColor("1", JColorUtil.COLOR.INFO),
                    jColorUtil.turnTextIntoColor("->", JColorUtil.COLOR.INFO),
                    jColorUtil.turnTextIntoColor("2", JColorUtil.COLOR.INFO),
                    jColorUtil.turnTextIntoColor("->", JColorUtil.COLOR.INFO),
                    jColorUtil.turnTextIntoColor("3", JColorUtil.COLOR.INFO),
                    jColorUtil.turnTextIntoColor("->", JColorUtil.COLOR.INFO),
                    jColorUtil.turnTextIntoColor("4", JColorUtil.COLOR.INFO),
                    jColorUtil.turnTextIntoColor("->", JColorUtil.COLOR.INFO),
                    jColorUtil.turnTextIntoColor("0", JColorUtil.COLOR.WARN),
                    jColorUtil.turnTextIntoColor("<-", JColorUtil.COLOR.WARN)
            );
            Optional<String> parsed = parseInput("[01234]");
            if (parsed.isEmpty()){
                System.out.printf("%sУказано неверное значение\n", jColorUtil.ERROR);
                continue;
            }
            if (parsed.get().equals("4")){
                employeeMenu();
            }
        }
    }
//    -----------------------------------------------------------------------------
    public void employeeMenu(){
        contextManager.enterContext(ConditionContext.EMPLOYEE_MENU);
        boolean printMenuInfo = true;
        while (true) {
            if (printMenuInfo) {
                printer.printInfo(contextManager.getCurrentContext());
            }
            printMenuInfo = true;

            Optional<String> parsed = parseInput("[012345]", "(/help|/back)");
            if (parsed.isEmpty()){
                System.out.printf("%sУказано неверное значение\n", jColorUtil.ERROR);
                continue;
            }
            String input = parsed.get();

            switch (input){
                case "1" -> selectCurrentEmployeeInteractive();
                case "2" -> {
                    Optional<Employee> employee = createNewEmployeeInteractive();
                    employee.ifPresent(sessionContext::setCurrentEmployee);
                }
                case "3" -> System.out.println("3");
                case "4" -> System.out.println("4");
                case "5" -> System.out.println("5");
                case "0", "/back" -> {
                    contextManager.exitContext();
                    return;
                }
                case "/help" -> {
                    printer.printInfo(ConditionContext.EMPLOYEE_MENU_HELP);
                    printMenuInfo = false;
                }
            }
        }
    }
    public void selectCurrentEmployeeInteractive(){
        contextManager.enterContext(ConditionContext.SELECT_CURRENT_EMPLOYEE_INTERACTIVE);
        List<Employee> employees = employeeService.findAll();

        if (employees.isEmpty()){
            System.out.printf("%sСотрудников не найдено\n", jColorUtil.INFO);

            boolean printQuestion = true;
            while (true){
                if (printQuestion){
                    System.out.printf("%sСоздать нового сотрудника? (y/n)\n", jColorUtil.INFO);
                }
                printQuestion = false;
                Optional<String> parsed = parseInput("[yYnN]", "(/help|/back)");
                if (parsed.isEmpty()){
                    System.out.printf("%sУказано неверное значение\n", jColorUtil.ERROR);
                } else {
                    if (parsed.get().matches(COMMANDS.get("help"))){
                        printer.printInfo(contextManager.getCurrentContext());
                        printQuestion = true;
                        continue;
                    }
                    if (parsed.get().equalsIgnoreCase("y")){
                        Optional<Employee> createdEmployee = createNewEmployeeInteractive();
                        createdEmployee.ifPresent(sessionContext::setCurrentEmployee);
                    }
                    contextManager.exitContext();
                    return;
                 }
            }
        } else if (employees.size() == 1){
            System.out.printf("%sНайден всего один сотрудник, он будет выбран автоматически\n", jColorUtil.INFO);

            sessionContext.setCurrentEmployee(employees.getFirst());
        } else {
            System.out.printf("%sВыберите сотрудника из списка:\n", jColorUtil.INFO);
            int index = 1;
            for (Employee employee : employees) {
                System.out.printf("%s. %s\n",
                        jColorUtil.turnTextIntoColor(Integer.toString(index++), JColorUtil.COLOR.INFO),
                        employee.getName());
            }
            while (true) {
                Optional<String> parsed = parseInput("\\d+");
                if (parsed.isEmpty()) {
                    System.out.printf("%sУказано неверное значение\n", jColorUtil.ERROR);
                } else {
                    int number = Integer.parseInt(parsed.get());
                    if(number <= 0 || number > employees.size()){
                        System.out.printf("%sДопустимые значения от %s до %s\n", jColorUtil.ERROR,
                                jColorUtil.turnTextIntoColor("1", JColorUtil.COLOR.INFO),
                                jColorUtil.turnTextIntoColor(
                                        Integer.toString(employees.size()), JColorUtil.COLOR.INFO));
                    } else {
                        sessionContext.setCurrentEmployee(employees.get(number-1));
                        return;
                    }
                }
            }
        }
        contextManager.exitContext();
    }

    public Optional<Employee> createNewEmployeeInteractive() {
        contextManager.enterContext(ConditionContext.CREATE_NEW_EMPLOYEE_INTERACTIVE);
        System.out.printf("%sСоздание нового сотрудника\n", jColorUtil.IN_PROCESS);

        Employee.EmployeeBuilder builder = Employee.builder();

        Optional<String> optionalName = setNameForEmployeeInteractive();

        if (optionalName.isPresent()){
            builder.name(optionalName.get());
        } else {
            return Optional.empty();
        }
        setGmailForEmployeeInteractive(builder);
        setPhoneNumberForEmployeeInteractive(builder);
        setPayRateForEmployeeInteractive(builder);

        return Optional.of(builder.build());
    }

    private Optional<String> setNameForEmployeeInteractive() {
        boolean firstInputAttempt = true;
        while (true) {
            if (firstInputAttempt) {
                System.out.printf("%sВведите имя:\n", jColorUtil.INFO);
            } else {
                System.out.printf("%sВведите другое имя:\n", jColorUtil.INFO);
            }

            firstInputAttempt = false;

            Optional<String> parsedName = parseInput("^[А-Яа-яЁё]{2,}([ .-]?[А-Яа-яЁё]+)*$", "(/help|/back)");
            if (parsedName.isEmpty()) {
                System.out.printf("""
                                %sНекорректное имя
                                %sВведите '%s' для получения справки
                                """, jColorUtil.ERROR, jColorUtil.INFO,
                        jColorUtil.turnTextIntoColor("/help", JColorUtil.COLOR.INFO));
            } else {
                String command = parsedName.get();
                if (command.matches(COMMANDS.get("help"))) {
                    firstInputAttempt = true;
//                    printer.print
                    continue;
                }
                if (command.matches(COMMANDS.get("back"))) {
                    return Optional.empty();
                } else {
                    String name = parsedName.get();
                    Optional<Employee> foundEmployeeByName = employeeService.findEmployeeByName(name);
                    if (foundEmployeeByName.isPresent()) {
                        System.out.printf("""
                                        %sСотрудник с таким именем уже существует
                                        %sВведите '%s' для получения справки
                                        """, jColorUtil.WARN, jColorUtil.INFO,
                                jColorUtil.turnTextIntoColor("/help", JColorUtil.COLOR.WARN));
                        continue;
                    }
                    System.out.printf("%sИмя '%s' успешно указано\n", jColorUtil.SUCCESS, jColorUtil.turnTextIntoColor(
                            name, JColorUtil.COLOR.SUCCESS));
                    return Optional.of(name);
                }
            }
        }
    }

    private void setGmailForEmployeeInteractive(Employee.EmployeeBuilder builder) {
        if(!isSkipField("gmail")){
            boolean firstInputAttempt = true;
            while (true) {
                if (firstInputAttempt) {
                    System.out.printf("%sУкажите gmail сотрудника:\n", jColorUtil.INFO);
                } else {
                    System.out.printf("%sУкажите другой gmail:\n", jColorUtil.INFO);
                }
                firstInputAttempt = false;
                Optional<String> parsedGmail = parseInput("^[a-zA-Z0-9._%+-]+@gmail\\.com$", "(/help|/back)");
                if (parsedGmail.isEmpty()) {
                    System.out.printf("""
                                    %sНекорректный gmail
                                    %sВведите '%s' для получения справки
                                    """, jColorUtil.WARN, jColorUtil.INFO,
                            jColorUtil.turnTextIntoColor("/help", JColorUtil.COLOR.WARN));
                } else {
                    String command = parsedGmail.get();
                    if (command.matches(COMMANDS.get("help"))) {
                        firstInputAttempt = true;
//                        printer.printInfo();
                    } else if (command.matches(COMMANDS.get("back"))) {
                        return;
                    } else {
                        String tempGmail = parsedGmail.get();
                        Optional<Employee> foundEmployeeByGmail = employeeService.findEmployeeByGmail(tempGmail);
                        if (foundEmployeeByGmail.isPresent()) {
                            System.out.printf("""
                                            %sСотрудник с таким gmail уже существует
                                            %sВведите '%s' для получения справки
                                            """, jColorUtil.WARN, jColorUtil.INFO,
                                    jColorUtil.turnTextIntoColor("/help", JColorUtil.COLOR.WARN));
                            continue;
                        }
                        builder.gmail(tempGmail);
                        System.out.printf("%sGmail '%s' успешно указан\n", jColorUtil.SUCCESS, jColorUtil.turnTextIntoColor(
                                tempGmail, JColorUtil.COLOR.SUCCESS));
                    }
                }
            }
        }
    }
    private void setPhoneNumberForEmployeeInteractive(Employee.EmployeeBuilder builder) {
        if (!isSkipField("номера телефона")) {
            boolean firstInputAttempt = true;
            while (true) {
                if (firstInputAttempt) {
                    System.out.printf("%sУкажите номер телефона сотрудника:\n", jColorUtil.INFO);
                } else {
                    System.out.printf("%sУкажите другой номер телефона:\n", jColorUtil.INFO);
                }
                firstInputAttempt = false;
                Optional<String> parsed = parseInput("^[87]\\d{10}$|^\\d{10}$");

                if (parsed.isEmpty()) {
                    System.out.printf("""
                                    %sНекорректный номер телефона
                                    %sВведите '%s' для получения справки
                                    """, jColorUtil.WARN, jColorUtil.INFO,
                            jColorUtil.turnTextIntoColor("/help", JColorUtil.COLOR.WARN));
                } else {
                    String command = parsed.get();
                    if (command.matches(COMMANDS.get("help"))) {
                        firstInputAttempt = true;
//                                    printer.printInfo();
                    } else if (command.matches(COMMANDS.get("back"))) {
                        return;
                    } else {
                        String tempPhoneNumber = parsed.get();
                        Optional<Employee> foundEmployee = employeeService.findEmployeeByPhoneNumber(tempPhoneNumber);
                        if (foundEmployee.isPresent()) {
                            System.out.printf("""
                                            %sСотрудник с таким номером телефона уже существует
                                            %sВведите '%s' для получения справки
                                            """, jColorUtil.WARN, jColorUtil.INFO,
                                    jColorUtil.turnTextIntoColor("/help", JColorUtil.COLOR.WARN));
                            continue;
                        }
                        builder.phoneNumber(tempPhoneNumber);
                        System.out.printf("%sНомер телефона '%s' успешно указан\n", jColorUtil.SUCCESS,
                                jColorUtil.turnTextIntoColor(tempPhoneNumber, JColorUtil.COLOR.SUCCESS));
                    }
                }
            }
        }
    }
    private void setPayRateForEmployeeInteractive(Employee.EmployeeBuilder builder) {
        if (!isSkipField("ставки")) {
            boolean firstInputAttempt = true;
            while (true) {
                if (firstInputAttempt) {
                    System.out.printf("%sУкажите ставку сотруднику:\n", jColorUtil.INFO);
                } else {
                    System.out.printf("%sУкажите другую ставку телефона:\n", jColorUtil.INFO);
                }
                firstInputAttempt = false;
                Optional<String> parsedPayRate = parseInput("\\d+", "(/help|/back)");

                if (parsedPayRate.isEmpty() || Double.parseDouble(parsedPayRate.get()) <= 0) {
                    System.out.printf("""
                                    %sНекорректная ставка
                                    %sВведите '%s' для получения справки
                                    """, jColorUtil.WARN, jColorUtil.INFO,
                            jColorUtil.turnTextIntoColor("/help", JColorUtil.COLOR.WARN));

                } else {
                    String command = parsedPayRate.get();
                    if (command.matches(COMMANDS.get("help"))) {
                        firstInputAttempt = true;
//                        printer.printInfo();
                        continue;
                    }
                    if (command.matches(COMMANDS.get("back"))) {
                        return;
                    }

                    String payRateStr = parsedPayRate.get();
                    double payRate = Double.parseDouble(payRateStr);
                    builder.payRate(payRate);
                    System.out.printf("%sСтавка '%s' успешно указана\n", jColorUtil.SUCCESS,
                            jColorUtil.turnTextIntoColor(payRateStr, JColorUtil.COLOR.SUCCESS));

                }
            }
        }
    }

//    -----------------------------------------------------------------------------
    public void monthSheetTab(){
        while(true){
            System.out.printf("""
                    =======================ЛИСТ ГРАФИКА=======================
                    Текущий лист: %s
                    1. Сменить текущий лист ->
                    2. Добавить сотрудника в лист ->
                    3. Удалить сотрудника из листа ->
                    4. Посчитать зарплату ->
                    5. Изменить ставку на листе ->
                    6. Получить информацию о листе []
                    0. Вернуться назад <-
                    """);
            Optional<String> parsed = parseInput("\\d");
        }
    }

//    -----------------------------------------------------------------------------
    public void pwzTab(){
        while(true){
            System.out.printf("""
                    ===========================ПВЗ===========================
                    Текущий ПВЗ: %s
                    1. Сменить текущий ПВЗ
                    2. Создать новый лист графика
                    3. Удалить лист графика
                    4. Посчитать зарплату
                    5. Изменить ставку на ПВЗ
                    6. Получить информацию о ПВЗ
                    0. Вернуться назад
                    """);
            Optional<String> parsed = parseInput("\\d");
        }
    }
//    -----------------------------------------------------------------------------
    public void rootFolderTab(){
        while(true){
            System.out.printf("""
                    =====================КОРНЕВАЯ ПАПКА======================
                    Текущая корневая папка: %s
                    1. Сменить текущую корневую папку
                    2. Создать новый ПВЗ
                    3. Удалить ПВЗ
                    4. Посчитать зарплату
                    5. Изменить ставку на корневой папке
                    6. Получить информацию о корневой папке
                    0. Вернуться назад
                    """);
            Optional<String> parsed = parseInput("\\d");
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
            String name = parseInput().orElseThrow();
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
            ownerDTO.setPassword(parseInput().orElseThrow());

            System.out.printf("%sПодтвердите пароль:\n", jColorUtil.INFO);
            ownerDTO.setConfirmPassword(parseInput().orElseThrow());

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
            String password = parseInput().orElseThrow();
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
            Optional<String> parsed = parseInput("\\d");
            if (parsed.isEmpty()) {
                System.out.printf("%sУказано неверное значение", jColorUtil.ERROR);
                continue;
            }
            int choiceNum = Integer.parseInt(parsed.get());

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
        String rootFolderTitle = parseInput().orElseThrow();
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

            Optional<String> parsed = parseInput("\\d");
            if (parsed.isEmpty()) {
                System.out.printf("%sУказано неверное значение", jColorUtil.ERROR);
                continue;
            }

            int choiceNum = Integer.parseInt(parsed.get());

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
            Optional<String> parsed = parseInput("^[А-ЯЁа-яё\\s]+\\s\\d+[а-яА-Я]?(([к/])?\\d+)?$", "(help|/help)");
            if (parsed.isEmpty()) {
                System.out.printf("""
                        %sУказанный адрес имеет неверный формат
                        Введите '%s' для получения справки:
                        """, jColorUtil.ERROR, jColorUtil.turnTextIntoColor("/help", JColorUtil.COLOR.INFO));
            } else {
                if(parsed.get().matches("(help|/help)")){
                    //todo help info с форматами
                } else{
                    pwzAddressIsValid = true;
                    Pwz pwz = pwzService.createPwz(parsed.get());
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

            Optional<String> parsed = parseInput("\\d");
            if (parsed.isEmpty()) {
                System.out.printf("%sУказано неверное значение", jColorUtil.ERROR);
                continue;
            }

            int choiceNum = Integer.parseInt(parsed.get());

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

            Optional<String> parsed = parseInput("\\d");
            if (parsed.isEmpty()) {
                System.out.printf("%sУказано неверное значение", jColorUtil.ERROR);
                continue;
            }

            int choiceNum = Integer.parseInt(parsed.get());

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
            Optional<String> parsed = parseInput("\\d+");
            if (parsed.isPresent()) {
                payRate = Double.parseDouble(parsed.get());
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
    public Optional<String> parseInput(@Language("RegExp") String... regexps) {
        System.out.print(jColorUtil.turnTextIntoColor(">", JColorUtil.COLOR.INFO));
        long regexpsCount = Arrays.stream(regexps).count();
        if(regexpsCount == 0)
            return Optional.of(inputOutputUtil.parseInput(scanner, regexps));
        else
            return Optional.ofNullable(inputOutputUtil.parseInput(scanner, regexps));
    }

    public boolean isSkipField(String fieldInGenitiveCase){
        boolean firstInputAttempt = true;
        while (true){
            if(firstInputAttempt) {
                System.out.printf("%sПропустить ввод поля %s? (y/n)\n", jColorUtil.INFO,
                        jColorUtil.turnTextIntoColor(fieldInGenitiveCase, JColorUtil.COLOR.INFO));
            }
            firstInputAttempt = false;

            Optional<String> parsed = parseInput("[yYnN]");
            if (parsed.isEmpty()) {
                System.out.printf("%sНедопустимое значение\n", jColorUtil.ERROR);
                continue;
            }
            String answer = parsed.get();
            return answer.equalsIgnoreCase("y");
        }
    }
}