package ru.stayyhydratedd.wbot.ShiftSheet.services;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.stayyhydratedd.wbot.ShiftSheet.context.SessionContext;
import ru.stayyhydratedd.wbot.ShiftSheet.enums.MimeType;
import ru.stayyhydratedd.wbot.ShiftSheet.models.RootFolder;
import ru.stayyhydratedd.wbot.ShiftSheet.repositories.RootFolderRepository;
import ru.stayyhydratedd.wbot.ShiftSheet.util.GoogleFileWorkerUtil;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RootFolderService {

    private final RootFolderRepository rootFolderRepository;
    private final GoogleFileWorkerUtil googleFileWorkerUtil;
    private final SessionContext sessionContext;


    public void save(RootFolder rootFolder) {
        rootFolderRepository.save(rootFolder);
    }

    public String createRootFolder(String folderName) {
        return googleFileWorkerUtil.createFile(folderName, "Google Disk", MimeType.FOLDER);
    }

    public void checkRootFolderForInternalFolders(RootFolder rootFolder) {
        Optional<FileList> foundFiles = googleFileWorkerUtil.getDirectoryFileList(rootFolder.getGoogleId());
        if (foundFiles.isPresent()) {
            FileList fileList = foundFiles.get();
            List<File> files = fileList.getFiles();
            boolean pwzsFolderFound = false;
            for (File file : files) {
                if (file.getName().equals("pwzs")){
                    sessionContext.setCurrentPwzsFolderId(file.getId());
                    pwzsFolderFound = true;
                    break;
                }
            }
            if (!pwzsFolderFound) {
                String pwzsFolderId = googleFileWorkerUtil.createFile(
                        "pwzs", rootFolder.getTitle(), MimeType.FOLDER, rootFolder.getGoogleId());
                sessionContext.setCurrentPwzsFolderId(pwzsFolderId);
            }
        } else {
            System.out.printf("Root folder does not exist: %s\n", rootFolder.getGoogleId());
        }
    }
}
