package tourGuide.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import tourGuide.model.UserReward;
import tourGuide.model.VisitedLocation;
import tripPricer.Provider;

public class User {
  private final List<VisitedLocation> visitedLocations = new CopyOnWriteArrayList<>();
  private final List<UserReward> userRewards = new ArrayList<>();
  private UUID userId;
  private String userName;
  private String phoneNumber;
  private String emailAddress;
  private Date latestLocationTimestamp;
  private UserPreferences userPreferences = new UserPreferences();
  private List<Provider> tripDeals = new ArrayList<>();

  public User() {}

  public User(UUID userId, String userName, String phoneNumber, String emailAddress) {
    this.userId = userId;
    this.userName = userName;
    this.phoneNumber = phoneNumber;
    this.emailAddress = emailAddress;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getUserName() {
    return userName;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public Date getLatestLocationTimestamp() {

    if (latestLocationTimestamp != null) {
      return new Date(latestLocationTimestamp.getTime());
    }
    return null;
  }

  public void setLatestLocationTimestamp(Date latestLocationTimestamp) {
    this.latestLocationTimestamp = new Date(latestLocationTimestamp.getTime());
  }

  public void addToVisitedLocations(VisitedLocation visitedLocation) {
    visitedLocations.add(visitedLocation);
  }

  public List<VisitedLocation> getVisitedLocations() {
    return List.copyOf(visitedLocations);
  }

  public void clearVisitedLocations() {
    visitedLocations.clear();
  }

  public void addUserReward(UserReward userReward) {
    userRewards.add(userReward);
  }

  public List<UserReward> getUserRewards() {
    return new ArrayList<>(userRewards);
  }

  public UserPreferences getUserPreferences() {
    return new UserPreferences(userPreferences);
  }

  public void setUserPreferences(UserPreferences userPreferences) {
    this.userPreferences = new UserPreferences(userPreferences);
  }

  public VisitedLocation getLastVisitedLocation() {
    return visitedLocations.get(visitedLocations.size() - 1);
  }

  public List<Provider> getTripDeals() {
    return new ArrayList<>(tripDeals);
  }

  public void setTripDeals(List<Provider> tripDeals) {
    this.tripDeals = new ArrayList<>(tripDeals);
  }

  @Override
  public String toString() {
    return "User{"
        + "userId="
        + userId
        + ", userName='"
        + userName
        + '\''
        + ", phoneNumber='"
        + phoneNumber
        + '\''
        + ", emailAddress='"
        + emailAddress
        + '\''
        + ", latestLocationTimestamp="
        + latestLocationTimestamp
        + ", visitedLocations="
        + visitedLocations
        + ", userRewards="
        + userRewards
        + ", userPreferences="
        + userPreferences
        + ", tripDeals="
        + tripDeals
        + '}';
  }
}
