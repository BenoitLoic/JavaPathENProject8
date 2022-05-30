package tourGuide.controller;

import java.util.Collection;
import tourGuide.model.UserReward;

/** Rest controller for Rewards feature. */
public interface RewardsController {

  /**
   * Get all rewards owned by the given user.username.
   *
   * @param userName the username
   * @return a collection of UserRewards, empty if none
   */
  Collection<UserReward> getRewards(String userName);
}
