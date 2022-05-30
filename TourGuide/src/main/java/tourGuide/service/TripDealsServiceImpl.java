package tourGuide.service;

import org.javamoney.moneta.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tourGuide.dto.AddUserPreferencesDto;
import tourGuide.model.UserReward;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.util.List;
import java.util.UUID;

@Service
public class TripDealsServiceImpl implements TripDealsService {

  @Value("${tripPricer.apiKey}")
  private String tripPricerApiKey;

  protected final Logger logger = LoggerFactory.getLogger(TripDealsServiceImpl.class);
  private final TripPricer tripPricer;
  private final TourGuideService tourGuideService;

  public TripDealsServiceImpl(TripPricer tripPricer, TourGuideService tourGuideService) {
    this.tripPricer = tripPricer;
    this.tourGuideService = tourGuideService;
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

    User user = tourGuideService.getUser(userPreferencesDto.username());

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
