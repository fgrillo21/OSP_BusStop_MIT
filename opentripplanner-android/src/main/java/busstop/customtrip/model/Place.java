package busstop.customtrip.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Objects;

import edu.usf.cutr.opentripplanner.android.R;

public class Place implements Serializable {

    private final String name;
    private final double lat;
    private final double lng;

    public Place(String name, double lat, double lng) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

    public String getName() {
        return name;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Place place = (Place) o;
        return Double.compare(place.lat, lat) == 0 &&
                Double.compare(place.lng, lng) == 0 &&
                name.equals(place.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, lat, lng);
    }
}
