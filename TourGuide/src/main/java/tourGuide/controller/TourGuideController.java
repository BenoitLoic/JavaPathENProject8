package tourGuide.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tourGuide.service.UserService;
import tourGuide.user.User;
import static tourGuide.config.Url.INDEX;

@RestController
public class TourGuideController {

  private final Logger logger = LoggerFactory.getLogger(TourGuideController.class);
  @Autowired private UserService userService;

  @RequestMapping(value = INDEX)
  public String index() {
    return "Greetings from TourGuide!";
  }

  /**
   * This method get the User object in data storage with its userName.
   *
   * @param userName the user's userName
   * @return the User
   */
  @GetMapping("/getUser")
  private User getUser(@RequestParam String userName) {

    return userService.getUser(userName);
  }
}
