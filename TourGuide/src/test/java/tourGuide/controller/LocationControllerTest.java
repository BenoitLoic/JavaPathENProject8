package tourGuide.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tourGuide.exception.DataNotFoundException;
import tourGuide.exception.IllegalArgumentException;
import tourGuide.exception.ResourceNotFoundException;
import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.VisitedLocation;
import tourGuide.service.LocationServiceImpl;
import tourGuide.service.RewardsServiceImpl;
import tourGuide.service.UserService;
import tourGuide.model.user.User;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static tourGuide.config.Url.*;

@WebMvcTest(controllers = LocationControllerImpl.class)
@AutoConfigureMockMvc
class LocationControllerTest {

  private static final String validUserName = "userName";
  private final UUID userId = UUID.randomUUID();
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
  @Autowired ObjectMapper MAPPER;
  @MockBean LocationServiceImpl locationServiceMock;
  @MockBean RewardsServiceImpl rewardsServiceMock;
  @MockBean UserService userServiceMock;

  @Test
  void getLocationValid() throws Exception {
    // GIVEN

    // WHEN
    when(userServiceMock.getUser(any())).thenReturn(validUser);
    when(locationServiceMock.getUserLocation(Mockito.any())).thenReturn(visitedLocationTest);
    // THEN
    mockMvc
        .perform(get(GET_LOCATION).param("userName", "validUserName"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void getLocationWithInvalidArgument() throws Exception {
    // GIVEN

    // WHEN

    // THEN
    mockMvc.perform(get(GET_LOCATION).param("userName", "")).andExpect(status().isBadRequest());
  }

  @Test
  void getLocationWhenUserDoesntExist_ShouldThrowDataNotFoundException() throws Exception {
    // GIVEN

    // WHEN
    when(locationServiceMock.getUserLocation(any())).thenThrow(DataNotFoundException.class);

    // THEN
    mockMvc
        .perform(get(GET_LOCATION).param("userName", "uknUserName"))
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
    when(locationServiceMock.getNearbyAttractions(Mockito.any())).thenReturn(new HashMap<>());
    // THEN
    mockMvc
        .perform(get(GET_NEARBY_ATTRACTIONS).param("userName", validUserName))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void getNearbyAttractionsInvalid() throws Exception {

    // GIVEN

    // WHEN

    // THEN
    mockMvc
        .perform(get(GET_NEARBY_ATTRACTIONS).param("userName", ""))
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
        .when(locationServiceMock)
        .getNearbyAttractions(any());
    // THEN
    mockMvc
        .perform(get(GET_NEARBY_ATTRACTIONS).param("userName", validUserName))
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
    when(locationServiceMock.getAllCurrentLocations()).thenReturn(new HashMap<>());
    // THEN
    mockMvc
        .perform(get(GET_ALL_CURRENT_LOCATIONS))
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
    when(locationServiceMock.getAllCurrentLocations()).thenReturn(returnMap);
    // THEN
    mockMvc
        .perform(get(GET_ALL_CURRENT_LOCATIONS))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.size()", org.hamcrest.core.IsEqual.equalTo(1)))
        .andExpect(result -> assertEquals(result.getResponse().getContentAsString(), json));
  }

  @Test
  void getAllCurrentLocations_ShouldThrowResourceNotFoundException() throws Exception {

    doThrow(ResourceNotFoundException.class).when(locationServiceMock).getAllCurrentLocations();

    mockMvc
        .perform(get(GET_ALL_CURRENT_LOCATIONS))
        .andExpect(status().isInternalServerError())
        .andExpect(
            result ->
                assertTrue(result.getResolvedException() instanceof ResourceNotFoundException));
  }
}
