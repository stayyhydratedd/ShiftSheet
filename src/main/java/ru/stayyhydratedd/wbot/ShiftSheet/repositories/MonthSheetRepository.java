package ru.stayyhydratedd.wbot.ShiftSheet.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.stayyhydratedd.wbot.ShiftSheet.models.MonthSheet;

@Repository
public interface MonthSheetRepository extends JpaRepository<MonthSheet, Integer> {
}
