package tourGuide.service;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import tourGuide.dto.AddUserPreferencesDto;
import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.UserReward;
import tourGuide.model.VisitedLocation;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tripPricer.Provider;
import tripPricer.TripPricer;
import tripPricer.TripPricerTask;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TripDealsServiceTest {

  private final UUID userId = UUID.randomUUID();
  private final String username = "usernameTest";
  private final String phoneNumber = "phoneTest";
  private final String email = "emailTest";

  @Mock TourGuideService tourGuideServiceMock;
  @Mock TripPricerTask tripPricerTaskMock;
  @Spy TripPricer tripPricerMock;

  @InjectMocks TripDealsServiceImpl tripDealsService;

  @Test
  void getTripDeals_ShouldReturnAListOf5Provider() {

    // GIVEN
    User user = new User(userId, username, phoneNumber, email);
    user.setUserPreferences(new UserPreferences());
    // WHEN
    lenient()
        .doCallRealMethod()
        .when(tripPricerMock)
        .getPrice(anyString(), any(UUID.class), anyInt(), anyInt(), anyInt(), anyInt());
    List<Provider> tripDeals = tripDealsService.getTripDeals(user);

    // THEN
    assertThat(tripDeals.size()).isEqualTo(5);
  }

  @Test
  void getTripDeals_WhenUserHaveRewardPoints_ShouldCallTripPricerGetPriceWithCorrectArgument() {

    // GIVEN
    User user = new User(userId, username, phoneNumber, email);
    UserPreferences userPreferences = new UserPreferences();
    userPreferences.setNumberOfChildren(15);
    userPreferences.setTripDuration(5);
    user.setUserPreferences(userPreferences);
    UserReward userReward =
        new UserReward(
            userId,
            new VisitedLocation(userId, new Location(12., 5.), new Date()),
            new Attraction("name", "city", "state", UUID.randomUUID(), new Location(12., 5.), 10.1),
            100);
    user.getUserRewards().add(userReward);
    // WHEN
    lenient()
        .doCallRealMethod()
        .when(tripPricerMock)
        .getPrice(anyString(), any(UUID.class), anyInt(), anyInt(), anyInt(), anyInt());
    List<Provider> tripDeals = tripDealsService.getTripDeals(user);

    // THEN
    assertThat(tripDeals.size()).isEqualTo(5);
    verify(tripPricerMock, times(1)).getPrice(null, userId, 1, 15, 5, 100);
  }

  @Test
  void addUserPreferences() {

    // GIVEN
    User user = new User(userId, username, phoneNumber, email);
    AddUserPreferencesDto userPreferencesToAdd =
        new AddUserPreferencesDto(username, 10, 50, 100, 2000, 2, 1, 1);

    UserPreferences expected = new UserPreferences();
    expected.setAttractionProximity(userPreferencesToAdd.attractionProximity());
    expected.setLowerPricePoint(
        Money.of(userPreferencesToAdd.lowerPricePoint(), expected.getCurrency()));
    expected.setHighPricePoint(
        Money.of(userPreferencesToAdd.highPricePoint(), expected.getCurrency()));
    expected.setTripDuration(userPreferencesToAdd.tripDuration());
    expected.setTicketQuantity(userPreferencesToAdd.ticketQuantity());
    expected.setNumberOfAdults(userPreferencesToAdd.numberOfAdults());
    expected.setNumberOfChildren(userPreferencesToAdd.numberOfChildren());

    // WHEN
    when(tourGuideServiceMock.getUser(anyString())).thenReturn(user);
    // THEN
    tripDealsService.addUserPreferences(userPreferencesToAdd);
    assertThat(user.getUserPreferences()).isEqualTo(expected);
  }
}
