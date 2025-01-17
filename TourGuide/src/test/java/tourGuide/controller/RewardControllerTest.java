package tourGuide.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tourGuide.config.Url;
import tourGuide.exception.DataNotFoundException;
import tourGuide.exception.IllegalArgumentException;
import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.UserReward;
import tourGuide.model.VisitedLocation;
import tourGuide.service.RewardsServiceImpl;
import tourGuide.service.UserServiceImpl;
import tourGuide.model.user.User;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tourGuide.config.Url.GET_REWARDS;

@WebMvcTest(controllers = RewardsControllerImpl.class)
@AutoConfigureMockMvc
public class RewardControllerTest {

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
  @MockBean RewardsServiceImpl rewardsServiceMock;

  @Test
  void getRewardsValid() throws Exception {

    // GIVEN
    List<UserReward> userRewards =
        Arrays.asList(
            new UserReward(userId, visitedLocationTest, attractionTest, 5),
            new UserReward(userId, visitedLocationTest, attractionTest, 5),
            new UserReward(userId, visitedLocationTest, attractionTest, 5));
    // WHEN
    when(userServiceImplMock.getUser(anyString())).thenReturn(validUser);
    when(rewardsServiceMock.getRewards(Mockito.any())).thenReturn(userRewards);
    // THEN
    mockMvc
        .perform(get(Url.GET_REWARDS).param("userName", validUserName))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void getRewardsInvalid() throws Exception {

    // GIVEN

    // WHEN

    // THEN
    mockMvc
        .perform(get(Url.GET_REWARDS).param("userName", ""))
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertTrue(result.getResolvedException() instanceof IllegalArgumentException));
  }

  @Test
  void getRewardsWhenUserDoesntExist_ShouldThrowDataNotFoundException() throws Exception {

    // GIVEN

    // WHEN
    doThrow(DataNotFoundException.class).when(userServiceImplMock).getUser(Mockito.anyString());
    // THEN
    mockMvc
        .perform(get(GET_REWARDS).param("userName", validUserName))
        .andExpect(status().isNotFound())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof DataNotFoundException));
  }
}
