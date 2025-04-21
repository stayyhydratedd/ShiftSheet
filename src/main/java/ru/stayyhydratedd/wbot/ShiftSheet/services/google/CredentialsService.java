package ru.stayyhydratedd.wbot.ShiftSheet.services.google;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.Credentials;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.stayyhydratedd.wbot.ShiftSheet.enums.GoogleCredentialsScope;
import ru.stayyhydratedd.wbot.ShiftSheet.util.JColorUtil;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CredentialsService {

    private final JColorUtil jColorUtil;

    private static final String TOKENS_DIRECTORY = "tokens";
    private static final String CREDENTIALS_FILE = "/credentials.json";

    public static final File storedCredential = new File(
            System.getProperty("user.dir") + "\\" + TOKENS_DIRECTORY + "\\StoredCredential");

    private final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    protected Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT, GoogleCredentialsScope scope)
            throws IOException {

        InputStream IN = Credentials.class.getResourceAsStream(CREDENTIALS_FILE);

        if (IN == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE);
        }

        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(IN));

        List<String> SCOPES =
                scope == GoogleCredentialsScope.DRIVE ?
                Arrays.asList(DriveScopes.DRIVE_METADATA, DriveScopes.DRIVE_FILE) :
                List.of(SheetsScopes.SPREADSHEETS);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(8888)
                .setCallbackPath("/")
                .build();

        return getCredential(flow, receiver);
    }

    private Credential getCredential(GoogleAuthorizationCodeFlow flow, LocalServerReceiver receiver) throws IOException {
        AuthorizationCodeInstalledApp app = new AuthorizationCodeInstalledApp(flow, receiver) {
            @Override
            protected void onAuthorization(AuthorizationCodeRequestUrl authorizationUrl) {
                String url = authorizationUrl.build();
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop desktop = Desktop.getDesktop();
                        desktop.browse(new URI(url));
                        System.out.printf("%sБраузер открылся\n", jColorUtil.SUCCESS);
                    } else {
                        System.out.printf("%sDesktop не поддерживается на этой системе\n", jColorUtil.WARN);
                        System.out.printf("%sПерейдите вручную по ссылке для авторизации: \n%s\n", jColorUtil.INFO, url);
                    }
                } catch (Exception e) {
                    System.out.printf("%sНе удалось открыть браузер: %s\n", jColorUtil.ERROR, e.getMessage());
                    System.out.printf("%sПерейдите вручную по ссылке для авторизации: \n%s\n", jColorUtil.INFO, url);
                }
            }
        };

        Credential credential = app.authorize("ShiftSheet");
        if (credential.getExpiresInSeconds() <= 0) {
            credential.refreshToken();
        }
        return credential;
    }

    protected JsonFactory getJsonFactory() {
        return JSON_FACTORY;
    }
}
