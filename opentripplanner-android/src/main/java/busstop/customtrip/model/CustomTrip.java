package busstop.customtrip.model;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial") //With this annotation we are going to hide compiler warnings
public class CustomTrip implements Serializable {
    private final float monuments;
    private final float greenAreas;
    private final float openSpaces;
    private int maxStops;
    private int maxDurationMinutes;
    private List<Place> intermediatePlaces;

    public static final int MAX = 100;

    public static CustomTripBuilder newActivityGroup() {
        return new CustomTripBuilder();
    }

    private CustomTrip(float monuments, float greenAreas, float openSpaces) {
        this.monuments = monuments;
        this.greenAreas = greenAreas;
        this.openSpaces = openSpaces;
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

    public static final class CustomTripBuilder {
        private float monuments;
        private float greenAreas;
        private float openSpaces;

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

        public CustomTrip build() {
            return new CustomTrip(monuments, greenAreas, openSpaces);
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
