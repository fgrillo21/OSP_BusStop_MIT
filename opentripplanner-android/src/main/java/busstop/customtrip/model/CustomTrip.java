package busstop.customtrip.model;

import java.io.Serializable;

@SuppressWarnings("serial") //With this annotation we are going to hide compiler warnings
public class CustomTrip implements Serializable {
    private final int monuments;
    private final int greenAreas;
    private final int openSpaces;

    public static CustomTripBuilder newActivityGroup() {
        return new CustomTripBuilder();
    }

    private CustomTrip(int monuments, int greenAreas, int openSpaces) {
        this.monuments = monuments;
        this.greenAreas = greenAreas;
        this.openSpaces = openSpaces;
    }

    public int getMonuments() {
        return monuments;
    }

    public int getGreenAreas() {
        return greenAreas;
    }

    public int getOpenSpaces() {
        return openSpaces;
    }

    public static final class CustomTripBuilder {
        private int monuments;
        private int greenAreas;
        private int openSpaces;

        public CustomTripBuilder withMonuments(int monuments) {
            this.monuments = monuments;
            return this;
        }

        public CustomTripBuilder withGreenAreas(int greenAreas) {
            this.greenAreas = greenAreas;
            return this;
        }

        public CustomTripBuilder withOpenSpaces(int openSpaces) {
            this.openSpaces = openSpaces;
            return this;
        }

        public CustomTrip build() {
            return new CustomTrip(monuments, greenAreas, openSpaces);
        }
    }
}
