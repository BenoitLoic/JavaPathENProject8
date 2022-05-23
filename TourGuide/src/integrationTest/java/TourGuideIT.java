import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tourGuide.Application;
import tourGuide.client.LocationClient;
import tourGuide.config.Url;
import tourGuide.exception.DataNotFoundException;
import tourGuide.exception.IllegalArgumentException;
import tourGuide.exception.ResourceNotFoundException;
import tourGuide.model.Location;
import tourGuide.model.VisitedLocation;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class TourGuideIT {

  private final UUID userId = UUID.randomUUID();
  private final Date dateTest = new Date();
  private  VisitedLocation visitedLocationTest;

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @MockBean LocationClient locationClientMock;

  @BeforeEach
  void setUp() {

    visitedLocationTest =
            new VisitedLocation(userId, new Location(50.54, 20.), dateTest);

  }
  @RepeatedTest(value = 10)
  void getLocationValid() throws Exception {

    // GIVEN
    String userName = "internalUser0";
    // WHEN
    when(locationClientMock.getLocation(Mockito.any())).thenReturn(visitedLocationTest);
    // THEN
    System.out.println("avant le premier perform");
    mockMvc
        .perform(get(Url.GETLOCATION).param("userName", userName))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.latitude", is(20.0)))
        .andExpect(jsonPath("$.longitude", is(50.54)));
  }
  @RepeatedTest(value = 10)
  void getLocationInvalid() throws Exception {
    mockMvc
        .perform(get(Url.GETLOCATION).param("userName", ""))
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertThat(result.getResolvedException() instanceof IllegalArgumentException)
                    .isTrue());
  }
  @RepeatedTest(value = 10)
  void getLocationWhenUserDoesntExist_ShouldThrowDataNotFoundException() throws Exception {

    mockMvc
        .perform(get(Url.GETLOCATION).param("userName", "UNKNOWN_USER"))
        .andExpect(status().isNotFound())
        .andExpect(
            result ->
                assertThat(result.getResolvedException() instanceof DataNotFoundException)
                    .isTrue());
  }
  @RepeatedTest(value = 10)
  void getAllCurrentLocations() throws Exception {

    // GIVEN
    UUID userId1 = UUID.randomUUID();
    UUID userId2 = UUID.randomUUID();
    UUID userId3 = UUID.randomUUID();
    UUID userId4 = UUID.randomUUID();
    UUID userId5 = UUID.randomUUID();
    Map<UUID, Location> uuidLocationMap = new LinkedHashMap<>();
    uuidLocationMap.put(userId1, new Location(1d, 1d));
    uuidLocationMap.put(userId2, new Location(1d, 1d));
    uuidLocationMap.put(userId3, new Location(1d, 1d));
    uuidLocationMap.put(userId4, new Location(1d, 1d));
    uuidLocationMap.put(userId5, new Location(1d, 1d));
    String json = objectMapper.writeValueAsString(uuidLocationMap);
    // WHEN
    when(locationClientMock.getAllLastLocation()).thenReturn(uuidLocationMap);
    // THEN
    mockMvc
        .perform(get(Url.GETALLCURRENTLOCATIONS))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.size()", IsEqual.equalTo(5)))
        .andExpect(result -> assertThat(result.getResponse().getContentAsString()).isEqualTo(json));
  }

  @RepeatedTest(value = 10)
  void getAllCurrentLocations_ShouldThrowDataNotFoundException() throws Exception {

    // WHEN
    when(locationClientMock.getAllLastLocation()).thenThrow(FeignException.class);
    // THEN
    mockMvc
        .perform(get(Url.GETALLCURRENTLOCATIONS))
        .andExpect(status().isInternalServerError())
        .andExpect(
            result ->
                assertTrue(result.getResolvedException() instanceof ResourceNotFoundException));
  }
}
