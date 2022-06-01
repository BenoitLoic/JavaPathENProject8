package tourGuide.tracker;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import tourGuide.service.LocationService;
import tourGuide.service.RewardsService;
import tourGuide.service.UserService;
import tourGuide.model.user.User;

@ExtendWith(MockitoExtension.class)
class TrackerTest {

  @Mock LocationService locationServiceMock;
  @Mock RewardsService rewardsServiceMock;
  @Mock UserService userServiceMock;

  @BeforeEach
  void setUp() {}

  @AfterEach
  void tearDown() {}

  @Test
  void track100Users_ShouldCallClient100Times() throws InterruptedException {

    // GIVEN
    CopyOnWriteArrayList<User> users = new CopyOnWriteArrayList<>();
    IntStream.range(0, 100)
        .forEach(
            i -> {
              users.add(
                  new User(
                      UUID.randomUUID(), String.valueOf(i), String.valueOf(i), String.valueOf(i)));
            });

    // WHEN
    Mockito.when(userServiceMock.getAllUsers()).thenReturn(users);
    Mockito.doNothing().when(locationServiceMock).trackUserLocation(Mockito.any(User.class));
    Mockito.doNothing().when(rewardsServiceMock).addRewardsForLastLocation(Mockito.any(User.class));

    Thread t =
        new Thread(() -> new Tracker(locationServiceMock, rewardsServiceMock, userServiceMock));
    t.start();
    TimeUnit.MILLISECONDS.sleep(100);
    // THEN
    Mockito.verify(userServiceMock, Mockito.times(1)).getAllUsers();
    Mockito.verify(rewardsServiceMock, Mockito.times(100))
        .addRewardsForLastLocation(Mockito.any(User.class));
    Mockito.verify(locationServiceMock, Mockito.times(100))
        .trackUserLocation(Mockito.any(User.class));
  }

  @Test
  void tracker_ShouldCallRewardClientAfterLocation() throws InterruptedException {

    // GIVEN
    UUID userId1 = UUID.randomUUID();
    UUID userId2 = UUID.randomUUID();
    CopyOnWriteArrayList<User> users = new CopyOnWriteArrayList<>();
    User user1 = new User(userId1, "usernameTest", "phoneTest", "emailTest");
    User user2 = new User(userId2, "usernameTest", "phoneTest", "emailTest");
    users.add(user1);
    users.add(user2);
    // WHEN
    Mockito.when(userServiceMock.getAllUsers()).thenReturn(users);
    Mockito.doNothing().when(locationServiceMock).trackUserLocation(Mockito.any(User.class));
    Mockito.doNothing().when(rewardsServiceMock).addRewardsForLastLocation(Mockito.any(User.class));
    Thread t =
        new Thread(() -> new Tracker(locationServiceMock, rewardsServiceMock, userServiceMock));
    t.start();
    TimeUnit.MILLISECONDS.sleep(100);
    // THEN
    InOrder inOrder = Mockito.inOrder(locationServiceMock, rewardsServiceMock);
    inOrder.verify(locationServiceMock).trackUserLocation(user1);
    inOrder.verify(rewardsServiceMock).addRewardsForLastLocation(user1);
  }

}
