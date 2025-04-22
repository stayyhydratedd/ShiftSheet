package ru.stayyhydratedd.wbot.ShiftSheet.context;

import org.springframework.stereotype.Component;
import ru.stayyhydratedd.wbot.ShiftSheet.enums.ConditionContext;

import java.util.ArrayDeque;
import java.util.Deque;

@Component
public class ConditionContextManager {

    private final Deque<ConditionContext> contextStack = new ArrayDeque<>();

    public ConditionContextManager() {
        contextStack.push(ConditionContext.LOGIN);
    }

    public void enterContext(ConditionContext context) {
        contextStack.push(context);
    }

    public void exitContext() {
        if (contextStack.size() > 1) {
            contextStack.pop();
        }
    }

    public ConditionContext getCurrentContext() {
        return contextStack.peek();
    }
}
