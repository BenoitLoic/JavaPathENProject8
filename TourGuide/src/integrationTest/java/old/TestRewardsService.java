package old;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tourGuide.Application;
import tourGuide.client.LocationClient;
import tourGuide.client.RewardClient;
import tourGuide.client.UserClient;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.Location;
import tourGuide.model.UserReward;
import tourGuide.model.VisitedLocation;
import tourGuide.service.RewardsServiceImpl;
import tourGuide.service.UserServiceImpl;
import tourGuide.user.User;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class TestRewardsService {

  @Autowired LocationClient locationClient;
  @Autowired UserClient userClient;
  @Autowired RewardClient rewardClient;

  @org.junit.jupiter.api.BeforeAll
  static void beforeAll() {
    java.util.Locale.setDefault(java.util.Locale.US);
    InternalTestHelper.setInternalUserNumber(1);
  }

  @Test
  public void userGetRewards() {
    RewardsServiceImpl rewardsService = new RewardsServiceImpl(rewardClient);
    GpsUtil gpsUtil = new GpsUtil();
    User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
    Attraction attraction = gpsUtil.getAttractions().get(0);
    user.addToVisitedLocations(
        new VisitedLocation(
            user.getUserId(), new Location(attraction.longitude, attraction.latitude), new Date()));
    rewardsService.addRewards(user);
    rewardsService.awaitTerminationAfterShutdown();
    List<UserReward> userRewards = user.getUserRewards();

    assertEquals(1, userRewards.size());
  }

  //  @Disabled("Needs fixed - can throw ConcurrentModificationException")
  @Test
  public void nearAllAttractions()  {
    GpsUtil gpsUtil = new GpsUtil();
    RewardsServiceImpl rewardsService = new RewardsServiceImpl(rewardClient);
    UserServiceImpl userServiceImpl =
        new UserServiceImpl(userClient);
    User user = userServiceImpl.getAllUsers().get(0);
    // clear visited location to avoid duplicate during test
    // (attractionId from gpsUtil and locationClient.gpsUtil are different)
    user.clearVisitedLocations();
    List<Attraction> attractions = gpsUtil.getAttractions();

    for (Attraction a : attractions) {
      user.addToVisitedLocations(
          new VisitedLocation(user.getUserId(), new Location(a.longitude, a.latitude), new Date()));
    }

    rewardsService.addRewards(user);
    rewardsService.awaitTerminationAfterShutdown();

    assertEquals(gpsUtil.getAttractions().size(), user.getUserRewards().size());
  }
}
