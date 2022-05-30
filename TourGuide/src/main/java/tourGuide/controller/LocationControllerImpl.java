package tourGuide.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tourGuide.dto.GetNearbyAttractionDto;
import tourGuide.exception.IllegalArgumentException;
import tourGuide.model.Location;
import tourGuide.service.LocationService;
import tourGuide.service.UserService;
import tourGuide.user.User;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static tourGuide.config.Url.*;

@RestController
public class LocationControllerImpl implements LocationController {

  private final Logger logger = LoggerFactory.getLogger(LocationControllerImpl.class);
  @Autowired private LocationService locationService;
  @Autowired private UserService userService;

  /**
   * Get the location of the user.
   *
   * @param userName the user's username
   * @return the location
   */
  @Override
  @GetMapping(value = GET_LOCATION)
  public Location getLocation(@RequestParam String userName) {

    if (userName == null || userName.isBlank()) {
      logger.warn("error, username is mandatory. username: " + userName);
      throw new IllegalArgumentException("error, username is mandatory.");
    }
    User user = userService.getUser(userName);
    return locationService.getUserLocation(user).location();
  }

  /**
   * Get the closest attractions based on the user location.
   *
   * @param userName the user's username
   * @return a map with the user location and the list of attraction ordered by distance
   */
  @Override
  @GetMapping(value = GET_NEARBY_ATTRACTIONS)
  public Map<Location, Collection<GetNearbyAttractionDto>> getNearbyAttractions(
      @RequestParam String userName) {

    if (userName == null || userName.isBlank()) {
      logger.warn("error, username is mandatory. username: " + userName);
      throw new IllegalArgumentException("error, username is mandatory.");
    }

    return locationService.getNearbyAttractions(userName);
  }
  /**
   * This method get a list of every user's most recent location as JSON
   *
   * @return JSON mapping of userId : Locations
   */
  @Override
  @GetMapping(value = GET_ALL_CURRENT_LOCATIONS)
  public Map<UUID, Location> getAllCurrentLocations() {

    return locationService.getAllCurrentLocations();
  }
}
