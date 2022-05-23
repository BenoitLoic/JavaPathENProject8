package tourGuide.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsEqual;
import org.mockito.internal.hamcrest.HamcrestArgumentMatcher;
import tourGuide.config.Url;
import tourGuide.exception.DataNotFoundException;
import tourGuide.exception.IllegalArgumentException;
import tourGuide.exception.ResourceNotFoundException;
import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.UserReward;
import tourGuide.model.VisitedLocation;
import tourGuide.service.RewardsServiceImpl;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;

import java.util.*;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tripPricer.Provider;

import static org.hamcrest.EasyMock2Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static tourGuide.config.Url.*;

@WebMvcTest(controllers = TourGuideController.class)
@AutoConfigureMockMvc
class TourGuideControllerTest {

  private final UUID userId = UUID.randomUUID();
  private final static String validUserName = "userName";
  private final Date date = new Date();
  private final VisitedLocation visitedLocationTest =
      new VisitedLocation(UUID.randomUUID(), new Location(56d, 22d), date);
  private final User validUser =
      new User(userId, validUserName, "phoneNumberTest", "emailAddressTest");

  private final Attraction attractionTest =
      new Attraction(
          "attractionNameTest",
          "attractionCityTest",
          "attractionStateTest",
          UUID.randomUUID(),
          new Location(22d, 56d),
          null);
  @Autowired MockMvc mockMvc;
  @MockBean TourGuideService tourGuideServiceMock;
  @MockBean RewardsServiceImpl rewardsServiceMock;
  @InjectMocks TourGuideController tourGuideController;

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  void index() throws Exception {
    mockMvc.perform(get(INDEX)).andExpect(status().isOk());
  }

