package tourGuide.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tourGuide.client.LocationClient;
import tourGuide.dto.GetNearbyAttractionDto;
import tourGuide.exception.DataNotFoundException;
import tourGuide.exception.ResourceNotFoundException;
import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.VisitedLocation;
import tourGuide.tracker.Tracker;
import tourGuide.model.user.User;

@Service
public class LocationServiceImpl implements LocationService {

  private final Logger logger = LoggerFactory.getLogger(LocationServiceImpl.class);
  private final ExecutorService executorService = Executors.newFixedThreadPool(200);
  public Tracker tracker;
  @Value("${tourGuide.testMode}")
  boolean testMode;
  private final LocationClient locationClient;
  private final RewardsService rewardsService;
  private final UserService userService;

  public LocationServiceImpl(LocationClient locationClient, RewardsService rewardsService, UserService userService) {
    this.locationClient = locationClient;
    this.rewardsService = rewardsService;
    this.userService = userService;
  }

  @PostConstruct
  void testModeInit() {
    if (!testMode) {
      logger.info("TestMode enabled");
      tracker = new Tracker(this, rewardsService, userService);
      addShutDownHook();
    }
  }

  /**
   * This method get the last location where the given user as gone, or the actual location if the
   * list is empty.
   *
   * @param user the user
   * @return the last visited location if exists, or the actual location
   */
  @Override
  public VisitedLocation getUserLocation(User user) {
    user.addToVisitedLocations(locationClient.getLocation(user.getUserId()));
    return user.getLastVisitedLocation();
  }

  /**
   * This method add the actual location as visitedLocation for the user.
   *
   * @param user the user
   */
  @Override
  public void trackUserLocation(User user) {

    CompletableFuture.supplyAsync(
            () -> locationClient.getLocation(user.getUserId()), executorService)
        .thenAccept(
            visitedLocation -> {
              user.addToVisitedLocations(visitedLocation);
              user.setLatestLocationTimestamp(visitedLocation.timeVisited());
            });
  }

  /**
   * This method find all attractions in range.
   *
   * @param userName the username
   * @return a list with the 5 closest attraction in range
   */
  @Override
  public Map<Location, Collection<GetNearbyAttractionDto>> getNearbyAttractions(String userName) {

    try {

      User user = userService.getUser(userName);

      if (user == null || user.getUserId() == null) {
        logger.warn("Error, user :" + userName + " doesn't exist.");
        throw new DataNotFoundException("Error, user : " + userName + " doesn't exist.");
      }

      UUID userId = user.getUserId();
      VisitedLocation visitedLocation = locationClient.getLocation(userId);

      if (visitedLocation == null || visitedLocation.location() == null) {
        logger.warn(
            "Error, locationClient.addLocation returned null for user : "
                + userName
                + " user id :"
                + userId);
        throw new DataNotFoundException("Error, can't get Location for user : " + userId);
      }

      Collection<Attraction> nearbyAttractions =
          locationClient.getNearbyAttractions(
              visitedLocation.location().latitude(), visitedLocation.location().longitude());
      Collection<GetNearbyAttractionDto> dtoCollection =
          rewardsService.calculateRewardsPoints(nearbyAttractions, userId);
      Map<Location, Collection<GetNearbyAttractionDto>> response = new HashMap<>(1);
      response.put(visitedLocation.location(), dtoCollection);
      return response;
    } catch (feign.FeignException fce) {
      logger.error("Error, Feign client failed. " + fce);
      throw new ResourceNotFoundException("Error, cant reach service.");
    }
  }

  /**
   * This method call location-service to retrieve the last known location of all user.
   *
   * @return a map with the user id : last location saved
   */
  @Override
  public Map<UUID, Location> getAllCurrentLocations() {
    try {
      return locationClient.getAllLastLocation();
    } catch (feign.FeignException fre) {
      logger.error("Error, fail to connect to location client.");
      throw new ResourceNotFoundException("Error, fail to connect to client.");
    }
  }

  /**
   * This method wait for the active threads from executorService to end gracefully or else shutdown
   * after the timeout
   */
  @Override
  public void awaitTerminationAfterShutdown() {
    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(20, TimeUnit.MINUTES)) {
        executorService.shutdownNow();
      }
    } catch (InterruptedException ex) {
      executorService.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  private void addShutDownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      tracker.stopTracking();
      this.awaitTerminationAfterShutdown();
    }));
  }
}
