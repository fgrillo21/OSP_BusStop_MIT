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

    public void build() {
        query = settings + timeout + ";";

        query += "(";

        for (String key : historicTags.keySet()) {
            for (String value : historicTags.get(key)) {
                if (value.length() > 0) {
                    query += "node[\""     + key + "\" = \"" + value + "\"]" + filterAround + ";";
                    query += "way[\""      + key + "\" = \"" + value + "\"]" + filterAround + ";";
                    query += "relation[\"" + key + "\" = \"" + value + "\"]" + filterAround + ";";
                }
                else {
                    query += "node[\""     + key + "\"]" + filterAround + ";";
                    query += "way[\""      + key + "\"]" + filterAround + ";";
                    query += "relation[\"" + key + "\"]" + filterAround + ";";
                }
            }
        }

        query += ") -> .monuments;";

        query += "(";

        for (String key : greenTags.keySet()) {
            for (String value : greenTags.get(key)) {
                if (value.length() > 0) {
                    query += "node[\""     + key + "\" = \"" + value + "\"]" + filterAround + ";";
                    query += "way[\""      + key + "\" = \"" + value + "\"]" + filterAround + ";";
                    query += "relation[\"" + key + "\" = \"" + value + "\"]" + filterAround + ";";
                }
                else {
                    query += "node[\""     + key + "\"]" + filterAround + ";";
                    query += "way[\""      + key + "\"]" + filterAround + ";";
                    query += "relation[\"" + key + "\"]" + filterAround + ";";
                }
            }
        }

        query += ") -> .green;";

        query += "(";

        for (String key : panoramicTags.keySet()) {
            for (String value : panoramicTags.get(key)) {
                if (value.length() > 0) {
                    query += "node[\""     + key + "\" = \"" + value + "\"]" + filterAround + ";";
                    query += "way[\""      + key + "\" = \"" + value + "\"]" + filterAround + ";";
                    query += "relation[\"" + key + "\" = \"" + value + "\"]" + filterAround + ";";
                }
                else {
                    query += "node[\""     + key + "\"]" + filterAround + ";";
                    query += "way[\""      + key + "\"]" + filterAround + ";";
                    query += "relation[\"" + key + "\"]" + filterAround + ";";
                }
            }
        }

        query += ") -> .panoramic;";

        query += out;
    }

    public String toQuery() {
        return query;
    }

    private String query;
    private String settings;
    private String timeout;

    String filterAround;
    String out;

    HashMap<String, Set<String>> historicTags;
    HashMap<String, Set<String>> greenTags;
    HashMap<String, Set<String>> panoramicTags;

    public Query() {
        query    = "";
        settings = "[out:json]";
        timeout  = "[timeout:800]";
        filterAround = "";
        out = ".monuments out count;" +
              ".green     out count;" +
              ".panoramic out count;" +"";
//              "(.monuments; .green; .panoramic;);" +
//              "out geom;";

        historicTags = new HashMap<>();
        greenTags = new HashMap<>();
        panoramicTags = new HashMap<>();
    }
}
