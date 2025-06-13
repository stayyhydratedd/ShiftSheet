package ru.stayyhydratedd.wbot.ShiftSheet.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.stayyhydratedd.wbot.ShiftSheet.context.SessionContext;
import ru.stayyhydratedd.wbot.ShiftSheet.enums.ConditionContext;

@Component
@RequiredArgsConstructor
public class PrinterUtil {

    private final JColorUtil jColorUtil;
    private final SessionContext sessionContext;

    public void printInfo(ConditionContext context) {
        switch (context){
            case MAIN_MENU -> printMainMenuInfo();
            case ROOT_FOLDER_MENU -> printRootFolderMenuInfo();
            case CURRENT_ROOT_FOLDER_DATA_MENU -> printCurrentRootFolderDataMenuInfo();
            case EDIT_CURRENT_ROOT_FOLDER_DATA_MENU -> printEditCurrentRootFolderDataMenuInfo();
            case CHANGE_CURRENT_ROOT_FOLDER_FROM_ROOT_FOLDER_MENU -> printCurrentRootFolderFromRootFolderMenuInfo();
            case CHANGE_CURRENT_ROOT_FOLDER_FROM_ROOT_IDENTITY -> printCurrentRootFolderFromRootIdentityInfo();
            case PWZ_MENU -> printPwzMenuInfo();
            case CHANGE_CURRENT_PWZ_INTERACTIVE_MENU -> printChangeCurrentPwzInteractiveMenuInfo();
            case EMPLOYEE_MENU -> printEmployeeMenuInfo();
            case CURRENT_EMPLOYEE_DATA_MENU -> printCurrentEmployeeDataMenuInfo();
        }
    }

    private void printMainMenuInfo() {
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
    }

