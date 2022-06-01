package old;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tourGuide.Application;
import tourGuide.client.LocationClient;
import tourGuide.client.UserClient;
import tourGuide.dto.GetNearbyAttractionDto;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.Location;
import tourGuide.service.*;
import tourGuide.model.user.User;
import tripPricer.Provider;
import tripPricer.TripPricer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class TestTourGuideService {

  @Autowired LocationClient locationClientMock;
  @Autowired UserClient userClientMock;
  @Autowired RewardsService rewardsServiceMock;
  @Autowired UserService userService;
  private String username1;
  private String username2;

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
    LocationService locationService = new LocationServiceImpl(locationClientMock,rewardsServiceMock,userService);

    User user = new User(UUID.randomUUID(), username1, "000", "jon@tourGuide.com");
    locationService.trackUserLocation(user);

    locationService.awaitTerminationAfterShutdown();
    tourGuide.model.VisitedLocation visitedLocation = user.getVisitedLocations().get(0);
    assertEquals(visitedLocation.userId(), user.getUserId());
  }

  @Test
  public void addUser() {

    User user = new User(UUID.randomUUID(), username1, "000", "jon@tourGuide.com");
    User user2 = new User(UUID.randomUUID(), username2, "000", "jon2@tourGuide.com");

    userService.addUser(user);
    userService.addUser(user2);

    User retrievedUser = userService.getUser(user.getUserName());
    User retrievedUser2 = userService.getUser(user2.getUserName());

    assertEquals(user, retrievedUser);
    assertEquals(user2, retrievedUser2);
  }

  @Test
  public void getAllUsers() {

    User user = new User(UUID.randomUUID(), username1, "000", "jon@tourGuide.com");
    User user2 = new User(UUID.randomUUID(), username2, "000", "jon2@tourGuide.com");

    userService.addUser(user);
    userService.addUser(user2);

    List<User> allUsers = userService.getAllUsers();

    assertTrue(allUsers.contains(user));
    assertTrue(allUsers.contains(user2));
  }

  @Test
  public void trackUser() {
    LocationService locationService = new LocationServiceImpl(locationClientMock,rewardsServiceMock,userService);

    User user = new User(UUID.randomUUID(), username1, "000", "jon@tourGuide.com");

    assertEquals(0, user.getVisitedLocations().size());
    locationService.trackUserLocation(user);
    locationService.awaitTerminationAfterShutdown();
    assertEquals(user.getUserId(), user.getVisitedLocations().get(0).userId());
  }

  @Test
  public void getNearbyAttractions() {

    LocationService locationService = new LocationServiceImpl(locationClientMock,rewardsServiceMock,userService);
    User user = new User(UUID.randomUUID(), username1, "000", "jon@tourGuide.com");
    userService.addUser(user);
    locationService.trackUserLocation(user);

    Map<Location, Collection<GetNearbyAttractionDto>> attractions =
        locationService.getNearbyAttractions(user.getUserName());

    assertEquals(5, attractions.entrySet().stream().findFirst().get().getValue().size());
  }

  @Test
  public void getTripDeals() {

    TripDealsService tripDealsService = new TripDealsServiceImpl(new TripPricer(), userService);

    User user = new User(UUID.randomUUID(), username1, "000", "jon@tourGuide.com");

    List<Provider> providers = tripDealsService.getTripDeals(user, UUID.randomUUID());

    assertEquals(5, providers.size());
  }
}
