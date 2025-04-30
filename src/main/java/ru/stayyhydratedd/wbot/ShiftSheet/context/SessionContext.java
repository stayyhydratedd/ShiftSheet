package ru.stayyhydratedd.wbot.ShiftSheet.context;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import ru.stayyhydratedd.wbot.ShiftSheet.models.*;

import java.util.Optional;

@Setter
@Component
public class SessionContext {

    @Getter
    private AppSettings appSettings;

    private User currentUser;

    private RootFolder currentRootFolder;

    private Pwz currentPwz;

    private MonthSheet currentMonthSheet;

    private Employee currentEmployee;

    private String currentPwzsFolderId;

    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
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

