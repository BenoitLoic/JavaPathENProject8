import gpsUtil.GpsUtil;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tourGuide.Application;
import tourGuide.client.LocationClient;
import tourGuide.client.UserClient;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.Location;
import tourGuide.model.VisitedLocation;
import tourGuide.service.LocationService;
import tourGuide.service.RewardsServiceImpl;
import tourGuide.service.UserService;
import tourGuide.model.user.User;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
@EnableConfigurationProperties
@ExtendWith(SpringExtension.class)
public class TestPerformance {

  @Autowired LocationClient locationClient;
  @Autowired UserClient userClient;
  @Autowired RewardsServiceImpl rewardsService;
  @Autowired LocationService locationService;
  @Autowired UserService userService;

  @BeforeAll
  static void beforeAll() {
    InternalTestHelper.setInternalUserNumber(10000);
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

  @Test
  public void highVolumeTrackLocation() {

    // Users should be incremented up to 100,000, and test finishes within 15 minutes

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    CopyOnWriteArrayList<User> allUsers = userService.getAllUsers();
    System.out.println(allUsers.size());
    for (User user : allUsers) {
      locationService.trackUserLocation(user);
    }

    locationService.awaitTerminationAfterShutdown();

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

  @Test
  public void highVolumeGetRewards() {
    GpsUtil gpsUtil = new GpsUtil();

    // Users should be incremented up to 100,000, and test finishes within 20 minutes

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    gpsUtil.location.Attraction temp = gpsUtil.getAttractions().get(0);

    List<User> allUsers = userService.getAllUsers();

    allUsers.forEach(User::clearVisitedLocations);
    allUsers.forEach(
        u ->
            u.addToVisitedLocations(
                new VisitedLocation(
                    u.getUserId(), new Location(temp.longitude, temp.latitude), new Date())));

    allUsers.forEach(u -> rewardsService.addRewards(u));

    rewardsService.awaitTerminationAfterShutdown();

    for (User user : allUsers) {
      assertTrue(user.getUserRewards().size() > 0);
    }

    stopWatch.stop();

    System.out.println(
        "highVolumeGetRewards: Time Elapsed: "
            + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime())
            + " seconds.");
    assertTrue(
        TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
  }
}
