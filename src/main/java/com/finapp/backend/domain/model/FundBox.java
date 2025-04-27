package com.finapp.backend.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "fund_box", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"owner_id", "name"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"owner", "collaborators", "deposits"})
@EqualsAndHashCode(exclude = {"owner", "collaborators", "deposits"})
public class FundBox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal financialGoal;

    @Column(nullable = false)
    private LocalDate targetDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "fundBox", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Deposit> deposits;

    @OneToMany(mappedBy = "fundBox", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FundBoxCollaborator> collaborators;
}
