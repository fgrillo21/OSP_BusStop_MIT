package busstop.customtrip.util;

import com.squareup.moshi.Moshi;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import busstop.customtrip.model.Query;
import nice.fontaine.overpass.models.response.OverpassResponse;
import nice.fontaine.overpass.models.response.adapters.ElementAdapter;
import nice.fontaine.overpass.models.response.adapters.Iso8601Adapter;
import nice.fontaine.overpass.models.response.adapters.MemberAdapter;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

//import nice.fontaine.overpass.OverpassApi;

public class Overpass {
    private String url;
    private OkHttpClient client;

    public Overpass() {
        url = null;
        client = null;
//        throw new IllegalStateException("Not to be initialized!");
    }

    public Call<OverpassResponse> ask(final Query query) {
        OverpassApi api = api();
        return api.ask(query.toQuery());
    }

    public void url(final String url) {
        this.url = url;
    }

    public void client(final OkHttpClient client) {
        this.client = client;
    }

    public OverpassApi api() {
//        if (client == null)
            client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        return new Retrofit.Builder()
                .baseUrl(url == null ? OverpassApi.BASE_URL : url)
                .addConverterFactory(MoshiConverterFactory.create(moshi()))
                .client(client)
                .build()
                .create(OverpassApi.class);
    }

    public Moshi moshi() {
        return new Moshi.Builder()
                .add(new MemberAdapter())
                .add(new ElementAdapter())
                .add(Date.class, new Iso8601Adapter())
                .build();
    }
}
