package tourGuide.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import tourGuide.client.RewardClient;
import tourGuide.dto.GetNearbyAttractionDto;
import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.UserReward;
import tourGuide.model.VisitedLocation;
import tourGuide.user.User;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
          new Location(22d, 56d),
          null);

  @Mock RewardClient rewardClientMock;
  @InjectMocks RewardsServiceImpl rewardsService;

  @Test
  void calculateRewardsPoints_ShouldReturn5() {

    // GIVEN
    Collection<Attraction> attractions = new ArrayList<>();
    attractions.add(attractionTest);
    // WHEN
    when(rewardClientMock.getReward(any(UUID.class), any(UUID.class))).thenReturn(5);
    // THEN
    Collection<GetNearbyAttractionDto> actual =
        rewardsService.calculateRewardsPoints(attractions, userId);
    assertThat(actual.stream().findFirst().isPresent()).isTrue();
    assertThat(actual.size()).isEqualTo(1);
    assertThat(actual.stream().findFirst().get().getRewardPoint()).isEqualTo(5);
  }

  @Test
  void calculateRewardsPoints_ShouldReturn25() {

    // GIVEN
    Collection<Attraction> attractions = new ArrayList<>();
    attractions.add(attractionTest);
    attractions.add(attractionTest);
    attractions.add(attractionTest);
    attractions.add(attractionTest);
    attractions.add(attractionTest);
    // WHEN
    when(rewardClientMock.getReward(any(UUID.class), any(UUID.class))).thenReturn(5);
    // THEN
    Collection<GetNearbyAttractionDto> actual =
        rewardsService.calculateRewardsPoints(attractions, userId);
    AtomicInteger count = new AtomicInteger();
    actual.forEach(
        u -> {
          Integer rewardPoint = u.getRewardPoint();
          count.getAndAdd(rewardPoint);
        });
    System.out.println(count);
    assertThat(actual.size()).isEqualTo(5);
    assertThat(count.intValue()).isEqualTo(25);
    verify(rewardClientMock, times(5)).getReward(any(UUID.class), any(UUID.class));
  }

  @Test
  void calculateRewardsPoints_ShouldReturnEmptyList() {

    // GIVEN
    Collection<Attraction> attractions = new ArrayList<>();

    // WHEN

    // THEN
    Collection<GetNearbyAttractionDto> actual =
        rewardsService.calculateRewardsPoints(attractions, userId);
    assertThat(actual.isEmpty()).isTrue();
    verify(rewardClientMock, times(0)).getReward(any(UUID.class), any(UUID.class));
  }

  @Test
  void addRewardsShouldCallRewardClient1Time() {

    // GIVEN
    User userMock = new User(userId, "userNameTest", "phoneTest", "emailTest");
    userMock.addToVisitedLocations(visitedLocationTest);

    // WHEN
    when(rewardClientMock.addUserReward(Mockito.any(), Mockito.any()))
        .thenReturn(new UserReward(userId, visitedLocationTest, attractionTest, 50));
    // THEN
    rewardsService.addRewards(userMock);

    // wait for completableFuture
    rewardsService.awaitTerminationAfterShutdown();

    verify(rewardClientMock, times(1)).addUserReward(userId, visitedLocationTest);
    assertThat(userMock.getUserRewards().size()).isEqualTo(1);
    assertThat(userMock.getUserRewards().get(0).rewardPoints()).isEqualTo(50);
  }

  @Test
  void addRewards_ShouldCallRewardClient5Times_ShouldAdd1Reward() {

    // GIVEN
    User userMock = new User(userId, "userNameTest", "phoneTest", "emailTest");
    userMock.addToVisitedLocations(visitedLocationTest);
    userMock.addToVisitedLocations(visitedLocationTest);
    userMock.addToVisitedLocations(visitedLocationTest);
    userMock.addToVisitedLocations(visitedLocationTest);
    userMock.addToVisitedLocations(visitedLocationTest);
    Collection<UserReward> expected = new ArrayList<>();
    expected.add(new UserReward(userId, visitedLocationTest, attractionTest, 50));

    // WHEN
    when(rewardClientMock.addUserReward(Mockito.any(), Mockito.any()))
        .thenReturn(new UserReward(userId, visitedLocationTest, attractionTest, 50));
    // THEN
    rewardsService.addRewards(userMock);

    // wait for completableFuture
    rewardsService.awaitTerminationAfterShutdown();

    assertThat(userMock.getUserRewards().size()).isEqualTo(expected.size());
    verify(rewardClientMock, times(5)).addUserReward(userId, visitedLocationTest);

    assertThat(userMock.getUserRewards()).isEqualTo(expected);
  }

  @Test
  void addRewards_ShouldCallRewardClient3Times_ShouldAdd2Rewards() {

    // GIVEN
    User userMock = new User(userId, "userNameTest", "phoneTest", "emailTest");
    userMock.addToVisitedLocations(visitedLocationTest);
    userMock.addToVisitedLocations(visitedLocationTest);
    userMock.addToVisitedLocations(visitedLocationTest);
    Attraction attractionTest2 =
        new Attraction(
            "attractionNameTest",
            "attractionCityTest",
            "attractionStateTest",
            UUID.randomUUID(),
            new Location(22d, 56d),
            null);
    Collection<UserReward> expected = new ArrayList<>();
    expected.add(new UserReward(userId, visitedLocationTest, attractionTest, 50));
    expected.add(new UserReward(userId, visitedLocationTest, attractionTest2, 50));

    // WHEN
    when(rewardClientMock.addUserReward(Mockito.any(), Mockito.any()))
        .thenReturn(
            new UserReward(userId, visitedLocationTest, attractionTest, 50),
            new UserReward(userId, visitedLocationTest, attractionTest, 50),
            new UserReward(userId, visitedLocationTest, attractionTest2, 50));
    // THEN
    rewardsService.addRewards(userMock);

    // wait for completableFuture
    rewardsService.awaitTerminationAfterShutdown();

    assertThat(userMock.getUserRewards().size()).isEqualTo(expected.size());
    verify(rewardClientMock, times(3)).addUserReward(userId, visitedLocationTest);

    assertThat(userMock.getUserRewards()).isEqualTo(expected);
  }

  @Test
  void getRewards_ShouldReturn2Rewards() {

    // GIVEN
    User userMock = new User(userId, "userNameTest", "phoneTest", "emailTest");
    userMock.addUserReward(new UserReward(userId, visitedLocationTest, attractionTest, 50));
    userMock.addUserReward(new UserReward(userId, visitedLocationTest, attractionTest, 50));
    // WHEN
    Collection<UserReward> rewards = rewardsService.getRewards(userMock);
    // THEN
    assertThat(rewards.size()).isEqualTo(2);
  }

  @Test
  void getRewards_ShouldReturnEmptyList() {

    // GIVEN
    User userMock = new User(userId, "userNameTest", "phoneTest", "emailTest");
    // WHEN
    Collection<UserReward> rewards = rewardsService.getRewards(userMock);
    // THEN
    assertThat(rewards.isEmpty()).isTrue();
  }
}
