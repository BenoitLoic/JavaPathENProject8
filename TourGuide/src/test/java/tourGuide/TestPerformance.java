package tourGuide;

import tourGuide.client.LocationClient;
import tourGuide.client.UserClient;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
// @ActiveProfiles("test")
@EnableConfigurationProperties
@ExtendWith(SpringExtension.class)
public class TestPerformance {

  @Autowired LocationClient locationClient;
  @Autowired UserClient userClient;
  //  @Autowired TourGuideService tourGuideService;
  @Autowired RewardsService rewardsService;

  @BeforeAll
  static void beforeAll() {
    InternalTestHelper.setInternalUserNumber(100);
  }

  @BeforeEach
  void setUp() {}

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
  public void highVolumeTrackLocation() throws Exception {

    // Users should be incremented up to 100,000, and test finishes within 15 minutes
    TourGuideService tourGuideService =
        new TourGuideService(locationClient, userClient, rewardsService);

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    CopyOnWriteArrayList<User> allUsers = tourGuideService.getAllUsers();
    for (User user : allUsers) {

      tourGuideService.trackUserLocation(user);
    }

    //    allUsers.parallelStream()
    //        .forEach(
    //            (u) -> {
    //              try {
    //                tourGuideService.trackUserLocation(u);
    //              } catch (Exception e) {
    //                throw new RuntimeException(e);
    //              }
    //            });
    for (User user : allUsers) {
      if (user.getVisitedLocations().size() < 4) {
        TimeUnit.MILLISECONDS.sleep(100);
        if (user.getVisitedLocations().size() < 4) {
          TimeUnit.MILLISECONDS.sleep(100);
        }
      }
    }

    for (User user : allUsers) {
      if (user.getVisitedLocations().size() < 4) {
        System.out.println("XXXX");
        TimeUnit.MILLISECONDS.sleep(100);
      }
    }
    tourGuideService.awaitTerminationAfterShutdown();

    stopWatch.stop();

    System.out.println(
        "highVolumeTrackLocation: Time Elapsed: "
            + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime())
            + " seconds.");
    Assertions.assertTrue(
        TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
  }

  //  @Disabled
  //  @Test
  //  public void highVolumeGetRewards() {
  //    GpsUtil gpsUtil = new GpsUtil();
  //    //    RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
  //
  //    // Users should be incremented up to 100,000, and test finishes within 20 minutes
  //    InternalTestHelper.setInternalUserNumber(100);
  //    StopWatch stopWatch = new StopWatch();
  //    stopWatch.start();
  //    //    TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
  //
  //    gpsUtil.location.Attraction temp = gpsUtil.getAttractions().get(0);
  //    Attraction attraction =
  //        new Attraction(
  //            temp.attractionName,
  //            temp.city,
  //            temp.state,
  //            temp.attractionId,
  //            temp.latitude,
  //            temp.longitude);
  //    List<User> allUsers = new ArrayList<>();
  //    allUsers = tourGuideService.getAllUsers();
  //    allUsers.forEach(
  //        u ->
  //            u.addToVisitedLocations(
  //                new VisitedLocation(
  //                    u.getUserId(),
  //                    new Location(attraction.longitude(), attraction.latitude()),
  //                    new Date())));
  //
  //    allUsers.forEach(u -> rewardsService.calculateRewards(u));
  //
  //    for (User user : allUsers) {
  //      assertTrue(user.getUserRewards().size() > 0);
  //    }
  //    stopWatch.stop();
  //    tourGuideService.tracker.stopTracking();
  //
  //    System.out.println(
  //        "highVolumeGetRewards: Time Elapsed: "
  //            + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime())
  //            + " seconds.");
  //    assertTrue(
  //        TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
  //  }
}
