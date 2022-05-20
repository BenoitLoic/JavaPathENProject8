package tourGuide.service;

import tourGuide.dto.GetNearbyAttractionDto;
import tourGuide.model.Attraction;
import tourGuide.model.UserReward;
import tourGuide.user.User;

import java.util.Collection;
import java.util.UUID;

public interface RewardsService {
    Collection<GetNearbyAttractionDto> calculateRewardsPoints(
            Collection<Attraction> attractionCollection, UUID userId);

    Collection<UserReward> getRewards(User user);
}
