package tourGuide;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


import tourGuide.client.LocationClient;
import tourGuide.helper.InternalTestHelper;

import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserReward;

@SpringBootTest
@ActiveProfiles("test")
@EnableConfigurationProperties
@ExtendWith(SpringExtension.class)
public class TestPerformance {

  @Autowired TourGuideService tourGuideService;
  @Autowired
  LocationClient locationClient;

  /*
   * A note on performance improvements:
   *
   *     The number of users generated for the high volume tests can be easily adjusted via this method:
   *
   *     		InternalTestHelper.setInternalUserNumber(100000);
   *
   *
   *     These tests can be modified to suit new solutions, just as long as the performance metrics
   *     at the end of the tests remains consistent.
   *
   *     These are performance metrics that we are trying to hit:
   *
   *     highVolumeTrackLocation: 100,000 users within 15 minutes:
   *     		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
   *
   *     highVolumeGetRewards: 100,000 users within 20 minutes:
   *          assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
   */


  @Test
  public void highVolumeTrackLocation() {

    // Users should be incremented up to 100,000, and test finishes within 15 minutes
    InternalTestHelper.setInternalUserNumber(10000);

    List<User> allUsers = new ArrayList<>();
    allUsers = tourGuideService.getAllUsers();

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    for (User user : allUsers) {
//      tourGuideService.trackUserLocation(user);
      locationClient.addLocation(user.getUserId());
    }
    stopWatch.stop();
    tourGuideService.tracker.stopTracking();

    System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(
        stopWatch.getTime()) + " seconds.");
    assertTrue(
        TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
  }

//  @Disabled
//  @Test
//  public void highVolumeGetRewards() {
//    GpsUtil        gpsUtil        = new GpsUtil();
//    RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
//
//    // Users should be incremented up to 100,000, and test finishes within 20 minutes
//    InternalTestHelper.setInternalUserNumber(100);
//    StopWatch stopWatch = new StopWatch();
//    stopWatch.start();
//    TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
//
//    Attraction attraction = gpsUtil.getAttractions().get(0);
//    List<User> allUsers   = new ArrayList<>();
//    allUsers = tourGuideService.getAllUsers();
//    allUsers.forEach(
//        u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));
//
//    allUsers.forEach(u -> rewardsService.calculateRewards(u));
//
//    for (User user : allUsers) {
//      assertTrue(user.getUserRewards().size() > 0);
//    }
//    stopWatch.stop();
//    tourGuideService.tracker.stopTracking();
//
//    System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(
//        stopWatch.getTime()) + " seconds.");
//    assertTrue(
//        TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
//  }

}
