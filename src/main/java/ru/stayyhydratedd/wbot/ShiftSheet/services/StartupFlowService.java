package ru.stayyhydratedd.wbot.ShiftSheet.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.stayyhydratedd.wbot.ShiftSheet.context.SessionContext;

@Service
@RequiredArgsConstructor
public class StartupFlowService {

    private final OwnerService ownerService;
    private final RootFolderService rootFolderService;
    private final EmployeeService employeeService;
    private final PwzService pwzService;
    private final MonthSheetService monthSheetService;
    private final SheetInfoService sheetInfoService;
    private final ConsoleService consoleService;

    private final SessionContext sessionContext;

    public void runFlow(){
        consoleService.ownerAuthenticationStage(ownerService.findAll());
        consoleService.rootIdentityStage(sessionContext.getAppSettings());
        rootFolderService.checkRootFolderForInternalFolders(sessionContext.getCurrentRootFolder());
        consoleService.pwzIdentityStage();
        consoleService.monthSheetIdentityStage();
    }
}
