package busstop.customtrip.model;

import com.google.android.gms.maps.model.LatLng;

import org.opentripplanner.api.model.Itinerary;

import java.util.List;

import nice.fontaine.overpass.models.response.geometries.Element;

public class EnrichedItinerary {

    final Itinerary     itinerary;
    final List<LatLng>  itineraryDecoded;
    final CustomTrip    tripPreferences;
    final FeaturesCount historicCount;
    final FeaturesCount greenCount;
    final FeaturesCount panoramicCount;
    final Element[]     elements;

    public EnrichedItinerary(Itinerary itinerary, List<LatLng> itineraryDecoded, CustomTrip tripPreferences, FeaturesCount historicCount, FeaturesCount greenCount, FeaturesCount panoramicCount, Element[] elements) {
        this.itinerary = itinerary;
        this.itineraryDecoded = itineraryDecoded;
        this.tripPreferences = tripPreferences;
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

    public CustomTrip getTripPreferences() {
        return tripPreferences;
    }

    public FeaturesCount getHistoricCount() {
        return historicCount;
    }

    public FeaturesCount getGreenCount() {
        return greenCount;
    }

    public FeaturesCount getPanoramicCount() {
        return panoramicCount;
    }

    public Element[] getElements() {
        return elements;
    }
}
