package com.pony101.tgqrref.bots;

import com.pony101.tgqrref.commands.HelpCommand;
import com.pony101.tgqrref.commands.RatingCommand;
import com.pony101.tgqrref.commands.StartCommand;
import com.pony101.tgqrref.models.BotReview;
import com.pony101.tgqrref.repositories.BotReviewRepository;
import com.pony101.tgqrref.services.ResponseMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.pony101.tgqrref.enums.ReviewStatus.*;
import static com.pony101.tgqrref.util.Constants.Queries.RATING;
import static com.pony101.tgqrref.util.Constants.Queries.REVIEW;

@Component
@PropertySource("classpath:application.yml")
@Slf4j
public class QrRefrigeratorBot extends TelegramLongPollingCommandBot {

    private final StartCommand startCommand;
    private final HelpCommand helpCommand;
    private final RatingCommand ratingCommand;
    private final BotReviewRepository repository;
    private final ResponseMessageService responseMessageService;

    @Value("${myApp.token}")
    private String token;

    public QrRefrigeratorBot(StartCommand startCommand, HelpCommand helpCommand,
                             RatingCommand ratingCommand, BotReviewRepository repository,
                             ResponseMessageService responseMessageService) {
        super("QrRefrigeratorBot");
        this.startCommand = startCommand;
        this.helpCommand = helpCommand;
        this.ratingCommand = ratingCommand;
        this.repository = repository;
        this.responseMessageService = responseMessageService;

        register(startCommand);
        register(helpCommand);
        register(ratingCommand);

        registerDefaultAction(
                (absSender, message) -> helpCommand.execute(
                        absSender, message.getFrom(), message.getChat(), new String[]{}));
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            replyOnMessage(update);
        } else if (update.hasCallbackQuery()) {
            replyOnQuery(update);
        }
    }

    private void replyOnMessage(Update update) {
        String chatId = update.getMessage().getChatId().toString();
        String incomingText = update.getMessage().getText();

        if (repository.existsByUserIdAndStatus(chatId, WAITING_FOR_REVIEW)) {
            processReviewQuery(chatId, incomingText);
        } else {
            defaultMessage(chatId);
        }
    }

    private void replyOnQuery(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String chatId = callbackQuery.getFrom().getId().toString();
        String rawData = callbackQuery.getData();

        if (rawData.startsWith(RATING)) {
            processRatingQuery(chatId, rawData);
        } else if (rawData.equals(REVIEW)) {
            processReviewQuery(chatId, null);
        }
    }

    private void processReviewQuery(String chatId, String rawData) {
        repository.findByUserIdAndStatus(chatId, WAITING_FOR_REVIEW)
                .ifPresentOrElse(
                        botReview -> saveReviewAndReply(chatId, rawData, botReview),
                        () -> defaultMessage(chatId));
    }

    private void processRatingQuery(String chatId, String rawData) {
        String data = rawData.split(" ")[1];

        SendMessage message = repository.findByUserIdAndStatus(chatId, WAITING_FOR_RATING)
                .map(botReview -> {
                    botReview.setStatus(WAITING_FOR_REVIEW);
                    botReview.setRating(Integer.valueOf(data));

                    return responseMessageService.getReviewMessage(chatId, repository.save(botReview));
                })
                .orElse(responseMessageService.getDefaultMessage(chatId));

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void saveReviewAndReply(String chatId, String rawData, BotReview botReview) {
        botReview.setReviewTxt(rawData);
        botReview.setStatus(FINISHED);
        repository.save(botReview);

        try {
            execute(responseMessageService.getSuccessReviewMessage(chatId));
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void defaultMessage(String chatId) {
        try {
            execute(responseMessageService.getDefaultMessage(chatId));
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }
}