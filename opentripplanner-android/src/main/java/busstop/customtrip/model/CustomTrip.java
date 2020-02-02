package busstop.customtrip.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial") //With this annotation we are going to hide compiler warnings
public class CustomTrip implements Serializable {
    private final float monuments;
    private final float greenAreas;
    private final float openSpaces;
    private final int maxStops;
    private final boolean isLimitDurationOn;
    private final int maxDurationMinutes;
    private final List<Place> intermediatePlaces;

    public static final int MAX = 100;

    /* Questo metodo si può usare se si vuole creare un nuovo customTrip da zero
    * al momento rimane private perché non dovrebbe servire all'esterno in questo progetto */
    private static CustomTripBuilder newCustomTrip() {
        return new CustomTripBuilder();
    }

    /* Questo metodo permette di aggiornare dei campi della classe senza sovrascrivere i valori già inizializzati di altri campi */
    public static CustomTripBuilder newCustomTrip(CustomTrip customTrip) {
        return CustomTrip.newCustomTrip()
                .withMonuments(customTrip.getMonuments())
                .withGreenAreas(customTrip.getGreenAreas())
                .withOpenSpaces(customTrip.getOpenSpaces())
                .withMaxStops(customTrip.getMaxStops())
                .withIsLimitDurationOn(customTrip.isLimitDurationOn())
                .withMaxDurationMinutes(customTrip.getMaxDurationMinutes())
                .withIntermediatePlaces(customTrip.getIntermediatePlaces());
    }

    /* Questo metodo inizializza dei valori di default in modo da non rischiare di avere dei NPE (null pointer exception)
    * Viene usato solo la prima volta dentro l'activity PresetActivity, che è quella dove si richiama la classe all'inizio */
    public static CustomTrip getCustomTripDefaultValues() {
        return CustomTrip.newCustomTrip()
                .withMonuments(0)
                .withGreenAreas(0)
                .withOpenSpaces(0)
                .withMaxStops(2)
                .withIsLimitDurationOn(false)
                .withMaxDurationMinutes(70)
                .withIntermediatePlaces(new ArrayList<Place>())
                .build();
    }

    private CustomTrip(float monuments, float greenAreas, float openSpaces, int maxStops, boolean isLimitDurationOn, int maxDurationMinutes, List<Place> intermediatePlaces) {
        this.monuments = monuments;
        this.greenAreas = greenAreas;
        this.openSpaces = openSpaces;
        this.maxStops = maxStops;
        this.isLimitDurationOn = isLimitDurationOn;
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

    public boolean isLimitDurationOn() {
        return isLimitDurationOn;
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
        private boolean isLimitDurationOn;
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

        public CustomTripBuilder withIsLimitDurationOn(boolean isLimitDurationOn) {
            this.isLimitDurationOn = isLimitDurationOn;
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
            return new CustomTrip(monuments, greenAreas, openSpaces, maxStops, isLimitDurationOn, maxDurationMinutes, intermediatePlaces);
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
