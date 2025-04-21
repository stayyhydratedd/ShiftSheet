package ru.stayyhydratedd.wbot.ShiftSheet.services;

import com.google.api.services.sheets.v4.model.Sheet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.stayyhydratedd.wbot.ShiftSheet.context.SessionContext;
import ru.stayyhydratedd.wbot.ShiftSheet.enums.MimeType;
import ru.stayyhydratedd.wbot.ShiftSheet.models.Pwz;
import ru.stayyhydratedd.wbot.ShiftSheet.repositories.PwzRepository;
import ru.stayyhydratedd.wbot.ShiftSheet.util.GoogleFileWorkerUtil;
import ru.stayyhydratedd.wbot.ShiftSheet.util.JColorUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PwzService {

    private final PwzRepository pwzRepository;
    private final GoogleFileWorkerUtil googleFileWorkerUtil;
    private final SessionContext sessionContext;
    private final JColorUtil jColorUtil;

    public List<Pwz> findAll() {
        return pwzRepository.findAll();
    }

    public void save(Pwz pwz) {
        pwzRepository.save(pwz);
    }

    public String createPwzFolder(String folderName) {
        return googleFileWorkerUtil.createFile(
                folderName, "pwzs", MimeType.FOLDER, sessionContext.getCurrentPwzsFolderId());
    }

    public Pwz createPwz(String pwzAddress){
        String pwzFolderId = createPwzFolder("ПВЗ " + pwzAddress);
        String employeeWorkSchedule = "График работы сотрудников ул. " + pwzAddress;

        String pwzId = googleFileWorkerUtil.createFile(
                employeeWorkSchedule, "ПВЗ " + pwzAddress,
                MimeType.SPREADSHEET, pwzFolderId);

        System.out.printf("""
                        %sСтавка по умолчанию для текущего ПВЗ = %s
                        %sЭто значение можно будет изменить позже
                        """, jColorUtil.INFO,
                jColorUtil.turnTextIntoColor(
                        sessionContext.getCurrentRootFolder().getPayRate().toString(), JColorUtil.COLOR.INFO),
                jColorUtil.INFO);
        Pwz pwz = Pwz.builder()
                .address(pwzAddress)
                .googleId(pwzId)
                .payRate(sessionContext.getCurrentRootFolder().getPayRate())
                .build();

        pwzRepository.save(pwz);
        return pwz;
    }

    public List<Sheet> getMonthSheets(String pwzGoogleId) {
        return googleFileWorkerUtil.getSheets(pwzGoogleId);
    }
}
