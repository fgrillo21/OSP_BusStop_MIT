package busstop.customtrip.util;

import nice.fontaine.overpass.models.response.OverpassResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface OverpassApi {

    // OSM Overpass interpreters addresses
    // String BASE_URL = "http://overpass-api.de/";
    // String BASE_URL = "https://overpass-api.de/api/interpreter/";
    String BASE_URL = "https://overpass.kumi.systems/api/interpreter/";

    /**
     * Returns a OverpassApi response for the given query.
     *
     * @param data OverpassApi QL string data part
     *             Example: [out:json];node(around:1600,52.516667,13.383333)["amenity"="post_box"];out qt 13;
     * @return a call to execute the actual query.
     */

    @POST("/api/interpreter")
    @FormUrlEncoded
    Call<OverpassResponse> ask(@Field("data") String data);
}
