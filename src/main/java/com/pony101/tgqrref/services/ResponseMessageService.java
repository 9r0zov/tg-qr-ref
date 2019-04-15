package com.pony101.tgqrref.services;

import com.pony101.tgqrref.models.BotReview;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static com.pony101.tgqrref.util.Constants.Queries.RATING;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class ResponseMessageService {

    private final MessageService msg;

    public SendMessage getRatingMessage(String chatId, String deviceId) {
        List<InlineKeyboardButton> buttons = IntStream.range(1, 6)
                .mapToObj(String::valueOf)
                .map(i -> {
                    InlineKeyboardButton ikb = new InlineKeyboardButton(i);
                    ikb.setCallbackData(RATING + " " + i);
                    return ikb;
                })
                .collect(toList());


        InlineKeyboardMarkup replies = new InlineKeyboardMarkup();
        replies.setKeyboard(Collections.singletonList(buttons));

        return new SendMessage()
                .setChatId(chatId)
                .setText(String.format(msg.get("ratingAsk"), deviceId))
                .setReplyMarkup(replies);
    }

    public SendMessage getReviewMessage(String chatId, BotReview botReview) {
        InlineKeyboardButton cancel = new InlineKeyboardButton("Cancel");
        cancel.setCallbackData("__review_false");

        InlineKeyboardMarkup replies = new InlineKeyboardMarkup();
        replies.setKeyboard(Collections.singletonList(Collections.singletonList(cancel)));

        return new SendMessage()
                .setChatId(chatId)
                .setText(String.format(msg.get("ratingProcessReply"), botReview.getRating()))
                .setReplyMarkup(replies)
                .setParseMode("markdown");
    }

    public SendMessage getStartReply(String chatId) {
        return new SendMessage()
                .setChatId(chatId)
                .setText(msg.get("startReply"));
    }

    public SendMessage getSuccessReviewMessage(String chatId) {
        return new SendMessage()
                .setChatId(chatId)
                .setText(msg.get("reviewThanks"));
    }

    public SendMessage getDefaultMessage(String chatId) {
        return new SendMessage()
                .setChatId(chatId)
                .setText(msg.get("defaultReply"));
    }

    public SendMessage getHelpMessage(String chatId) {
        return new SendMessage()
                .setChatId(chatId)
                .setText(msg.get("helpReply"));
    }

    public SendMessage getReviewExistsMessage(String chatId) {
        return new SendMessage()
                .setChatId(chatId)
                .setText(msg.get("reviewAlreadyExists"));
    }
}
