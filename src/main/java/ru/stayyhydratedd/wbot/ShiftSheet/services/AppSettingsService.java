package ru.stayyhydratedd.wbot.ShiftSheet.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.stayyhydratedd.wbot.ShiftSheet.context.SessionContext;
import ru.stayyhydratedd.wbot.ShiftSheet.models.AppSettings;
import ru.stayyhydratedd.wbot.ShiftSheet.repositories.AppSettingsRepository;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class AppSettingsService {

    private final AppSettingsRepository repository;
    private final SessionContext sessionContext;

    public static final int SINGLETON_ID = 1;

    private AppSettings save(AppSettings settings) {
        return repository.save(settings);
    }

    public AppSettings getSettings() {
        return repository.findById(SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("AppSettings must be initialized manually in the DB"));
    }

    public AppSettings updateSettings(Consumer<AppSettings> updater) {
        AppSettings settings = getSettings();
        updater.accept(settings);
        return save(settings);
    }

    @PostConstruct
    private void initializeAppSettingsInSessionContext(){
        sessionContext.setAppSettings(getSettings());
    }
}