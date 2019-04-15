package com.pony101.tgqrref.context;

import com.pony101.tgqrref.bots.QrRefrigeratorBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

@Slf4j
@Component
@RequiredArgsConstructor
public class Application {

    private final QrRefrigeratorBot qrRefrigeratorBot;

    @EventListener(classes = ContextRefreshedEvent.class)
    public void listenContextRefresh(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        if (applicationContext.getParent() != null) {
            return;
        }

        TelegramBotsApi botsApi = new TelegramBotsApi();
        try {
            botsApi.registerBot(qrRefrigeratorBot);
        } catch (TelegramApiRequestException e) {
            log.error(e.getMessage(), e);
        }
    }

}
