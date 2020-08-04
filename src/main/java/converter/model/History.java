package converter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "history")
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "currency_from")
    @NotNull
    private String currencyFrom;

    @Column(name = "currency_to")
    @NotNull
    private String currencyTo;

    @NotNull
    @Column(name = "exchange_sum")
    private double exchangeSum;

    @NotNull
    @Column(name = "getting_sum")
    private double gettingSum;

    @NotNull
    private LocalDate date;

    @NotNull
    @Column(name = "user_id")
    private int userId;

    @NotNull
    @Column(name = "rate_id")
    private int rateId;
}
