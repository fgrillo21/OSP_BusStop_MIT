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

        if (historicTags.containsKey(key)) {
            historicTags.get(key).add(value);
        } else {
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

        query += historicFeatures();

        query += ") -> .monuments;";

        query += "(";

        query += greenFeatures();

        query += ") -> .green;";

        query += "(";

        query += panoramicFeatures();

        query += ") -> .panoramic;";

        query += outCount;
    }

    public void buildComplexQuery() {

        query = settings + timeout + ";";

        query += "(";

        query += historicFeatures();

        query += ") -> .monuments;";

        query += "(";

        query += greenFeatures();

        query += ") -> .green;";

        query += "(";

        query += panoramicFeatures();

        query += ") -> .panoramic;";

        query += outCount;

        query += " (.monuments;.green;.panoramic;); >;" + outFeatures;
    }

    public void buildHistoricQuery() {

        query = settings + timeout + ";";

        query += "(";

        query += historicFeatures();

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

        return getFeaturesQuery(panoramicTags);
    }

    private String greenFeatures() {

        return getFeaturesQuery(greenTags);
    }

    private String historicFeatures() {

        return getFeaturesQuery(historicTags);
    }

    private String getFeaturesQuery(HashMap<String, Set<String>> tags) {

        String queryString = "";

        for (String key : tags.keySet()) {
            for (String value : tags.get(key)) {

                String[] keys = key.split(",");

                if (keys.length == 1) {
                    if (value.length() > 0) {
                        queryString += "node[\"" + key + "\" = \"" + value + "\"]" + filterAround + ";";
                        queryString += "way[\"" + key + "\" = \"" + value + "\"]" + filterAround + ";";
                        queryString += "relation[\"" + key + "\" = \"" + value + "\"]" + filterAround + ";";
                    } else {
                        queryString += "node[\"" + key + "\"]" + filterAround + ";";
                        queryString += "way[\"" + key + "\"]" + filterAround + ";";
                        queryString += "relation[\"" + key + "\"]" + filterAround + ";";
                    }
                } else {
                    String[] values = value.split(",");

                    if (value.length() > 0) {
                        queryString += "node";

                        int i = 0;
                        for (String k : keys) {
                            queryString += "[\"" + k + "\" = \"" + values[i] + "\"]";
                            i++;
                        }

                        queryString += filterAround + ";" + "way";

                        i = 0;
                        for (String k : keys) {
                            queryString += "[\"" + k + "\" = \"" + values[i] + "\"]";
                            i++;
                        }

                        queryString += filterAround + ";" + "relation";

                        i = 0;
                        for (String k : keys) {
                            queryString += "[\"" + k + "\" = \"" + values[i] + "\"]";
                            i++;
                        }

                        queryString += filterAround + ";";

                    }
                    else {
                        throw new IllegalArgumentException("Bad tag array");
                    }
                }
            }
        }

        return queryString;
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
        outFeatures = " out count; out geom;";
        historicTags = new HashMap<>();
        greenTags = new HashMap<>();
        panoramicTags = new HashMap<>();
    }
}
