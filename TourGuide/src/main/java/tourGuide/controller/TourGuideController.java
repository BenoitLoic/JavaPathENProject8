package tourGuide.controller;

import com.jsoniter.output.JsonStream;
import tourGuide.dto.GetNearbyAttractionDto;
import tourGuide.exception.DataNotFoundException;
import tourGuide.exception.IllegalArgumentException;
import tourGuide.model.Location;
import tourGuide.model.UserReward;
import tourGuide.model.VisitedLocation;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import java.util.Collection;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static tourGuide.config.Url.*;

@RestController
public class TourGuideController {

  private final Logger logger = LoggerFactory.getLogger(TourGuideController.class);
  @Autowired  TourGuideService tourGuideService;
  @Autowired  RewardsService rewardsService;

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
  
  @GetMapping(value = GETNEARBYATTRACTIONS)
  public Map<Location, Collection<GetNearbyAttractionDto>> getNearbyAttractions(
      @RequestParam String userName) {

    if (userName == null || userName.isBlank()) {
      logger.warn("error, username is mandatory. username: " + userName);
      throw new IllegalArgumentException("error, username is mandatory.");
    }

    Map<Location, Collection<GetNearbyAttractionDto>> attractions =
        tourGuideService.getNearbyAttractions(userName);
    return attractions;
  }

  /**
   * This method get the reward points owned by the user.
   *
   * @param userName the user's userName
   * @return the reward points as Json
   */
  @GetMapping(value = GETREWARDS)
  public Collection<UserReward> getRewards(@RequestParam String userName) {

    if (userName == null || userName.isBlank()) {
      logger.warn("error, username is mandatory. username: " + userName);
      throw new IllegalArgumentException("error, username is mandatory.");
    }

    User user = tourGuideService.getUser(userName);

    return rewardsService.getRewards(user);
  }

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
  @GetMapping("/getUser")
  private User getUser(@RequestParam String userName) {

    User user = tourGuideService.getUser(userName);
    if (user == null) {
      logger.warn("Error, user :" + userName + " doesn't exist.");
      throw new DataNotFoundException("Error, user : " + userName + " doesn't exist.");
    }
    return user;
  }
}
