package ru.stayyhydratedd.wbot.ShiftSheet.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.stayyhydratedd.wbot.ShiftSheet.context.SessionContext;

@Service
@RequiredArgsConstructor
public class StartupFlowService {

    private final UserService userService;
    private final ConsoleService consoleService;

    private final SessionContext sessionContext;

    public void runFlow(){

        //todo appFolderStage
        consoleService.userAuthenticationStage(userService.findAll());
        consoleService.initializeSettings();
        consoleService.setPayRateForAppSettingsInteractive();
        consoleService.rootIdentityStage(sessionContext.getAppSettings());
        consoleService.pwzIdentityStage(sessionContext.getAppSettings());
        consoleService.mainMenu();
//        consoleService.pwzIdentityStage(sessionContext.getAppSettings());
//        consoleService.monthSheetIdentityStage(sessionContext.getAppSettings());
    }
}
