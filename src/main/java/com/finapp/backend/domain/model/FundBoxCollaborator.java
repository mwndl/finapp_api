package com.finapp.backend.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcType;

import java.time.LocalDate;
import java.util.UUID;

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
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JdbcType(org.hibernate.type.descriptor.jdbc.CharJdbcType.class)
    @Column(updatable = false, nullable = false, columnDefinition = "CHAR(36)")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fund_box_id")
    private FundBox fundBox;

    @Column(nullable = false)
    private LocalDate joinedAt = LocalDate.now();
}

