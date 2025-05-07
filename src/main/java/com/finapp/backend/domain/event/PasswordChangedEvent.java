package com.finapp.backend.domain.event;

import com.finapp.backend.domain.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PasswordChangedEvent {

    private final User user;
}
