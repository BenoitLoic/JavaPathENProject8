package tourGuide.service;

import feign.FeignException;
import tourGuide.client.LocationClient;
import tourGuide.client.UserClient;
import tourGuide.dto.GetNearbyAttractionDto;
import tourGuide.exception.DataNotFoundException;
import tourGuide.exception.ResourceNotFoundException;
import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.VisitedLocation;
import tourGuide.user.User;

import java.util.*;
import java.util.concurrent.ExecutionException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import tourGuide.user.UserPreferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TourGuideServiceTest {

  private final UUID userId = UUID.randomUUID();
  private VisitedLocation visitedLocationTest;
  private User userTest;
  @Mock LocationClient locationClientMock;
  @Mock UserClient userClientMock;
  @Mock RewardsServiceImpl rewardsServiceMock;
  @InjectMocks TourGuideService tourGuideService;

  @BeforeEach
  void setUp() {
    userTest = new User(userId, "username", "phone", "email");
    Date dateTest = new Date();
    visitedLocationTest = new VisitedLocation(userId, new Location(50., 20.), dateTest);
    tourGuideService.testMode = false;
  }

  // reçois un user -> renvoi une VisitedLocation
  // appel LocationClient
  @Test
  void getUserLocation() {

    // GIVEN

    // WHEN
    Mockito.when(locationClientMock.getLocation(userId)).thenReturn(visitedLocationTest);
    // THEN
    VisitedLocation actual = tourGuideService.getUserLocation(userTest);
    Assertions.assertThat(actual).isEqualTo(visitedLocationTest);
    verify(locationClientMock, times(1)).getLocation(userId);
  }
  // appel locationClient pour récupérer la liste des 5 attractions les plus proches
  // locationClient renvoi une liste de 5 Attractions avec les RewardPoints en champ additionnel
  @Test
  void getNearbyAttractions_VerifyClientCall() {

    // GIVEN
    UUID attractionId1 = UUID.randomUUID();
    Attraction attraction1 =
        new Attraction(
            "attractionNameTest1",
            "cityTest",
            "stateTest",
            attractionId1,
            new Location(120d, 50d),
            5d);

    // WHEN
    Mockito.when(userClientMock.getUserByUsername(Mockito.anyString())).thenReturn(userTest);

    when(locationClientMock.getLocation(Mockito.any(UUID.class))).thenReturn(visitedLocationTest);

    when(locationClientMock.getNearbyAttractions(anyDouble(), anyDouble()))
        .thenReturn(Arrays.asList(attraction1, attraction1, attraction1, attraction1, attraction1));
    when(rewardsServiceMock.calculateRewardsPoints(Mockito.any(), Mockito.any()))
        .thenReturn(
            Arrays.asList(
                new GetNearbyAttractionDto(),
                new GetNearbyAttractionDto(),
                new GetNearbyAttractionDto(),
                new GetNearbyAttractionDto(),
                new GetNearbyAttractionDto()));

        tourGuideService.getNearbyAttractions("username");
    // THEN
    verify(userClientMock, times(1)).getUserByUsername("username");
    verify(locationClientMock, times(1)).getLocation(userId);
    verify(locationClientMock, times(1))
        .getNearbyAttractions(
            visitedLocationTest.location().latitude(), visitedLocationTest.location().longitude());
  }

  @Test
  void getNearbyAttractions_WhenUserClientReturnNull_ShouldThrowDataNotFoundException() {

    // WHEN
    when(userClientMock.getUserByUsername(Mockito.anyString())).thenReturn(null);

    // THEN
    assertThrows(
        DataNotFoundException.class, () -> tourGuideService.getNearbyAttractions("username"));
  }

  @Test
  void
      getNearbyAttractions_WhenLocationClientReturnNullVisitedLocation_ShouldThrowDataNotFoundException() {

    // GIVEN

    // WHEN
    when(userClientMock.getUserByUsername(Mockito.anyString()))
        .thenReturn(new User(userId, "username", "phone", "email"));

    // THEN
    assertThrows(
        DataNotFoundException.class, () -> tourGuideService.getNearbyAttractions("username"));
  }

  @Test
  void getNearbyAttractions_WhenLocationClientThrowException_ShouldThrowDataNotFoundException() {

    // WHEN
    doThrow(FeignException.FeignClientException.class)
        .when(userClientMock)
        .getUserByUsername(Mockito.anyString());

    // THEN
    assertThrows(
        ResourceNotFoundException.class, () -> tourGuideService.getNearbyAttractions("username"));
  }

  @Test
  void getAllCurrentLocations() {

    // WHEN
    when(locationClientMock.getAllLastLocation()).thenReturn(new HashMap<>());
    // THEN
    tourGuideService.getAllCurrentLocations();
    verify(locationClientMock, times(1)).getAllLastLocation();
  }

  @Test
  void getTripDeals() {

    // GIVEN
    UserPreferences userPreferences = new UserPreferences();
    userPreferences.setTripDuration(5);
    userPreferences.setNumberOfAdults(2);
    userPreferences.setNumberOfChildren(18);

    // WHEN

    // THEN

  }

  @Disabled
  @Test
  void getUserRewards() {}

  @Disabled
  @Test
  void getUser() {}

  @Disabled
  @Test
  void getAllUsers() {}

  @Disabled
  @Test
  void addUser() {}

  @Disabled
  @Test
  void trackUserLocation() {}
}
