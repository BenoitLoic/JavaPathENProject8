import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import feign.FeignException;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tourGuide.Application;
import tourGuide.client.LocationClient;
import tourGuide.client.RewardClient;
import tourGuide.client.UserClient;
import tourGuide.config.Url;
import tourGuide.exception.DataNotFoundException;
import tourGuide.exception.IllegalArgumentException;
import tourGuide.exception.ResourceNotFoundException;
import tourGuide.helper.InternalTestRepository;
import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.VisitedLocation;
import tourGuide.service.RewardsService;
import tourGuide.service.UserService;
import tourGuide.service.UserServiceImpl;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class LocationIT {

  private final UUID userId = UUID.randomUUID();
  private final Date dateTest = new Date();
  private VisitedLocation visitedLocationTest;

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @MockBean LocationClient locationClientMock;
  @MockBean UserClient userClientMock;
  @SpyBean UserService userServiceMock;
  @MockBean RewardClient rewardClientMock;

  @BeforeEach
  void setUp() {
    visitedLocationTest = new VisitedLocation(userId, new Location(50.54, 20.), dateTest);
  }

  @Test
  void getLocationValid() throws Exception {

    // GIVEN
    String userName = "internalUser0";
    System.out.println(InternalTestRepository.getInternalUserMap().size());
    // WHEN
    when(locationClientMock.getLocation(Mockito.any())).thenReturn(visitedLocationTest);
    // THEN
    mockMvc
        .perform(get(Url.GET_LOCATION).param("userName", userName))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.latitude", is(20.0)))
        .andExpect(jsonPath("$.longitude", is(50.54)));
  }

  @Test
  void getLocationInvalid() throws Exception {
    mockMvc
        .perform(get(Url.GET_LOCATION).param("userName", ""))
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertThat(result.getResolvedException() instanceof IllegalArgumentException)
                    .isTrue());
  }

  @Test
  void getLocationWhenUserDoesntExist_ShouldThrowDataNotFoundException() throws Exception {

    mockMvc
        .perform(get(Url.GET_LOCATION).param("userName", "UNKNOWN_USER"))
        .andExpect(status().isNotFound())
        .andExpect(
            result ->
                assertThat(result.getResolvedException() instanceof DataNotFoundException)
                    .isTrue());
  }

  @Test
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
        .perform(get(Url.GET_ALL_CURRENT_LOCATIONS))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.size()", IsEqual.equalTo(5)))
        .andExpect(result -> assertThat(result.getResponse().getContentAsString()).isEqualTo(json));
  }

  @Test
  void getAllCurrentLocations_ShouldThrowDataNotFoundException() throws Exception {

    // WHEN
    when(locationClientMock.getAllLastLocation()).thenThrow(FeignException.class);
    // THEN
    mockMvc
        .perform(get(Url.GET_ALL_CURRENT_LOCATIONS))
        .andExpect(status().isInternalServerError())
        .andExpect(
            result ->
                assertTrue(result.getResolvedException() instanceof ResourceNotFoundException));
  }

  @Test
  void getNearbyAttractions() throws Exception {

    // GIVEN I'm registered
    Attraction attraction =
        new Attraction("name", "city", "state", UUID.randomUUID(), new Location(10d, 10d), 15.);
    List<Attraction> attractions =
        Arrays.asList(attraction, attraction, attraction, attraction, attraction);
    // WHEN I request with my username
    when(locationClientMock.getLocation(any(UUID.class))).thenReturn(visitedLocationTest);
    when(locationClientMock.getNearbyAttractions(anyDouble(), anyDouble())).thenReturn(attractions);
    when(rewardClientMock.getReward(any(), any())).thenReturn(500000);
    // THEN I get a list with 5 attractions

    MvcResult mvcResult =
        mockMvc
            .perform(get(Url.GET_NEARBY_ATTRACTIONS).param("userName", "internalUser5"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andReturn();

    ObjectNode root =
        (ObjectNode) objectMapper.readTree(mvcResult.getResponse().getContentAsString());
    assertThat(root.get("Location[longitude=50.54, latitude=20.0]").size()).isEqualTo(5);
  }

  @Test
  void getNearbyAttractions_InvalidUsername_ShouldThrowIllegalArgumentException() throws Exception {

    // GIVEN I'm registered

    // WHEN I request without my username

    // THEN I get an error
    mockMvc
        .perform(get(Url.GET_NEARBY_ATTRACTIONS).param("userName", ""))
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertThat(result.getResolvedException() instanceof IllegalArgumentException).isTrue());
  }

  @Test
  void getNearbyAttractions_NotRegistered_ShouldThrowDataNotFoundException() throws Exception {

    // GIVEN I'm NOT registered

    // WHEN I request with a username

    // THEN I get an error
    mockMvc
            .perform(get(Url.GET_NEARBY_ATTRACTIONS).param("userName", "mrNo"))
            .andExpect(status().isNotFound())
            .andExpect(
                    result ->
                            assertThat(result.getResolvedException() instanceof DataNotFoundException).isTrue());
  }
}
