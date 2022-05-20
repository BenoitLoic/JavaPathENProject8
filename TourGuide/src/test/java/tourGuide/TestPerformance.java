package tourGuide;

import gpsUtil.GpsUtil;
import org.junit.jupiter.api.*;
import org.springframework.test.context.ActiveProfiles;
import tourGuide.client.LocationClient;
import tourGuide.client.UserClient;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.VisitedLocation;
import tourGuide.service.RewardsServiceImpl;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@EnableConfigurationProperties
@ExtendWith(SpringExtension.class)
public class TestPerformance {

  @Autowired LocationClient locationClient;
  @Autowired UserClient userClient;
  //  @Autowired TourGuideService tourGuideService;
  @Autowired RewardsServiceImpl rewardsService;

  @BeforeAll
  static void beforeAll() {
    InternalTestHelper.setInternalUserNumber(50);
    java.util.Locale.setDefault(java.util.Locale.US);
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
  @Disabled
  @Test
  public void highVolumeTrackLocation() {

    // Users should be incremented up to 100,000, and test finishes within 15 minutes
    TourGuideService tourGuideService =
        new TourGuideService(locationClient, userClient, rewardsService);

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    CopyOnWriteArrayList<User> allUsers = tourGuideService.getAllUsers();
    for (User user : allUsers) {

      tourGuideService.trackUserLocation(user);
    }

    tourGuideService.awaitTerminationAfterShutdown();

    for (User user : allUsers) {
      assertTrue(user.getVisitedLocations().size() > 3);
    }
    stopWatch.stop();

    System.out.println(
        "highVolumeTrackLocation: Time Elapsed: "
            + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime())
            + " seconds.");
    assertTrue(
        TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
  }

  @Disabled
  @Test
  public void highVolumeGetRewards() {
    GpsUtil gpsUtil = new GpsUtil();
    TourGuideService tourGuideService =
        new TourGuideService(locationClient, userClient, rewardsService);
    //        RewardsService rewardsService = new RewardsService();

    // Users should be incremented up to 100,000, and test finishes within 20 minutes

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    //    TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

    gpsUtil.location.Attraction temp = gpsUtil.getAttractions().get(0);
    Attraction attraction =
        new Attraction(
            temp.attractionName,
            temp.city,
            temp.state,
            temp.attractionId,
            new Location(temp.latitude, temp.longitude),
            null);
    List<User> allUsers = tourGuideService.getAllUsers();
    allUsers.forEach(
        u ->
            u.addToVisitedLocations(
                new VisitedLocation(
                    u.getUserId(),
                    new Location(
                        attraction.location().longitude(), attraction.location().latitude()),
                    new Date())));

    allUsers.forEach(u -> rewardsService.getRewards(u));

    for (User user : allUsers) {
      assertTrue(user.getUserRewards().size() > 0);
    }
    stopWatch.stop();
    //    tourGuideService.tracker.stopTracking();

    System.out.println(
        "highVolumeGetRewards: Time Elapsed: "
            + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime())
            + " seconds.");
    assertTrue(
        TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
  }
}
