package busstop.customtrip.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Query {

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getSettings() {
        return settings;
    }

    public void setSettings(String settings) {
        this.settings = settings;
    }

    public int getTimeout() {
        return Integer.parseInt(timeout);
    }

    public void setTimeout(int timeout) {
        this.timeout = String.valueOf(timeout);
    }

    public String getAroundFilter() {
        return filterAround;
    }

    public void setAroundFilter(float dist, List<Float> lat, List<Float> lng) {
        this.filterAround = "(around:" + String.valueOf(dist)+ ",";

        for (int i = 0; i < lat.size(); ++i) {
            if (i > 0)
                filterAround += ",";

            filterAround +=  String.valueOf(lat.get(i)) + "," + String.valueOf(lng.get(i));
        }

        filterAround += ")";
    }

    public void setAroundFilter(float dist, List<LatLng> points) {
        this.filterAround = "(around:" + String.valueOf(dist)+ ",";

        for (int i = 0; i < points.size(); ++i) {
            if (i > 0)
                filterAround += ",";

            filterAround +=  String.valueOf(points.get(i).latitude) + "," + String.valueOf(points.get(i).longitude);
        }

        filterAround += ")";
    }

    @Override
    public String toString() {
        return "Query{" +
                "query='" + query + '\'' +
                '}';
    }

    public void addHistoricTag(String key, String value) {
        if (historicTags.containsKey(key)){
            historicTags.get(key).add(value);
        }
        else {
            Set<String> set = new HashSet<String>();
            set.add(value);
            historicTags.put(key, set);
        }
    }

    public void addGreenTag(String key, String value) {
        if (greenTags.containsKey(key)){
            greenTags.get(key).add(value);
        }
        else {
            Set<String> set = new HashSet<String>();
            set.add(value);
            greenTags.put(key, set);
        }
    }

    public void addPanoramicTag(String key, String value) {
        if (panoramicTags.containsKey(key)){
            panoramicTags.get(key).add(value);
        }
        else {
            Set<String> set = new HashSet<String>();
            set.add(value);
            panoramicTags.put(key, set);
        }
    }

    public void buildCountQuery() {

        query = settings + timeout + ";";

        query += "(";

        query += historicFeatuers();

        query += ") -> .monuments;";

        query += "(";

        query += greenFeatures();

        query += ") -> .green;";

        query += "(";

        query += panoramicFeatures();

        query += ") -> .panoramic;";

        query += outCount;
    }

    public void buildHistoricQuery() {

        query = settings + timeout + ";";

        query += "(";

        query += historicFeatuers();

        query += ");";

        query += outFeatures;
    }

    public void buildGreenQuery() {

        query = settings + timeout + ";";

        query += "(";

        query += greenFeatures();

        query += ");";

        query += outFeatures;
    }

    public void buildPanoramicQuery () {

        query = settings + timeout + ";";

        query += "(";

        query += panoramicFeatures();

        query += ");";

        query += outFeatures;
    }

    private String panoramicFeatures() {

        String panoramicQuery = "";

        for (String key : panoramicTags.keySet()) {
            for (String value : panoramicTags.get(key)) {
                if (value.length() > 0) {
                    panoramicQuery += "node[\""     + key + "\" = \"" + value + "\"]" + filterAround + ";";
                    panoramicQuery += "way[\""      + key + "\" = \"" + value + "\"]" + filterAround + ";";
                    panoramicQuery += "relation[\"" + key + "\" = \"" + value + "\"]" + filterAround + ";";
                }
                else {
                    panoramicQuery += "node[\""     + key + "\"]" + filterAround + ";";
                    panoramicQuery += "way[\""      + key + "\"]" + filterAround + ";";
                    panoramicQuery += "relation[\"" + key + "\"]" + filterAround + ";";
                }
            }
        }

        return panoramicQuery;
    }

    private String greenFeatures() {

        String greenQuery = "";

        for (String key : greenTags.keySet()) {
            for (String value : greenTags.get(key)) {
                if (value.length() > 0) {
                    greenQuery += "node[\""     + key + "\" = \"" + value + "\"]" + filterAround + ";";
                    greenQuery += "way[\""      + key + "\" = \"" + value + "\"]" + filterAround + ";";
                    greenQuery += "relation[\"" + key + "\" = \"" + value + "\"]" + filterAround + ";";
                }
                else {
                    greenQuery += "node[\""     + key + "\"]" + filterAround + ";";
                    greenQuery += "way[\""      + key + "\"]" + filterAround + ";";
                    greenQuery += "relation[\"" + key + "\"]" + filterAround + ";";
                }
            }
        }

        return greenQuery;
    }

    private String historicFeatuers() {

        String historicQuery = "";

        for (String key : historicTags.keySet()) {
            for (String value : historicTags.get(key)) {
                if (value.length() > 0) {
                    historicQuery += "node[\""     + key + "\" = \"" + value + "\"]" + filterAround + ";";
                    historicQuery += "way[\""      + key + "\" = \"" + value + "\"]" + filterAround + ";";
                    historicQuery += "relation[\"" + key + "\" = \"" + value + "\"]" + filterAround + ";";
                }
                else {
                    historicQuery += "node[\""     + key + "\"]" + filterAround + ";";
                    historicQuery += "way[\""      + key + "\"]" + filterAround + ";";
                    historicQuery += "relation[\"" + key + "\"]" + filterAround + ";";
                }
            }
        }

        return historicQuery;
    }

    public String toQuery() {
        return query;
    }

    private String query;
    private String settings;
    private String timeout;
    private String filterAround;

    private final String outCount;
    private final String outFeatures;

    HashMap<String, Set<String>> historicTags;
    HashMap<String, Set<String>> greenTags;
    HashMap<String, Set<String>> panoramicTags;

    public Query() {
        query    = "";
        settings = "[out:json]";
        timeout  = "[timeout:800]";
        filterAround = "";
        outCount = ".monuments out count;" +
              ".green     out count;" +
              ".panoramic out count;";
        outFeatures = "out geom;";
        historicTags = new HashMap<>();
        greenTags = new HashMap<>();
        panoramicTags = new HashMap<>();
    }
}
