package ru.stayyhydratedd.wbot.ShiftSheet.context;

import lombok.Setter;
import org.springframework.stereotype.Component;
import ru.stayyhydratedd.wbot.ShiftSheet.models.*;

import java.util.Optional;

@Component
public class SessionContext {

    @Setter
    private AppSettings appSettings;
    @Setter
    private Owner currentOwner;
    @Setter
    private RootFolder currentRootFolder;
    @Setter
    private Pwz currentPwz;
    @Setter
    private MonthSheet currentMonthSheet;
    @Setter
    private Employee currentEmployee;
    @Setter
    private String currentPwzsFolderId;

    public Optional<AppSettings> getCurrentAppSettings() {
        return Optional.ofNullable(appSettings);
    }
    public Optional<Owner> getCurrentOwner() {
        return Optional.ofNullable(currentOwner);
    }
    public Optional<RootFolder> getCurrentRootFolder() {
        return Optional.ofNullable(currentRootFolder);
    }
    public Optional<Pwz> getCurrentPwz() {
        return Optional.ofNullable(currentPwz);
    }
    public Optional<MonthSheet> getCurrentMonthSheet() {
        return Optional.ofNullable(currentMonthSheet);
    }
    public Optional<Employee> getCurrentEmployee() {
        return Optional.ofNullable(currentEmployee);
    }
    public Optional<String> getCurrentPwzsFolderId() {
        return Optional.ofNullable(currentPwzsFolderId);
    }
}
