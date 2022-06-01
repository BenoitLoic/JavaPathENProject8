package tourGuide.service;

import java.util.Collection;
import java.util.UUID;
import tourGuide.dto.GetNearbyAttractionDto;
import tourGuide.model.Attraction;
import tourGuide.model.UserReward;
import tourGuide.model.user.User;

/** Reward service interface. */
public interface RewardsService {

  /**
   * Get the reward point that could be won by the user for the given list of attractions.
   *
   * @param attractionCollection the list of attraction
   * @param userId the user id
   * @return a list of attraction dto with rewardPoint field
   */
  Collection<GetNearbyAttractionDto> calculateRewardsPoints(
      Collection<Attraction> attractionCollection, UUID userId);

  /**
   * Check if there is a reward for the last visitedLocation.
   *
   * @param user the user
   */
  void addRewardsForLastLocation(User user);

  /**
   * Get all the rewards already owned by a user.
   *
   * @param user the user
   * @return a list of user's rewards
   */
  Collection<UserReward> getRewards(User user);

  /**
   * Check the user visited location and add the rewards accordingly.
   *
   * @param user the user
   */
  void addRewards(User user);
}
