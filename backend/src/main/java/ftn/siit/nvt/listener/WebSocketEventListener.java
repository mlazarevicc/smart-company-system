package ftn.siit.nvt.listener;

import ftn.siit.nvt.service.FactoryAvailabilityBroadcaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final FactoryAvailabilityBroadcaster broadcaster;

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        log.info("WebSocket Session Connected: {}", headers.getSessionId());
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headers.getSessionId();

        log.info("WebSocket Session Disconnected: {}", sessionId);

        broadcaster.handleSessionDisconnect(sessionId);
    }
}
