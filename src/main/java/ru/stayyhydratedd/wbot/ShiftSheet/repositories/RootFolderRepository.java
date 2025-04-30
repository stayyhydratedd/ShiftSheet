package ru.stayyhydratedd.wbot.ShiftSheet.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.stayyhydratedd.wbot.ShiftSheet.models.RootFolder;

import java.util.Optional;

@Repository
public interface RootFolderRepository extends JpaRepository<RootFolder, Integer> {

    @Query("""
    SELECT r
    FROM RootFolder r
    LEFT JOIN FETCH r.pwzs
    LEFT JOIN FETCH r.users
    WHERE r.googleId = :googleId
    """)
    Optional<RootFolder> findByGoogleIdWithPwzsAndUsers(@Param("googleId") String googleId);

    @Query("""
    SELECT r
    FROM RootFolder r
    LEFT JOIN FETCH r.pwzs
    LEFT JOIN FETCH r.users
    WHERE r.id = :id
    """)
    Optional<RootFolder> findByIdWithPwzsAndUsers(@Param("id") int id);
}
