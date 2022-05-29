package old;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tourGuide.Application;
import tourGuide.client.LocationClient;
import tourGuide.client.UserClient;
import tourGuide.dto.GetNearbyAttractionDto;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.Location;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.TripDealsService;
import tourGuide.service.TripDealsServiceImpl;
import tourGuide.user.User;
import tripPricer.Provider;
import tripPricer.TripPricer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class TestTourGuideService {

  private String username1;
  private String username2;

  @Autowired LocationClient locationClientMock;
  @Autowired UserClient userClientMock;
  @Autowired RewardsService rewardsServiceMock;

  @BeforeAll
  static void beforeAll() {
    java.util.Locale.setDefault(java.util.Locale.US);
    InternalTestHelper.setInternalUserNumber(0);
  }

  @BeforeEach
  void setUp() {
    username1 = UUID.randomUUID().toString();
    username2 = UUID.randomUUID().toString();
  }

  @Test
  public void getUserLocation() {
    TourGuideService tourGuideService =
        new TourGuideService(locationClientMock, userClientMock, rewardsServiceMock);

    User user = new User(UUID.randomUUID(), username1, "000", "jon@tourGuide.com");
    tourGuideService.trackUserLocation(user);

    tourGuideService.awaitTerminationAfterShutdown();
    tourGuide.model.VisitedLocation visitedLocation = user.getVisitedLocations().get(0);
    assertEquals(visitedLocation.userId(), user.getUserId());
  }

  @Test
  public void addUser() {

    TourGuideService tourGuideService =
        new TourGuideService(locationClientMock, userClientMock, rewardsServiceMock);

    User user = new User(UUID.randomUUID(), username1, "000", "jon@tourGuide.com");
    User user2 = new User(UUID.randomUUID(), username2, "000", "jon2@tourGuide.com");

    System.out.println(user);
    tourGuideService.addUser(user);
    tourGuideService.addUser(user2);

    User retrievedUser = tourGuideService.getUser(user.getUserName());
    User retrievedUser2 = tourGuideService.getUser(user2.getUserName());

    assertEquals(user, retrievedUser);
    assertEquals(user2, retrievedUser2);
  }

  @Test
  public void getAllUsers() {

    TourGuideService tourGuideService =
        new TourGuideService(locationClientMock, userClientMock, rewardsServiceMock);
    User user = new User(UUID.randomUUID(), username1, "000", "jon@tourGuide.com");
    User user2 = new User(UUID.randomUUID(), username2, "000", "jon2@tourGuide.com");

    tourGuideService.addUser(user);
    tourGuideService.addUser(user2);

    List<User> allUsers = tourGuideService.getAllUsers();

    assertTrue(allUsers.contains(user));
    assertTrue(allUsers.contains(user2));
  }

  @Test
  public void trackUser() {
    TourGuideService tourGuideService =
        new TourGuideService(locationClientMock, userClientMock, rewardsServiceMock);

    User user = new User(UUID.randomUUID(), username1, "000", "jon@tourGuide.com");

    assertEquals(0, user.getVisitedLocations().size());
    tourGuideService.trackUserLocation(user);
    tourGuideService.awaitTerminationAfterShutdown();
    assertEquals(user.getUserId(), user.getVisitedLocations().get(0).userId());
  }

  @Test
  public void getNearbyAttractions() {

    TourGuideService tourGuideService =
        new TourGuideService(locationClientMock, userClientMock, rewardsServiceMock);

    User user = new User(UUID.randomUUID(), username1, "000", "jon@tourGuide.com");
    tourGuideService.addUser(user);
    tourGuideService.trackUserLocation(user);

    Map<Location, Collection<GetNearbyAttractionDto>> attractions =
        tourGuideService.getNearbyAttractions(user.getUserName());

    assertEquals(5, attractions.entrySet().stream().findFirst().get().getValue().size());
  }

  @Test
  public void getTripDeals() {
    TourGuideService tourGuideService =
        new TourGuideService(locationClientMock, userClientMock, rewardsServiceMock);

    TripDealsService tripDealsService =
        new TripDealsServiceImpl(new TripPricer(), tourGuideService);

    User user = new User(UUID.randomUUID(), username1, "000", "jon@tourGuide.com");

    List<Provider> providers = tripDealsService.getTripDeals(user, UUID.randomUUID());

    assertEquals(5, providers.size());
  }
}
