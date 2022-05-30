package tourGuide.controller;

import org.springframework.web.bind.annotation.*;
import tourGuide.dto.GetNearbyAttractionDto;
import tourGuide.exception.DataNotFoundException;
import tourGuide.exception.IllegalArgumentException;
import tourGuide.model.Location;
import tourGuide.model.UserReward;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.TripDealsService;
import tourGuide.user.User;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
  @GetMapping(value = GET_LOCATION)
  public Location getLocation(@RequestParam String userName) {

    if (userName == null || userName.isBlank()) {
      logger.warn("error, username is mandatory. username: " + userName);
      throw new IllegalArgumentException("error, username is mandatory.");
    }
    User user = tourGuideService.getUser(userName);
    return tourGuideService.getUserLocation(user).location();
  }

  @GetMapping(value = GET_NEARBY_ATTRACTIONS)
  public Map<Location, Collection<GetNearbyAttractionDto>> getNearbyAttractions(
      @RequestParam String userName) {

    if (userName == null || userName.isBlank()) {
      logger.warn("error, username is mandatory. username: " + userName);
      throw new IllegalArgumentException("error, username is mandatory.");
    }

    return tourGuideService.getNearbyAttractions(userName);
  }

  /**
   * This method get a list of every user's most recent location as JSON
   *
   * @return JSON mapping of userId : Locations
   */
  @GetMapping(value = GET_ALL_CURRENT_LOCATIONS)
  public Map<UUID, Location> getAllCurrentLocations() {

    return tourGuideService.getAllCurrentLocations();
  }

  /**
   * This method get the User object in data storage with its userName.
   *
   * @param userName the user's userName
   * @return the User
   */
  @GetMapping("/getUser")
  private User getUser(@RequestParam String userName) {

    return tourGuideService.getUser(userName);
  }

}
