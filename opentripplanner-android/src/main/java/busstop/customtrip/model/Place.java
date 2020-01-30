package busstop.customtrip.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import edu.usf.cutr.opentripplanner.android.R;

public class Place {

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

//    public void setName(String name) {
//        this.name = name;
//    }

    public double getLat() {
        return lat;
    }

//    public void setLat(double lat) {
//        this.lat = lat;
//    }

    public double getLng() {
        return lng;
    }

//    public void setLng(double lng) {
//        this.lng = lng;
//    }
}
