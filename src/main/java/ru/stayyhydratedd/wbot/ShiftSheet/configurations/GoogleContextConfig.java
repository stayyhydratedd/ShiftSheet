package ru.stayyhydratedd.wbot.ShiftSheet.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.stayyhydratedd.wbot.ShiftSheet.services.google.CredentialsService;
import ru.stayyhydratedd.wbot.ShiftSheet.services.google.GoogleService;
import ru.stayyhydratedd.wbot.ShiftSheet.util.InputOutputUtil;
import ru.stayyhydratedd.wbot.ShiftSheet.util.JColorUtil;

@Configuration
public class GoogleContextConfig {

    @Bean
    public JColorUtil jColorUtil() {
        return new JColorUtil();
    }

    @Bean
    public InputOutputUtil inputOutputUtil() {
        return new InputOutputUtil(jColorUtil());
    }

    @Bean
    public CredentialsService credentialsService() {
        return new CredentialsService(jColorUtil());
    }

    @Bean
    public GoogleService googleService() {
        return new GoogleService(credentialsService(), inputOutputUtil(), jColorUtil());
    }
}
