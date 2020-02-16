/*
 * Copyright 2011 Marcy Gordon
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.usf.cutr.opentripplanner.android.tasks;

import org.opentripplanner.api.model.Leg;
import org.opentripplanner.api.model.TripPlan;
import org.opentripplanner.api.model.error.PlannerError;
import org.opentripplanner.api.ws.Message;
import org.opentripplanner.api.ws.Request;
import org.opentripplanner.api.model.Itinerary;
import org.opentripplanner.api.ws.Response;
import org.opentripplanner.routing.core.TraverseMode;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.util.Pair;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import busstop.customtrip.model.CustomTrip;
import busstop.customtrip.model.EnrichedItinerary;
import busstop.customtrip.model.FeaturesCount;
import busstop.customtrip.model.Query;
import busstop.customtrip.util.Overpass;
import busstop.customtrip.util.OverpassParser;
import edu.usf.cutr.opentripplanner.android.OTPApp;
import edu.usf.cutr.opentripplanner.android.R;
import edu.usf.cutr.opentripplanner.android.fragments.MainFragment;
import edu.usf.cutr.opentripplanner.android.listeners.TripRequestCompleteListener;
import edu.usf.cutr.opentripplanner.android.model.Server;
import edu.usf.cutr.opentripplanner.android.util.ConversionUtils;
import edu.usf.cutr.opentripplanner.android.util.JacksonConfig;
import nice.fontaine.overpass.models.response.OverpassResponse;
import nice.fontaine.overpass.models.response.geometries.Element;
import retrofit2.Call;

/**
 * AsyncTask that invokes a trip planning request to the OTP Server
 *
 * @author Khoa Tran
 * @author Sean Barbeau (conversion to Jackson)
 */

public class TripRequest extends AsyncTask<Request, Integer, Long> {

    final String filterTag = "TRQ_Filter";
    final String osmTag    = "TRQ_OSM_Query";

    private Response response;

    private ProgressDialog progressDialog;

    private WeakReference<Activity> activity;

    private Context context;

    private Resources resources;

    private String currentRequestString = "";

    private Server selectedServer;

    private TripRequestCompleteListener callback;

    private TripPlan tPlan = null;

    private List<LatLng> legsDecoded = new ArrayList<>();

    private Query countFeaturesQuery;

    private CustomTrip customTrip;

    private List<EnrichedItinerary> itinerariesToSelect = null;

    private List<EnrichedItinerary> itinerariesSelected = null;

    static int tripRequest = 0;

    public TripRequest(WeakReference<Activity> activity, Context context, Resources resources,
                       Server selectedServer, TripRequestCompleteListener callback, CustomTrip customTrip) {
        this.activity = activity;
        this.context = context;
        this.selectedServer = selectedServer;
        this.callback = callback;
        this.resources = resources;
        this.customTrip = customTrip;

        if (activity != null) {
            Activity activityRetrieved = activity.get();
            progressDialog = new ProgressDialog(activityRetrieved);
        }

        // OpenStreetMap features for itinerary filtering
        this.countFeaturesQuery = new Query();

        countFeaturesQuery.addHistoricTag("historic", "");
        countFeaturesQuery.addHistoricTag("amenity", "arts_centre");
        countFeaturesQuery.addHistoricTag("amenity", "fountain");
        countFeaturesQuery.addHistoricTag("tourism", "attraction");
        countFeaturesQuery.addHistoricTag("tourism", "artwork");
        countFeaturesQuery.addHistoricTag("tourism", "gallery");
        countFeaturesQuery.addHistoricTag("covered", "arcade");
        countFeaturesQuery.addHistoricTag("covered", "colonnade");

        countFeaturesQuery.addGreenTag("leisure", "park");
        countFeaturesQuery.addGreenTag("landuse", "grass");
        countFeaturesQuery.addGreenTag("leisure", "garden");

        countFeaturesQuery.addPanoramicTag("amenity", "marketplace");
        countFeaturesQuery.addPanoramicTag("highway", "marketplace");
        countFeaturesQuery.addPanoramicTag("highway,area", "pedestrian,yes");
    }

