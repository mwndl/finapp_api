package com.finapp.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"owner", "collaborators", "investments"})
@EqualsAndHashCode(exclude = {"owner", "collaborators", "investments"})
@Entity
@Table(name = "fund_box", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"owner_id", "name"})
})
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

    @ManyToMany
    @JoinTable(
            name = "fund_box_collaborators",
            joinColumns = @JoinColumn(name = "fund_box_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> collaborators;

    @OneToMany(mappedBy = "fundBox", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Investment> investments;
}
