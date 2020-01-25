/*
 * Copyright 2012 University of South Florida
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package edu.usf.cutr.opentripplanner.android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.gms.maps.model.LatLng;
import org.opentripplanner.api.model.Leg;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import busstop.customtrip.model.CustomTrip;
import busstop.customtrip.model.EnrichedItinerary;
import edu.usf.cutr.opentripplanner.android.fragments.DirectionListFragment;
import edu.usf.cutr.opentripplanner.android.fragments.MainFragment;
import edu.usf.cutr.opentripplanner.android.listeners.DateCompleteListener;
import edu.usf.cutr.opentripplanner.android.listeners.OtpFragment;
import edu.usf.cutr.opentripplanner.android.model.OTPBundle;
import edu.usf.cutr.opentripplanner.android.model.Server;
import edu.usf.cutr.opentripplanner.android.sqlite.ServersDataSource;
import edu.usf.cutr.opentripplanner.android.tasks.ServerChecker;

/**
 * Main Activity for the OTP for Android app
 *
 * @author Marcy Gordon
 * @author Khoa Tran
 * @author Sean Barbeau (conversion to Jackson)
 * @author Vreixo Gonz�lez (update to Google Maps API v2, UI and general app improvements)
 */

public class MyActivity extends AppCompatActivity implements OtpFragment {

    private List<Leg> currentItinerary = new ArrayList<Leg>();

    private List<EnrichedItinerary> currentItineraryList = new ArrayList<EnrichedItinerary>();

    private int currentItineraryIndex = -1;

    private OTPBundle bundle = null;

    private MainFragment mainFragment;

    private String currentRequestString = "";

    private boolean isButtonStartLocation = false;

