package tourGuide.controller;

import java.util.Collection;
import java.util.UUID;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import tourGuide.dto.AddUserPreferencesDto;
import tourGuide.exception.IllegalArgumentException;
import tourGuide.service.TripDealsService;
import tourGuide.service.UserService;
import tourGuide.user.User;
import tripPricer.Provider;
import static tourGuide.config.Url.ADD_USER_PREFERENCES;
import static tourGuide.config.Url.GET_TRIP_DEALS;

@RestController
public class TripDealsControllerImpl implements TripDealsController {

  private final Logger logger = LoggerFactory.getLogger(TourGuideController.class);
  @Autowired private UserService userService;
  @Autowired private TripDealsService tripDealsService;
  /**
   * This method get a list of TripDeals (providers) for given user. TripDeals are based on user
   * preferences.
   *
   * @param userName the user's userName
   * @param attractionId the attractionId for which we want to get the deals
   * @return list of providers as JSON
   */
  @Override
  @GetMapping(value = GET_TRIP_DEALS)
  public Collection<Provider> getTripDeals(
      @RequestParam String userName, @RequestParam UUID attractionId) {

    if (userName == null || userName.isBlank()) {
      logger.warn("error, username is mandatory. username: " + userName);
      throw new IllegalArgumentException("error, username is mandatory.");
    }
    User user = userService.getUser(userName);

    return tripDealsService.getTripDeals(user, attractionId);
  }

  /**
   * Add UserPreferences to the user.
   *
   * <p>overwrite the previous UserPreferences.
   *
   * @param userPreferences the user preferences
   */
  @Override
  @PostMapping(value = ADD_USER_PREFERENCES)
  @ResponseStatus(HttpStatus.CREATED)
  public void addUserPreferences(
      @Valid @RequestBody AddUserPreferencesDto userPreferences, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      logger.warn("Error, invalid username:" + userPreferences.username());
      throw new IllegalArgumentException("Error, username is mandatory");
    }
    tripDealsService.addUserPreferences(userPreferences);
  }
}
