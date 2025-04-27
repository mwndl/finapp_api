package com.finapp.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "fund_box_collaborators", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"fund_box_id", "user_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user", "fundBox"})
@EqualsAndHashCode(exclude = {"user", "fundBox"})
public class FundBoxCollaborator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fund_box_id")
    private FundBox fundBox;

    @Column(nullable = false)
    private LocalDate joinedAt = LocalDate.now();
}

