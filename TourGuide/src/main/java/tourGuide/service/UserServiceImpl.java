package tourGuide.service;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tourGuide.client.UserClient;
import tourGuide.dto.AddNewUser;
import tourGuide.exception.DataNotFoundException;
import tourGuide.helper.InternalTestRepository;
import tourGuide.user.User;

@Service
public class UserServiceImpl implements UserService {
  protected final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
  private final Map<String, User> internalUserMap;

  private final UserClient userClient;

  @Autowired
  public UserServiceImpl(UserClient userClient) {
    this.userClient = userClient;
    logger.debug("Initializing users");
    internalUserMap = InternalTestRepository.getInternalUserMap();
    logger.error("Finished initializing " + internalUserMap.size() + " users");
  }

  /**
   * INTERNAL TEST METHOD this method get the user with the given username.
   *
   * @param userName the username
   * @return the user
   */
  public User getUser(String userName) {
    User user = internalUserMap.get(userName);
    if (user == null) {
      logger.warn("Error, username : " + userName + " doesn't exist.");
      throw new DataNotFoundException("error user doesn't exist.");
    }
    return user;
  }

  /**
   * INTERNAL TEST METHOD this method get the list of all users.
   *
   * @return the list of users
   */
  public CopyOnWriteArrayList<User> getAllUsers() {
    return new CopyOnWriteArrayList<>(internalUserMap.values());
  }

  /**
   * INTERNAL TEST METHOD This method add a new user to the database.
   *
   * @param user the user to add
   */
  public void addUser(User user) {
    if (!internalUserMap.containsKey(user.getUserName())) {
      logger.debug("add new user:" + user.getUserName());
      internalUserMap.put(user.getUserName(), user);
    }
    AddNewUser newUser =
        new AddNewUser(user.getUserName(), user.getPhoneNumber(), user.getEmailAddress());
    userClient.addUser(newUser);
  }
}
