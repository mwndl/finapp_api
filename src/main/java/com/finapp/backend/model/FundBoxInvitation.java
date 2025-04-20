package com.finapp.backend.model;

import com.finapp.backend.model.enums.InvitationStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class FundBoxInvitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fund_box_id")
    private FundBox fundBox;

    @ManyToOne
    @JoinColumn(name = "inviter_id")
    private User inviter;

    @ManyToOne
    @JoinColumn(name = "invitee_id")
    private User invitee;

    @Enumerated(EnumType.STRING)
    private InvitationStatus status;

    private LocalDateTime invitationDate;
    private LocalDateTime acceptedDate;

}
