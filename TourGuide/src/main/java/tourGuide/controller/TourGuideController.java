package tourGuide.controller;

import tourGuide.exception.DataNotFoundException;
import tourGuide.exception.IllegalArgumentException;
import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.VisitedLocation;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import static tourGuide.config.Url.GETLOCATION;
import static tourGuide.config.Url.GETNEARBYATTRACTIONS;
import static tourGuide.config.Url.INDEX;

@RestController
public class TourGuideController {

  @Autowired private TourGuideService tourGuideService;
  private final Logger logger = LoggerFactory.getLogger(TourGuideController.class);

  @RequestMapping(value = INDEX)
  public String index() {
    return "Greetings from TourGuide!";
  }

  /**
   * This method get the location of the user.
   *
   * @param userName the username
   * @return the location
   */
  @GetMapping(value = GETLOCATION)
  public Location getLocation(@RequestParam String userName) {

    if (userName == null || userName.isBlank()) {
      logger.warn("error, username is mandatory. username: " + userName);
      throw new IllegalArgumentException("error, username is mandatory.");
    }

    VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));

    return visitedLocation.location();
  }

  //  //  TODO: Change this method to no longer return a List of Attractions.
  //  //  Instead: Get the closest five tourist attractions to the user - no matter how far away
  // they
  //  // are.
  //  //  Return a new JSON object that contains:
  //  // Name of Tourist attraction,
  //  // Tourist attractions lat/long,
  //  // The user's location lat/long,
  //  // The distance in miles between the user's location and each of the attractions.
  //  // The reward points for visiting each Attraction.
  //  //    Note: Attraction reward points can be gathered from RewardsCentral
  @GetMapping(value = GETNEARBYATTRACTIONS)
  public Collection getNearbyAttractions(@RequestParam String userName) {

    if (userName == null || userName.isBlank()) {
      logger.warn("error, username is mandatory. username: " + userName);
      throw new IllegalArgumentException("error, username is mandatory.");
    }

    Collection<Attraction> attractions = tourGuideService.getNearbyAttractions(userName);
    return attractions;
  }
  //
  //  /**
  //   * This method get the reward points owned by the user.
  //   *
  //   * @param userName the user's userName
  //   * @return the reward points as Json
  //   */
  //  @GetMapping(value = GETREWARDS)
  //  public String getRewards(@RequestParam String userName) {
  //    return JsonStream.serialize(tourGuideService.getUserRewards(getUser(userName)));
  //  }
  //
  //  /**
  //   * This method get a list of every user's most recent location as JSON
  //   *
  //   * @return JSON mapping of userId : Locations
  //   */
  //  @GetMapping(value = GETALLCURRENTLOCATIONS)
  //  public String getAllCurrentLocations() {
  //    // TODO: Get a list of every user's most recent location as JSON
  //    // - Note: does not use gpsUtil to query for their current location,
  //    //        but rather gathers the user's current location from their stored location history.
  //    //
  //    // Return object should be the just a  similar to:
  //    //     {
  //    //        "019b04a9-067a-4c76-8817-ee75088c3822":
  // {"longitude":-48.188821,"latitude":74.84371}
  //    //        ...
  //    //     }
  //
  //    return JsonStream.serialize("");
  //  }
  //
  //  /**
  //   * This method get a list of TripDeals (providers) for given user. TripDeals are based on user
  //   * preferences.
  //   *
  //   * @param userName the user's userName
  //   * @return list of providers as JSON
  //   */
  //  @RequestMapping(value = GETTRIPDEALS)
  //  public String getTripDeals(@RequestParam String userName) {
  //    List<Provider> providers = tourGuideService.getTripDeals(getUser(userName));
  //    return JsonStream.serialize(providers);
  //  }

  /**
   * This method get the User object in data storage with its userName.
   *
   * @param userName the user's userName
   * @return the User
   */
  private User getUser(String userName) {

    User user = tourGuideService.getUser(userName);
    if (user == null) {
      logger.warn("Error, user :" + userName + " doesn't exist.");
      throw new DataNotFoundException("Error, user : " + userName + " doesn't exist.");
    }
    return user;
  }
}
