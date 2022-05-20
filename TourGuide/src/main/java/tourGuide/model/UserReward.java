package tourGuide.model;

import java.util.UUID;

public record UserReward(UUID userId, VisitedLocation visitedLocation, Attraction attraction, int rewardPoints) {
}
