package converter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "currency")
public class Currency {

    @Id
    @Column(name = "currency_id")
    @NotNull
    private String currencyId;

    @Column (name = "num_code")
    @NotNull
    private String numCode;

    @Column (name = "char_code")
    @NotNull
    private String charCode;

    @NotNull
    private String name;
}
