package com.pony101.tgqrref.models;

import com.pony101.tgqrref.enums.ReviewStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class BotReview {

    @Id
    private String id;
    private String userId;
    private String deviceId;
    private String reviewTxt;
    private int rating;
    private ReviewStatus status = ReviewStatus.WAITING_FOR_RATING;

}
