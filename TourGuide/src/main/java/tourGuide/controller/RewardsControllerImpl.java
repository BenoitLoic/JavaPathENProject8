package tourGuide.controller;

import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tourGuide.exception.IllegalArgumentException;
import tourGuide.model.UserReward;
import tourGuide.service.RewardsService;
import tourGuide.service.UserService;
import tourGuide.model.user.User;
import static tourGuide.config.Url.GET_REWARDS;

@RestController
public class RewardsControllerImpl implements RewardsController {

  private final Logger logger = LoggerFactory.getLogger(RewardsControllerImpl.class);
  @Autowired private UserService userService;
  @Autowired private RewardsService rewardsService;
  /**
   * Get all rewards owned by the given user.username.
   *
   * @param userName the username
   * @return a collection of UserRewards, empty if none
   */
  @Override
  @GetMapping(value = GET_REWARDS)
  public Collection<UserReward> getRewards(@RequestParam String userName) {

    if (userName == null || userName.isBlank()) {
      logger.warn("error, username is mandatory. username: " + userName);
      throw new IllegalArgumentException("error, username is mandatory.");
    }

    User user = userService.getUser(userName);

    return rewardsService.getRewards(user);
  }
}
