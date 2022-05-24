package tourGuide.controller;

import com.jsoniter.output.JsonStream;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tourGuide.dto.AddUserPreferencesDto;
import tourGuide.dto.GetNearbyAttractionDto;
import tourGuide.exception.DataNotFoundException;
import tourGuide.exception.IllegalArgumentException;
import tourGuide.model.Location;
import tourGuide.model.UserReward;
import tourGuide.model.VisitedLocation;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.TripDealsService;
import tourGuide.user.User;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import tourGuide.user.UserPreferences;
import tripPricer.Provider;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static tourGuide.config.Url.*;

@RestController
public class TourGuideController {

  private final Logger logger = LoggerFactory.getLogger(TourGuideController.class);
  @Autowired TourGuideService tourGuideService;
  @Autowired RewardsService rewardsService;
  @Autowired TripDealsService tripDealsService;

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
  @GetMapping(value = GETTRIPDEALS)
  public Collection<Provider> getTripDeals(@RequestParam String userName, @RequestParam UUID attractionId) {

    if (userName == null || userName.isBlank()) {
      logger.warn("error, username is mandatory. username: " + userName);
      throw new IllegalArgumentException("error, username is mandatory.");
    }
    User user = tourGuideService.getUser(userName);

    return tripDealsService.getTripDeals(user,attractionId);
  }

  @PostMapping(value = ADDUSERPREFERENCES)
  @ResponseStatus(HttpStatus.CREATED)
  public void addUserPreferences(@Valid @RequestBody AddUserPreferencesDto userPreferences) {
    tripDealsService.addUserPreferences(userPreferences);
  }
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
