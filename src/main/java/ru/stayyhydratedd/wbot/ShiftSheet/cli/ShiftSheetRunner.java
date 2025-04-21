package ru.stayyhydratedd.wbot.ShiftSheet.cli;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.stayyhydratedd.wbot.ShiftSheet.services.StartupFlowService;
import ru.stayyhydratedd.wbot.ShiftSheet.util.JColorUtil;

@Component
@RequiredArgsConstructor
public class ShiftSheetRunner implements CommandLineRunner {

    private final StartupFlowService startupFlowService;
    private final JColorUtil jColorUtil;

    @Override
    public void run(String... args) {
        jColorUtil.printLogo();
        startupFlowService.runFlow();
    }
}
