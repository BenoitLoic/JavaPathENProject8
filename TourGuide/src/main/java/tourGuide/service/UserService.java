package tourGuide.service;

import java.util.concurrent.CopyOnWriteArrayList;
import tourGuide.model.user.User;

/** Service for user feature. */
public interface UserService {

  /**
   * Get the user with the given username.
   *
   * @param userName the username
   * @return the user
   */
  User getUser(String userName);
  /**
   * Get the list of all users.
   *
   * @return the list of users
   */
  CopyOnWriteArrayList<User> getAllUsers();
  /**
   * Add a new user to the database.
   *
   * @param user the user to add
   */
  void addUser(User user);
}
