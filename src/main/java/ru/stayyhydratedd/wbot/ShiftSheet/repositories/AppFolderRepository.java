package ru.stayyhydratedd.wbot.ShiftSheet.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.stayyhydratedd.wbot.ShiftSheet.models.AppFolder;

@Repository
public interface AppFolderRepository extends JpaRepository<AppFolder, Integer> {
}
