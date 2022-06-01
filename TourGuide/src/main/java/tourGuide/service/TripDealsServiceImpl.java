package tourGuide.service;

import java.util.List;
import java.util.UUID;
import org.javamoney.moneta.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tourGuide.dto.AddUserPreferencesDto;
import tourGuide.model.UserReward;
import tourGuide.model.user.User;
import tourGuide.model.user.UserPreferences;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TripDealsServiceImpl implements TripDealsService {

  protected final Logger logger = LoggerFactory.getLogger(TripDealsServiceImpl.class);
  private final TripPricer tripPricer;
  private final UserService userService;
  @Value("${tripPricer.apiKey}")
  private String tripPricerApiKey;

  public TripDealsServiceImpl(TripPricer tripPricer, UserService userService) {
    this.tripPricer = tripPricer;
    this.userService = userService;
  }

  /**
   * This method get a list of providers for the given user based on its user preferences.
   *
   * @param user the user
   * @return the list of providers
   */
  @Override
  public List<Provider> getTripDeals(User user, UUID attractionId) {

    int cumulativeRewardPoints =
        user.getUserRewards().stream().mapToInt(UserReward::rewardPoints).sum();

    logger.debug("getting trip deals for:" + user.getUserName());

    return tripPricer.getPrice(
        tripPricerApiKey,
        attractionId,
        user.getUserPreferences().getNumberOfAdults(),
        user.getUserPreferences().getNumberOfChildren(),
        user.getUserPreferences().getTripDuration(),
        cumulativeRewardPoints);
  }

  /**
   * This method add a UserPreferences to the user with the given username.
   *
   * @param userPreferencesDto the userPreferences to add
   */
  @Override
  public void addUserPreferences(AddUserPreferencesDto userPreferencesDto) {

    User user = userService.getUser(userPreferencesDto.username());

    UserPreferences userPreferences = new UserPreferences();

    userPreferences.setAttractionProximity(userPreferencesDto.attractionProximity());
    userPreferences.setLowerPricePoint(
        Money.of(userPreferencesDto.lowerPricePoint(), userPreferences.getCurrency()));
    userPreferences.setHighPricePoint(
        Money.of(userPreferencesDto.highPricePoint(), userPreferences.getCurrency()));
    userPreferences.setTripDuration(userPreferencesDto.tripDuration());
    userPreferences.setTicketQuantity(userPreferencesDto.ticketQuantity());
    userPreferences.setNumberOfAdults(userPreferencesDto.numberOfAdults());
    userPreferences.setNumberOfChildren(userPreferencesDto.numberOfChildren());

    user.setUserPreferences(userPreferences);
  }
}