  @Test
  void getLocationValid() throws Exception {
    // GIVEN

    // WHEN
    when(tourGuideServiceMock.getUser(any())).thenReturn(validUser);
    when(tourGuideServiceMock.getUserLocation(Mockito.any())).thenReturn(visitedLocationTest);
    // THEN
    mockMvc
        .perform(get(GETLOCATION).param("userName", "validUserName"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void getLocationWithInvalidArgument() throws Exception {
    // GIVEN

    // WHEN

    // THEN
    mockMvc.perform(get(GETLOCATION).param("userName", "")).andExpect(status().isBadRequest());
  }

  @Test
  void getLocationWhenUserDoesntExist_ShouldThrowDataNotFoundException() throws Exception {
    // GIVEN

    // WHEN
    when(tourGuideServiceMock.getUserLocation(any())).thenThrow(DataNotFoundException.class);

    // THEN
    mockMvc
        .perform(get(GETLOCATION).param("userName", "uknUserName"))
        .andExpect(status().isNotFound());
  }

  // cette méthode doit retourner les 5 attractions les plus proches de l'utilisateur quelque soit
  // la distance.
  // chaque bloc comprend :
  //                          1. nom de l'attraction
  //                          2. lat/long de l'attraction
  //                          3. lat long de l'utilisateur
  //                          4. la distance en miles entre l'attraction et l'utilisateur
  //                          5. les rewardsPoints obtenus en cas de visite de l'attraction
  @Test
  void getNearbyAttractionsValid() throws Exception {

    // GIVEN

    // WHEN
    when(tourGuideServiceMock.getNearbyAttractions(Mockito.any())).thenReturn(new HashMap<>());
    // THEN
    mockMvc
        .perform(get(GETNEARBYATTRACTIONS).param("userName", validUserName))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void getNearbyAttractionsInvalid() throws Exception {

    // GIVEN

    // WHEN

    // THEN
    mockMvc
        .perform(get(GETNEARBYATTRACTIONS).param("userName", ""))
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertTrue(result.getResolvedException() instanceof IllegalArgumentException));
  }

  @Test
  void getNearbyAttractionsWhenUserDoesntExist_ShouldThrowDataNotFoundException() throws Exception {

    // GIVEN

    // WHEN
    Mockito.doThrow(DataNotFoundException.class)
        .when(tourGuideServiceMock)
        .getNearbyAttractions(any());
    // THEN
    mockMvc
        .perform(get(GETNEARBYATTRACTIONS).param("userName", validUserName))
        .andExpect(status().isNotFound())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof DataNotFoundException));
  }

  @Test
  void getRewardsValid() throws Exception {

    // GIVEN
    List<UserReward> userRewards =
        Arrays.asList(
            new UserReward(userId, visitedLocationTest, attractionTest, 5),
            new UserReward(userId, visitedLocationTest, attractionTest, 5),
            new UserReward(userId, visitedLocationTest, attractionTest, 5));
    // WHEN
    when(tourGuideServiceMock.getUser(anyString())).thenReturn(validUser);
    when(rewardsServiceMock.getRewards(Mockito.any())).thenReturn(userRewards);
    // THEN
    mockMvc
        .perform(get(Url.GETREWARDS).param("userName", validUserName))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void getRewardsInvalid() throws Exception {

    // GIVEN

    // WHEN

    // THEN
    mockMvc
        .perform(get(Url.GETREWARDS).param("userName", ""))
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertTrue(result.getResolvedException() instanceof IllegalArgumentException));
  }

  @Test
  void getRewardsWhenUserDoesntExist_ShouldThrowDataNotFoundException() throws Exception {

    // GIVEN

    // WHEN
    doThrow(DataNotFoundException.class).when(tourGuideServiceMock).getUser(Mockito.anyString());
    // THEN
    mockMvc
        .perform(get(GETREWARDS).param("userName", validUserName))
        .andExpect(status().isNotFound())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof DataNotFoundException));
  }

  // doit retourner toutes la derniere position connue pour tout les utilisateurs.
  // cette position doit etre récupéré dans les données sauvegardés de l'utilisateur.
  // la réponse est en JSON avec un mapping -> userId : position
  @Test
  void getAllCurrentLocationsValid() throws Exception {

    // GIVEN

    // WHEN
    when(tourGuideServiceMock.getAllCurrentLocations()).thenReturn(new HashMap<>());
    // THEN
    mockMvc
        .perform(get(GETALLCURRENTLOCATIONS))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void getAllCurrentLocations_ShouldReturnTheCorrectMapping() throws Exception {

    // GIVEN

    Map<UUID, Location> returnMap = new HashMap<>();
    returnMap.put(userId, new Location(5d, 1d));
    String json = MAPPER.writeValueAsString(returnMap);

    // WHEN
    when(tourGuideServiceMock.getAllCurrentLocations()).thenReturn(returnMap);
    // THEN
    mockMvc
        .perform(get(GETALLCURRENTLOCATIONS))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.size()", org.hamcrest.core.IsEqual.equalTo(1)))
        .andExpect(result -> assertEquals(result.getResponse().getContentAsString(), json));
  }

  @Test
  void getAllCurrentLocations_ShouldThrowResourceNotFoundException() throws Exception {

    doThrow(ResourceNotFoundException.class).when(tourGuideServiceMock).getAllCurrentLocations();

    mockMvc
        .perform(get(GETALLCURRENTLOCATIONS))
        .andExpect(status().isInternalServerError())
        .andExpect(
            result ->
                assertTrue(result.getResolvedException() instanceof ResourceNotFoundException));
  }

  @Test
  void getTripDealsValid() throws Exception {
    // GIVEN
    UUID tripId = UUID.randomUUID();
    double tripPrice = 99.5;
    Provider provider = new Provider(tripId, "providerNameTest", tripPrice);
    List<Provider> providers = new ArrayList<>();
    providers.add(provider);
    providers.add(provider);
    // WHEN
    when(tourGuideServiceMock.getTripDeals(Mockito.any())).thenReturn(providers);
    when(tourGuideServiceMock.getUser(anyString())).thenReturn(validUser);
    // THEN
    mockMvc
        .perform(get(GETTRIPDEALS).param("userName", validUserName))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.size()", IsEqual.equalTo(2)));
  }

  @Test
  void getTripDealsInvalid() throws Exception {
    // GIVEN

    // WHEN

    // THEN
    mockMvc
        .perform(get(GETTRIPDEALS).param("userName", ""))
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertTrue(result.getResolvedException() instanceof IllegalArgumentException));
  }

  @Test
  void getTripDealsWhenUserDoesntExist_ShouldThrowDataNotFoundException() throws Exception {
    // GIVEN

    // WHEN
    doThrow(DataNotFoundException.class).when(tourGuideServiceMock).getUser(anyString());
    // THEN
    mockMvc
        .perform(get(GETTRIPDEALS).param("userName", "unknownUser"))
        .andExpect(status().isNotFound())
        .andExpect(
            result ->
                assertTrue(result.getResolvedException() instanceof DataNotFoundException));
  }
}
