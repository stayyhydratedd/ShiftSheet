package ru.stayyhydratedd.wbot.ShiftSheet.services.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.Sheets;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.stayyhydratedd.wbot.ShiftSheet.enums.GoogleCredentialsScope;
import ru.stayyhydratedd.wbot.ShiftSheet.util.InputOutputUtil;
import ru.stayyhydratedd.wbot.ShiftSheet.util.JColorUtil;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
@RequiredArgsConstructor
public class GoogleService {

    private Drive DRIVE_SERVICE;
    private Sheets SHEETS_SERVICE;
    private final CredentialsService credentialsService;
    private final InputOutputUtil inputOutputUtil;
    private final JColorUtil jColorUtil;

    private Drive createDriveService(Credential driveCredential) throws GeneralSecurityException, IOException {
        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), credentialsService.getJsonFactory(), driveCredential)
                .setApplicationName("Google Drive App")
                .build();
    }

    private Sheets createSheetsService(Credential sheetsCredential) throws GeneralSecurityException, IOException {
        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), credentialsService.getJsonFactory(), sheetsCredential)
                .setApplicationName("Google Sheets App")
                .build();
    }

    private void initServices() throws IOException, GeneralSecurityException {
        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Credential driveCredential = credentialsService.getCredentials(HTTP_TRANSPORT, GoogleCredentialsScope.DRIVE);
        Credential sheetsCredential = credentialsService.getCredentials(HTTP_TRANSPORT, GoogleCredentialsScope.SHEETS);

        DRIVE_SERVICE = createDriveService(driveCredential);
        SHEETS_SERVICE = createSheetsService(sheetsCredential);
    }

    @PostConstruct
    public void initialize() throws IOException, GeneralSecurityException {
        if (!CredentialsService.storedCredential.exists()) {
            inputOutputUtil.printSequenceFromString(String.format("""
                    %sСейчас откроется браузер, где необходимо будет авторизоваться и разрешить доступ для этого приложения
                    %sОткрываю браузер
                    """, jColorUtil.INFO, jColorUtil.INFO)
            );
        }
        initServices();
    }

    public Drive getDriveService() {
        return DRIVE_SERVICE;
    }

    public Sheets getSheetsService() {
        return SHEETS_SERVICE;
    }
}
