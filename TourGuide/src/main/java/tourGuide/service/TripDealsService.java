package tourGuide.service;

import java.util.List;
import java.util.UUID;
import tourGuide.dto.AddUserPreferencesDto;
import tourGuide.model.user.User;
import tripPricer.Provider;

public interface TripDealsService {
  /**
   * This method get a list of providers for the given user based on its user preferences.
   *
   * @param user the user
   * @return the list of providers
   */
  List<Provider> getTripDeals(User user, UUID attractionId);

  /**
   * This method add a UserPreferences to the user with the given username.
   *
   * @param userPreferences the userPreferences to add
   */
  void addUserPreferences(AddUserPreferencesDto userPreferences);
}
