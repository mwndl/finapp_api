package com.finapp.backend.dto.fundbox.v2;

import com.finapp.backend.dto.user.InviteResponse;
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