    protected void onPreExecute() {
        if (activity.get() != null) {
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(true);
            Activity activityRetrieved = activity.get();
            if (activityRetrieved != null) {
                progressDialog = ProgressDialog.show(activityRetrieved, "",
                        resources.getText(R.string.task_progress_tripplanner_progress), true);
            }
        }
    }

    protected Long doInBackground(Request... reqs) {
        long totalSize = 0;
        if (selectedServer == null) {
            Toast.makeText(context,
                    resources.getString(R.string.toast_no_server_selected_error),
                    Toast.LENGTH_SHORT).show();
            return null;
        }
        else{
            String prefix = PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(OTPApp.PREFERENCE_KEY_FOLDER_STRUCTURE_PREFIX
                            , OTPApp.FOLDER_STRUCTURE_PREFIX_NEW);
            String baseURL = selectedServer.getBaseURL();
            for (Request req : reqs) {
                response = requestPlan(req, prefix, baseURL);
            }
        }

        Log.d(filterTag, "********  " + tripRequest + " ********");

        if (response != null && response.getPlan() != null
                && response.getPlan().getItinerary().get(0) != null) {

            List<Itinerary> itineraries = response.getPlan().getItinerary();

            // ******** Begin Filtering ********
            // Itinerary comparison based on feature selection

            Log.d(filterTag, "******** Start filtering " + itineraries.size() + " itineraries. ********");

            tPlan = response.getPlan();

            int i = 0;
            StringBuilder strLog;

            List<String> busName = new ArrayList<>();
            List<LatLng> legPoints;
            List<List<LatLng>> itinerariesDecoded = new ArrayList<>();

            for (Itinerary it : itineraries) {

                Log.d(filterTag, "** Analyzing itinerary " + i);

                strLog = new StringBuilder();
                legsDecoded = new ArrayList<>();

                for (Leg leg : it.legs) {

                    TraverseMode traverseMode = TraverseMode.valueOf(leg.mode);

                    if (traverseMode.isTransit()) {
                        busName.add(i, ConversionUtils.getRouteShortNameSafe(leg.routeShortName,leg.routeLongName, context));
                    }
                    else {
                        busName.add("");
                    }

                    strLog.append("\n" + "Itinerary [" + (i + 1) + "] -> Leg: " + leg.legGeometry.getPoints());

                    legPoints = PolyUtil.decode(leg.legGeometry.getPoints());

                    legsDecoded.addAll(legPoints);
                }

                String str = "** Itinerary [" + (i + 1) + "] -> From {"
                        + tPlan.from.getLat() + ", " + tPlan.from.getLon() + "} to {"
                        + tPlan.to.getLat()   + ", " + tPlan.to.getLon()   + "}";

                if (busName.size() > i)
                    str = str + " -- {" + busName.get(i) + "} **" + "\n";

                Log.d(filterTag, str);
//                        + strLog.toString().replace("\\", "\\\\") + "\n");

                itinerariesDecoded.add(legsDecoded);

                i += 1;
            }

            Log.d(osmTag, "** Start features control with OSM **");

            EnrichedItinerary enrichedItinerary;
            itinerariesToSelect = new ArrayList<>();
            int j = 0;

            for (List<LatLng> itinerary : itinerariesDecoded) {

                countFeaturesQuery.setAroundFilter(30, itinerary);

                OverpassResponse body = null;
                retrofit2.Response<OverpassResponse> response = null;

                try {
                    Log.d(osmTag, "Start retrieving features COUNT");
                    countFeaturesQuery.buildCountQuery();
                    response = executeOverpassQuery(countFeaturesQuery);
                    body     = response.body();
                    FeaturesCount historicCount  = OverpassParser.parseHistoricToCount(body.elements);
                    FeaturesCount greenCount     = OverpassParser.parseGreenToCount(body.elements);
                    FeaturesCount panoramicCount = OverpassParser.parsePanoramicToCount(body.elements);
                    Log.d(osmTag, historicCount.toString());
                    Log.d(osmTag, greenCount.toString());
                    Log.d(osmTag, panoramicCount.toString());

                    Log.d(osmTag, "Start retrieving HISTORICAL features");
                    countFeaturesQuery.buildHistoricQuery();
                    response           = executeOverpassQuery(countFeaturesQuery);
                    body               = response.body();
                    Element[] historic = body.elements;
                    Log.d(osmTag, "HISTORICAL features successfully retrieved");

                    Log.d(osmTag, "Start retrieving GREEN features");
                    countFeaturesQuery.buildGreenQuery();
                    response        = executeOverpassQuery(countFeaturesQuery);
                    body            = response.body();
                    Element[] green = body.elements;
                    Log.d(osmTag, "GREEN features successfully retrieved");

                    Log.d(osmTag, "Start retrieving PANORAMIC features");
                    countFeaturesQuery.buildPanoramicQuery();
                    response            = executeOverpassQuery(countFeaturesQuery);
                    body                = response.body();
                    Element[] panoramic = body.elements;
                    Log.d(osmTag, "PANORAMIC features successfully retrieved");

                    enrichedItinerary = EnrichedItinerary.newEnrichedItinerary()
                            .withItinerary(itineraries.get(j))
                            .withItineraryDecoded(itinerary)
                            .withHistoricCount(historicCount)
                            .withGreenCount(greenCount)
                            .withPanoramicCount(panoramicCount)
                            .withHistoricalFeatures(historic)
                            .withGreenFeatures(green)
                            .withPanoramicFeatures(panoramic)
                            .withName(busName.get(j))
                            .withTransfersCount(MainFragment.getItineraryTransfersCount(itineraries.get(j)))
                            .build();

                    itinerariesToSelect.add(enrichedItinerary);

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(osmTag, response.errorBody().toString());
                }

                j += 1;
            }

            // Inizio la selezione fra gli itinerari trovati.
            // Now we have three itineraries among we have to choose the best one based on the features requested

//                itinerariesSelected = selectTripByFeatures(itinerariesToSelect, customTrip);

            itinerariesSelected = sortItinerariesList(itinerariesToSelect, customTrip);
            // Dopo questa chiamata itinerariesSelected contiene la porzione di itinerariesToSelect
            // che è coerente con le percentuali specificate (preset o custom) mentre itinerariesRemaining,
            // che all'inizio è uguale a itinerariesToSelect, conterrà la porzione di itinerari non selezionata
            // ma comunque disponibile.
        }



        tripRequest += 1;

        return totalSize;
    }

