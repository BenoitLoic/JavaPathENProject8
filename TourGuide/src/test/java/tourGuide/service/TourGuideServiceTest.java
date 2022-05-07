package tourGuide.service;

import feign.FeignException;
import tourGuide.client.LocationClient;
import tourGuide.client.UserClient;
import tourGuide.exception.DataNotFoundException;
import tourGuide.exception.ResourceNotFoundException;
import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.VisitedLocation;
import tourGuide.user.User;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TourGuideServiceTest {

  private UUID userId = UUID.randomUUID();
  private Date dateTest = new Date();
  private VisitedLocation visitedLocationTest =
      new VisitedLocation(userId, new Location(50., 20.), dateTest);

  @Mock LocationClient locationClientMock;
  @Mock UserClient userClientMock;
  @InjectMocks TourGuideService tourGuideService;

  // reçois un user -> renvoi une VisitedLocation
  // appel LocationClient
  @Test
  void getUserLocation() {

    // GIVEN
    User user = new User(userId, "userNameTest", "phoneTest", "emailAddressTest");
    // WHEN
    when(locationClientMock.addLocation(Mockito.any())).thenReturn(visitedLocationTest);
    // THEN
    VisitedLocation actual = tourGuideService.getUserLocation(user);
    Assertions.assertThat(actual).isEqualTo(visitedLocationTest);
    verify(locationClientMock, times(1)).addLocation(userId);
  }
  // appel locationClient pour récupérer la liste des 5 attractions les plus proches
  // locationClient renvoi une liste de 5 Attractions avec les RewardPoints en champ additionnel
  @Test
  void getNearbyAttractions_VerifyClientCall() {

    // GIVEN
    UUID attractionId1 = UUID.randomUUID();
    Attraction attraction1 =
        new Attraction(
            "attractionNameTest1", "cityTest", "stateTest", attractionId1, new Location(50d, 120d));
    // WHEN
    when(userClientMock.getUserByUsername(any()))
        .thenReturn(new User(userId, "username", "phone", "email"));
    when(locationClientMock.addLocation(any())).thenReturn(visitedLocationTest);
    when(locationClientMock.getNearbyAttractions(anyDouble(), anyDouble()))
        .thenReturn(Arrays.asList(attraction1, attraction1, attraction1, attraction1, attraction1));
    Collection<Attraction> actual = tourGuideService.getNearbyAttractions("username");
    // THEN
    verify(userClientMock, times(1)).getUserByUsername("username");
    verify(locationClientMock, times(1)).addLocation(userId);
    verify(locationClientMock, times(1))
        .getNearbyAttractions(
            visitedLocationTest.location().latitude(), visitedLocationTest.location().longitude());
    assertEquals(5, actual.size());
  }

  @Test
  void getNearbyAttractions_WhenUserClientReturnNull_ShouldThrowDataNotFoundException() {

    // WHEN
    when(userClientMock.getUserByUsername(any())).thenReturn(null);

    // THEN
    assertThrows(
        DataNotFoundException.class, () -> tourGuideService.getNearbyAttractions("username"));
  }

  @Test
  void
      getNearbyAttractions_WhenLocationClientReturnNullVisitedLocation_ShouldThrowDataNotFoundException() {

    // GIVEN
    UUID attractionId1 = UUID.randomUUID();
    Attraction attraction1 =
        new Attraction(
            "attractionNameTest1", "cityTest", "stateTest", attractionId1, new Location(50d, 120d));
    UUID attractionId2 = UUID.randomUUID();
    Attraction attraction2 =
        new Attraction(
            "attractionNameTest2", "cityTest", "stateTest", attractionId2, new Location(50d, 20d));

    List<Attraction> attractions =
        Arrays.asList(attraction1, attraction2, attraction1, attraction2, attraction1);
    // WHEN
    when(userClientMock.getUserByUsername(any()))
        .thenReturn(new User(userId, "username", "phone", "email"));
    when(locationClientMock.addLocation(any())).thenReturn(null);

    // THEN
    assertThrows(
        DataNotFoundException.class, () -> tourGuideService.getNearbyAttractions("username"));
  }

  @Test
  void getNearbyAttractions_WhenLocationClientThrowException_ShouldThrowDataNotFoundException() {

    // WHEN
    doThrow(FeignException.FeignClientException.class)
        .when(userClientMock)
        .getUserByUsername(any());

    // THEN
    assertThrows(
        ResourceNotFoundException.class, () -> tourGuideService.getNearbyAttractions("username"));
  }

  @Test
  void getUserRewards() {}

  @Test
  void getUser() {}

  @Test
  void getAllUsers() {}

  @Test
  void addUser() {}

  @Test
  void getTripDeals() {}

  @Test
  void trackUserLocation() {}
}
