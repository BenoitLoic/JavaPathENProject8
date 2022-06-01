import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tourGuide.Application;
import tourGuide.client.RewardClient;
import tourGuide.config.Url;
import tourGuide.exception.DataNotFoundException;
import tourGuide.exception.IllegalArgumentException;
import tourGuide.helper.InternalTestRepository;
import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.UserReward;
import tourGuide.model.VisitedLocation;
import tourGuide.service.UserServiceImpl;
import tourGuide.model.user.User;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RewardsIT {

  private final UUID attractionId = UUID.randomUUID();
  private final UUID userId = UUID.randomUUID();
  private final Date dateTest = new Date();
  private final VisitedLocation visitedLocationTest =
      new VisitedLocation(userId, new Location(50.54, 20.), dateTest);
  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @MockBean RewardClient rewardClientMock;
  @Autowired UserServiceImpl tourGuideService;
  private User user;
  private UserReward userReward;

  @BeforeEach
  void setUp() {
    user = new User(userId, "username", "phone", "email");
    Attraction attraction =
        new Attraction(
            "attractionName",
            "cityTest",
            "state",
            attractionId,
            visitedLocationTest.location(),
            1d);
    userReward = new UserReward(userId, visitedLocationTest, attraction, 50);
  }

  @Test
  void getRewards() throws Exception {

    // GIVEN i'm registered, have rewards, i give my username
    user.addUserReward(userReward);
    InternalTestRepository.getInternalUserMap().put(user.getUserName(), user);
    // WHEN

    // THEN i get my rewards
    MvcResult result =
        mockMvc
            .perform(get(Url.GET_REWARDS).param("userName", "username"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

    UserReward[] userRewards =
        objectMapper.readValue(result.getResponse().getContentAsString(), UserReward[].class);
    UserReward resultReward = Arrays.stream(userRewards).findFirst().get();
    assertThat(userReward).isEqualTo(resultReward);
    InternalTestRepository.getInternalUserMap().remove(user.getUserName());
  }

  @Test
  void getRewards_WithNoRewards_ReturnEmptyList() throws Exception {

    // GIVEN i'm registered, have NO rewards, i give my username
    InternalTestRepository.getInternalUserMap().put(user.getUserName(), user);

    // WHEN

    // THEN i get an empty list
    MvcResult result =
        mockMvc
            .perform(get(Url.GET_REWARDS).param("userName", "username"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

    UserReward[] userRewards =
        objectMapper.readValue(result.getResponse().getContentAsString(), UserReward[].class);
    assertThat(userRewards).isEmpty();
    InternalTestRepository.getInternalUserMap().remove(user.getUserName());
  }

  @Test
  void getRewards_InvalidUsername_ThrowIllegalArgumentException() throws Exception {

    // GIVEN i'm registered, i don't give my username

    // WHEN

    // THEN i get an error
    mockMvc
        .perform(get(Url.GET_REWARDS).param("userName", ""))
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertThat(result.getResolvedException() instanceof IllegalArgumentException)
                    .isTrue());
  }

  @Test
  void getRewards_InvalidUnknownUser_ThrowDataNotFoundException() throws Exception {

    // GIVEN i'm not registered

    // WHEN

    // THEN i get an error
    mockMvc
        .perform(get(Url.GET_REWARDS).param("userName", "Remi sans compte"))
        .andExpect(status().isNotFound())
        .andExpect(
            result ->
                assertThat(result.getResolvedException() instanceof DataNotFoundException)
                    .isTrue());
  }
}
