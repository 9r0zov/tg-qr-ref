package com.pony101.tgqrref.repositories;

import com.pony101.tgqrref.enums.ReviewStatus;
import com.pony101.tgqrref.models.BotReview;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface BotReviewRepository extends MongoRepository<BotReview, String> {

    Optional<BotReview> findByUserIdAndStatus(String userId, ReviewStatus reviewStatus);

    Optional<BotReview> findByDeviceIdAndUserId(String deviceId, String userId);

    boolean existsByUserIdAndStatus(String userId, ReviewStatus status);

    boolean existsByUserIdAndDeviceIdAndStatus(String userId, String deviceId, ReviewStatus status);

}
