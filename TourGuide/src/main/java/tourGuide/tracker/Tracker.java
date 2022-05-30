package tourGuide.tracker;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tourGuide.service.LocationService;
import tourGuide.service.RewardsService;
import tourGuide.service.UserService;
import tourGuide.user.User;

public class Tracker extends Thread {
  private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(5);
  private final Logger logger = LoggerFactory.getLogger(Tracker.class);
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();
  private final LocationService locationService;
  private final RewardsService rewardsService;
  private final UserService userService;
  private boolean stop = false;

  public Tracker(
      LocationService locationService, RewardsService rewardsService, UserService userService) {
    this.locationService = locationService;
    this.rewardsService = rewardsService;
    this.userService = userService;
    executorService.submit(this);
  }

  /** Assures to shut down the Tracker thread */
  public void stopTracking() {
    stop = true;
    executorService.shutdownNow();
  }

  @Override
  public void run() {
    StopWatch stopWatch = new StopWatch();
    while (true) {
      if (Thread.currentThread().isInterrupted() || stop) {
        logger.debug("Tracker stopping");
        break;
      }

      List<User> users = userService.getAllUsers();

      logger.debug("Begin Tracker. Tracking " + users.size() + " users.");

      stopWatch.start();
      users.stream()
          .parallel()
          .forEach(
              user -> {
                try {
                  locationService.trackUserLocation(user);
                  rewardsService.addRewardsForLastLocation(user);
                } catch (Exception e) {
                  throw new RuntimeException(e);
                }
              });
      stopWatch.stop();

      logger.debug(
          "Tracker Time Elapsed: "
              + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime())
              + " seconds.");

      stopWatch.reset();
      try {
        logger.debug("Tracker sleeping");
        TimeUnit.SECONDS.sleep(trackingPollingInterval);
      } catch (InterruptedException e) {
        break;
      }
    }
  }
}
