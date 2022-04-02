package tourGuide.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.exception.DataNotFoundException;
import tourGuide.service.TourGuideService;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tourGuide.config.Url.*;

@WebMvcTest(controllers =
    TourGuideController.class)
@AutoConfigureMockMvc
class TourGuideControllerTest {

  @Autowired
  MockMvc             mockMvc;
  @MockBean
  TourGuideService    tourGuideServiceMock;
  @InjectMocks
  TourGuideController tourGuideController;

  private static final ObjectMapper MAPPER = new ObjectMapper();

//  @org.junit.jupiter.api.BeforeEach
//  void setUp() {
//    //locale.US ?
//  }

  @Test
  void index() throws Exception {
    mockMvc.perform(
        get(INDEX)
    ).andExpect(status().isOk());
  }

  @Test
  void getLocationValid() throws Exception {
    // GIVEN
    VisitedLocation visitedLocationTest = new VisitedLocation(UUID.randomUUID(),
        new Location(56d, 22d), new Date());
    // WHEN
    when(tourGuideServiceMock.getUserLocation(Mockito.any())).thenReturn(visitedLocationTest)
    ;
    // THEN
    mockMvc.perform(get(GETLOCATION).param("userName", "validUserName"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

  }

  @Test
  void getLocationWithInvalidArgument() throws Exception {
    // GIVEN

    // WHEN

    // THEN
    mockMvc.perform(get(GETLOCATION).param("userName", ""))
        .andExpect(status().isBadRequest());

  }

  @Test
  void getLocationWhenUserDoesntExist_ShouldThrowDataNotFoundException() throws Exception {
    // GIVEN

    // WHEN
    when(tourGuideServiceMock.getUserLocation(any())).thenThrow(DataNotFoundException.class);

    // THEN
    mockMvc.perform(get(GETLOCATION).param("userName", "uknUserName"))
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
  void getNearbyAttractionsValid() {

    // GIVEN

    // WHEN

    // THEN
    assertEquals(0, 1);

  }

  @Test
  void getNearbyAttractionsInvalid() {

    // GIVEN

    // WHEN

    // THEN
    assertEquals(0, 1);

  }

  @Test
  void getNearbyAttractionsWhenUserDoesntExist_ShouldThrowDataNotFoundException() {

    // GIVEN

    // WHEN

    // THEN
    assertEquals(0, 1);

  }

  @Test
  void getRewardsValid() {

    // GIVEN

    // WHEN

    // THEN


  }

  @Test
  void getRewardsInvalid() {

    // GIVEN

    // WHEN

    // THEN


  }

  @Test
  void getRewardsWhenUserDoesntExist_ShouldThrowDataNotFoundException() {

    // GIVEN

    // WHEN

    // THEN


  }

  // doit retourner toutes la derniere position connue pour tout les utilisateurs.
  // cette position doit etre récupéré dans les données sauvegardés de l'utilisateur.
  // la réponse est en JSON avec un mapping -> userId : position
  @Test
  void getAllCurrentLocationsShouldReturn2Positions() {

    // GIVEN

    // WHEN

    // THEN


  }


  @Test
  void getTripDealsValid() {
    // GIVEN

    // WHEN

    // THEN


  }
  @Test
  void getTripDealsInvalid() {
    // GIVEN

    // WHEN

    // THEN


  }
  @Test
  void getTripDealsWhenUserDoesntExist_ShouldThrowDataNotFoundException() {
    // GIVEN

    // WHEN

    // THEN


  }
}