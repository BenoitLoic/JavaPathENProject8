//package tourGuide;
//
//import java.util.Collection;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//import org.junit.jupiter.api.RepeatedTest;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import tourGuide.client.LocationClient;
//import tourGuide.client.UserClient;
//import tourGuide.dto.GetNearbyAttractionDto;
//import tourGuide.helper.InternalTestHelper;
//import tourGuide.model.Location;
//import tourGuide.service.RewardsService;
//import tourGuide.service.TourGuideService;
//import tourGuide.service.TripDealsService;
//import tourGuide.service.TripDealsServiceImpl;
//import tourGuide.user.User;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import tripPricer.Provider;
//import tripPricer.TripPricer;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//@SpringBootTest
//public class TestTourGuideService {
//
//  @Autowired LocationClient locationClientMock;
//  @Autowired UserClient userClientMock;
//  @Autowired RewardsService rewardsServiceMock;
//
//  @BeforeAll
//  static void beforeAll() {
//    java.util.Locale.setDefault(java.util.Locale.US);
//    InternalTestHelper.setInternalUserNumber(0);
//  }
//
//  @Test
//  public void getUserLocation() throws InterruptedException {
//
//    TourGuideService tourGuideService =
//        new TourGuideService(locationClientMock, userClientMock, rewardsServiceMock);
//
//    User user = new User(UUID.randomUUID(), "jony", "000", "jon@tourGuide.com");
//    tourGuideService.trackUserLocation(user);
//
//    tourGuideService.awaitTerminationAfterShutdown();
//
//    tourGuide.model.VisitedLocation visitedLocation = user.getVisitedLocations().get(0);
//    assertEquals(visitedLocation.userId(), user.getUserId());
//  }
//
//  @Test
//  public void addUser() {
//
//    InternalTestHelper.setInternalUserNumber(0);
//    TourGuideService tourGuideService =
//        new TourGuideService(locationClientMock, userClientMock, rewardsServiceMock);
//
//    User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
//    User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");
//
//    System.out.println(user);
//    tourGuideService.addUser(user);
//    tourGuideService.addUser(user2);
//
//    User retrievedUser = tourGuideService.getUser(user.getUserName());
//    User retrievedUser2 = tourGuideService.getUser(user2.getUserName());
//
//    assertEquals(user, retrievedUser);
//    assertEquals(user2, retrievedUser2);
//  }
//
//  @Test
//  public void getAllUsers() {
//
//    InternalTestHelper.setInternalUserNumber(0);
//    TourGuideService tourGuideService =
//        new TourGuideService(locationClientMock, userClientMock, rewardsServiceMock);
//
//    User user = new User(UUID.randomUUID(), "jonx", "000", "jon@tourGuide.com");
//    User user2 = new User(UUID.randomUUID(), "jonx2", "000", "jon2@tourGuide.com");
//
//    tourGuideService.addUser(user);
//    tourGuideService.addUser(user2);
//
//    List<User> allUsers = tourGuideService.getAllUsers();
//
//    assertTrue(allUsers.contains(user));
//    assertTrue(allUsers.contains(user2));
//  }
//
//  @Test
//  public void trackUser() {
//
//    TourGuideService tourGuideService =
//        new TourGuideService(locationClientMock, userClientMock, rewardsServiceMock);
//    User user = new User(UUID.randomUUID(), "jonz", "000", "jon@tourGuide.com");
//
//    assertEquals(0, user.getVisitedLocations().size());
//    tourGuideService.trackUserLocation(user);
//    tourGuideService.awaitTerminationAfterShutdown();
//    assertEquals(user.getUserId(), user.getVisitedLocations().get(0).userId());
//  }
//
//  @Test
//  public void getNearbyAttractions() {
//
//    TourGuideService tourGuideService =
//        new TourGuideService(locationClientMock, userClientMock, rewardsServiceMock);
//
//    User user = new User(UUID.randomUUID(), "jonb", "000", "jon@tourGuide.com");
//tourGuideService.addUser(user);
//    tourGuideService.trackUserLocation(user);
//
//    Map<Location, Collection<GetNearbyAttractionDto>> attractions =
//        tourGuideService.getNearbyAttractions(user.getUserName());
//
//    assertEquals(5, attractions.size());
//  }
//
//  @Test
//  public void getTripDeals() {
//    TourGuideService tourGuideService =
//        new TourGuideService(locationClientMock, userClientMock, rewardsServiceMock);
//
//    TripDealsService tripDealsService =
//        new TripDealsServiceImpl(new TripPricer(), tourGuideService);
//
//    User user = new User(UUID.randomUUID(), "jonk", "000", "jon@tourGuide.com");
//
//    List<Provider> providers = tripDealsService.getTripDeals(user, UUID.randomUUID());
//
//    assertEquals(5, providers.size());
//  }
//}
