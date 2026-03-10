package ftn.siit.nvt.config;

import ftn.siit.nvt.dto.factory.RealTimeAvailabilityDTO;
import ftn.siit.nvt.dto.vehicle.RealTimeVehicleAvailabilityDTO;
import ftn.siit.nvt.dto.warehouse.RealTimeTemperatureDTO;
import ftn.siit.nvt.security.TokenUtils;
import ftn.siit.nvt.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final TokenUtils jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final ApplicationContext applicationContext;

    private static final Pattern FACTORY_PATTERN =
            Pattern.compile("/topic/factory/(\\d+)/availability");

    private static final Pattern WAREHOUSE_PATTERN =
            Pattern.compile("/topic/warehouse/(\\d+)/temperature");

    private static final Pattern VEHICLE_PATTERN =
            Pattern.compile("/topic/vehicle/(\\d+)/availability");


    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000", "http://localhost:4200", "http://localhost")
                .withSockJS();

        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null) {
                    StompCommand command = accessor.getCommand();
                    String sessionId = accessor.getSessionId();

                    if (StompCommand.CONNECT.equals(command)) {
                        handleConnect(accessor);
                    }

                    if (StompCommand.SUBSCRIBE.equals(command)) {
                        handleSubscribe(accessor, sessionId);
                    }
                }

                return message;
            }
        });
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String authToken = accessor.getFirstNativeHeader("Authorization");

        if (authToken != null && authToken.startsWith("Bearer ")) {
            String jwt = authToken.substring(7);
            try {
                String username = jwtAuthenticationFilter.getEmailFromToken(jwt);
                if (username != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (jwtAuthenticationFilter.validateToken(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities()
                                );
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        accessor.setUser(authentication);
                        log.info("WebSocket authenticated user: {}", username);
                    }
                }
            } catch (Exception e) {
                log.error("WebSocket authentication failed: {}", e.getMessage());
            }
        }
    }

    private void handleSubscribe(StompHeaderAccessor accessor, String sessionId) {
        String destination = accessor.getDestination();

        log.info("SUBSCRIBE - Session: {}, Destination: {}", sessionId, destination);

        if (destination != null) {
            Matcher factoryMatcher = FACTORY_PATTERN.matcher(destination);
            if (factoryMatcher.matches()) {
                handleFactorySubscription(factoryMatcher, sessionId, destination);
                return;
            }
            Matcher warehouseMatcher = WAREHOUSE_PATTERN.matcher(destination);
            if (warehouseMatcher.matches()) {
                handleWarehouseSubscription(warehouseMatcher, sessionId, destination);
            }
            Matcher vehicleMatcher = VEHICLE_PATTERN.matcher(destination);
            if (vehicleMatcher.matches()) {
                handleVehicleSubscription(vehicleMatcher, sessionId, destination);
            }
        }
    }
    private void handleFactorySubscription(Matcher matcher, String sessionId, String destination) {
        Long factoryId = Long.parseLong(matcher.group(1));
        log.info("Registering factory subscription for factory {} (session: {})",
                factoryId, sessionId);

        try {
            FactoryAvailabilityBroadcaster broadcaster =
                    applicationContext.getBean(FactoryAvailabilityBroadcaster.class);
            broadcaster.subscribeToFactory(factoryId, sessionId);

            FactoryRealTimeService realTimeService =
                    applicationContext.getBean(FactoryRealTimeService.class);
            SimpMessagingTemplate messagingTemplate =
                    applicationContext.getBean(SimpMessagingTemplate.class);

            RealTimeAvailabilityDTO initialData =
                    realTimeService.getInitialAvailabilityData(factoryId);
            messagingTemplate.convertAndSend(destination, initialData);

            log.info("Factory initial data sent successfully");
        } catch (Exception e) {
            log.error("Failed to handle factory subscription: {}", e.getMessage(), e);
        }
    }

    private void handleWarehouseSubscription(Matcher matcher, String sessionId, String destination) {
        Long warehouseId = Long.parseLong(matcher.group(1));
        log.info("Registering warehouse subscription for warehouse {} (session: {})",
                warehouseId, sessionId);

        try {
            WarehouseAvailabilityBroadcaster broadcaster =
                    applicationContext.getBean(WarehouseAvailabilityBroadcaster.class);
            broadcaster.subscribeToWarehouse(warehouseId, sessionId);

            WarehouseRealTimeService realTimeService =
                    applicationContext.getBean(WarehouseRealTimeService.class);
            SimpMessagingTemplate messagingTemplate =
                    applicationContext.getBean(SimpMessagingTemplate.class);

            RealTimeTemperatureDTO initialData =
                    realTimeService.getInitialTemperatureData(warehouseId);
            messagingTemplate.convertAndSend(destination, initialData);

            log.info("Warehouse initial temperature data sent successfully");
        } catch (Exception e) {
            log.error("Failed to handle warehouse subscription: {}", e.getMessage(), e);
        }
    }

    private void handleVehicleSubscription(Matcher matcher, String sessionId, String destination) {
        Long vehicleId = Long.parseLong(matcher.group(1));
        log.info("Registering vehicle subscription for vehicle {} (session: {})",
                vehicleId, sessionId);

        try {
            DeliveryVehicleAvailabilityBroadcaster broadcaster =
                    applicationContext.getBean(DeliveryVehicleAvailabilityBroadcaster.class);
            broadcaster.subscribeToVehicle(vehicleId, sessionId);

            DeliveryVehicleRealTimeService realTimeService =
                    applicationContext.getBean(DeliveryVehicleRealTimeService.class);
            SimpMessagingTemplate messagingTemplate =
                    applicationContext.getBean(SimpMessagingTemplate.class);

            RealTimeVehicleAvailabilityDTO initialData =
                    realTimeService.getInitialAvailabilityData(vehicleId);
            messagingTemplate.convertAndSend(destination, initialData);

            log.info("Vehicle initial distance data sent successfully");
        } catch (Exception e) {
            log.error("Failed to handle vehicle subscription: {}", e.getMessage(), e);
        }
    }
}
