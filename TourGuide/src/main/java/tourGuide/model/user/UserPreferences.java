package tourGuide.model.user;

import java.util.Objects;
import javax.money.CurrencyUnit;
import javax.money.Monetary;
import org.javamoney.moneta.Money;

public class UserPreferences {

  private final CurrencyUnit currency = Monetary.getCurrency("USD");
  private int attractionProximity = Integer.MAX_VALUE;
  private Money lowerPricePoint = Money.of(0, currency);
  private Money highPricePoint = Money.of(Integer.MAX_VALUE, currency);
  private int tripDuration = 1;
  private int ticketQuantity = 1;
  private int numberOfAdults = 1;
  private int numberOfChildren = 0;

  public UserPreferences() {}

  public UserPreferences(UserPreferences userPreferences) {
    this.attractionProximity = userPreferences.attractionProximity;
    this.lowerPricePoint = userPreferences.lowerPricePoint;
    this.highPricePoint = userPreferences.highPricePoint;
    this.tripDuration = userPreferences.tripDuration;
    this.ticketQuantity = userPreferences.ticketQuantity;
    this.numberOfAdults = userPreferences.numberOfAdults;
    this.numberOfChildren = userPreferences.numberOfChildren;
  }

  public UserPreferences(
      int attractionProximity,
      Money lowerPricePoint,
      Money highPricePoint,
      int tripDuration,
      int ticketQuantity,
      int numberOfAdults,
      int numberOfChildren) {
    this.attractionProximity = attractionProximity;
    this.lowerPricePoint = lowerPricePoint;
    this.highPricePoint = highPricePoint;
    this.tripDuration = tripDuration;
    this.ticketQuantity = ticketQuantity;
    this.numberOfAdults = numberOfAdults;
    this.numberOfChildren = numberOfChildren;
  }

  public CurrencyUnit getCurrency() {
    return currency;
  }

  public int getAttractionProximity() {
    return attractionProximity;
  }

  public void setAttractionProximity(int attractionProximity) {
    this.attractionProximity = attractionProximity;
  }

  public Money getLowerPricePoint() {
    return lowerPricePoint;
  }

  public void setLowerPricePoint(Money lowerPricePoint) {
    this.lowerPricePoint = lowerPricePoint;
  }

  public Money getHighPricePoint() {
    return highPricePoint;
  }

  public void setHighPricePoint(Money highPricePoint) {
    this.highPricePoint = highPricePoint;
  }

  public int getTripDuration() {
    return tripDuration;
  }

  public void setTripDuration(int tripDuration) {
    this.tripDuration = tripDuration;
  }

  public int getTicketQuantity() {
    return ticketQuantity;
  }

  public void setTicketQuantity(int ticketQuantity) {
    this.ticketQuantity = ticketQuantity;
  }

  public int getNumberOfAdults() {
    return numberOfAdults;
  }

  public void setNumberOfAdults(int numberOfAdults) {
    this.numberOfAdults = numberOfAdults;
  }

  public int getNumberOfChildren() {
    return numberOfChildren;
  }

  public void setNumberOfChildren(int numberOfChildren) {
    this.numberOfChildren = numberOfChildren;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserPreferences that = (UserPreferences) o;
    return attractionProximity == that.attractionProximity
        && tripDuration == that.tripDuration
        && ticketQuantity == that.ticketQuantity
        && numberOfAdults == that.numberOfAdults
        && numberOfChildren == that.numberOfChildren
        && Objects.equals(currency, that.currency)
        && Objects.equals(lowerPricePoint, that.lowerPricePoint)
        && Objects.equals(highPricePoint, that.highPricePoint);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        attractionProximity,
        currency,
        lowerPricePoint,
        highPricePoint,
        tripDuration,
        ticketQuantity,
        numberOfAdults,
        numberOfChildren);
  }

  @Override
  public String toString() {
    return "UserPreferences{"
        + "attractionProximity="
        + attractionProximity
        + ", currency="
        + currency.getCurrencyCode()
        + ", lowerPricePoint="
        + lowerPricePoint.getNumber()
        + ", highPricePoint="
        + highPricePoint.getNumber()
        + ", tripDuration="
        + tripDuration
        + ", ticketQuantity="
        + ticketQuantity
        + ", numberOfAdults="
        + numberOfAdults
        + ", numberOfChildren="
        + numberOfChildren
        + '}';
  }
}
