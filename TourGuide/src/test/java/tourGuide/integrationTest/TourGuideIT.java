package tourGuide.integrationTest;

import tourGuide.client.LocationClient;
import tourGuide.config.Url;
import tourGuide.exception.DataNotFoundException;
import tourGuide.exception.IllegalArgumentException;
import tourGuide.model.Location;
import tourGuide.model.VisitedLocation;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TourGuideIT {

  private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000000");
  private final Date dateTest = new Date();
  private final VisitedLocation visitedLocationTest =
      new VisitedLocation(userId, new Location(50.54, 20.), dateTest);

  @Autowired MockMvc mockMvc;

  @MockBean LocationClient locationClientMock;

  @Test
  void getLocation() throws Exception {

    // GIVEN
    String userName = "internalUser0";
    // WHEN
    when(locationClientMock.addLocation(Mockito.any())).thenReturn(visitedLocationTest);
    // THEN
    mockMvc
        .perform(MockMvcRequestBuilders.get(Url.GETLOCATION)
                .param("userName", userName))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.latitude", is(20.0)))
        .andExpect(jsonPath("$.longitude", is(50.54)));

    mockMvc
            .perform(MockMvcRequestBuilders.get(Url.GETLOCATION)
                    .param("userName", ""))
            .andExpect(status().isBadRequest())
            .andExpect(result -> assertThat(result.getResolvedException() instanceof IllegalArgumentException).isTrue());

    mockMvc
            .perform(MockMvcRequestBuilders.get(Url.GETLOCATION)
                    .param("userName", "UNKNOWN_USER"))
            .andExpect(status().isNotFound())
            .andExpect(result -> assertThat(result.getResolvedException() instanceof DataNotFoundException).isTrue());

  }
}
