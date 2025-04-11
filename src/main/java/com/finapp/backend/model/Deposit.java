package com.finapp.backend.model;

import com.finapp.backend.model.enums.InvestmentSubtype;
import com.finapp.backend.model.enums.InvestmentType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user", "fundBox"})
@EqualsAndHashCode(exclude = {"user", "fundBox"})
@Entity
@Table(name = "deposit")
public class Deposit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate date;

    private String description;

    /* future implementation: investment support

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvestmentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvestmentSubtype subtype;

    @PrePersist
    @PreUpdate
    private void validateSubtypeCompatibility() {
        if (subtype.getParentType() != type) {
            throw new IllegalArgumentException(
                    String.format("Subtype %s is not valid for type %s", subtype, type)
            );
        }
    }
    */

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "fund_box_id")
    private FundBox fundBox;

}