    private retrofit2.Response<OverpassResponse> executeOverpassQuery(Query query) throws RuntimeException, IOException {

        Overpass overpassManager = new Overpass();

        OverpassResponse body = null;

        Log.d(osmTag, countFeaturesQuery.toString());

        retrofit2.Response<OverpassResponse> responseR = null;

        final int maxTries = 3;
        int currentTry = 0;

        while (currentTry < maxTries) {

            Log.d(osmTag, "Attempt " + currentTry);

                Call<OverpassResponse> call = overpassManager.ask(countFeaturesQuery);
                responseR = call.execute();

                body = responseR.body();

                if (body != null)
                    break;

                currentTry += 1;
        }

        if (body == null)
            throw new RuntimeException("No answer from overpass interpreter");

        return responseR;
    }

    protected void onCancelled(Long result) {

        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            Log.e(OTPApp.TAG, "Error in TripRequest Cancelled dismissing dialog: " + e);
        }

        Activity activityRetrieved = activity.get();
        if (activityRetrieved != null) {
            AlertDialog.Builder geocoderAlert = new AlertDialog.Builder(activityRetrieved);
            geocoderAlert.setTitle(R.string.tripplanner_results_title)
                    .setMessage(R.string.tripplanner_error_request_timeout)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });

            AlertDialog alert = geocoderAlert.create();
            alert.show();
        }

        Log.e(OTPApp.TAG, "No route to display!");
    }

    protected void onPostExecute(Long result) {
        if (activity.get() != null) {
            try {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            } catch (Exception e) {
                Log.e(OTPApp.TAG, "Error in TripRequest PostExecute dismissing dialog: " + e);
            }
        }

        if (response != null && response.getPlan() != null
                && response.getPlan().getItinerary().get(0) != null) {

            if (itinerariesSelected == null || itinerariesSelected.size() == 0) {

                Activity activityRetrieved = activity.get();

                if (activityRetrieved != null) {

                    AlertDialog.Builder feedback = new AlertDialog.Builder(activityRetrieved);
                    feedback.setTitle(resources.getString(R.string.tripplanner_error_dialog_title));
                    feedback.setNeutralButton(resources.getString(android.R.string.ok), null);

                    String msg = resources.getString(R.string.customtrip_tripplanner_error_not_defined);

                    PlannerError error = response.getError();

                    if (error != null) {
                        int errorCode = error.getId();

                        if (response != null && response.getError() != null
                                && errorCode != Message.PLAN_OK
                                .getId()) {

                            msg = getErrorMessage(response.getError().getId());
                            if (msg == null) {
                                msg = response.getError().getMsg();
                            }
                        }
                    }
                    feedback.setMessage(msg);
                    feedback.create().show();
                }

                Log.e(filterTag, "No custom route to display!");
            }

            callback.onTripRequestComplete(itinerariesSelected, currentRequestString);
        }
        else {
            Activity activityRetrieved = activity.get();
            if (activityRetrieved != null) {
                AlertDialog.Builder feedback = new AlertDialog.Builder(activityRetrieved);
                feedback.setTitle(resources
                        .getString(R.string.tripplanner_error_dialog_title));
                feedback.setNeutralButton(resources.getString(android.R.string.ok),
                        null);
                String msg = resources
                        .getString(R.string.tripplanner_error_not_defined);

                PlannerError error = response.getError();
                if (error != null) {
                    int errorCode = error.getId();

                    if (response != null && response.getError() != null
                            && errorCode != Message.PLAN_OK
                            .getId()) {

                        msg = getErrorMessage(response.getError().getId());
                        if (msg == null) {
                            msg = response.getError().getMsg();
                        }
                    }
                }
                feedback.setMessage(msg);
                feedback.create().show();
            }

            Log.e(OTPApp.TAG, "No route to display!");
        }
    }

    protected String getErrorMessage(int errorCode) {
        if (errorCode == Message.SYSTEM_ERROR.getId()) {
            return (resources.getString(R.string.tripplanner_error_system));
        } else if (errorCode == Message.OUTSIDE_BOUNDS.getId()) {
            return (resources.getString(R.string.tripplanner_error_outside_bounds));
        } else if (errorCode == Message.PATH_NOT_FOUND.getId()) {
            return (resources.getString(R.string.tripplanner_error_path_not_found));
        } else if (errorCode == Message.NO_TRANSIT_TIMES.getId()) {
            return (resources.getString(R.string.tripplanner_error_no_transit_times));
        } else if (errorCode == Message.REQUEST_TIMEOUT.getId()) {
            return (resources.getString(R.string.tripplanner_error_request_timeout));
        } else if (errorCode == Message.BOGUS_PARAMETER.getId()) {
            return (resources.getString(R.string.tripplanner_error_bogus_parameter));
        } else if (errorCode == Message.GEOCODE_FROM_NOT_FOUND.getId()) {
            return (resources
                    .getString(R.string.tripplanner_error_geocode_from_not_found));
        } else if (errorCode == Message.GEOCODE_TO_NOT_FOUND.getId()) {
            return (resources
                    .getString(R.string.tripplanner_error_geocode_to_not_found));
        } else if (errorCode == Message.GEOCODE_FROM_TO_NOT_FOUND.getId()) {
            return (resources
                    .getString(R.string.tripplanner_error_geocode_from_to_not_found));
        } else if (errorCode == Message.TOO_CLOSE.getId()) {
            return (resources.getString(R.string.tripplanner_error_too_close));
        } else if (errorCode == Message.LOCATION_NOT_ACCESSIBLE.getId()) {
            return (resources
                    .getString(R.string.tripplanner_error_location_not_accessible));
        } else if (errorCode == Message.GEOCODE_FROM_AMBIGUOUS.getId()) {
            return (resources
                    .getString(R.string.tripplanner_error_geocode_from_ambiguous));
        } else if (errorCode == Message.GEOCODE_TO_AMBIGUOUS.getId()) {
            return (resources
                    .getString(R.string.tripplanner_error_geocode_to_ambiguous));
        } else if (errorCode == Message.GEOCODE_FROM_TO_AMBIGUOUS.getId()) {
            return (resources
                    .getString(R.string.tripplanner_error_geocode_from_to_ambiguous));
        } else if (errorCode == Message.UNDERSPECIFIED_TRIANGLE.getId()
                || errorCode == Message.TRIANGLE_NOT_AFFINE.getId()
                || errorCode == Message.TRIANGLE_OPTIMIZE_TYPE_NOT_SET.getId()
                || errorCode == Message.TRIANGLE_VALUES_NOT_SET.getId()) {
            return (resources.getString(R.string.tripplanner_error_triangle));
        } else {
            return null;
        }
    }

    protected Response requestPlan(Request requestParams, String prefix, String baseURL) {
        String str;
        HashMap<String, String> tmp = requestParams.getParameters();

        Collection c = tmp.entrySet();
        Iterator itr = c.iterator();

        String params = "";
        boolean first = true;
        while (itr.hasNext()) {
            if (first) {
                params += "?" + itr.next();
                first = false;
            } else {
                params += "&" + itr.next();
            }
        }

        if (requestParams.getBikeRental()) {
            String updatedString;
            if (prefix.equals(OTPApp.FOLDER_STRUCTURE_PREFIX_NEW)){
                updatedString = params.replace(TraverseMode.BICYCLE.toString(),
                        TraverseMode.BICYCLE.toString() + OTPApp.OTP_RENTAL_QUALIFIER);
            }
            else{
                updatedString = params.replace(TraverseMode.BICYCLE.toString(),
                        TraverseMode.BICYCLE.toString() + ", " + TraverseMode.WALK.toString());
            }

            params = updatedString;
        }


        if (requestParams.getModes().getTrainish()) {
            // TraverseModeSet.toString() enumerates activated modes, which might not be supported by server
            // so we filter them out. Should be solved differently, e.g. by  decoupling and introducing
            // an interface with version dependent implementations
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            if (prefs.getInt(OTPApp.PREFERENCE_KEY_API_VERSION, OTPApp.API_VERSION_V3)
                    >= OTPApp.API_VERSION_V3) {
                String updatedString;
                updatedString = params.replace(TraverseMode.TRAINISH.toString(), "");
                updatedString = updatedString.replace(TraverseMode.BUSISH.toString(), "");

                params = updatedString;
            }
        }

        List<String> intermediatePlaces = requestParams.getIntermediatePlaces();

        if (intermediatePlaces.size() > 0) {

            Iterator iPlaces = intermediatePlaces.iterator();

            while (iPlaces.hasNext()) {
                params += "&intermediatePlaces=" + iPlaces.next();
            }

            params += "&intermediatePlacesOrdered=false";
        }

        String u = baseURL + prefix + OTPApp.PLAN_LOCATION + params;

        Log.d(OTPApp.TAG, "URL: " + u);

        currentRequestString = u;

        HttpURLConnection urlConnection = null;
        URL url;
        Response plan = null;

        try {
            url = new URL(u);

            disableConnectionReuseIfNecessary(); // For bugs in HttpURLConnection pre-Froyo

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setConnectTimeout(OTPApp.HTTP_CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(OTPApp.HTTP_SOCKET_TIMEOUT);
            plan = JacksonConfig.getObjectReaderInstance()
                    .readValue(urlConnection.getInputStream());
        } catch (java.net.SocketTimeoutException e) {
            Log.e(OTPApp.TAG, "Timeout fetching JSON or XML: " + e);
            e.printStackTrace();
            cancel(true);
        } catch (IOException e) {
            Log.e(OTPApp.TAG, "Error fetching JSON or XML: " + e);
            e.printStackTrace();
            cancel(true);
            // Reset timestamps to show there was an error
            // requestStartTime = 0;
            // requestEndTime = 0;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return plan;
    }

    /**
     * Disable HTTP connection reuse which was buggy pre-froyo
     */
    private void disableConnectionReuseIfNecessary() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }



    private List<EnrichedItinerary> selectTripByFeatures(List<EnrichedItinerary> enrichedItineraries, CustomTrip customTrip) {

        final String selectionTag = "TRQ_Custom";

        ArrayList<EnrichedItinerary> selectedItineraries  = new ArrayList<>();

        final float reqHistoricPercentage  = customTrip.getMonuments();
        final float reqGreenPercentage     = customTrip.getGreenAreas();
        final float reqPanoramicPercentage = customTrip.getOpenSpaces();
        final int   maxStops               = customTrip.getMaxStops();

        Log.d(selectionTag, customTrip.toString());

        int maxHistoricFounded = 0;
        int maxGreenFounded = 0;
        int maxPanoramicFounded = 0;

        for (EnrichedItinerary itinerary : enrichedItineraries) {

            final int itHistoricCount   = itinerary.getHistoricAggregatedCount();
            final int itGreenCount      = itinerary.getGreenAggregatedCount();
            final int itPanoramicCount  = itinerary.getPanoramicAggregatedCount();

            if (itHistoricCount > maxHistoricFounded) {
                maxHistoricFounded = itHistoricCount;
            }

            if (itGreenCount > maxGreenFounded){
                maxGreenFounded = itGreenCount;
            }

            if (itPanoramicCount > maxPanoramicFounded) {
                maxPanoramicFounded = itPanoramicCount;
            }
        }

        if (reqHistoricPercentage == CustomTrip.MAX && reqGreenPercentage == 0 && reqPanoramicPercentage == 0) {

            for (EnrichedItinerary itinerary : enrichedItineraries) {

                if (itinerary.getHistoricAggregatedCount() == maxHistoricFounded && maxHistoricFounded > 0) {
                    if (itinerary.getTransfersCount() <= maxStops) {
                        selectedItineraries.add(itinerary);
                    }
                }
            }
        }
        else if (reqGreenPercentage == CustomTrip.MAX && reqHistoricPercentage == 0 && reqPanoramicPercentage == 0) {

            for (EnrichedItinerary itinerary : enrichedItineraries) {

                if (itinerary.getGreenAggregatedCount() == maxGreenFounded && maxGreenFounded > 0) {
                    if(itinerary.getTransfersCount() <= maxStops) {
                        selectedItineraries.add(itinerary);
                    }
                }
            }
        }
        else if (reqPanoramicPercentage == CustomTrip.MAX && reqHistoricPercentage == 0 && reqGreenPercentage == 0) {

            for (EnrichedItinerary itinerary : enrichedItineraries) {

                if (itinerary.getPanoramicAggregatedCount() == maxPanoramicFounded && maxPanoramicFounded > 0) {
                    if (itinerary.getTransfersCount() <= maxStops) {
                        selectedItineraries.add(itinerary);
                    }
                }
            }
        }
        else {

            final double threshold = 0.08;
            final int numItineraries = enrichedItineraries.size();
            double[]     scarto    = new double[numItineraries];
            double[]     eNorms    = new double[numItineraries];
            double       min       = 32000.0;

            int i = 0;
            for (EnrichedItinerary itinerary : enrichedItineraries) {

                final int   itHistoricCount       = itinerary.getHistoricAggregatedCount();
                final int   itGreenCount          = itinerary.getGreenAggregatedCount();
                final int   itPanoramicCount      = itinerary.getPanoramicAggregatedCount();
                final int   itTotalFeatures       = itHistoricCount + itGreenCount + itPanoramicCount;
                final float itHistoricPercentage  = (float) itHistoricCount  * 100 / itTotalFeatures;
                final float itGreenPercentage     = (float) itGreenCount     * 100 / itTotalFeatures;
                final float itPanoramicPercentage = (float) itPanoramicCount * 100 / itTotalFeatures;

                scarto[0] = Math.abs(reqHistoricPercentage  - itHistoricPercentage);
                scarto[1] = Math.abs(reqGreenPercentage     - itGreenPercentage);
                scarto[2] = Math.abs(reqPanoramicPercentage - itPanoramicPercentage);

                Log.d(selectionTag, itinerary.getName());
                Log.d(selectionTag, "Historic : " + itHistoricCount  + " " + itHistoricPercentage  + "%");
                Log.d(selectionTag, "Green    : " + itGreenCount     + " " + itGreenPercentage     + "%");
                Log.d(selectionTag, "Panoramic: " + itPanoramicCount + " " + itPanoramicPercentage + "%");
                Log.d(selectionTag, "{" + scarto[0] + ", " + scarto[1] + ", " + scarto[2] + "}");

                eNorms[i] = euclideanNorm(scarto);

                if (eNorms[i] < min) {
                    min = eNorms[i];
                }

                Log.d(selectionTag, "Norm[" + itinerary.getName() + "] = " + eNorms[i]);

                i += 1;
            }

            for (i = 0; i < numItineraries; ++i) {

                if ((Math.abs(eNorms[i] - min) <= threshold) && (enrichedItineraries.get(i).getTransfersCount() <= maxStops)) {
                    selectedItineraries.add(enrichedItineraries.get(i));
                    Log.d(selectionTag, "Added " + enrichedItineraries.get(i).getName() + "(Norm = " + eNorms[i] + ")");
                }
            }
        }

        return selectedItineraries;
    }

    private double euclideanNorm(double[] vector) {
        return Math.sqrt(Math.pow(vector[0],2) + Math.pow(vector[1], 2) + Math.pow(vector[2], 2));
    }

    private List<EnrichedItinerary> sortItinerariesList(List<EnrichedItinerary> list, CustomTrip customTrip) {

        List<Pair<Double, EnrichedItinerary>> toOrder = new ArrayList<>();
        List<EnrichedItinerary> toReturn = new ArrayList<>();
        Double value = 0.0;
        double[] scarto = new double[3];
        boolean preset = (customTrip.getMonuments()  == CustomTrip.MAX) ||
                         (customTrip.getGreenAreas() == CustomTrip.MAX) ||
                         (customTrip.getOpenSpaces() == CustomTrip.MAX);

        for (EnrichedItinerary it : list) {

            // Usare itXCount volendo privilegiare il numero maggiore di feature
            // Usare itXPerentage se si vuole l'itinerario con la composizione più aderente

            final int   itHistoricCount       = it.getHistoricAggregatedCount();
            final int   itGreenCount          = it.getGreenAggregatedCount();
            final int   itPanoramicCount      = it.getPanoramicAggregatedCount();
            final int   itTotalFeatures       = itHistoricCount + itGreenCount + itPanoramicCount;
            final float itHistoricPercentage  = (float) itHistoricCount  * 100 / itTotalFeatures;
            final float itGreenPercentage     = (float) itGreenCount     * 100 / itTotalFeatures;
            final float itPanoramicPercentage = (float) itPanoramicCount * 100 / itTotalFeatures;

            if (preset) {
                if (customTrip.getMonuments() == CustomTrip.MAX) {
                    value = (double) itHistoricPercentage;
                } else if (customTrip.getGreenAreas() == CustomTrip.MAX) {
                    value = (double) itGreenPercentage;
                } else if (customTrip.getOpenSpaces() == CustomTrip.MAX) {
                    value = (double) itPanoramicPercentage;
                }
            }
            else {

                scarto[0] = Math.abs(customTrip.getMonuments()  - itHistoricPercentage);
                scarto[1] = Math.abs(customTrip.getGreenAreas() - itGreenPercentage);
                scarto[2] = Math.abs(customTrip.getOpenSpaces() - itPanoramicPercentage);

                value = euclideanNorm(scarto);
            }

            toOrder.add(new Pair<Double, EnrichedItinerary>(value, it));
        }

        int extremeIndex;

        for (int i = 0; i < toOrder.size() - 1; ++i) {
            extremeIndex = i;
            for (int j = i + 1; j < toOrder.size(); ++j) {
                if (preset) {
                    if (toOrder.get(j).first > toOrder.get(extremeIndex).first) {
                        extremeIndex = j;
                    }
                }
                else {
                    if (toOrder.get(j).first < toOrder.get(extremeIndex).first) {
                        extremeIndex = j;
                    }
                }
            }

            Pair<Double, EnrichedItinerary> tmp = toOrder.get(extremeIndex);
            toOrder.set(extremeIndex, toOrder.get(i));
            toOrder.set(i, tmp);
        }

        for (Pair<Double, EnrichedItinerary> p : toOrder) {
            toReturn.add(p.second);
        }

        return toReturn;
    }
}
