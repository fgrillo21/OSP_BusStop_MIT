package busstop.customtrip.model;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial") //With this annotation we are going to hide compiler warnings
public class CustomTrip implements Serializable {
    private final float monuments;
    private final float greenAreas;
    private final float openSpaces;
    private final int maxStops;
    private final int maxDurationMinutes;
    private final List<Place> intermediatePlaces;

    public static final int MAX = 100;

    public static CustomTripBuilder newActivityGroup() {
        return new CustomTripBuilder();
    }

    private CustomTrip(float monuments, float greenAreas, float openSpaces, int maxStops, int maxDurationMinutes, List<Place> intermediatePlaces) {
        this.monuments = monuments;
        this.greenAreas = greenAreas;
        this.openSpaces = openSpaces;
        this.maxStops = maxStops;
        this.maxDurationMinutes = maxDurationMinutes;
        this.intermediatePlaces = intermediatePlaces;
    }

    public float getMonuments() {
        return monuments;
    }

    public float getGreenAreas() {
        return greenAreas;
    }

    public float getOpenSpaces() {
        return openSpaces;
    }

    public int getMaxStops() {
        return maxStops;
    }

    public int getMaxDurationMinutes() {
        return maxDurationMinutes;
    }

    public List<Place> getIntermediatePlaces() {
        return intermediatePlaces;
    }

    public static final class CustomTripBuilder {
        private float monuments;
        private float greenAreas;
        private float openSpaces;
        private int maxStops;
        private int maxDurationMinutes;
        private List<Place> intermediatePlaces;

        public CustomTripBuilder withMonuments(float monuments) {
            this.monuments = monuments;
            return this;
        }

        public CustomTripBuilder withGreenAreas(float greenAreas) {
            this.greenAreas = greenAreas;
            return this;
        }

        public CustomTripBuilder withOpenSpaces(float openSpaces) {
            this.openSpaces = openSpaces;
            return this;
        }

        public CustomTripBuilder withMaxStops(int maxStops) {
            this.maxStops = maxStops;
            return this;
        }

        public CustomTripBuilder withMaxDurationMinutes(int maxDurationMinutes) {
            this.maxDurationMinutes = maxDurationMinutes;
            return this;
        }

        public CustomTripBuilder withIntermediatePlaces(List<Place> intermediatePlaces) {
            this.intermediatePlaces = intermediatePlaces;
            return this;
        }

        public CustomTrip build() {
            return new CustomTrip(monuments, greenAreas, openSpaces, maxStops, maxDurationMinutes, intermediatePlaces);
        }
    }

    @Override
    public String toString() {
        return "CustomTrip{" +
                "monuments=" + monuments +
                ", greenAreas=" + greenAreas +
                ", openSpaces=" + openSpaces +
                '}';
    }
}
