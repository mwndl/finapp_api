package com.finapp.backend.v2.dto.fundbox;

import com.finapp.backend.v1.dto.user.InviteResponse;
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
