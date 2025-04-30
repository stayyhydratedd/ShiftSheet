package ru.stayyhydratedd.wbot.ShiftSheet.services;

import com.google.api.services.sheets.v4.model.Sheet;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import ru.stayyhydratedd.wbot.ShiftSheet.context.ConditionContextManager;
import ru.stayyhydratedd.wbot.ShiftSheet.context.SessionContext;
import ru.stayyhydratedd.wbot.ShiftSheet.controllers.ConsoleController;
import ru.stayyhydratedd.wbot.ShiftSheet.dtos.AuthUserDTO;
import ru.stayyhydratedd.wbot.ShiftSheet.dtos.RegisterUserDTO;
import ru.stayyhydratedd.wbot.ShiftSheet.enums.ChangeRootMethod;
import ru.stayyhydratedd.wbot.ShiftSheet.enums.ConditionContext;
import ru.stayyhydratedd.wbot.ShiftSheet.models.*;
import ru.stayyhydratedd.wbot.ShiftSheet.util.*;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ConsoleService {

    private final UserService userService;
    private final RoleService roleService;
    private final AppSettingsService appSettingsService;
    private final RootFolderService rootFolderService;
    private final PwzService pwzService;
    private final MonthSheetService monthSheetService;
    private final EmployeeService employeeService;
    private final SessionContext sessionContext;
    private final ConditionContextManager contextManager;
    private final JColorUtil jColorUtil;
    private final InputOutputUtil inputUtil;
    private final DateUtil dateUtil;
    private final PrinterUtil printer;
    private final HelperUtil helper;
    private final SalaryCalculatorUtil salaryCalculator;

    private final ConsoleController consoleController; //todo контроллер не должен использоваться в сервисе

//    =============================================================================
    public void mainMenu(){
        contextManager.enterContext(ConditionContext.MAIN_MENU);
        while(true){

            printer.printInfo(contextManager.getCurrentContext());

            Optional<String> parsed = inputUtil.parseInput("[01234]", "(/help|/back)");

            if (parsed.isEmpty()){
                System.out.printf("%sУказано неверное значение\n", jColorUtil.ERROR);
                continue;
            }

            switch (parsed.get()){
                case "1" -> rootFolderMenu();
                case "2" -> pwzMenu();
                case "3" -> monthSheetMenu();
                case "4" -> employeeMenu();
                case "0", "/back" -> {
                    //todo: logout method
                    return;
                }
                case "/help" -> {
                    helper.getHelp(contextManager.getCurrentContext());
                }
            }
        }
    }
//    -----------------------------------------------------------------------------
    public void rootFolderMenu(){
        contextManager.enterContext(ConditionContext.ROOT_FOLDER_MENU);
        boolean printMenuInfo = true;
        while(true){
            if (printMenuInfo){
                printer.printInfo(contextManager.getCurrentContext());
            }
            printMenuInfo = true;

            Optional<String> parsed = inputUtil.parseInput("[0-4]", "(/help|/back)");
            if (parsed.isEmpty()){
                printMenuInfo = false;
                System.out.printf("%sУказано неверное значение\n", jColorUtil.ERROR);
                continue;
            }
            switch (parsed.get()){
                case "1" -> changeCurrentRootFolderInteractive(contextManager.getCurrentContext());
                case "2" -> rootFolderService.deleteRootFolder();
                case "3" -> salaryCalculator.calculateSalary(contextManager.getCurrentContext());
                case "4" -> currentRootFolderDataMenu();
                case "0", "/back" -> {
                    contextManager.exitContext();
                    return;
                }
                case "/help" -> {
                    helper.getHelp(contextManager.getCurrentContext());
                }
            }
        }
    }

    public void changeCurrentRootFolderInteractive(ConditionContext callContext) {
        if (callContext.equals(ConditionContext.ROOT_FOLDER_MENU)) {
            contextManager.enterContext(ConditionContext.CHANGE_CURRENT_ROOT_FOLDER_FROM_ROOT_FOLDER_MENU);
        } else if (callContext.equals(ConditionContext.ROOT_IDENTITY)) {
            contextManager.enterContext(ConditionContext.CHANGE_CURRENT_ROOT_FOLDER_FROM_ROOT_IDENTITY);
        }
        while (true) {

            printer.printInfo(contextManager.getCurrentContext());

            Optional<String> parsed = inputUtil.parseInput("[0-3]", "(/help|/back)");
            if (parsed.isEmpty()) {
                System.out.printf("%sУказано неверное значение\n", jColorUtil.ERROR);
                continue;
            }

            try {
                if (contextManager.getCurrentContext().equals(ConditionContext.CHANGE_CURRENT_ROOT_FOLDER_FROM_ROOT_FOLDER_MENU)){
                    switch (parsed.get()) {
                        case "1" -> rootFolderService.changeCurrentRootFolderOnExist(ChangeRootMethod.GOOGLE_ID);
                        case "2" -> rootFolderService.changeCurrentRootFolderOnExist(ChangeRootMethod.TITLE);
                        case "3" -> rootFolderService.createNewRootFolderInteractive();
                        case "0", "/back" -> {
                            contextManager.exitContext();
                            return;
                        }
                        case "/help" -> {
                        }
                    }
                } else {
                    switch (parsed.get()) {
                        case "1" -> {
                            rootFolderService.changeCurrentRootFolderOnExist(ChangeRootMethod.GOOGLE_ID);
                            contextManager.exitContext();
                            return;
                        }
                        case "2" -> {
                            rootFolderService.changeCurrentRootFolderOnExist(ChangeRootMethod.TITLE);
                            contextManager.exitContext();
                            return;
                        }
                        case "3" -> {
                            rootFolderService.createNewRootFolderInteractive();
                            contextManager.exitContext();
                            return;
                        }
                        case "0", "/back" -> {
                            contextManager.exitContext();
                            return;
                        }
                        case "/help" -> {
                        }
                    }
                }
            } catch (AuthorizationDeniedException e) {
                System.out.printf("%sУ вас нет прав для выполнения этой операции\n", jColorUtil.ERROR);
            }
        }
    }

    public void currentRootFolderDataMenu(){
        if (sessionContext.getCurrentRootFolder().isEmpty()){
            System.out.printf("%sНе выбрана корневая папка, выберите ее, чтобы посмотреть данные о ней\n",
                    jColorUtil.WARN);
        } else {
            contextManager.enterContext(ConditionContext.CURRENT_ROOT_FOLDER_DATA_MENU);
            boolean printMenuInfo = true;
            while (true) {
                if (printMenuInfo){
                    printer.printInfo(contextManager.getCurrentContext());
                }
                printMenuInfo = true;
                Optional<String> parsed = inputUtil.parseInput("[0-2]", "(/help|/back)");

                if (parsed.isEmpty()) {
                    printMenuInfo = false;
                    System.out.printf("%sУказано неверное значение\n", jColorUtil.ERROR);
                    continue;
                }
                String input = parsed.get();

                switch (input) {
                    case "1" -> getDataCurrentRootFolder();
                    case "2" -> editDataCurrentRootFolder();
                    case "0", "/back" -> {
                        contextManager.exitContext();
                        return;
                    }
                    case "/help" -> {
                        helper.getHelp(contextManager.getCurrentContext());
                        printMenuInfo = false;
                    }
                }
            }
        }
    }
    public void getDataCurrentRootFolder(){
        RootFolder currentRootFolder;
        if(sessionContext.getCurrentRootFolder().isPresent()){
            currentRootFolder = rootFolderService.findById(sessionContext.getCurrentRootFolder().get().getId()).orElseThrow();
        } else {
            return;
        }
        System.out.printf("""
                Имя корневой папки: %s
                Google id: %s
                Ставка: %s
                Дата создания: %s
                """, jColorUtil.turnTextIntoColor(currentRootFolder.getTitle(), JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor(currentRootFolder.getGoogleId(), JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor(currentRootFolder.getPayRate().toString(), JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor(currentRootFolder.getCreatedTime().toString(), JColorUtil.COLOR.INFO));

        StringBuilder builder = new StringBuilder();
        List<Pwz> pwzs = currentRootFolder.getPwzs();
        if(pwzs.isEmpty()){
            builder.append(String.format("ПВЗ: %s\n",
                    jColorUtil.turnTextIntoColor("не найдено", JColorUtil.COLOR.INFO)));
        } else {
            builder.append(String.format("ПВЗ: %s\n", jColorUtil.turnTextIntoColor("[", JColorUtil.COLOR.SUCCESS)));
            int pwzNum = 1;
            for (Pwz pwz : pwzs) {
                builder.append(String.format("   %s. %s\n", pwzNum++,
                        jColorUtil.turnTextIntoColor(pwz.getAddress(), JColorUtil.COLOR.INFO)));
            }
            builder.append(String.format("%s\n", jColorUtil.turnTextIntoColor("]", JColorUtil.COLOR.SUCCESS)));
        }
        System.out.print(builder);
        builder.setLength(0);
        builder.append(String.format("Пользователи, имеющие доступ: %s\n",
                jColorUtil.turnTextIntoColor("[", JColorUtil.COLOR.SUCCESS)));

        Set<User> users = currentRootFolder.getUsers();
        int userNum = 1;
        for(User user : users){
            builder.append(String.format("   %s. %s\n", userNum++,
                    jColorUtil.turnTextIntoColor(user.getUsername(), JColorUtil.COLOR.INFO)));
        }
        builder.append(String.format("%s\n", jColorUtil.turnTextIntoColor("]", JColorUtil.COLOR.SUCCESS)));
        System.out.print(builder);
    }

    public void editDataCurrentRootFolder(){
        contextManager.enterContext(ConditionContext.EDIT_CURRENT_ROOT_FOLDER_DATA_MENU);
        while(true){
            printer.printInfo(contextManager.getCurrentContext());
            Optional<String> parsed = inputUtil.parseInput("[0-2]");
            if (parsed.isEmpty()) {
                continue;
            }
            switch (parsed.get()) {
                case "1" -> rootFolderService.renameRootFolder();
                case "2" -> rootFolderService.changePayRate();
                case "3" -> {} //todo: добавить пвз
                case "4" -> {} //todo: удалить пвз
                case "5" -> {} //todo: открыть доступ пользователю
                case "6" -> {} //todo: закрыть доступ пользователю
                case "0", "/back" -> {
                    contextManager.exitContext();
                    return;
                }
                case "/help" -> {}
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

            Optional<String> parsed = inputUtil.parseInput("[01234]", "(/help|/back)");
            if (parsed.isEmpty()){
                printMenuInfo = false;
                System.out.printf("%sУказано неверное значение\n", jColorUtil.ERROR);
                continue;
            }

            String input = parsed.get();

            switch (input){
                case "1" -> changeCurrentEmployeeInteractive();
                case "2" -> createNewEmployeeInteractive();
                case "3" -> deleteCurrentEmployeeInteractive();
                case "4" -> currentEmployeeDataMenu();
                case "0", "/back" -> {
                    contextManager.exitContext();
                    return;
                }
                case "/help" -> {
                    helper.getHelp(contextManager.getCurrentContext());
                    printMenuInfo = false;
                }
            }
        }
    }

    public void changeCurrentEmployeeInteractive(){
        contextManager.enterContext(ConditionContext.CHANGE_CURRENT_EMPLOYEE);
        List<Employee> employees = employeeService.findAll();

        if (employees.isEmpty()){
            System.out.printf("%sСотрудников не найдено\n", jColorUtil.INFO);

            String question = "Создать нового сотрудника";
            if (inputUtil.askYesOrNo(question, "", JColorUtil.COLOR.INFO)){
                Optional<Employee> createdEmployee = createNewEmployeeInteractive();
                createdEmployee.ifPresent(sessionContext::setCurrentEmployee);
            }
            contextManager.exitContext();
        } else if (employees.size() == 1){
            System.out.printf("%sНайден всего один сотрудник, он будет выбран автоматически\n", jColorUtil.INFO);
            contextManager.exitContext();
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
                Optional<String> parsed = inputUtil.parseInput("\\d+");
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
                        Employee employee = employees.get(number - 1);
                        if (sessionContext.getCurrentEmployee().isPresent()){
                            if(sessionContext.getCurrentEmployee().get().equals(employee)){
                                System.out.printf("%sСотрудник '%s' уже был выбран\n", jColorUtil.INFO,
                                        jColorUtil.turnTextIntoColor(employee.getName(), JColorUtil.COLOR.INFO));

                                contextManager.exitContext();
                                return;
                            }
                        }

                        System.out.printf("%sСотрудник '%s' успешно выбран\n", jColorUtil.SUCCESS,
                                jColorUtil.turnTextIntoColor(employee.getName(), JColorUtil.COLOR.SUCCESS));
                        sessionContext.setCurrentEmployee(employee);

                        contextManager.exitContext();
                        return;
                    }
                }
            }
        }
    }

    public Optional<Employee> createNewEmployeeInteractive() {

        System.out.printf("%sСоздание нового сотрудника\n", jColorUtil.IN_PROCESS);

        Employee.EmployeeBuilder employeeBuilder = Employee.builder();

        Optional<String> optionalName = setNameForEmployeeInteractive(false, false);

        String question = "Пропустить ввод поля";
        setGmailForEmployeeInteractive(employeeBuilder,
                inputUtil.askYesOrNo(question, "gmail", JColorUtil.COLOR.INFO), false);
        setPhoneNumberForEmployeeInteractive(employeeBuilder,
                inputUtil.askYesOrNo(question, "номера телефона", JColorUtil.COLOR.INFO), false);
        setPayRateForEmployeeInteractive(employeeBuilder,
                inputUtil.askYesOrNo(question, "ставки", JColorUtil.COLOR.INFO), false);

        if (optionalName.isPresent()){
            employeeBuilder.name(optionalName.get());
            employeeService.save(employeeBuilder.build());

            System.out.printf("%sСотрудник '%s' был успешно создан\n", jColorUtil.SUCCESS,
                    jColorUtil.turnTextIntoColor(optionalName.get(), JColorUtil.COLOR.SUCCESS));

            return Optional.of(employeeBuilder.build());
        } else {
            return Optional.empty();
        }
    }

    public void deleteCurrentEmployeeInteractive() {
        if (sessionContext.getCurrentEmployee().isEmpty()){
            System.out.printf("%sНе выбран сотрудник, выберите его, перед тем как производить удаление\n",
                    jColorUtil.WARN);
            changeCurrentEmployeeInteractive();
        } else {
            Employee currentEmployee = sessionContext.getCurrentEmployee().get();

            String question = "Вы действительно хотите удалить сотрудника";
            if(inputUtil.askYesOrNo(question, "'" + currentEmployee.getName() + "'", JColorUtil.COLOR.WARN)){
                employeeService.delete(currentEmployee);
                sessionContext.setCurrentEmployee(null);
                System.out.printf("%sСотрудник '%s' был успешно удален\n", jColorUtil.SUCCESS,
                        jColorUtil.turnTextIntoColor(currentEmployee.getName(), JColorUtil.COLOR.SUCCESS));
            }
        }
    }

    public void currentEmployeeDataMenu() {
        if (sessionContext.getCurrentEmployee().isEmpty()){
            System.out.printf("%sНе выбран сотрудник, выберите его, чтобы посмотреть данные о нем\n",
                    jColorUtil.WARN);
            changeCurrentEmployeeInteractive();
        } else {
            contextManager.enterContext(ConditionContext.CURRENT_EMPLOYEE_DATA_MENU);
            boolean printMenuInfo = true;
            while (true) {
                if (printMenuInfo){
                    printer.printInfo(contextManager.getCurrentContext());
                }
                printMenuInfo = true;
                Optional<String> parsed = inputUtil.parseInput("[012]", "(/help|/back)");

                if (parsed.isEmpty()) {
                    printMenuInfo = false;
                    System.out.printf("%sУказано неверное значение\n", jColorUtil.ERROR);
                    continue;
                }
                String input = parsed.get();

                switch (input) {
                    case "1" -> getDataCurrentEmployee();
                    case "2" -> editDataCurrentEmployee();
                    case "0", "/back" -> {
                        contextManager.exitContext();
                        return;
                    }
                    case "/help" -> {
                        helper.getHelp(contextManager.getCurrentContext());
                        printMenuInfo = false;
                    }
                }
            }
        }
    }

    private void getDataCurrentEmployee() {
        Employee currentEmployee;
        if (sessionContext.getCurrentEmployee().isPresent()){
            currentEmployee = sessionContext.getCurrentEmployee().get();
        } else {
            return;
        }
        String name = currentEmployee.getName();
        String gmailOrNotSpecified = "не указано";
        String phoneNumberOrNotSpecified = "не указано";
        String payRateOrNotSpecified = "не указано";
        if (currentEmployee.getGmail().isPresent()){
            gmailOrNotSpecified = currentEmployee.getGmail().get();
        }
        if (currentEmployee.getPhoneNumber().isPresent()){
            phoneNumberOrNotSpecified = currentEmployee.getPhoneNumber().get();
        }
        if (currentEmployee.getPayRate().isPresent()){
            payRateOrNotSpecified = currentEmployee.getPayRate().get().toString();
        }
        System.out.printf("""
                Имя сотрудника: %s
                Gmail: %s
                Номер телефона: %s
                Ставка: %s
                """, jColorUtil.turnTextIntoColor(name, JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor(gmailOrNotSpecified, JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor(phoneNumberOrNotSpecified, JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor(payRateOrNotSpecified, JColorUtil.COLOR.INFO));
    }

    private void editDataCurrentEmployee() {
        Employee currentEmployee = sessionContext.getCurrentEmployee().orElseThrow();
        Employee.EmployeeBuilder employeeBuilder = Employee.builder();
        employeeBuilder
                .id(currentEmployee.getId())
                .name(currentEmployee.getName());
        if (currentEmployee.getGmail().isPresent()){
            employeeBuilder.gmail(currentEmployee.getGmail().get());
        }
        if (currentEmployee.getPhoneNumber().isPresent()){
            employeeBuilder.phoneNumber(currentEmployee.getPhoneNumber().get());
        }
        if (currentEmployee.getPayRate().isPresent()){
            employeeBuilder.payRate(currentEmployee.getPayRate().get());
        }
        while (true){
            System.out.printf("""
                    ================%s================
                    %sВыберите поле для изменения:
                    %s. Имя сотрудника %s
                    %s. Gmail %s
                    %s. Номер телефона %s
                    %s. Ставка %s
                    %s. Вернуться назад %s
                    """, jColorUtil.turnTextIntoColor("РЕДАКТИРОВАНИЕ_СОТРУДНИКА", JColorUtil.COLOR.SUCCESS),
                    jColorUtil.INFO,
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
            Optional<String> parsed = inputUtil.parseInput("[01234]", "(/help|/back)");
            if (parsed.isEmpty()) {
                System.out.printf("%sУказано неверное значение", jColorUtil.ERROR);
                continue;
            }

            String question = "Вы уверены что хотите изменить";
            switch (parsed.get()) {
                case "1" -> {
                    Optional<String> nameOpt = setNameForEmployeeInteractive(
                            inputUtil.askYesOrNo(question, "имя сотрудника", JColorUtil.COLOR.WARN),
                            true);
                    nameOpt.ifPresent(employeeBuilder::name);
                }
                case "2" -> setGmailForEmployeeInteractive(
                        employeeBuilder, !inputUtil.askYesOrNo(question, "gmail", JColorUtil.COLOR.WARN),
                        true);
                case "3" -> setPhoneNumberForEmployeeInteractive(
                        employeeBuilder, !inputUtil.askYesOrNo(question, "номер телефона", JColorUtil.COLOR.WARN),
                        true);
                case "4" -> setPayRateForEmployeeInteractive(
                        employeeBuilder, !inputUtil.askYesOrNo(question, "ставку", JColorUtil.COLOR.WARN),
                        true);
                case "/help" -> {
                    System.out.println("help information");
                    continue;
                }
                case "0", "/back" -> {
                    return;
                }
            }
            Employee editedEmployee = employeeBuilder.build();
            employeeService.save(editedEmployee);
            sessionContext.setCurrentEmployee(editedEmployee);
        }
    }

    private Optional<String> setNameForEmployeeInteractive(boolean isSkipField, boolean isEditField) {
        if (!isSkipField) {
            boolean firstInputAttempt = true;
            String currentName = null;
            while (true) {
                if (firstInputAttempt) {
                    if (isEditField) {
                        currentName = sessionContext.getCurrentEmployee().orElseThrow().getName();
                        System.out.printf("""
                                        %sТекущее имя: %s
                                        %sУкажите новое имя:
                                        """, jColorUtil.INFO,
                                jColorUtil.turnTextIntoColor(currentName, JColorUtil.COLOR.INFO),
                                jColorUtil.INFO);
                    } else {
                        System.out.printf("%sВведите имя:\n", jColorUtil.INFO);
                    }
                } else {
                    System.out.printf("%sВведите другое имя:\n", jColorUtil.INFO);
                }
                firstInputAttempt = false;

                Optional<String> parsedName =
                        inputUtil.parseInput("^[А-Яа-яЁё]{2,}([ .-]?[А-Яа-яЁё]+)*$", "(/help|/back)");
                if (parsedName.isEmpty()) {
                    System.out.printf("""
                                    %sНекорректное имя
                                    %sВведите '%s' для получения справки
                                    """, jColorUtil.ERROR, jColorUtil.INFO,
                            jColorUtil.turnTextIntoColor("/help", JColorUtil.COLOR.INFO));
                } else {
                    String command = parsedName.get();
                    if (command.matches("/help")) {
                        firstInputAttempt = true;
//                    printer.print
                        continue;
                    }
                    if (command.matches("/back")) {
                        return Optional.empty();
                    } else {
                        String newName = parsedName.get();

                        if (isEditField && currentName.equals(newName)) {
                            System.out.printf("%sУказано то же самое имя\n", jColorUtil.ERROR);
                            continue;
                        }

                        Optional<Employee> foundEmployeeByName = employeeService.findEmployeeByName(newName);

                        if (foundEmployeeByName.isPresent()) {
                            System.out.printf("""
                                            %sСотрудник с таким именем уже существует
                                            %sВведите '%s' для получения справки
                                            """, jColorUtil.WARN, jColorUtil.INFO,
                                    jColorUtil.turnTextIntoColor("/help", JColorUtil.COLOR.WARN));
                            continue;
                        }
                        if (isEditField)
                            System.out.printf("%sИмя сотрудника '%s' успешно изменено на '%s'\n", jColorUtil.SUCCESS,
                                    jColorUtil.turnTextIntoColor(currentName, JColorUtil.COLOR.INFO),
                                    jColorUtil.turnTextIntoColor(newName, JColorUtil.COLOR.INFO));
                        else
                            System.out.printf("%sИмя '%s' успешно указано\n", jColorUtil.SUCCESS,
                                    jColorUtil.turnTextIntoColor(newName, JColorUtil.COLOR.SUCCESS));
                        return Optional.of(newName);
                    }
                }
            }
        }
        return Optional.empty();
    }

    private void setGmailForEmployeeInteractive(Employee.EmployeeBuilder builder,
                                                boolean isSkipField, boolean isEditField) {
        if(!isSkipField){
            boolean firstInputAttempt = true;
            String currentGmail = "не указано";
            while (true) {
                if (firstInputAttempt) {
                    if(isEditField) {
                        if (sessionContext.getCurrentEmployee().orElseThrow().getGmail().isPresent()) {
                            currentGmail = sessionContext.getCurrentEmployee().orElseThrow().getGmail().get();
                        }
                        System.out.printf("""
                                %sТекущий gmail: %s
                                %sУкажите новый gmail:
                                """, jColorUtil.INFO,
                                jColorUtil.turnTextIntoColor(currentGmail, JColorUtil.COLOR.INFO),
                                jColorUtil.INFO);
                    } else {
                        System.out.printf("%sУкажите gmail сотрудника:\n", jColorUtil.INFO);
                    }
                } else {
                    System.out.printf("%sУкажите другой gmail:\n", jColorUtil.INFO);
                }
                firstInputAttempt = false;
                Optional<String> parsedGmail = inputUtil.parseInput("^[a-zA-Z0-9._%+-]+@gmail\\.com$", "/help");
                if (parsedGmail.isEmpty()) {
                    System.out.printf("""
                                    %sНекорректный gmail
                                    %sВведите '%s' для получения справки
                                    """, jColorUtil.WARN, jColorUtil.INFO,
                            jColorUtil.turnTextIntoColor("/help", JColorUtil.COLOR.WARN));
                } else {
                    String command = parsedGmail.get();
                    if (command.matches("/help")) {
                        firstInputAttempt = true;
//                        printer.printInfo();
                    } else {
                        String newGmail = parsedGmail.get();

                        if (isEditField && currentGmail.equals(newGmail)) {
                            System.out.printf("%sУказан тот же самый gmail\n", jColorUtil.ERROR);
                            continue;
                        }

                        Optional<Employee> foundEmployeeByGmail = employeeService.findEmployeeByGmail(newGmail);
                        if (foundEmployeeByGmail.isPresent()) {
                            System.out.printf("""
                                            %sСотрудник с таким gmail уже существует
                                            %sВведите '%s' для получения справки
                                            """, jColorUtil.WARN, jColorUtil.INFO,
                                    jColorUtil.turnTextIntoColor("/help", JColorUtil.COLOR.WARN));
                        }
                        builder.gmail(newGmail);
                        if (isEditField) {
                            if (currentGmail.equals("не указано"))
                                System.out.printf("%sGmail успешно изменен на '%s'\n", jColorUtil.SUCCESS,
                                        jColorUtil.turnTextIntoColor(newGmail, JColorUtil.COLOR.INFO));
                            else
                                System.out.printf("%sGmail '%s' успешно изменен на '%s'\n", jColorUtil.SUCCESS,
                                        jColorUtil.turnTextIntoColor(currentGmail, JColorUtil.COLOR.INFO),
                                        jColorUtil.turnTextIntoColor(newGmail, JColorUtil.COLOR.INFO));
                        } else {
                            System.out.printf("%sGmail '%s' успешно указан\n", jColorUtil.SUCCESS,
                                    jColorUtil.turnTextIntoColor(newGmail, JColorUtil.COLOR.SUCCESS));
                        }
                        return;
                    }
                }
            }
        }
    }

    private void setPhoneNumberForEmployeeInteractive(Employee.EmployeeBuilder builder,
                                                      boolean isSkipField, boolean isEditField) {
        if (!isSkipField) {
            boolean firstInputAttempt = true;
            String currentPhoneNumber = "не указано";
            while (true) {
                if (firstInputAttempt) {
                    if(isEditField) {
                        if (sessionContext.getCurrentEmployee().orElseThrow().getPhoneNumber().isPresent()) {
                            currentPhoneNumber = sessionContext.getCurrentEmployee().orElseThrow().getPhoneNumber().get();
                        }
                        System.out.printf("""
                                %sТекущий номер телефона: %s
                                %sУкажите новый номер телефона:
                                """, jColorUtil.INFO,
                                jColorUtil.turnTextIntoColor(currentPhoneNumber, JColorUtil.COLOR.INFO),
                                jColorUtil.INFO);
                    } else {
                        System.out.printf("%sУкажите номер телефона сотрудника:\n", jColorUtil.INFO);
                    }
                } else {
                    System.out.printf("%sУкажите другой номер телефона:\n", jColorUtil.INFO);
                }
                firstInputAttempt = false;
                Optional<String> parsed = inputUtil.parseInput("^[87]\\d{10}$|^\\d{10}$");

                if (parsed.isEmpty()) {
                    System.out.printf("""
                                    %sНекорректный номер телефона
                                    %sВведите '%s' для получения справки
                                    """, jColorUtil.WARN, jColorUtil.INFO,
                            jColorUtil.turnTextIntoColor("/help", JColorUtil.COLOR.WARN));
                } else {
                    String command = parsed.get();
                    if (command.matches("/help")) {
                        firstInputAttempt = true;
//                                    printer.printInfo();
                    } else {
                        String newPhoneNumber = parsed.get();

                        if (isEditField && currentPhoneNumber.equals(newPhoneNumber)) {
                            System.out.printf("%sУказан тот же самый номер телефона\n", jColorUtil.ERROR);
                            continue;
                        }
                        Optional<Employee> foundEmployee = employeeService.findEmployeeByPhoneNumber(newPhoneNumber);

                        if (foundEmployee.isPresent()) {
                            System.out.printf("""
                                            %sСотрудник с таким номером телефона уже существует
                                            %sВведите '%s' для получения справки
                                            """, jColorUtil.WARN, jColorUtil.INFO,
                                    jColorUtil.turnTextIntoColor("/help", JColorUtil.COLOR.WARN));
                            continue;
                        }
                        builder.phoneNumber(newPhoneNumber);
                        if (isEditField) {
                            if (currentPhoneNumber.equals("не указано"))
                                System.out.printf("%sНомер телефона успешно изменен на '%s'\n", jColorUtil.SUCCESS,
                                        jColorUtil.turnTextIntoColor(newPhoneNumber, JColorUtil.COLOR.INFO));
                            else
                                System.out.printf("%sНомер телефона '%s' успешно изменен на '%s'\n", jColorUtil.SUCCESS,
                                        jColorUtil.turnTextIntoColor(currentPhoneNumber, JColorUtil.COLOR.INFO),
                                        jColorUtil.turnTextIntoColor(newPhoneNumber, JColorUtil.COLOR.INFO));
                        } else {
                            System.out.printf("%sНомер телефона '%s' успешно указан\n", jColorUtil.SUCCESS,
                                    jColorUtil.turnTextIntoColor(newPhoneNumber, JColorUtil.COLOR.SUCCESS));
                        }
                        return;
                    }
                }
            }
        }
    }

    private void setPayRateForEmployeeInteractive(Employee.EmployeeBuilder builder,
                                                  boolean isSkipField, boolean isEditField) {
        if (!isSkipField) {
            boolean firstInputAttempt = true;
            String currentPayRate = "не указано";
            while (true) {
                if (firstInputAttempt) {
                    if(isEditField) {
                        if (sessionContext.getCurrentEmployee().orElseThrow().getPayRate().isPresent()) {
                            currentPayRate = sessionContext.getCurrentEmployee().orElseThrow().getPayRate().get().toString();
                        }
                        System.out.printf("""
                                %sТекущий ставка: %s
                                %sУкажите новую ставку:
                                """, jColorUtil.INFO,
                                jColorUtil.turnTextIntoColor(currentPayRate, JColorUtil.COLOR.INFO),
                                jColorUtil.INFO);
                    } else {
                        System.out.printf("%sУкажите ставку сотруднику:\n", jColorUtil.INFO);
                    }
                } else {
                    System.out.printf("%sУкажите другую ставку:\n", jColorUtil.INFO);
                }
                firstInputAttempt = false;
                Optional<String> parsed = inputUtil.parseInput("\\d+", "/help");

                if (parsed.isEmpty() || Double.parseDouble(parsed.get()) <= 0) {
                    System.out.printf("""
                                    %sНекорректная ставка
                                    %sВведите '%s' для получения справки
                                    """, jColorUtil.WARN, jColorUtil.INFO,
                            jColorUtil.turnTextIntoColor("/help", JColorUtil.COLOR.WARN));

                } else {
                    String command = parsed.get();

                    if (command.matches("/help")) {
                        firstInputAttempt = true;
//                        printer.printInfo();
                    } else{
                        String newPayRateStr = parsed.get();

                        if (isEditField && currentPayRate.equals(newPayRateStr)) {
                            System.out.printf("%sУказана та же самая ставка\n", jColorUtil.ERROR);
                            continue;
                        }
                        double newPayRate = Double.parseDouble(newPayRateStr);
                        builder.payRate(newPayRate);

                        if (isEditField) {
                            if (currentPayRate.equals("не указано"))
                                System.out.printf("%sСтавка успешно изменена на '%s'\n", jColorUtil.SUCCESS,
                                        jColorUtil.turnTextIntoColor(newPayRateStr, JColorUtil.COLOR.INFO));
                            else
                                System.out.printf("%sСтавка '%s' успешно изменена на '%s'\n", jColorUtil.SUCCESS,
                                        jColorUtil.turnTextIntoColor(currentPayRate, JColorUtil.COLOR.INFO),
                                        jColorUtil.turnTextIntoColor(newPayRateStr, JColorUtil.COLOR.INFO));
                        } else {
                            System.out.printf("%sСтавка '%s' успешно указана\n", jColorUtil.SUCCESS,
                                    jColorUtil.turnTextIntoColor(newPayRateStr, JColorUtil.COLOR.SUCCESS));
                        }
                        return;
                    }
                }
            }
        }
    }
//    -----------------------------------------------------------------------------
    public void pwzMenu(){
        contextManager.enterContext(ConditionContext.PWZ_MENU);
        while(true){
            printer.printInfo(contextManager.getCurrentContext());
            Optional<String> parsed = inputUtil.parseInput("[0-4]", "(/help|/back)");
            if (parsed.isEmpty()) {
                System.out.printf("%sУказано неверное значение\n", jColorUtil.ERROR);
                continue;
            }
            switch (parsed.get()){
                case "1" -> changeCurrentPwzInteractiveMenu();
                case "2" -> pwzService.createNewPwz();
                case "3" -> {}
                case "4" -> {}
                case "0", "/back" -> {
                    contextManager.exitContext();
                    return;
                }
                case "/help" -> {}
            }
        }
    }

    public void changeCurrentPwzInteractiveMenu(){
        contextManager.enterContext(ConditionContext.CHANGE_CURRENT_PWZ_INTERACTIVE_MENU);
        while(true){
            printer.printInfo(contextManager.getCurrentContext());
            Optional<String> parsed = inputUtil.parseInput("[0-4]", "(/help|/back)");
            if (parsed.isEmpty()) {
                System.out.printf("%sУказано неверное значение\n", jColorUtil.ERROR);
                continue;
            }
            switch (parsed.get()){
                case "1" -> {}
                case "2" -> {}
                case "3" -> {}
                case "4" -> {}
                case "0", "/back" -> {
                    contextManager.exitContext();
                    return;
                }
                case "/help" -> {}
            }
        }
    }
//    -----------------------------------------------------------------------------
    public void monthSheetMenu(){
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
            Optional<String> parsed = inputUtil.parseInput("\\d");
        }
    }
//    -----------------------------------------------------------------------------

//    =============================================================================
    public void userAuthenticationStage(List<User> users){
        if(users.isEmpty()) {
            System.out.printf("%sНе удалось обнаружить пользователей, создайте нового, чтобы продолжить работу\n",
                    jColorUtil.INFO);
            createUserInteractive(new RegisterUserDTO(), true);
        } else if(users.size() == 1) {
            AuthUserDTO authUserDTO = AuthUserDTO.builder().username(users.getFirst().getUsername()).build();
            consoleController.authenticate(authUserDTO);
        } else {
            System.out.printf("%sВыберите пользователя для входа:\n", jColorUtil.INFO);
            AuthUserDTO authUserDTO = selectUserInteractive(users);
            consoleController.authenticate(authUserDTO);
        }
    }

    public void createUserInteractive(RegisterUserDTO registerUserDTO, boolean firstUser) {
        boolean usernameAlreadyExists = true;
        boolean firstUsernameAttempt = true;

        do {
            if (firstUsernameAttempt)
                System.out.printf("%sВведите имя нового пользователя:\n", jColorUtil.INFO);
            else
                System.out.printf("%sВведите другое имя:\n", jColorUtil.INFO);
            Optional<String> parsed = inputUtil.parseInput();
            if (parsed.isPresent()) {
                String username = parsed.get();
                Optional<User> foundUser = userService.findByUsername(username);
                if (foundUser.isPresent()) {
                    System.out.printf("%sПользователь с таким именем уже существует\n", jColorUtil.WARN);
                    firstUsernameAttempt = false;
                } else {
                    usernameAlreadyExists = false;
                    registerUserDTO.setUsername(username);
                }
            } else{
                return;
            }
        } while (usernameAlreadyExists);

        boolean passwordsDoNotMatch = true;

        do{
            System.out.printf("%sВведите новый пароль для входа:\n", jColorUtil.INFO);
            registerUserDTO.setPassword(inputUtil.parseInput().orElseThrow());

            System.out.printf("%sПодтвердите пароль:\n", jColorUtil.INFO);
            registerUserDTO.setConfirmPassword(inputUtil.parseInput().orElseThrow());

            if (!registerUserDTO.getPassword().equals(registerUserDTO.getConfirmPassword())) {
                System.out.printf("%sВведенные пароли не совпадают\n", jColorUtil.ERROR);
            } else {
                passwordsDoNotMatch = false;
                User user = userService.registerUserDtoToUser(registerUserDTO);
                if(firstUser){
                    user.setRoles(Set.of(
                            roleService.findByName("ROLE_USER").orElseThrow(),
                            roleService.findByName("ROLE_ADMIN").orElseThrow()));
                } else {
                    user.setRoles(Set.of(roleService.findByName("ROLE_USER").orElseThrow()));
                }
                userService.saveWithPasswordEncoding(user);

                if (firstUser){
                    consoleController.setAuthentication(userService.registerUserDtoToAuthUserDto(registerUserDTO));
                    System.out.printf("%sПользователь '%s' успешно создан и аутентифицирован\n", jColorUtil.SUCCESS,
                            jColorUtil.turnTextIntoColor(registerUserDTO.getUsername(), JColorUtil.COLOR.SUCCESS));
                } else {
                    System.out.printf("%sПользователь '%s' успешно создан\n", jColorUtil.SUCCESS,
                            jColorUtil.turnTextIntoColor(registerUserDTO.getUsername(), JColorUtil.COLOR.SUCCESS));
                }

            }
        } while (passwordsDoNotMatch);
    }

    public AuthUserDTO selectUserInteractive(List<User> users){
        int userNumber = 1;
        for(User user : users) {
            System.out.printf("%d. %s\n", userNumber++, user.getUsername());
        }
        while (true) {
            Optional<String> parsed = inputUtil.parseInput("\\d");
            if (parsed.isEmpty()) {
                System.out.printf("%sУказано неверное значение", jColorUtil.ERROR);
                continue;
            }
            int choiceNum = Integer.parseInt(parsed.get());

            if (choiceNum <= 0 || choiceNum > users.size()){
                System.out.println("Пользователя под таким номером нет");
            } else {
                System.out.printf("Пользователь '%s' успешно выбран.\n", users.get(choiceNum - 1).getUsername());
                return AuthUserDTO.builder().username(users.get(choiceNum - 1).getUsername()).build();
            }
        }
    }
//    =============================================================================
    public void rootIdentityStage(AppSettings appSettings) {
        contextManager.enterContext(ConditionContext.ROOT_IDENTITY);
        if (appSettings.getLastRootFolder().isPresent()) {
            System.out.printf("%sРабочая папка '%s' была автоматически выбрана с прошлой сессии\n",
                    jColorUtil.INFO,
                    jColorUtil.turnTextIntoColor(appSettings.getLastRootFolder().get().getTitle(), JColorUtil.COLOR.INFO));
            sessionContext.setCurrentRootFolder(appSettings.getLastRootFolder().get());
        } else {
            if(sessionContext.getAppSettings().getRootFolders().isEmpty()){
                String question = "Не удалось найти рабочих папок, хотите указать рабочую папку";
                if (inputUtil.askYesOrNo(question, "", JColorUtil.COLOR.INFO)){
                    changeCurrentRootFolderInteractive(contextManager.getCurrentContext());
                }
            }
        }
        contextManager.exitContext();
    }
//    =============================================================================
    public void pwzIdentityStage(AppSettings appSettings){
        if(appSettings.getLastPwz().isPresent()) {
            Pwz lastPwz = appSettings.getLastPwz().get();
            sessionContext.setCurrentPwz(lastPwz);
            System.out.printf("%sПвз '%s' был автоматически выбран с прошлой сессии\n", jColorUtil.INFO,
                    jColorUtil.turnTextIntoColor(lastPwz.getAddress(), JColorUtil.COLOR.INFO));
        }
    }

    public void createNewPwzInteractive() {
        boolean pwzAddressIsValid = false;
        System.out.printf("%sУкажите адрес вашего пвз:\n", jColorUtil.INFO);
        do{
            Optional<String> parsed = inputUtil.parseInput(
                    "^[А-ЯЁа-яё\\s]+\\s\\d+[а-яА-Я]?(([к/])?\\d+)?$", "(help|/help)");
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

            Optional<String> parsed = inputUtil.parseInput("\\d");
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
        if(appSettings.getLastMonthSheet().isPresent()){
            MonthSheet lastMonthSheet = appSettings.getLastMonthSheet().get();
            sessionContext.setCurrentMonthSheet(lastMonthSheet);

            System.out.printf("%sЛист '%s' был автоматически выбран с прошлой сессии", jColorUtil.INFO,
                    jColorUtil.turnTextIntoColor(
                            dateUtil.getFullMonthSheetDate(lastMonthSheet), JColorUtil.COLOR.INFO));
        } else{
            List<MonthSheet> monthSheets = monthSheetService.findAll();
            if(monthSheets.isEmpty()){
                createNewMonthSheetInteractive(sessionContext.getCurrentPwz().get().getGoogleId());
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

            Optional<String> parsed = inputUtil.parseInput("\\d");
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
        if (sessionContext.getAppSettings().getPayRate() == null) {
            System.out.printf("""
                %sУкажите ставку по которой будет производиться подсчет зарплаты
                %sЭто значение можно будет изменить позже
                """, jColorUtil.INFO, jColorUtil.INFO);
            while (true) {
                Optional<String> parsed = inputUtil.parseInput("\\d+");
                if (parsed.isPresent()) {
                    double payRate = Double.parseDouble(parsed.get());
                    if (payRate <= 0) {
                        System.out.printf("%sСтавка не может быть нулевой или отрицательной\n", jColorUtil.ERROR);
                        continue;
                    }
                    final double payRateConst = payRate;
                    AppSettings appSettings = appSettingsService.updateSettings(settings ->
                            settings.setPayRate(payRateConst));
                    sessionContext.setAppSettings(appSettings);
                    System.out.printf("%sСтавка успешно указана\n", jColorUtil.SUCCESS);
                    return;
                } else {
                    System.out.printf("%sУказано неверное значение для ставки\n", jColorUtil.ERROR);
                }
            }
        }
    }
//    =============================================================================

    public void initializeSettings(){
        System.out.printf("%sНастройки инициализированы\n", jColorUtil.INFO);
        sessionContext.setCurrentPwzsFolderId(appSettingsService.getSettings().getPwzsFolderId());
        sessionContext.setAppSettings(appSettingsService.getSettings());
    }

}