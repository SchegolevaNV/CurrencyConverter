package converter.repositories;

import converter.model.Rate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface RateRepository extends JpaRepository<Rate, Integer> {

    Rate findByCurrencyCodeAndDate (String currencyCode, LocalDate date);

    @Query("SELECT COUNT(*) FROM Rate r WHERE r.date = :date")
    int getRateCountByDate (@Param("date") LocalDate date);
}
