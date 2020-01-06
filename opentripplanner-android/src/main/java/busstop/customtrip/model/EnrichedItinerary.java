package busstop.customtrip.model;

import com.google.android.gms.maps.model.LatLng;

import org.opentripplanner.api.model.Itinerary;

import java.util.List;

import nice.fontaine.overpass.models.response.geometries.Element;

public class EnrichedItinerary {

    final Itinerary     itinerary;
    final List<LatLng>  itineraryDecoded;
    final FeaturesCount historicCount;
    final FeaturesCount greenCount;
    final FeaturesCount panoramicCount;
    final Element[]     elements;

    public EnrichedItinerary(Itinerary itinerary, List<LatLng> itineraryDecoded, FeaturesCount historicCount, FeaturesCount greenCount, FeaturesCount panoramicCount, Element[] elements) {
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
}
