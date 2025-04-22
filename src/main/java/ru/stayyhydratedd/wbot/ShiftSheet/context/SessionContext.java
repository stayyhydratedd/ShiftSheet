package ru.stayyhydratedd.wbot.ShiftSheet.context;

import lombok.Data;
import org.springframework.stereotype.Component;
import ru.stayyhydratedd.wbot.ShiftSheet.models.*;

@Component
@Data
public class SessionContext {

    private AppSettings appSettings;
    private Owner currentOwner;
    private RootFolder currentRootFolder;
    private Pwz currentPwz;
    private MonthSheet currentMonthSheet;
    private Employee currentEmployee;
    private String currentPwzsFolderId;
}
