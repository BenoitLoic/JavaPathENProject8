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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tripPricer.Provider;

import static tourGuide.config.Url.*;

@RestController
public class TourGuideController {

  private final Logger logger = LoggerFactory.getLogger(TourGuideController.class);
  @Autowired TourGuideService tourGuideService;
  @Autowired RewardsService rewardsService;

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
    User user = tourGuideService.getUser(userName);
    return tourGuideService.getUserLocation(user).location();
  }

  @GetMapping(value = GETNEARBYATTRACTIONS)
  public Map<Location, Collection<GetNearbyAttractionDto>> getNearbyAttractions(
      @RequestParam String userName) {

    if (userName == null || userName.isBlank()) {
      logger.warn("error, username is mandatory. username: " + userName);
      throw new IllegalArgumentException("error, username is mandatory.");
    }

    return tourGuideService.getNearbyAttractions(userName);
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

  /**
   * This method get a list of every user's most recent location as JSON
   *
   * @return JSON mapping of userId : Locations
   */
  @GetMapping(value = GETALLCURRENTLOCATIONS)
  public Map<UUID, Location> getAllCurrentLocations() {

    return tourGuideService.getAllCurrentLocations();
  }

  /**
   * This method get a list of TripDeals (providers) for given user. TripDeals are based on user
   * preferences.
   *
   * @param userName the user's userName
   * @return list of providers as JSON
   */
  @RequestMapping(value = GETTRIPDEALS)
  public Collection<Provider> getTripDeals(@RequestParam String userName) {

    if (userName == null || userName.isBlank()) {
      logger.warn("error, username is mandatory. username: " + userName);
      throw new IllegalArgumentException("error, username is mandatory.");
    }
    User user = tourGuideService.getUser(userName);

    return tourGuideService.getTripDeals(user);
  }

}
