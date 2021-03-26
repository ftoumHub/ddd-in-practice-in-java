package kz.sabyr.dddinpractice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.sabyr.dddinpractice.core.snackmachine.domain.SnackMachine;
import kz.sabyr.dddinpractice.core.snackmachine.domain.repository.SnackMachineRepository;
import kz.sabyr.dddinpractice.web.command.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.isNull;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SocketHandler extends TextWebSocketHandler {
    private Map<String, SnackMachine> sessions = new ConcurrentHashMap<>();

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SnackMachineRepository snackMachineRepository;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUri(session.getUri()).build().getQueryParams();
        String sessionId = queryParams.getFirst("sessionId");
        if(!sessions.containsKey(sessionId)) {
            snackMachineRepository.findById(1L)
                    .ifPresent(machine -> sessions.put(sessionId, machine));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUri(session.getUri()).build().getQueryParams();
        String sessionId = queryParams.getFirst("sessionId");
        if(isNull(sessionId)) {
            System.out.println("Session ID is null");
            return;
        }

        SnackMachine snackMachine = sessions.get(sessionId);
        if(isNull(snackMachine)) {
            return;
        }

        Command command = objectMapper.readValue(message.getPayload(), Command.class);
        if(isNull(command)) {
            System.out.println("Command is null");
            return;
        }

        String action = command.getAction();
        Handler handler = null;
        switch (action) {
            case "put":
                handler = objectMapper.readValue(command.getCommand(), PutMoneyHandler.class);
                break;
            case "buy":
                handler = objectMapper.readValue(command.getCommand(), BuySnackHandler.class);
                ((BuySnackHandler)handler).setSnackMachineRepository(snackMachineRepository);
                break;
            case "return":
                handler = objectMapper.readValue(command.getCommand(), ReturnMoneyHandler.class);
                break;
        }

        if(isNull(handler)) {
            System.out.println("Handler is null");
            return;
        }

        HandlerResult result = handler.handle(snackMachine);

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(result)));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUri(session.getUri()).build().getQueryParams();
        String sessionId = queryParams.getFirst("sessionId");
        sessions.remove(sessionId);
    }
}
