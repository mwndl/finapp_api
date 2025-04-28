package com.finapp.backend.domain.listener;

import com.finapp.backend.domain.event.PasswordChangedEvent;
import com.finapp.backend.domain.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordChangedListener {

    private final SessionService sessionService;

    @EventListener
    public void onPasswordChanged(PasswordChangedEvent event) {
        sessionService.revokeAllUserSessions(event.getUser());
    }
}
