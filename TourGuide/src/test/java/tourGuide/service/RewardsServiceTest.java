package tourGuide.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import tourGuide.client.LocationClient;
import tourGuide.client.RewardClient;
import tourGuide.exception.DataNotFoundException;
import tourGuide.exception.ResourceNotFoundException;
import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.UserReward;
import tourGuide.model.VisitedLocation;
import tourGuide.user.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardsServiceTest {

  private final UUID userId = UUID.randomUUID();
  private final Date date = new Date();
  private final VisitedLocation visitedLocationTest =
      new VisitedLocation(UUID.randomUUID(), new Location(56d, 22d), date);
  private final Attraction attractionTest =
      new Attraction(
          "attractionNameTest",
          "attractionCityTest",
          "attractionStateTest",
          UUID.randomUUID(),
          new Location(
          22d,
          56d),
          null);

  @Mock RewardClient rewardClientMock;
  @InjectMocks RewardsServiceImpl rewardsService;

  @Test
  void calculateRewardsPoints() {}


  @Test
  void getRewardsShouldCallRewardClient1Time() throws InterruptedException {

    // GIVEN
    User userMock = new User(userId, "userNameTest", "phoneTest", "emailTest");
    userMock.addToVisitedLocations(visitedLocationTest);
    Collection<UserReward> expected = new ArrayList<>();
    expected.add(new UserReward(userId, visitedLocationTest, attractionTest, 50));

    // WHEN
    when(rewardClientMock.addUserReward(Mockito.any(), Mockito.any()))
        .thenReturn(new UserReward(userId, visitedLocationTest, attractionTest, 50));
    // THEN
    Collection<UserReward> actual = rewardsService.getRewards(userMock);
    TimeUnit.MILLISECONDS.sleep(15);
    verify(rewardClientMock, times(1)).addUserReward(userId, visitedLocationTest);
    assertThat(actual.size()).isEqualTo(1);
    assertThat(actual).isEqualTo(expected);

  }

  @Test
  void getRewardsShouldCallRewardClient5Times() throws InterruptedException {

    // GIVEN
    User userMock = new User(userId, "userNameTest", "phoneTest", "emailTest");
    userMock.addToVisitedLocations(visitedLocationTest);
    userMock.addToVisitedLocations(visitedLocationTest);
    userMock.addToVisitedLocations(visitedLocationTest);
    userMock.addToVisitedLocations(visitedLocationTest);
    userMock.addToVisitedLocations(visitedLocationTest);
    Collection<UserReward> expected = new ArrayList<>();
    expected.add(new UserReward(userId, visitedLocationTest, attractionTest, 50));
    expected.add(new UserReward(userId, visitedLocationTest, attractionTest, 50));
    expected.add(new UserReward(userId, visitedLocationTest, attractionTest, 50));
    expected.add(new UserReward(userId, visitedLocationTest, attractionTest, 50));
    expected.add(new UserReward(userId, visitedLocationTest, attractionTest, 50));

    // WHEN
    when(rewardClientMock.addUserReward(Mockito.any(), Mockito.any()))
        .thenReturn(new UserReward(userId, visitedLocationTest, attractionTest, 50));
    // THEN
    Collection<UserReward> actual = rewardsService.getRewards(userMock);
    TimeUnit.MILLISECONDS.sleep(15);
    verify(rewardClientMock, times(5)).addUserReward(userId, visitedLocationTest);
    assertThat(actual.size()).isEqualTo(5);
    assertThat(actual).isEqualTo(expected);

  }

@Disabled
  @Test
  void getRewards_WhenClientThrowException_ShouldThrowResourceNotFoundException(){

    // GIVEN
    User userMock = new User(userId, "userNameTest", "phoneTest", "emailTest");
    userMock.addToVisitedLocations(visitedLocationTest);
    // WHEN
    doThrow(feign.FeignException.class).when(rewardClientMock).addUserReward(any(),any());
    // THEN
    assertThrows(ResourceNotFoundException.class,()->rewardsService.getRewards(userMock));

  }

}
