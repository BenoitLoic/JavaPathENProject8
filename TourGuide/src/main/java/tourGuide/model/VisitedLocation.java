package tourGuide.model;

import java.util.Date;
import java.util.UUID;
import javax.validation.constraints.NotNull;

/**
 * Record for VisitedLocation object.
 * Contain Validation annotation for javax validation.
 *
 * @param userId      the user id, can't be null
 * @param location    the Location, can't be null
 * @param timeVisited the date
 */
public record VisitedLocation(@NotNull UUID userId, @NotNull Location location, Date timeVisited) {
    public VisitedLocation(@NotNull UUID userId, @NotNull Location location, Date timeVisited) {
        this.userId = userId;
        this.location = location;
        this.timeVisited = new Date(timeVisited.getTime());
    }

    @Override
    public Date timeVisited() {
        return new Date(timeVisited.getTime());
    }
}