    DateCompleteListener dateCompleteCallback;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState !=null)
        Log.d("TRQ", "Creo MyActivity: " + savedInstanceState.toString());
    else Log.d("TRQ", "Creo MyActivity: ");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setContentView(R.layout.activity);

        if (savedInstanceState != null) {
            mainFragment = (MainFragment) getSupportFragmentManager().findFragmentByTag(
                    OTPApp.TAG_FRAGMENT_MAIN_FRAGMENT);//recuperar o tag adecuado e pillar ese fragment

        }

        if (savedInstanceState == null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                    .beginTransaction();
            mainFragment = new MainFragment();
            fragmentTransaction
                    .replace(R.id.mainFragment, mainFragment, OTPApp.TAG_FRAGMENT_MAIN_FRAGMENT);
            fragmentTransaction.commit();

        }

        if(mainFragment != null) {
            /* Get here the info about custom trip*/
            Intent i = getIntent();
            CustomTrip customTrip = (CustomTrip)i.getSerializableExtra("customTrip");
            mainFragment.setCustomTripInfo(customTrip);
        }
    }

    @Override
    protected void onNewIntent (Intent intent){
        if (intent.getAction() == OTPApp.INTENT_NOTIFICATION_RESUME_APP_WITH_TRIP_ID){
            mainFragment.openModeMarker(intent.getStringExtra(OTPApp.BUNDLE_KEY_INTENT_TRIP_ID));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case OTPApp.SETTINGS_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    boolean shouldRefresh = data
                            .getBooleanExtra(OTPApp.REFRESH_SERVER_RETURN_KEY, false);
                    boolean changedSelectedCustomServer = data
                            .getBooleanExtra(OTPApp.CHANGED_SELECTED_CUSTOM_SERVER_RETURN_KEY,
                                    false);
                    boolean changedTileProvider = data
                            .getBooleanExtra(OTPApp.CHANGED_MAP_TILE_PROVIDER_RETURN_KEY, false);
                    boolean liveUpdatesDisabled = data
                            .getBooleanExtra(OTPApp.LIVE_UPDATES_DISABLED_RETURN_KEY, false);
                    boolean changedParametersMustRequestTrip = data
                            .getBooleanExtra(OTPApp.CHANGED_PARAMETERS_MUST_REQUEST_TRIP_RETURN_KEY, false);

                    //				Toast.makeText(this, "Should server list refresh? " + shouldRefresh, Toast.LENGTH_LONG).show();
                    if (shouldRefresh) {
                        mainFragment.setNeedToRunAutoDetect(true);
                        mainFragment.setNeedToUpdateServersList(true);
                    }
                    if (changedSelectedCustomServer) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

                        WeakReference<Activity> weakContext = new WeakReference<Activity>(this);
                        ServerChecker serverChecker = new ServerChecker(weakContext, this.getApplicationContext(),
                                mainFragment, true, false, false);
                        Server server;
                        if (prefs.getBoolean(OTPApp.PREFERENCE_KEY_SELECTED_CUSTOM_SERVER, false)){
                            server = new Server(prefs.getString(OTPApp.PREFERENCE_KEY_CUSTOM_SERVER_URL, ""),
                                    this);
                        }
                        else{
                            ServersDataSource dataSource = ServersDataSource.getInstance(this);
                            dataSource.open();
                            server = new Server(dataSource
                                    .getServer(prefs.getLong(OTPApp.PREFERENCE_KEY_SELECTED_SERVER, 0)));
                            dataSource.close();
                        }
                        mainFragment.setmCustomServerMetadata(null);
                        serverChecker.execute(server);
                    }
                    if (changedTileProvider) {
                        mainFragment.updateOverlay(null);
                    }
                    if (liveUpdatesDisabled) {
                        mainFragment.listenForTripTimeUpdates(false, 0);
                    }
                    if (changedParametersMustRequestTrip) {
                        Log.d("TRQ_Trigger", "Case setteings_request");
                        mainFragment.processRequestTrip();
                    }
                    break;
                }
            case OTPApp.CHOOSE_CONTACT_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Log.d(OTPApp.TAG, "CHOOSE CONTACT RESULT OK");

                    Uri contactData = data.getData();
                    Cursor c = managedQuery(contactData, null, null, null, null);
                    if (c.moveToFirst()) {
                        String address = c.getString(c.getColumnIndexOrThrow(
                                ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));

                        mainFragment.setTextBoxLocation(address, isButtonStartLocation);

                        SharedPreferences.Editor prefsEditor = PreferenceManager
                                .getDefaultSharedPreferences(this).edit();
                        if (isButtonStartLocation){
                            prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_ORIGIN_IS_MY_LOCATION, false);
                        }
                        else{
                            prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_DESTINATION_IS_MY_LOCATION, false);
                        }
                        prefsEditor.commit();
                        mainFragment.processAddress(isButtonStartLocation, address, false);
                        Log.d("TRQ_Trigger", "Case choose_Request");
                        mainFragment.processRequestTrip();
                    }

                    break;
                }
        }
    }

    @Override
    protected void onDestroy() {

        mainFragment = null;

        Log.d(OTPApp.TAG, "Released mainFragment with map in MyActivity.onDestroy()");

        super.onDestroy();
    }

    @Override
    public void onItinerariesLoaded(List<EnrichedItinerary> itineraries) {
        currentItineraryList.clear();
        currentItineraryList.addAll(itineraries);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean(OTPApp.PREFERENCE_KEY_LIVE_UPDATES,true)){
            boolean realtimeLegsOnItineraries = false;
            long soonerRealTimeDeparture = Long.MAX_VALUE;
            for (EnrichedItinerary itinerary : itineraries){
                for (Leg leg : itinerary.getItinerary().legs){
                    if (leg.realTime){
                        long legRealtimeDeparture = Long.parseLong(leg.startTime);
                        if (legRealtimeDeparture < soonerRealTimeDeparture){
                            soonerRealTimeDeparture = legRealtimeDeparture;
                        }
                        realtimeLegsOnItineraries = true;
                    }
                }
                if (realtimeLegsOnItineraries){
                    break;
                }
            }
            if (realtimeLegsOnItineraries){
                SharedPreferences.Editor prefsEditor = prefs.edit();
                prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_REALTIME_AVAILABLE, true);
                prefsEditor.commit();
                mainFragment.listenForTripTimeUpdates(true, soonerRealTimeDeparture);
            }
        }
    }

    @Override
    public void onItinerarySelected(int i, int animateCamera) {
        if (i >= currentItineraryList.size()) {
            return;
        }

        currentItineraryIndex = i;
        currentItinerary.clear();
        currentItinerary.addAll(currentItineraryList.get(i).getItinerary().legs);

        mainFragment.showRouteOnMap(currentItinerary, animateCamera);
        mainFragment.showFeaturesOnMap(currentItineraryList.get(i), false);
    }

    @Override
    public List<Leg> getCurrentItinerary() {
        return currentItinerary;
    }

    @Override
    public void onSwitchedToDirectionFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        Fragment directionFragment = new DirectionListFragment();
        transaction.add(R.id.mainFragment, directionFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.addToBackStack(null);

        Log.d("TRQ", "Creo DirectioN");

        transaction.commit();
    }

    @Override
    public OTPBundle getOTPBundle() {
        return bundle;
    }

    @Override
    public void setOTPBundle(OTPBundle b) {
        this.bundle = b;
        this.bundle.setCurrentItineraryIndex(currentItineraryIndex);
        this.bundle.setItineraryList(currentItineraryList);
    }

    @Override
    public void onSwitchedToMainFragment(Fragment f) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.remove(f);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fm.popBackStack();
        transaction.commit();
    }

    @Override
    public void setCurrentRequestString(String url) {
        currentRequestString = url;
    }

    @Override
    public String getCurrentRequestString() {
        return currentRequestString;
    }

    @Override
    public void zoomToLocation(LatLng location) {
        mainFragment.zoomToLocation(location);
    }

    @Override
    public List<EnrichedItinerary> getCurrentItineraryList() {
        return currentItineraryList;
    }

    @Override
    public int getCurrentItineraryIndex() {
        return currentItineraryIndex;
    }

    /**
     * @return the isButtonStartLocation
     */
    public boolean isButtonStartLocation() {
        return isButtonStartLocation;
    }

    /**
     * @param isButtonStartLocation the isButtonStartLocation to set
     */
    public void setButtonStartLocation(boolean isButtonStartLocation) {
        this.isButtonStartLocation = isButtonStartLocation;
    }

    public void setDateCompleteCallback(DateCompleteListener callback) {
        this.dateCompleteCallback = callback;
    }

    public void onDateComplete(Date tripDate, boolean scheduleType) {
        dateCompleteCallback.onDateComplete(tripDate, scheduleType);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}
