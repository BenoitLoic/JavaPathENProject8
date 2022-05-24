package tourGuide.service;

import tourGuide.user.User;
import tripPricer.Provider;

import java.util.List;

public interface TripDealsService {
    /**
     * This method get a list of providers for the given user based on its user preferences.
     *
     * @param user the user
     * @return the list of providers
     */
    List<Provider> getTripDeals(User user);
}
