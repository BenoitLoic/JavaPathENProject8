package tourGuide.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tourGuide.model.UserReward;
import tourGuide.user.User;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.util.List;

@Service
public class TripDealsServiceImpl implements TripDealsService {

  @Value("${tripPricer.apiKey}")
  private String tripPricerApiKey;

 private final TripPricer tripPricer;

  public TripDealsServiceImpl(TripPricer tripPricer) {
    this.tripPricer = tripPricer;
  }

  /**
   * This method get a list of providers for the given user based on its user preferences.
   *
   * @param user the user
   * @return the list of providers
   */
  @Override
  public List<Provider> getTripDeals(User user) {

    int cumulativeRewardPoints =
        user.getUserRewards().stream().mapToInt(UserReward::rewardPoints).sum();

    List<Provider> providers = tripPricer.getPrice(
            tripPricerApiKey,
            user.getUserId(),
            user.getUserPreferences().getNumberOfAdults(),
            user.getUserPreferences().getNumberOfChildren(),
            user.getUserPreferences().getTripDuration(),
            cumulativeRewardPoints);

    return providers;
  }
}
