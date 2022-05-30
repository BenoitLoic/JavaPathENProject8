package tourGuide.service;

import feign.FeignException;
import java.util.*;
import java.util.stream.IntStream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import tourGuide.client.LocationClient;
import tourGuide.dto.GetNearbyAttractionDto;
import tourGuide.exception.DataNotFoundException;
import tourGuide.exception.ResourceNotFoundException;
import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.VisitedLocation;
import tourGuide.user.User;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

  private final UUID userId = UUID.randomUUID();
  @Mock LocationClient locationClientMock;
  @Mock UserService userServiceMock;
  @Mock RewardsServiceImpl rewardsServiceMock;
  @InjectMocks LocationServiceImpl locationService;
  private VisitedLocation visitedLocationTest;
  private User userTest;

  @BeforeEach
  void setUp() {
    userTest = new User(userId, "username", "phone", "email");
    Date dateTest = new Date();
    visitedLocationTest = new VisitedLocation(userId, new Location(50., 20.), dateTest);
    locationService.testMode = false;
  }

  // reçois un user -> renvoi une VisitedLocation
  // appel LocationClient
  @Test
  void getUserLocation() {

    // GIVEN

    // WHEN
    Mockito.when(locationClientMock.getLocation(userId)).thenReturn(visitedLocationTest);
    // THEN
    VisitedLocation actual = locationService.getUserLocation(userTest);
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
    Mockito.when(userServiceMock.getUser(Mockito.anyString())).thenReturn(userTest);

    when(locationClientMock.getLocation(any(UUID.class))).thenReturn(visitedLocationTest);

    when(locationClientMock.getNearbyAttractions(anyDouble(), anyDouble()))
        .thenReturn(Arrays.asList(attraction1, attraction1, attraction1, attraction1, attraction1));
    when(rewardsServiceMock.calculateRewardsPoints(any(), any()))
        .thenReturn(
            Arrays.asList(
                new GetNearbyAttractionDto(),
                new GetNearbyAttractionDto(),
                new GetNearbyAttractionDto(),
                new GetNearbyAttractionDto(),
                new GetNearbyAttractionDto()));

    locationService.getNearbyAttractions("username");
    // THEN
    verify(userServiceMock, times(1)).getUser("username");
    verify(locationClientMock, times(1)).getLocation(userId);
    verify(locationClientMock, times(1))
        .getNearbyAttractions(
            visitedLocationTest.location().latitude(), visitedLocationTest.location().longitude());
  }

  @Test
  void getNearbyAttractions_WhenUserClientReturnNull_ShouldThrowDataNotFoundException() {

    // WHEN
    when(userServiceMock.getUser(Mockito.anyString())).thenReturn(null);

    // THEN
    assertThrows(
        DataNotFoundException.class, () -> locationService.getNearbyAttractions("username"));
  }

  @Test
  void
      getNearbyAttractions_WhenLocationClientReturnNullVisitedLocation_ShouldThrowDataNotFoundException() {

    // GIVEN

    // WHEN
    when(userServiceMock.getUser(Mockito.anyString()))
        .thenReturn(new User(userId, "username", "phone", "email"));

    // THEN
    assertThrows(
        DataNotFoundException.class, () -> locationService.getNearbyAttractions("username"));
  }

  @Test
  void getNearbyAttractions_WhenLocationClientThrowException_ShouldThrowDataNotFoundException() {

    // WHEN
    doThrow(FeignException.FeignClientException.class)
        .when(userServiceMock)
        .getUser(Mockito.anyString());

    // THEN
    assertThrows(
        ResourceNotFoundException.class, () -> locationService.getNearbyAttractions("username"));
  }

  @Test
  void getAllCurrentLocations() {

    // WHEN
    when(locationClientMock.getAllLastLocation()).thenReturn(new HashMap<>());
    // THEN
    locationService.getAllCurrentLocations();
    verify(locationClientMock, times(1)).getAllLastLocation();
  }

  @Test
  void getAllCurrentLocations_whenClientThrowsException_ShouldThrowResourceNotFoundException() {

    doThrow(feign.FeignException.class).when(locationClientMock).getAllLastLocation();

    assertThrows(ResourceNotFoundException.class, () -> locationService.getAllCurrentLocations());
  }

  @Test
  void trackUserLocation() {

    // GIVEN

    // WHEN
    when(locationClientMock.getLocation(any(UUID.class))).thenReturn(visitedLocationTest);
    locationService.trackUserLocation(userTest);
    locationService.awaitTerminationAfterShutdown();
    // THEN
    assertThat(userTest.getVisitedLocations().size()).isEqualTo(1);
    assertThat(userTest.getVisitedLocations().stream().findFirst().get())
        .isEqualTo(visitedLocationTest);
    assertThat(userTest.getLatestLocationTimestamp()).isEqualTo(visitedLocationTest.timeVisited());
    verify(locationClientMock, times(1)).getLocation(userTest.getUserId());
  }

  @Test
  void trackUserLocation_WhenCallInParallel() {

    // GIVEN
    List<User> users = new ArrayList<>();
    IntStream.range(0, 20)
        .forEach(i -> users.add(new User(UUID.randomUUID(), "name" + i, "phone", "email")));

    // WHEN
    when(locationClientMock.getLocation(any(UUID.class))).thenReturn(visitedLocationTest);
    users.parallelStream().forEach(user -> locationService.trackUserLocation(user));
    locationService.awaitTerminationAfterShutdown();
    // THEN
    for (User user : users) {
      assertThat(user.getVisitedLocations().size()).isEqualTo(1);
    }
    verify(locationClientMock, times(20)).getLocation(any(UUID.class));
  }
}
