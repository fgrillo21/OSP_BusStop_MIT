package busstop.customtrip.model;

import com.google.android.gms.maps.model.LatLng;

import org.opentripplanner.api.model.Itinerary;

import java.util.List;

import nice.fontaine.overpass.models.response.geometries.Element;

public class EnrichedItinerary {
    private final Itinerary     itinerary;
    private final List<LatLng>  itineraryDecoded;
    private final FeaturesCount historicCount;
    private final FeaturesCount greenCount;
    private final FeaturesCount panoramicCount;
    private final Element[]     elements;

    public static EnrichedItineraryBuilder newEnrichedItinerary() {
        return new EnrichedItineraryBuilder();
    }

    private EnrichedItinerary(Itinerary itinerary, List<LatLng> itineraryDecoded, FeaturesCount historicCount, FeaturesCount greenCount, FeaturesCount panoramicCount, Element[] elements) {
        this.itinerary = itinerary;
        this.itineraryDecoded = itineraryDecoded;
        this.historicCount = historicCount;
        this.greenCount = greenCount;
        this.panoramicCount = panoramicCount;
        this.elements = elements;
    }

    public Itinerary getItinerary() {
        return itinerary;
    }

    public List<LatLng> getItineraryDecoded() {
        return itineraryDecoded;
    }

    public FeaturesCount getHistoricCount() {
        return historicCount;
    }

    public int getHistoricAggregatedCount() {
        return historicCount.aggregatedCount();
    }

    public FeaturesCount getGreenCount() {
        return greenCount;
    }

    public int getGreenAggregatedCount() {
        return greenCount.aggregatedCount();
    }

    public FeaturesCount getPanoramicCount() {
        return panoramicCount;
    }

    public int getPanoramicAggregatedCount() {
        return panoramicCount.aggregatedCount();
    }

    public Element[] getElements() {
        return elements;
    }

    public static final class EnrichedItineraryBuilder {
        private Itinerary     itinerary;
        private List<LatLng>  itineraryDecoded;
        private FeaturesCount historicCount;
        private FeaturesCount greenCount;
        private FeaturesCount panoramicCount;
        private Element[]     elements;

        public EnrichedItineraryBuilder withItinerary(Itinerary itinerary) {
            this.itinerary = itinerary;
            return this;
        }

        public EnrichedItineraryBuilder withItineraryDecoded(List<LatLng> itineraryDecoded) {
            this.itineraryDecoded = itineraryDecoded;
            return this;
        }

        public EnrichedItineraryBuilder withHistoricCount(FeaturesCount historicCount) {
            this.historicCount = historicCount;
            return this;
        }

        public EnrichedItineraryBuilder withGreenCount(FeaturesCount greenCount) {
            this.greenCount = greenCount;
            return this;
        }

        public EnrichedItineraryBuilder withPanoramicCount(FeaturesCount panoramicCount) {
            this.panoramicCount = panoramicCount;
            return this;
        }

        public EnrichedItineraryBuilder withElements(Element[] elements) {
            this.elements = elements;
            return this;
        }

        public EnrichedItinerary build() {
            return new EnrichedItinerary(itinerary, itineraryDecoded, historicCount, greenCount, panoramicCount, elements);
        }
    }

    @Override
    public String toString() {
        return "EnrichedItinerary{" +
                "itinerary=" + itinerary +
                ", itineraryDecoded=" + itineraryDecoded +
                ", historicCount=" + historicCount +
                ", greenCount=" + greenCount +
                ", panoramicCount=" + panoramicCount +
                ", elements=" + elements +
                '}';
    }
}
