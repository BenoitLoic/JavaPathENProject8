package tourGuide.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
public class TripDealsServiceTest {

  private final UUID userId = UUID.randomUUID();
  private final String username = "usernameTest";
  private final String phoneNumber = "phoneTest";
  private final String email = "emailTest";

  private final TripDealsService tripDealsService = new TripDealsServiceImpl(new TripPricer());

  @Test
  void getTripDeals_ShouldReturnAListOf5Provider() {

    // GIVEN
    User user = new User(userId, username, phoneNumber, email);
    user.setUserPreferences(new UserPreferences());
    // WHEN

    List<Provider> tripDeals = tripDealsService.getTripDeals(user);

    // THEN
    assertThat(tripDeals.size()).isEqualTo(5);
  }
}