    private void printRootFolderMenuInfo() {
        String currentRootFolderTitle = "не выбрана";
        if (sessionContext.getCurrentRootFolder().isPresent()){
            currentRootFolderTitle = sessionContext.getCurrentRootFolder().get().getTitle();
        }
        System.out.printf("""
                =====================%s======================
                Текущая корневая папка: %s
                %s. Сменить текущую корневую папку %s
                %s. Удалить корневую папку %s
                %s. Посчитать зарплату %s
                %s. Данные о корневой папке %s
                %s. Вернуться назад %s
                """, jColorUtil.turnTextIntoColor("КОРНЕВАЯ ПАПКА", JColorUtil.COLOR.SUCCESS),
                jColorUtil.turnTextIntoColor(currentRootFolderTitle, JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("1", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("->", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("2", JColorUtil.COLOR.ERROR),
                jColorUtil.turnTextIntoColor("{}", JColorUtil.COLOR.ERROR),
                jColorUtil.turnTextIntoColor("3", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("->", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("4", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("->", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("0", JColorUtil.COLOR.WARN),
                jColorUtil.turnTextIntoColor("<-", JColorUtil.COLOR.WARN)
                );

    }

    private void printEmployeeMenuInfo(){
        String currentEmployeeName = "не выбран";
        if(sessionContext.getCurrentEmployee().isPresent()){
            currentEmployeeName = sessionContext.getCurrentEmployee().get().getName();
        }
        System.out.printf("""
                 =======================%s========================
                 Текущий сотрудник: %s
                 %s. Сменить текущего сотрудника %s
                 %s. Создать нового сотрудника %s
                 %s. Удалить сотрудника %s
                 %s. Данные о сотруднике %s
                 %s. Вернуться назад %s
                 """, jColorUtil.turnTextIntoColor("СОТРУДНИКИ", JColorUtil.COLOR.SUCCESS),
                jColorUtil.turnTextIntoColor(currentEmployeeName, JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("1", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("->", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("2", JColorUtil.COLOR.IN_PROCESS),
                jColorUtil.turnTextIntoColor("<>", JColorUtil.COLOR.IN_PROCESS),
                jColorUtil.turnTextIntoColor("3", JColorUtil.COLOR.ERROR),
                jColorUtil.turnTextIntoColor("{}", JColorUtil.COLOR.ERROR),
                jColorUtil.turnTextIntoColor("4", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("->", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("0", JColorUtil.COLOR.WARN),
                jColorUtil.turnTextIntoColor("<-", JColorUtil.COLOR.WARN)
        );
    }
    //    =========================================================================================

    private void printCurrentEmployeeDataMenuInfo() {
        System.out.printf("""
                ===================%s=====================
                %s. Посмотреть данные %s
                %s. Изменить данные %s
                %s. Вернуться назад %s
                """, jColorUtil.turnTextIntoColor("ДАННЫЕ СОТРУДНИКА", JColorUtil.COLOR.SUCCESS),
                jColorUtil.turnTextIntoColor("1", JColorUtil.COLOR.SUCCESS),
                jColorUtil.turnTextIntoColor("[]", JColorUtil.COLOR.SUCCESS),
                jColorUtil.turnTextIntoColor("2", JColorUtil.COLOR.IN_PROCESS),
                jColorUtil.turnTextIntoColor("<>", JColorUtil.COLOR.IN_PROCESS),
                jColorUtil.turnTextIntoColor("0", JColorUtil.COLOR.WARN),
                jColorUtil.turnTextIntoColor("<-", JColorUtil.COLOR.WARN)
                );
    }

    private void printCurrentRootFolderFromRootFolderMenuInfo() {
        System.out.printf("""
                ==================%s===================
                %s. На существующую по google id %s
                %s. На существующую по названию %s
                %s. Создать новую %s
                %s. Вернуться назад %s
                """, jColorUtil.turnTextIntoColor("СМЕНА КОРНЕВОЙ ПАПКИ", JColorUtil.COLOR.SUCCESS),
                jColorUtil.turnTextIntoColor("1", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("->", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("2", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("->", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("3", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("->", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("0", JColorUtil.COLOR.WARN),
                jColorUtil.turnTextIntoColor("<-", JColorUtil.COLOR.WARN)
        );
    }

    private void printCurrentRootFolderFromRootIdentityInfo() {
        System.out.printf("""
                ================%s=================
                %s. Существующую по google id %s
                %s. Существующую по названию %s
                %s. Создать новую %s
                %s. На главную %s
                """, jColorUtil.turnTextIntoColor("УСТАНОВКА КОРНЕВОЙ ПАПКИ", JColorUtil.COLOR.SUCCESS),
                jColorUtil.turnTextIntoColor("1", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("->", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("2", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("->", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("3", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("->", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("0", JColorUtil.COLOR.WARN),
                jColorUtil.turnTextIntoColor("<-", JColorUtil.COLOR.WARN)
        );
    }

    private void printCurrentRootFolderDataMenuInfo() {
        System.out.printf("""
                ==================%s==================
                %s. Посмотреть данные %s
                %s. Изменить данные %s
                %s. Вернуться назад %s
                """, jColorUtil.turnTextIntoColor("ДАННЫЕ КОРНЕВОЙ ПАПКИ", JColorUtil.COLOR.SUCCESS),
                jColorUtil.turnTextIntoColor("1", JColorUtil.COLOR.SUCCESS),
                jColorUtil.turnTextIntoColor("[]", JColorUtil.COLOR.SUCCESS),
                jColorUtil.turnTextIntoColor("2", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("->", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("0", JColorUtil.COLOR.WARN),
                jColorUtil.turnTextIntoColor("<-", JColorUtil.COLOR.WARN)
        );
    }

    private void printEditCurrentRootFolderDataMenuInfo() {
        System.out.printf("""
                =============%s=============
                %s. Изменить имя корневой папки %s
                %s. Изменить ставку %s
                %s. Вернуться назад %s
                """, jColorUtil.turnTextIntoColor("ИЗМЕНЕНИЕ ДАННЫХ КОРНЕВОЙ ПАПКИ", JColorUtil.COLOR.SUCCESS),
                jColorUtil.turnTextIntoColor("1", JColorUtil.COLOR.IN_PROCESS),
                jColorUtil.turnTextIntoColor("<>", JColorUtil.COLOR.IN_PROCESS),
                jColorUtil.turnTextIntoColor("2", JColorUtil.COLOR.IN_PROCESS),
                jColorUtil.turnTextIntoColor("<>", JColorUtil.COLOR.IN_PROCESS),
                jColorUtil.turnTextIntoColor("0", JColorUtil.COLOR.WARN),
                jColorUtil.turnTextIntoColor("<-", JColorUtil.COLOR.WARN)
        );
    }
    private void printPwzMenuInfo(){
        String currentPwzAddress = "не выбран";
        if(sessionContext.getCurrentPwz().isPresent()){
            currentPwzAddress = sessionContext.getCurrentPwz().get().getAddress();
        }
        System.out.printf("""
                ===========================%s===========================
                Текущий ПВЗ: %s
                %s. Сменить текущий ПВЗ %s
                %s. Создать новый ПВЗ %s
                %s. Удалить ПВЗ %s
                %s. Данные о ПВЗ %s
                %s. Вернуться назад %s
                """, jColorUtil.turnTextIntoColor("ПВЗ", JColorUtil.COLOR.SUCCESS),
                jColorUtil.turnTextIntoColor(currentPwzAddress, JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("1", JColorUtil.COLOR.IN_PROCESS),
                jColorUtil.turnTextIntoColor("<>", JColorUtil.COLOR.IN_PROCESS),
                jColorUtil.turnTextIntoColor("2", JColorUtil.COLOR.IN_PROCESS),
                jColorUtil.turnTextIntoColor("<>", JColorUtil.COLOR.IN_PROCESS),
                jColorUtil.turnTextIntoColor("3", JColorUtil.COLOR.ERROR),
                jColorUtil.turnTextIntoColor("{}", JColorUtil.COLOR.ERROR),
                jColorUtil.turnTextIntoColor("4", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("->", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("0", JColorUtil.COLOR.WARN),
                jColorUtil.turnTextIntoColor("<-", JColorUtil.COLOR.WARN)
        );
    }

    private void printChangeCurrentPwzInteractiveMenuInfo(){
        System.out.printf("""
                ========================%s========================
                %s. На существующий из корневой папки %s
                %s. На доступный мне %s
                %s. Вернуться назад %s
                """, jColorUtil.turnTextIntoColor("СМЕНА ПВЗ", JColorUtil.COLOR.SUCCESS),
                jColorUtil.turnTextIntoColor("1", JColorUtil.COLOR.IN_PROCESS),
                jColorUtil.turnTextIntoColor("<>", JColorUtil.COLOR.IN_PROCESS),
                jColorUtil.turnTextIntoColor("2", JColorUtil.COLOR.IN_PROCESS),
                jColorUtil.turnTextIntoColor("<>", JColorUtil.COLOR.IN_PROCESS),
                jColorUtil.turnTextIntoColor("0", JColorUtil.COLOR.WARN),
                jColorUtil.turnTextIntoColor("<-", JColorUtil.COLOR.WARN)
        );
    }
}
