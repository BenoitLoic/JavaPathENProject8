package tourGuide.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;
import java.util.UUID;


public class GetNearbyAttractionDto {
  private String attractionName;
  @JsonIgnore
  private String city;
  @JsonIgnore private String state;
  @JsonIgnore() private UUID attractionId;
  private Double latitude;
  private Double longitude;
  private Double distance;
  private Integer rewardPoint;

  public GetNearbyAttractionDto() {}

  public GetNearbyAttractionDto(
      String attractionName,
      String city,
      String state,
      UUID attractionId,
      Double latitude,
      Double longitude,
      Double distance,
      Integer rewardPoint) {
    this.attractionName = attractionName;
    this.city = city;
    this.state = state;
    this.attractionId = attractionId;
    this.latitude = latitude;
    this.longitude = longitude;
    this.distance = distance;
    this.rewardPoint = rewardPoint;
  }

  public String getAttractionName() {
    return attractionName;
  }

  public void setAttractionName(String attractionName) {
    this.attractionName = attractionName;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public UUID getAttractionId() {
    return attractionId;
  }

  public void setAttractionId(UUID attractionId) {
    this.attractionId = attractionId;
  }

  public Double getLatitude() {
    return latitude;
  }

  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }

  public Double getLongitude() {
    return longitude;
  }

  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }

  public Double getDistance() {
    return distance;
  }

  public void setDistance(Double distance) {
    this.distance = distance;
  }

  public Integer getRewardPoint() {
    return rewardPoint;
  }

  public void setRewardPoint(Integer rewardPoint) {
    this.rewardPoint = rewardPoint;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    GetNearbyAttractionDto that = (GetNearbyAttractionDto) o;
    return Objects.equals(attractionName, that.attractionName)
        && Objects.equals(city, that.city)
        && Objects.equals(state, that.state)
        && Objects.equals(attractionId, that.attractionId)
        && Objects.equals(latitude, that.latitude)
        && Objects.equals(longitude, that.longitude)
        && Objects.equals(distance, that.distance)
        && Objects.equals(rewardPoint, that.rewardPoint);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        attractionName, city, state, attractionId, latitude, longitude, distance, rewardPoint);
  }

  @Override
  public String toString() {
    return "getNearbyAttractionDto{"
        + "attractionName='"
        + attractionName
        + '\''
        + ", city='"
        + city
        + '\''
        + ", state='"
        + state
        + '\''
        + ", attractionId="
        + attractionId
        + ", latitude="
        + latitude
        + ", longitude="
        + longitude
        + ", distance="
        + distance
        + ", rewardPoint="
        + rewardPoint
        + '}';
  }
}
