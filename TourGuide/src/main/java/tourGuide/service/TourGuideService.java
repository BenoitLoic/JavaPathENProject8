package tourGuide.service;

import feign.Feign;
import feign.FeignException;
import tourGuide.client.LocationClient;
import tourGuide.client.UserClient;
import tourGuide.dto.GetNearbyAttractionDto;
import tourGuide.exception.DataNotFoundException;
import tourGuide.exception.ResourceNotFoundException;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.UserReward;
import tourGuide.model.VisitedLocation;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tripPricer.Provider;
import tripPricer.TripPricer;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("DMI_RANDOM_USED_ONLY_ONCE")
@Service
public class TourGuideService {
  public Tracker tracker;

  @Value("${tripPricer.apiKey}")
  private static String tripPricerApiKey;

  protected final Logger logger = LoggerFactory.getLogger(TourGuideService.class);
  protected final TripPricer tripPricer = new TripPricer();
  private final ExecutorService executorService = Executors.newFixedThreadPool(100);

  @Value("${tourGuide.testMode}")
  boolean testMode;

  private final LocationClient locationClient;
  private final UserClient userClient;
  private final RewardsService rewardsService;

  public TourGuideService(
      LocationClient locationClient, UserClient userClient, RewardsService rewardsService) {
    this.locationClient = locationClient;
    this.userClient = userClient;
    this.rewardsService = rewardsService;

    logger.info("TestMode enabled");
    logger.debug("Initializing users");
    initializeInternalUsers();
    logger.debug("Finished initializing users");
  }

  @PostConstruct
  void testModeInit() {
    if (testMode) {
      tracker = new Tracker(this);
      addShutDownHook();
    }
  }
  /**
   * This method return the list of rewards owned by the given user.
   *
   * @param user the user
   * @return a list of rewards
   */
  public List<UserReward> getUserRewards(User user) {
    return user.getUserRewards();
  }

  /**
   * This method get the last location where the given user as gone, or the actual location if the
   * list is empty.
   *
   * @param user the user
   * @return the last visited location if exists, or the actual location
   */
  public VisitedLocation getUserLocation(User user) {

    user.addToVisitedLocations(locationClient.getLocation(user.getUserId()));

    return user.getLastVisitedLocation();
  }

  /**
   * INTERNAL TEST METHOD this method get the user with the given username.
   *
   * @param userName the username
   * @return the user
   */
  public User getUser(String userName) {
    User user = internalUserMap.get(userName);
    if (user == null) {
      logger.warn("Error, username : " + userName + " doesn't exist.");
      throw new DataNotFoundException("error user doesn't exist.");
    }
    return user;
  }

  /**
   * INTERNAL TEST METHOD this method get the list of all users.
   *
   * @return the list of users
   */
  public CopyOnWriteArrayList<User> getAllUsers() {
    return new CopyOnWriteArrayList<>(internalUserMap.values());
  }

  /**
   * INTERNAL TEST METHOD This method add a new user to the database.
   *
   * @param user the user to add
   */
  public void addUser(User user) {
    if (!internalUserMap.containsKey(user.getUserName())) {
      internalUserMap.put(user.getUserName(), user);
    }
  }

  /**
   * This method get a list of providers for the given user based on its user preferences.
   *
   * @param user the user
   * @return the list of providers
   */
  public List<Provider> getTripDeals(User user) {

    int cumulativeRewardPoints =
        user.getUserRewards().stream().mapToInt(UserReward::rewardPoints).sum();

    List<Provider> providers =
        tripPricer.getPrice(
            tripPricerApiKey,
            user.getUserId(),
            user.getUserPreferences().getNumberOfAdults(),
            user.getUserPreferences().getNumberOfChildren(),
            user.getUserPreferences().getTripDuration(),
            cumulativeRewardPoints);

    user.setTripDeals(providers);

    return providers;
  }

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
  /**
   * This method add the actual location as visitedLocation for the user.
   *
   * @param user the user
   */
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
  public Map<Location, Collection<GetNearbyAttractionDto>> getNearbyAttractions(String userName) {

    //  Instead: Get the closest five tourist attractions to the user - no matter how far away they
    // are.
    //  Return a new JSON object that contains:
    // Name of Tourist attraction,
    // Tourist attractions lat/long,
    // The user's location lat/long,
    // The distance in miles between the user's location and each of the attractions.
    // The reward points for visiting each Attraction.
    //    Note: Attraction reward points can be gathered from RewardsCentral
    try {

      User user = userClient.getUserByUsername(userName);

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
      logger.error("Error, Feign client failed." + fce);
      throw new ResourceNotFoundException("Error, cant reach service.");
    }
  }

  /**
   * This method call location-service to retrieve the last known location of all user.
   *
   * @return a map with the user id : last location saved
   */
  public Map<UUID, Location> getAllCurrentLocations() {
    try {
      return locationClient.getAllLastLocation();
    } catch (feign.FeignException fre) {
      logger.error("Error, fail to connect to location client.");
      throw new ResourceNotFoundException("Error, fail to connect to client.");
    }
  }

  private void addShutDownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> tracker.stopTracking()));
  }

  /**********************************************************************************
   *
   * Methods Below: For Internal Testing
   *
   **********************************************************************************/

  // Database connection will be used for external users, but for testing purposes internal users
  // are provided and stored in memory
  protected final Map<String, User> internalUserMap = new ConcurrentHashMap<>();

  protected void initializeInternalUsers() {
    IntStream.range(0, InternalTestHelper.getInternalUserNumber())
        .forEach(
            i -> {
              String userName = "internalUser" + i;
              String phone = "000";
              String email = userName + "@tourGuide.com";
              String userNumber = String.format("%06d", i);
              UUID userId = UUID.fromString("0000-00-00-00-" + userNumber);

              User user = new User(userId, userName, phone, email);
              generateUserLocationHistory(user);

              internalUserMap.put(userName, user);
            });
    logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
  }

  private void generateUserLocationHistory(User user) {
    IntStream.range(0, 3)
        .forEach(
            i ->
                user.addToVisitedLocations(
                    new VisitedLocation(
                        user.getUserId(),
                        new Location(generateRandomLatitude(), generateRandomLongitude()),
                        getRandomTime())));
  }

  private double generateRandomLongitude() {
    double leftLimit = -180;
    double rightLimit = 180;
    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
  }

  private double generateRandomLatitude() {
    double leftLimit = -85.05112878;
    double rightLimit = 85.05112878;
    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
  }

  private Date getRandomTime() {
    LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
    return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
  }
}
