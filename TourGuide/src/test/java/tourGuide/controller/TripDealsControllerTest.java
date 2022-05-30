package tourGuide.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tourGuide.dto.AddUserPreferencesDto;
import tourGuide.exception.DataNotFoundException;
import tourGuide.exception.IllegalArgumentException;
import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.VisitedLocation;
import tourGuide.service.TripDealsService;
import tourGuide.service.UserServiceImpl;
import tourGuide.user.User;
import tripPricer.Provider;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static tourGuide.config.Url.ADD_USER_PREFERENCES;
import static tourGuide.config.Url.GET_TRIP_DEALS;

@WebMvcTest(controllers = TripDealsControllerImpl.class)
@AutoConfigureMockMvc
public class TripDealsControllerTest {

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
  @MockBean UserServiceImpl userServiceImplMock;
  @MockBean TripDealsService tripDealsServiceMock;

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
    when(tripDealsServiceMock.getTripDeals(Mockito.any(), Mockito.any())).thenReturn(providers);
    when(userServiceImplMock.getUser(anyString())).thenReturn(validUser);
    // THEN
    mockMvc
        .perform(
            get(GET_TRIP_DEALS)
                .param("userName", validUserName)
                .param("attractionId", UUID.randomUUID().toString()))
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
        .perform(
            get(GET_TRIP_DEALS)
                .param("userName", "")
                .param("attractionId", UUID.randomUUID().toString()))
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertTrue(result.getResolvedException() instanceof IllegalArgumentException));
  }

  @Test
  void getTripDealsWhenUserDoesntExist_ShouldThrowDataNotFoundException() throws Exception {
    // GIVEN

    // WHEN
    doThrow(DataNotFoundException.class).when(userServiceImplMock).getUser(anyString());
    // THEN
    mockMvc
        .perform(
            get(GET_TRIP_DEALS)
                .param("userName", "unknownUser")
                .param("attractionId", UUID.randomUUID().toString()))
        .andExpect(status().isNotFound())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof DataNotFoundException));
  }

  @Test
  void addUserPreferencesValid() throws Exception {

    // GIVEN
    String username = "usernameTest";

    //    AddUserPreferencesDto( int attractionProximity, int lowerPricePoint, int highPricePoint,
    // int tripDuration, int ticketQuantity, int numberOfAdults, int numberOfChildren)

    AddUserPreferencesDto validUserPreferences =
        new AddUserPreferencesDto(username, 0, 0, 0, 0, 0, 0, 0);
    String jsonBody = MAPPER.writeValueAsString(validUserPreferences);
    System.out.println(jsonBody);
    // WHEN
    Mockito.doNothing().when(tripDealsServiceMock).addUserPreferences(Mockito.any());
    // THEN
    mockMvc
        .perform(
            post(ADD_USER_PREFERENCES).content(jsonBody).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());
    verify(tripDealsServiceMock, times(1)).addUserPreferences(validUserPreferences);
  }

  @Test
  void addUserPreferencesInvalid() throws Exception {

    // GIVEN
    String username = "";

    //    AddUserPreferencesDto( int attractionProximity, int lowerPricePoint, int highPricePoint,
    // int tripDuration, int ticketQuantity, int numberOfAdults, int numberOfChildren)

    AddUserPreferencesDto validUserPreferences =
        new AddUserPreferencesDto(username, 0, 0, 0, 0, 0, 0, 0);
    String jsonBody = MAPPER.writeValueAsString(validUserPreferences);
    // WHEN

    // THEN
    mockMvc
        .perform(
            post(ADD_USER_PREFERENCES).content(jsonBody).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertTrue(result.getResolvedException() instanceof IllegalArgumentException));
  }

  @Test
  void addUserPreferencesWhenUserDoesntExist_ShouldThrowDataNotFoundException() throws Exception {

    // GIVEN
    String uknUserUsername = "ukn";
    AddUserPreferencesDto validUserPreferences =
        new AddUserPreferencesDto(uknUserUsername, 0, 0, 0, 0, 0, 0, 0);
    String jsonBody = MAPPER.writeValueAsString(validUserPreferences);
    // WHEN
    doThrow(DataNotFoundException.class)
        .when(tripDealsServiceMock)
        .addUserPreferences(Mockito.any());
    // THEN
    mockMvc
        .perform(
            post(ADD_USER_PREFERENCES).content(jsonBody).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof DataNotFoundException));
  }
}
