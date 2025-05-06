package com.finapp.backend.features.v2.fundbox.dto;

import com.finapp.backend.features.v1.user.dto.InviteResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Collaboration {
    private OwnerResponseV2 owner;
    private List<CollaboratorResponseV2> collaborators;
    private List<InviteResponse> invites;
}
