package ru.stayyhydratedd.wbot.ShiftSheet.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.stayyhydratedd.wbot.ShiftSheet.context.SessionContext;
import ru.stayyhydratedd.wbot.ShiftSheet.enums.ConditionContext;

@Component
@RequiredArgsConstructor
public class HelperUtil {

    private final JColorUtil jColorUtil;
    private final SessionContext sessionContext;

    public void getHelp(ConditionContext context) {
        switch (context) {
            case ROOT_FOLDER_MENU -> {}
            case ASK_YES_OR_NO -> printHelpAskYesOrNo();
        }
    }
    private void printHelpAskYesOrNo(){
        System.out.printf("%sВведите %s - для подтверждения или %s - для отказа\n", jColorUtil.INFO,
                jColorUtil.turnTextIntoColor("y", JColorUtil.COLOR.INFO),
                jColorUtil.turnTextIntoColor("n", JColorUtil.COLOR.INFO));
    }
}
