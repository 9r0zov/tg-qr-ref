package com.pony101.tgqrref.commands;

import com.pony101.tgqrref.enums.ReviewStatus;
import com.pony101.tgqrref.models.BotReview;
import com.pony101.tgqrref.repositories.BotReviewRepository;
import com.pony101.tgqrref.services.ResponseMessageService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class StartCommand extends BotCommand {

    private final BotReviewRepository repository;
    private final ResponseMessageService responseMessageService;

    public StartCommand(BotReviewRepository repository, ResponseMessageService responseMessageService) {
        super("/start", "You can start bot with refrigerator id as parameter");
        this.repository = repository;
        this.responseMessageService = responseMessageService;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        try {
            if (arguments.length == 0) {
                absSender.execute(responseMessageService.getStartReply(chat.getId().toString()));
            } else {
                precessQr(absSender, chat.getId().toString(), arguments[0]);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void precessQr(AbsSender absSender, String chatId, String deviceId) throws TelegramApiException {
        if (repository.existsByUserIdAndDeviceIdAndStatus(chatId, deviceId, ReviewStatus.FINISHED)) {
            absSender.execute(responseMessageService.getReviewExistsMessage(chatId));
        } else {
            final BotReview review = repository.findByDeviceIdAndUserId(deviceId, chatId)
                    .orElseGet(() -> {
                        BotReview botReview = new BotReview();
                        botReview.setDeviceId(deviceId);
                        botReview.setUserId(chatId);
                        return repository.save(botReview);
                    });

            switch (review.getStatus()) {
                case WAITING_FOR_RATING:
                    absSender.execute(responseMessageService.getRatingMessage(chatId, deviceId));
                    break;
                case WAITING_FOR_REVIEW:
                    absSender.execute(responseMessageService.getReviewMessage(chatId, review));
                    break;
            }
        }
    }

}
