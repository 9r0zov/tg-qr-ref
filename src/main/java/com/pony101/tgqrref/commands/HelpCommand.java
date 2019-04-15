package com.pony101.tgqrref.commands;

import com.pony101.tgqrref.services.ResponseMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class HelpCommand extends BotCommand {

    private final ResponseMessageService responseMessageService;

    @Autowired
    public HelpCommand(ResponseMessageService responseMessageService) {
        super("/help", "View list of commands and special info.");
        this.responseMessageService = responseMessageService;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        try {
            absSender.execute(responseMessageService.getHelpMessage(chat.getId().toString()));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
