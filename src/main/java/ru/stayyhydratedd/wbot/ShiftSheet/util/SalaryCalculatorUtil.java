package ru.stayyhydratedd.wbot.ShiftSheet.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.stayyhydratedd.wbot.ShiftSheet.context.ConditionContextManager;
import ru.stayyhydratedd.wbot.ShiftSheet.enums.ConditionContext;

@Component
@RequiredArgsConstructor
public class SalaryCalculatorUtil {

    private final ConditionContextManager contextManager;

    public void calculateSalary(ConditionContext context) {
//        if(context.equals(ConditionContext.))
    }
}
