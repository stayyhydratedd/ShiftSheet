package ru.stayyhydratedd.wbot.ShiftSheet.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.stayyhydratedd.wbot.ShiftSheet.context.SessionContext;
import ru.stayyhydratedd.wbot.ShiftSheet.enums.ConditionContext;

@Component
@RequiredArgsConstructor
public class ContextPrinterUtil {

    private final JColorUtil jColorUtil;
    private final SessionContext sessionContext;

    public void printInfo(ConditionContext context) {
        switch (context){
            case SELECT_CURRENT_EMPLOYEE_INTERACTIVE -> printHelpSelectCurrentEmployeeInteractive();
            case EMPLOYEE_MENU -> printEmployeeMenuInfo();
            case EMPLOYEE_MENU_HELP -> printEmployeeMenuHelp();
        }
    }


    private void printEmployeeMenuInfo(){
        String currentEmployeeName;
        if(sessionContext.getCurrentEmployee() == null){
            currentEmployeeName = "не выбран";
        } else {
            currentEmployeeName = sessionContext.getCurrentEmployee().getName();
        }
        System.out.printf("""
                                =======================%s========================
                                Текущий сотрудник: %s
                                %s. Сменить текущего сотрудника %s
                                %s. Зарегистрировать нового сотрудника %s
                                %s. Удалить сотрудника %s
                                %s. Изменить ставку у сотрудника %s
                                %s. Информация о сотруднике %s
                                %s. Вернуться назад %s
                                """, jColorUtil.turnTextIntoColor("СОТРУДНИКИ", JColorUtil.COLOR.SUCCESS),
                jColorUtil.turnTextIntoColor(currentEmployeeName, JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("1", JColorUtil.COLOR.IN_PROCESS),
                jColorUtil.turnTextIntoColor("<>", JColorUtil.COLOR.IN_PROCESS),
                jColorUtil.turnTextIntoColor("2", JColorUtil.COLOR.IN_PROCESS),
                jColorUtil.turnTextIntoColor("<>", JColorUtil.COLOR.IN_PROCESS),
                jColorUtil.turnTextIntoColor("3", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("<>", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("4", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("->", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("5", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("->", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("0", JColorUtil.COLOR.WARN),
                jColorUtil.turnTextIntoColor("<-", JColorUtil.COLOR.WARN)
        );
    }
    private void printEmployeeMenuHelp(){
        System.out.printf("%s1.\n2.\n3.\n4.\n5.\n0.\n", jColorUtil.INFO);
    }
//    =========================================================================================
    private void printHelpSelectCurrentEmployeeInteractive(){
        System.out.printf("%sПж выбери буквой %s или %s\n", jColorUtil.INFO,
                jColorUtil.turnTextIntoColor("y", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("n", JColorUtil.COLOR.INFO));
    }
}
