package tourGuide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import tourGuide.client.LocationClient;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.VisitedLocation;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {
  public final    Tracker        tracker;


  boolean testMode = true;
  @Autowired
  private LocationClient locationClient;

  public TourGuideService( ) {



    if (testMode) {
      logger.info("TestMode enabled");
      logger.debug("Initializing users");
      initializeInternalUsers();
      logger.debug("Finished initializing users");
    }
    tracker = new Tracker(this);
    addShutDownHook();

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
   * This method get the last location where the given user as gone, or the actual
   * location if the list is empty.
   *
   * @param user the user
   * @return the last visited location if exists, or the actual location
   */
  public VisitedLocation getUserLocation(User user) {
    VisitedLocation visitedLocation =
        trackUserLocation(user);
    return visitedLocation;
  }

  /**
   * INTERNAL TEST METHOD
   * this method get the user with the given username.
   *
   * @param userName the username
   * @return the user
   */
  public User getUser(String userName) {

    return internalUserMap.get(userName);
  }

  /**
   * INTERNAL TEST METHOD
   * this method get the list of all users.
   *
   * @return the list of users
   */
  public List<User> getAllUsers() {
    return internalUserMap.values().stream().collect(Collectors.toList());
  }

  /**
   * INTERNAL TEST METHOD
   * This method add a new user to the database.
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

    int cumulativeRewardPoints = user.getUserRewards().stream().mapToInt(
        i -> i.getRewardPoints()).sum();

    List<Provider> providers = tripPricer.getPrice(
        tripPricerApiKey,
        user.getUserId(),
        user.getUserPreferences().getNumberOfAdults(),
        user.getUserPreferences().getNumberOfChildren(),
        user.getUserPreferences().getTripDuration(),
        cumulativeRewardPoints);

    user.setTripDeals(providers);

    return providers;
  }

  /**
   * This method add the actual location as visitedLocation for the user.
   *
   * @param user the user
   * @return the location
   */
  public VisitedLocation trackUserLocation(User user) {

    VisitedLocation visitedLocation = locationClient.addLocation(user.getUserId());
    user.addToVisitedLocations(visitedLocation);
//    rewardsService.calculateRewards(user);
    return visitedLocation;
  }

//  /**
//   * This method find all attractions in range.
//   *
//   * @param visitedLocation the location
//   * @return a list of all attraction in range
//   */
//  public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
//
//    List<Attraction> nearbyAttractions = new ArrayList<>();
//
//    for (Attraction attraction : gpsUtil.getAttractions()) {
//      if (rewardsService.isWithinAttractionProximity(attraction, visitedLocation.location)) {
//        nearbyAttractions.add(attraction);
//      }
//    }
//
//    return nearbyAttractions;
//  }

  private void addShutDownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        tracker.stopTracking();
      }
    });
  }

  /**********************************************************************************
   *
   * Methods Below: For Internal Testing
   *
   **********************************************************************************/
  private static final String tripPricerApiKey = "test-server-api-key";
  protected final      Logger logger           = LoggerFactory.getLogger(
      TourGuideService.class);

  protected final TripPricer        tripPricer      = new TripPricer();
  // Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
  protected final Map<String, User> internalUserMap = new HashMap<>();

  protected void initializeInternalUsers() {
    IntStream.range(0,
        InternalTestHelper.getInternalUserNumber()).forEach(i -> {
      String userName = "internalUser" + i;
      String phone    = "000";
      String email    = userName + "@tourGuide.com";
      String userNumber = String.format("%06d", i);
      UUID userId = UUID.fromString("0000-00-00-00-" + userNumber);

      User user = new User(userId, userName,
          phone, email);
      generateUserLocationHistory(user);

      internalUserMap.put(userName, user);
    });
    logger.debug(
        "Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
  }

  private void generateUserLocationHistory(User user) {
    IntStream.range(0, 3).forEach(i -> {
      user.addToVisitedLocations(new VisitedLocation(user.getUserId(),
          new Location(generateRandomLatitude(), generateRandomLongitude()),
          getRandomTime()));
    });
  }

  private double generateRandomLongitude() {
    double leftLimit  = -180;
    double rightLimit = 180;
    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
  }

  private double generateRandomLatitude() {
    double leftLimit  = -85.05112878;
    double rightLimit = 85.05112878;
    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
  }

  private Date getRandomTime() {
    LocalDateTime localDateTime = LocalDateTime.now().minusDays(
        new Random().nextInt(30));
    return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
  }

}
