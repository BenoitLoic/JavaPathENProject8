package tourGuide.controller;

import java.util.Collection;
import java.util.UUID;
import org.springframework.validation.BindingResult;
import tourGuide.dto.AddUserPreferencesDto;
import tripPricer.Provider;

/** Rest controller for TripDeals. */
public interface TripDealsController {

  /**
   * This method get a list of TripDeals (providers) for given user. TripDeals are based on user
   * preferences.
   *
   * @param userName the user's userName
   * @param attractionId the attractionId for which we want to get the deals
   * @return list of providers as JSON
   */
  Collection<Provider> getTripDeals(String userName, UUID attractionId);

  /**
   * Add UserPreferences to the user.
   *
   * <p>overwrite the previous UserPreferences.
   *
   * @param userPreferences the user preferences
   */
  void addUserPreferences(AddUserPreferencesDto userPreferences, BindingResult bindingResult);
}
