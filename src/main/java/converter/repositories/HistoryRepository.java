package converter.repositories;

import converter.model.History;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<History, Integer> {

    @Query("SELECT COUNT(*) FROM History h WHERE h.userId = :userId")
    int getHistoryCountByUser (@Param("userId") int userId);

    @Query("SELECT COUNT(*) FROM History h WHERE h.userId = :userId AND h.date = :date")
    int getHistoryCountByUserAndDate (@Param("userId") int userId, @Param("date") LocalDate date);

    List<History> findByUserId (int userId, Pageable pageable);
    List<History> findByUserIdAndDate (int userId, LocalDate date, Pageable pageable);
}
