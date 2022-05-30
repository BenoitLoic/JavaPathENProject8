import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tourGuide.Application;
import tourGuide.client.UserClient;
import tourGuide.config.Url;
import tourGuide.dto.AddUserPreferencesDto;
import tourGuide.exception.DataNotFoundException;
import tourGuide.exception.IllegalArgumentException;
import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.UserReward;
import tourGuide.model.VisitedLocation;
import tourGuide.service.UserServiceImpl;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tripPricer.TripPricer;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TripDealsIT {

  private final UUID attractionId = UUID.randomUUID();
  private final UUID userId = UUID.randomUUID();
  private final Date dateTest = new Date();
  private User user;
  private final VisitedLocation visitedLocationTest =
      new VisitedLocation(userId, new Location(50.54, 20.), dateTest);
  private UserReward userReward;
  private UserPreferences userPreferences;
  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @Autowired Environment environment;
  @MockBean UserClient userClientMock;
  @SpyBean TripPricer tripPricerMock;
  @Autowired
  UserServiceImpl userServiceImpl;

  @BeforeEach
  void setUp() {
    Attraction attraction = new Attraction(
            "attractionName",
            "cityTest",
            "state",
            attractionId,
            visitedLocationTest.location(),
            1d);
    userReward = new UserReward(userId, visitedLocationTest, attraction, 50);
    userPreferences = new UserPreferences();
    userPreferences.setTripDuration(2);
    userPreferences.setTicketQuantity(15);
    userPreferences.setNumberOfChildren(2);
    userPreferences.setNumberOfAdults(2);
  }

  @Test
  void getTripDeals_WithRewardsAndPreferences() throws Exception {

    // GIVEN i'm logged in / have rewards / have preferences
    user = new User(userId, "username1", "phone", "email");
    user.addUserReward(userReward);
    user.setUserPreferences(userPreferences);
    userServiceImpl.addUser(user);

    // WHEN

    Mockito.when(userClientMock.addUser(Mockito.any())).thenReturn(user);

    //    String apiKey, UUID attractionId, int adults, int children, int nightsStay, int
    // rewardsPoints
    Mockito.when(
            tripPricerMock.getPrice(
                anyString(), Mockito.any(UUID.class), anyInt(), anyInt(), anyInt(), anyInt()))
        .thenCallRealMethod();
    // THEN I should get my trip deals for the given attraction with my preferences and price cut.
    mockMvc
        .perform(
            get(Url.GET_TRIP_DEALS)
                .param("userName", user.getUserName())
                .param("attractionId", attractionId.toString()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    Mockito.verify(tripPricerMock, Mockito.times(1))
        .getPrice(
            environment.getProperty("tripPricer.apiKey"),
            attractionId,
            userPreferences.getNumberOfAdults(),
            userPreferences.getNumberOfChildren(),
            userPreferences.getTripDuration(),
            userReward.rewardPoints());
  }

  @Test
  void getTripDeals_WithPreferences() throws Exception {

    // GIVEN i'm logged in / have NO rewards / have preferences
    user = new User(userId, "username2", "phone", "email");
    user.setUserPreferences(userPreferences);
    userServiceImpl.addUser(user);
    // WHEN
    Mockito.when(
            tripPricerMock.getPrice(
                anyString(), Mockito.any(UUID.class), anyInt(), anyInt(), anyInt(), anyInt()))
        .thenCallRealMethod();
    // THEN I should get my trip deals for the given attraction with my preferences.
    mockMvc
        .perform(
            get(Url.GET_TRIP_DEALS)
                .param("userName", user.getUserName())
                .param("attractionId", attractionId.toString()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    Mockito.verify(tripPricerMock, Mockito.times(1))
        .getPrice(
            environment.getProperty("tripPricer.apiKey"),
            attractionId,
            userPreferences.getNumberOfAdults(),
            userPreferences.getNumberOfChildren(),
            userPreferences.getTripDuration(),
            0);
  }

  @Test
  void getTripDeals_Default() throws Exception {

    // GIVEN i'm logged in / have NO rewards / have NO preferences
    user = new User(userId, "username3", "phone", "email");
    userServiceImpl.addUser(user);
    UserPreferences defaultPreferences = new UserPreferences();
    // WHEN
    Mockito.when(
            tripPricerMock.getPrice(
                anyString(), Mockito.any(UUID.class), anyInt(), anyInt(), anyInt(), anyInt()))
        .thenCallRealMethod();
    // THEN I should get my trip deals with default preferences.
    mockMvc
        .perform(
            get(Url.GET_TRIP_DEALS)
                .param("userName", user.getUserName())
                .param("attractionId", attractionId.toString()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    Mockito.verify(tripPricerMock, Mockito.times(1))
        .getPrice(
            environment.getProperty("tripPricer.apiKey"),
            attractionId,
            defaultPreferences.getNumberOfAdults(),
            defaultPreferences.getNumberOfChildren(),
            defaultPreferences.getTripDuration(),
            0);
  }

  @Test
  void getTripDeals_InvalidUserName_ShouldThrowIllegalArgumentException() throws Exception {

    // GIVEN I don't give my username

    // WHEN

    // THEN I get an error
    mockMvc
        .perform(
            get(Url.GET_TRIP_DEALS)
                .param("userName", "")
                .param("attractionId", attractionId.toString()))
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertTrue(result.getResolvedException() instanceof IllegalArgumentException));
  }

  @Test
  void getTripDeals_UnknownUser_ShouldThrowDataNotFoundException() throws Exception {

    // GIVEN I'm not registered

    // WHEN

    // THEN I get an error
    mockMvc
        .perform(
            get(Url.GET_TRIP_DEALS)
                .param("userName", "unknownUser")
                .param("attractionId", attractionId.toString()))
        .andExpect(status().isNotFound())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof DataNotFoundException));
  }

  @Test
  void addUserPreferences_ShouldAddNew() throws Exception {

    // GIVEN i'm logged in and don't have preferences
    user = new User(userId, "username4", "phone", "email");
    userServiceImpl.addUser(user);
    AddUserPreferencesDto newPreferences =
        new AddUserPreferencesDto(user.getUserName(), 20, 50, 50000, 3, 2, 1, 1);
    String json = objectMapper.writeValueAsString(newPreferences);

    // WHEN i add a new preferences

    // THEN my preferences should be added
    mockMvc
        .perform(post(Url.ADD_USER_PREFERENCES).content(json).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());
    assertEquals(
        20,
        userServiceImpl.getUser(user.getUserName()).getUserPreferences().getAttractionProximity());
  }

  @Test
  void addUserPreferences_ShouldUpdateExisting() throws Exception {

    // GIVEN i'm logged in and already have preferences
    user = new User(userId, "username5", "phone", "email");
    user.setUserPreferences(userPreferences);
    userServiceImpl.addUser(user);
    AddUserPreferencesDto newPreferences =
        new AddUserPreferencesDto(user.getUserName(), 123456, 50, 50000, 3, 2, 1, 1);
    String json = objectMapper.writeValueAsString(newPreferences);
    // WHEN i add a new preferences

    // THEN my preferences should be updated
    mockMvc
        .perform(post(Url.ADD_USER_PREFERENCES).content(json).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());
    assertEquals(
        123456,
        userServiceImpl.getUser(user.getUserName()).getUserPreferences().getAttractionProximity());
  }

  @Test
  void addUserPreferences_WhenUserDontExist_ShouldThrowDataNotFoundException() throws Exception {

    // GIVEN i'm not registered
    AddUserPreferencesDto newPreferences =
        new AddUserPreferencesDto("unknUser", 50, 50, 50000, 3, 2, 1, 1);
    String json = objectMapper.writeValueAsString(newPreferences);
    // WHEN i add a new preferences

    // THEN i get an error
    mockMvc
        .perform(post(Url.ADD_USER_PREFERENCES).content(json).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof DataNotFoundException));
  }

  @Test
  void addUserPreferences_Invalid_ShouldThrowIllegalArgumentException() throws Exception {

    // GIVEN i don't give a valid username
    AddUserPreferencesDto newPreferences =
            new AddUserPreferencesDto("", 50, 50, 50000, 3, 2, 1, 1);
    String json = objectMapper.writeValueAsString(newPreferences);
    // WHEN i add a new preferences

    // THEN i get an error
    mockMvc
            .perform(post(Url.ADD_USER_PREFERENCES).content(json).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(
                    result -> assertTrue(result.getResolvedException() instanceof IllegalArgumentException));
  }
}
