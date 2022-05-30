package tourGuide.controller;

import org.springframework.web.bind.annotation.RequestParam;
import tourGuide.model.UserReward;

import java.util.Collection;

/**
 * Rest controller for Rewards feature.
 */
public interface RewardsController {

    /**
     * Get all rewards owned by the given user.username.
     * @param userName the username
     * @return a collection of UserRewards, empty if none
     */
    Collection<UserReward> getRewards( String userName);

}
