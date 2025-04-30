package ru.stayyhydratedd.wbot.ShiftSheet.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.stayyhydratedd.wbot.ShiftSheet.models.Pwz;

import java.util.Optional;

@Repository
public interface PwzRepository extends JpaRepository<Pwz, Integer> {

    @Query("""
    SELECT pwz
    FROM Pwz pwz
    LEFT JOIN FETCH pwz.monthSheets
    WHERE pwz.id = :id
    """)
    Optional<Pwz> findByIdWithMonthSheets(@Param("id") int id);
}
