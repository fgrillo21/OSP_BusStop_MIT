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

package edu.usf.cutr.opentripplanner.android.fragments;

import android.Manifest;
import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

import org.opentripplanner.api.model.Itinerary;
import org.opentripplanner.api.model.Leg;
import org.opentripplanner.api.ws.GraphMetadata;
import org.opentripplanner.api.ws.Request;
import org.opentripplanner.index.model.TripTimeShort;
import org.opentripplanner.routing.bike_rental.BikeRentalStationList;
import org.opentripplanner.routing.core.OptimizeType;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import busstop.customtrip.model.CustomTrip;
import busstop.customtrip.model.EnrichedItinerary;
import busstop.customtrip.model.Place;
import edu.usf.cutr.opentripplanner.android.MyActivity;
import edu.usf.cutr.opentripplanner.android.OTPApp;
import edu.usf.cutr.opentripplanner.android.R;
import edu.usf.cutr.opentripplanner.android.SettingsActivity;
import edu.usf.cutr.opentripplanner.android.listeners.BikeRentalLoadCompleteListener;
import edu.usf.cutr.opentripplanner.android.listeners.DateCompleteListener;
import edu.usf.cutr.opentripplanner.android.listeners.MetadataRequestCompleteListener;
import edu.usf.cutr.opentripplanner.android.listeners.OTPGeocodingListener;
import edu.usf.cutr.opentripplanner.android.listeners.OtpFragment;
import edu.usf.cutr.opentripplanner.android.listeners.RequestTimesForTripsCompleteListener;
import edu.usf.cutr.opentripplanner.android.listeners.ServerCheckerCompleteListener;
import edu.usf.cutr.opentripplanner.android.listeners.ServerSelectorCompleteListener;
import edu.usf.cutr.opentripplanner.android.listeners.TripRequestCompleteListener;
import edu.usf.cutr.opentripplanner.android.maps.CustomUrlTileProvider;
import edu.usf.cutr.opentripplanner.android.model.OTPBundle;
import edu.usf.cutr.opentripplanner.android.model.OptimizeSpinnerItem;
import edu.usf.cutr.opentripplanner.android.model.Server;
import edu.usf.cutr.opentripplanner.android.sqlite.ServersDataSource;
import edu.usf.cutr.opentripplanner.android.tasks.BikeRentalLoad;
import edu.usf.cutr.opentripplanner.android.tasks.MetadataRequest;
import edu.usf.cutr.opentripplanner.android.tasks.OTPGeocoding;
import edu.usf.cutr.opentripplanner.android.tasks.RequestTimesForTrips;
import edu.usf.cutr.opentripplanner.android.tasks.ServerChecker;
import edu.usf.cutr.opentripplanner.android.tasks.ServerSelector;
import edu.usf.cutr.opentripplanner.android.tasks.TripRequest;
import edu.usf.cutr.opentripplanner.android.util.BikeRentalStationInfo;
import edu.usf.cutr.opentripplanner.android.util.ConversionUtils;
import edu.usf.cutr.opentripplanner.android.util.CustomAddress;
import edu.usf.cutr.opentripplanner.android.util.CustomInfoWindowAdapter;
import edu.usf.cutr.opentripplanner.android.util.DateTimeDialog;
import edu.usf.cutr.opentripplanner.android.util.DirectionsGenerator;
import edu.usf.cutr.opentripplanner.android.util.LocationUtil;
import edu.usf.cutr.opentripplanner.android.util.PlacesAutoCompleteAdapter;
import edu.usf.cutr.opentripplanner.android.util.RangeSeekBar;
import edu.usf.cutr.opentripplanner.android.util.RangeSeekBar.OnRangeSeekBarChangeListener;
import edu.usf.cutr.opentripplanner.android.util.RightDrawableOnTouchListener;
import edu.usf.cutr.opentripplanner.android.util.TripInfo;
import nice.fontaine.overpass.models.response.geometries.Element;
import nice.fontaine.overpass.models.response.geometries.Node;
import nice.fontaine.overpass.models.response.geometries.Relation;
import nice.fontaine.overpass.models.response.geometries.Way;

import static com.google.android.gms.location.LocationServices.API;
import static com.google.android.gms.location.LocationServices.FusedLocationApi;

/**
 * Main UI screen of the mOTPApp, showing the map.
 *
 * @author Khoa Tran
 */

public class MainFragment extends Fragment implements
        ServerSelectorCompleteListener,
        TripRequestCompleteListener, MetadataRequestCompleteListener,
        BikeRentalLoadCompleteListener, RequestTimesForTripsCompleteListener, OTPGeocodingListener,
        DateCompleteListener, OnRangeSeekBarChangeListener<Double>, ServerCheckerCompleteListener,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleMap.OnCameraChangeListener {

    private final String tripTag = "TRQ_TriggerRequest";

    private static LocationManager sLocationManager;

    private OTPApp mOTPApp;

    private Context mApplicationContext;

    private GoogleApiClient mGoogleApiClient;

    private OtpFragment mFragmentListener;

    private SharedPreferences mPrefs;

    private boolean mAppStarts = true;

    private Boolean mNeedToUpdateServersList = false;

    private Boolean mNeedToRunAutoDetect = false;

    private ImageButton mBtnDateDialog;

    private ImageButton mBtnSwapOriginDestination;

    private ImageButton mBtnMyLocation;

    private View mPanelDisplayDirection;

    private Spinner mItinerarySelectionSpinner;

    private ImageButton mBtnDisplayDirection;

    private MenuItem mGPS;

    private GoogleMap mMap;

    private boolean mMapFailed;

    private float mMaxZoomLevel;

    private TileOverlay mSelectedTileOverlay;

    private LatLng mSavedLastLocation;

    private LatLng mSavedLastLocationCheckedForServer;

    private Polyline mBoundariesPolyline;

    private AutoCompleteTextView mTbStartLocation;

    private AutoCompleteTextView mTbEndLocation;

    private CustomAddress mStartAddress;

    private CustomAddress mEndAddress;

    private String mResultTripStartLocation;

    private String mResultTripEndLocation;

    private Marker mStartMarker;

    private Marker mEndMarker;

    private LatLng mStartMarkerPosition;

    private LatLng mEndMarkerPosition;

    private boolean mIsStartLocationGeocodingCompleted = true;

    private boolean mIsEndLocationGeocodingCompleted = false;

    private boolean mIsStartLocationChangedByUser = true;

    private boolean mIsEndLocationChangedByUser = true;

    private Map<Marker, TripInfo> mModeMarkers;

    private Map<Marker, String> mLinks;

    private List<Polyline> mRoute;

    private Map<Marker, BikeRentalStationInfo> mBikeRentalStations;

    private Date mTripDate;

    private boolean mArriveBy;

    private int mMapPaddingLeft;

    private int mMapPaddingTop;

    private int mMapPaddingRight;

    private int mMapPaddingBottom;

    private AlarmManager mAlarmMgr;

    PendingIntent mAlarmIntentTripTimeUpdate;

    boolean mIsAlarmTripTimeUpdateActive;

    PendingIntent mAlarmIntentBikeRentalUpdate;

    boolean mIsAlarmBikeRentalUpdateActive;

    AlarmReceiver mAlarmReceiver;

    IntentFilter mIntentFilter;

    Intent mTripTimeUpdateIntent;

    Intent mBikeRentalUpdateIntent;

    CustomInfoWindowAdapter mCustomInfoWindowAdapter;

    boolean mNewAppVersion = false;

    PlacesAutoCompleteAdapter startLocationPlacesAutoCompleteAdapter;

    PlacesAutoCompleteAdapter endLocationPlacesAutoCompleteAdapter;

    boolean changingTextBoxWithAutocomplete = false;

    private OptimizeType previousOptimization;

    private GraphMetadata mCustomServerMetadata = null;

    private OTPGeocoding mGeoCodingTask;

    private CustomTrip customTrip;

    private List<EnrichedItinerary> itinerariesSelected = null;

    ImageButton mBtnShowFeatures;

    private boolean showFeatures = false;

    private List<Marker> featureMarkers = new ArrayList<>();

    enum FeatureType {
        HISTORIC,
        GREEN,
        PANORAMIC
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static void removeOnGlobalLayoutListener(View v,
            OnGlobalLayoutListener listener) {
        ViewTreeObserver viewTreeObserver = v.getViewTreeObserver();
        if (viewTreeObserver != null) {
            if (Build.VERSION.SDK_INT < 16) {
                viewTreeObserver.removeGlobalOnLayoutListener(listener);
            } else {
                viewTreeObserver.removeOnGlobalLayoutListener(listener);
            }
        } else {
            Log.w(OTPApp.TAG,
                    "Problems obtaining exact element's positions on screen, some other elements"
                            + "can be misplaced");
        }
    }

    public void setShowFeaturesOnMap(boolean show) {
        showFeatures = show;
    }

    public boolean isShowFeaturesEnabled() {
        return showFeatures;
    }

    public void setCustomTripInfo
    (CustomTrip customTrip) {
        Log.d("TRQ CUSTOM TRIP", "{" + customTrip.getMonuments() + ", " + customTrip.getGreenAreas() + ", " + customTrip.getOpenSpaces() +"}");
        this.customTrip = customTrip;
    }

    public void setNeedToUpdateServersList(Boolean needToUpdateServersList) {
        this.mNeedToUpdateServersList = needToUpdateServersList;
    }

    public void setNeedToRunAutoDetect(Boolean needToRunAutoDetect) {
        this.mNeedToRunAutoDetect = needToRunAutoDetect;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d("TRQ", "Main Attach");

        try {
            ((MyActivity) activity).setDateCompleteCallback(this);
            setFragmentListener((OtpFragment) activity);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OtpFragment");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState !=null) {
            Log.d(OTPApp.TAG, "Main onCreate" + savedInstanceState.toString());
        } else {
            Log.d("TRQ", "Main Create");
        }

        getActivity().getSupportFragmentManager()
                .addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        Log.i(OTPApp.TAG, "back stack changed ");
                        int backCount = getActivity().getSupportFragmentManager()
                                .getBackStackEntryCount();
                        if (backCount == 0) {
                            if (getFragmentListener() != null) {
                                mItinerarySelectionSpinner.setSelection(
                                        getFragmentListener().getCurrentItineraryIndex());
                            }
                        }
                    }
                });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        final View mainView = inflater.inflate(R.layout.main, container, false);

        if (mainView != null) {
            ViewTreeObserver vto = mainView.getViewTreeObserver();

            if (vto != null) {
                vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onGlobalLayout() {
                        MainFragment.removeOnGlobalLayoutListener(mainView, this);
                        int locationTbEndLocation[] = new int[2];
                        mTbEndLocation.getLocationInWindow(locationTbEndLocation);
                        int locationItinerarySelectionSpinner[] = new int[2];
                        mItinerarySelectionSpinner
                                .getLocationInWindow(locationItinerarySelectionSpinner);
                        DisplayMetrics metrics = MainFragment.this.getResources()
                                .getDisplayMetrics();
                        int windowHeight = metrics.heightPixels;
                        int paddingMargin = MainFragment.this.getResources()
                                .getInteger(R.integer.map_padding_margin);
                        mMapPaddingTop = locationTbEndLocation[1] + mTbEndLocation.getHeight() / 2
                                + paddingMargin;
                        mMapPaddingRight = 0;
                        mMapPaddingBottom = windowHeight - locationItinerarySelectionSpinner[1]
                                + paddingMargin;

                        if (mMap != null) {
                            mMap.setPadding(mMapPaddingLeft, mMapPaddingTop, mMapPaddingRight, mMapPaddingBottom);
                        }
                    }
                });
            } else {
                Log.w(OTPApp.TAG,
                        "Not possible to obtain exact element's positions on screen, some other"
                                + "elements can be misplaced");
            }

            mTbStartLocation = (AutoCompleteTextView) mainView
                    .findViewById(R.id.tbStartLocation);
            mTbEndLocation = (AutoCompleteTextView) mainView.findViewById(R.id.tbEndLocation);

            mBtnSwapOriginDestination = (ImageButton) mainView.findViewById(R.id.btnSwapOriginDestination);

            mBtnMyLocation = (ImageButton) mainView.findViewById(R.id.btnMyLocation);

            mBtnDateDialog = (ImageButton) mainView.findViewById(R.id.btnDateDialog);

            mBtnDisplayDirection = (ImageButton) mainView.findViewById(R.id.btnDisplayDirection);

            mPanelDisplayDirection = mainView.findViewById(R.id.panelDisplayDirection);

            mBtnShowFeatures = (ImageButton) mainView.findViewById(R.id.btnShowFeatures);

            mTbStartLocation.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            mTbEndLocation.setImeOptions(EditorInfo.IME_ACTION_DONE);
            mTbEndLocation.setImeActionLabel(getResources().getString(R.string.text_box_virtual_keyboard_done_label), EditorInfo.IME_ACTION_DONE);
            mTbEndLocation.requestFocus();

            mItinerarySelectionSpinner = (Spinner) mainView.findViewById(R.id.itinerarySelection);

            Log.d(OTPApp.TAG, "finish onStart()");

            if (Build.VERSION.SDK_INT > 11) {
                LayoutTransition l = new LayoutTransition();
                ViewGroup mainButtons = (ViewGroup) mainView
                        .findViewById(R.id.content_frame);
                mainButtons.setLayoutTransition(l);
            }

            return mainView;
        } else {
            Log.e(OTPApp.TAG, "Not possible to obtain main view, UI won't be correctly created");
            return null;
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null)
            Log.d(OTPApp.TAG, "onActivityCreated: " + savedInstanceState.toString());
        else
            Log.d(OTPApp.TAG, "onActivityCreated: NULL");

        mApplicationContext = getActivity().getApplicationContext();

        mIntentFilter = new IntentFilter(OTPApp.INTENT_UPDATE_BIKE_RENTAL_ACTION);
        mIntentFilter.addAction(OTPApp.INTENT_UPDATE_TRIP_TIME_ACTION);
        mIntentFilter.addAction(OTPApp.INTENT_NOTIFICATION_ACTION_OPEN_APP);
        mIntentFilter.addAction(OTPApp.INTENT_NOTIFICATION_ACTION_DISMISS_UPDATES);
        mBikeRentalUpdateIntent = new Intent(OTPApp.INTENT_UPDATE_BIKE_RENTAL_ACTION);
        mAlarmIntentBikeRentalUpdate = PendingIntent.getBroadcast(mApplicationContext, 0, mBikeRentalUpdateIntent, 0);
        mTripTimeUpdateIntent = new Intent(OTPApp.INTENT_UPDATE_TRIP_TIME_ACTION);
        mAlarmIntentTripTimeUpdate = PendingIntent.getBroadcast(mApplicationContext, 0, mTripTimeUpdateIntent, 0);
        mAlarmMgr = (AlarmManager)mApplicationContext.getSystemService(Context.ALARM_SERVICE);
        mIsAlarmBikeRentalUpdateActive = false;
        mIsAlarmTripTimeUpdateActive = false;
        mAlarmReceiver = new AlarmReceiver();

        mMap = retrieveMap(mMap);

        mOTPApp = ((OTPApp) getActivity().getApplication());

        mPrefs = PreferenceManager.getDefaultSharedPreferences(
                mApplicationContext);

        sLocationManager = (LocationManager) getActivity()
                .getSystemService(Context.LOCATION_SERVICE);

        if (savedInstanceState == null) {
            SharedPreferences.Editor prefsEditor = mPrefs.edit();
            prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_ORIGIN_IS_MY_LOCATION, true);
            prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_DESTINATION_IS_MY_LOCATION, false);
            prefsEditor.commit();
        }

        checkAppVersion();

        if (savedInstanceState == null) {
            mArriveBy = false;
            setTextBoxLocation(getResources().getString(R.string.text_box_my_location), true);
        }

        ArrayAdapter<OptimizeSpinnerItem> optimizationAdapter
                = new ArrayAdapter<OptimizeSpinnerItem>(
                getActivity(),
                android.R.layout.simple_list_item_single_choice,
                new OptimizeSpinnerItem[]{
                        new OptimizeSpinnerItem(
                                getResources().getString(R.string.left_panel_optimization_quick),
                                OptimizeType.QUICK),
                        new OptimizeSpinnerItem(
                                getResources().getString(R.string.left_panel_optimization_safe),
                                OptimizeType.SAFE),
                        new OptimizeSpinnerItem(
                                getResources().getString(R.string.left_panel_optimization_fewest_transfers),
                                OptimizeType.TRANSFERS)});

        Server selectedServer = mOTPApp.getSelectedServer();
        if (selectedServer != null) {
            if (!mMapFailed) {
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getServerCenter(selectedServer),getServerInitialZoom(selectedServer)));
                setInitialCameraLocation(false, selectedServer);
            }
        }

        mCustomInfoWindowAdapter = new CustomInfoWindowAdapter(getLayoutInflater(savedInstanceState), mApplicationContext);

        restoreState(savedInstanceState);

        if (!mMapFailed) {
            updateSelectedServer(false);
        }

        if (!mMapFailed) {
            initializeMapInterface(mMap);
        }
    }


    private void initializeMapInterface(GoogleMap mMap) {
        UiSettings uiSettings = mMap.getUiSettings();
        mMap.setMyLocationEnabled(true);
        mMap.setOnCameraChangeListener(this);
        uiSettings.setMyLocationButtonEnabled(false);
        uiSettings.setCompassEnabled(true);
        uiSettings.setAllGesturesEnabled(true);
        uiSettings.setZoomControlsEnabled(false);

        updateOverlay(ConversionUtils.getOverlayString(mApplicationContext));

        addInterfaceListeners();
    }


    private void addInterfaceListeners() {

        final OnMapClickListener onMapClickListener = new OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latlng) {
                InputMethodManager imm = (InputMethodManager) MainFragment.this.getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mTbEndLocation.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(mTbStartLocation.getWindowToken(), 0);

                if (mTbStartLocation.hasFocus()) {
                    Log.d(tripTag, "setMarker_Start addInterface");
                    setMarker(true, latlng, true, true);
                } else {
                    Log.d(tripTag, "setMarker_Start addInterface else");
                    setMarker(false, latlng, true, true);
                }
                Log.d(tripTag, "onMapClickListener");
                processRequestTrip();
            }
        };
        mMap.setOnMapClickListener(onMapClickListener);

        OnFocusChangeListener tbLocationOnFocusChangeListener = new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    TextView tv = (TextView) v;
                    mMap.setOnMapClickListener(onMapClickListener);
                    CharSequence tvCharSequence = tv.getText();

                    if (tvCharSequence != null) {
                        String text = tvCharSequence.toString();

                        if (!TextUtils.isEmpty(text)) {
                            if (v.getId() == R.id.tbStartLocation) {
                                if (!mIsStartLocationGeocodingCompleted) {
                                    mTbStartLocation.showDropDown();
                                }
                            } else if (v.getId() == R.id.tbEndLocation) {
                                if (!mIsEndLocationGeocodingCompleted) {
                                    mTbEndLocation.showDropDown();
                                }
                            }
                        }
                    } else {
                        Log.w(OTPApp.TAG,
                                "Focus has changed, but was not possible to obtain start/end"
                                        + " textbox text");
                    }
                }
            }
        };
        mTbStartLocation.setOnFocusChangeListener(tbLocationOnFocusChangeListener);
        mTbEndLocation.setOnFocusChangeListener(tbLocationOnFocusChangeListener);

        OnMarkerDragListener onMarkerDragListener = new OnMarkerDragListener() {

            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng markerLatlng = marker.getPosition();

                if (((mOTPApp.getSelectedServer() != null) && LocationUtil
                        .checkPointInBoundingBox(markerLatlng, mOTPApp.getSelectedServer()))
                        || (mOTPApp.getSelectedServer() == null)) {
                    if ((mStartMarker != null) && (marker.hashCode() == mStartMarker.hashCode())) {
                        if (mPrefs
                                .getBoolean(OTPApp.PREFERENCE_KEY_USE_INTELLIGENT_MARKERS, true)) {
                            updateMarkerPosition(markerLatlng, true);
                        } else {
                            mIsStartLocationGeocodingCompleted = true;
                            removeFocus(true);
                            Log.d(tripTag, "setMarker_Start marer drag end");
                            setMarker(true, markerLatlng, false, true);
                        }
                        mStartMarkerPosition = markerLatlng;
                        Log.d(tripTag, "onMarkerDragEnd_start");
                        processRequestTrip();
                    } else if ((mEndMarker != null) && (marker.hashCode() == mEndMarker
                            .hashCode())) {
                        if (mPrefs
                                .getBoolean(OTPApp.PREFERENCE_KEY_USE_INTELLIGENT_MARKERS, true)) {
                            updateMarkerPosition(markerLatlng, false);
                        } else {
                            mIsEndLocationGeocodingCompleted = true;
                            removeFocus(false);
                            setMarker(false, markerLatlng, false, true);
                        }
                        mEndMarkerPosition = markerLatlng;
                        Log.d(tripTag, "onMarkerDragEnd_End");
                        processRequestTrip();
                    }
                } else {

                    if ((mStartMarker != null) && (marker.hashCode() == mStartMarker.hashCode())) {
                        marker.setPosition(mStartMarkerPosition);
                    } else {
                        marker.setPosition(mEndMarkerPosition);
                    }
                    Toast.makeText(mApplicationContext, mApplicationContext.getResources()
                            .getString(R.string.toast_map_markers_marker_out_of_boundaries), Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onMarkerDragStart(Marker marker) {
                InputMethodManager imm = (InputMethodManager) MainFragment.this.getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mTbEndLocation.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(mTbStartLocation.getWindowToken(), 0);
            }
        };
        mMap.setOnMarkerDragListener(onMarkerDragListener);

        OnMapLongClickListener onMapLongClickListener = new OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latlng) {
                InputMethodManager imm = (InputMethodManager) MainFragment.this.getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mTbEndLocation.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(mTbStartLocation.getWindowToken(), 0);

                final LatLng latLngFinal = latlng;
                final CharSequence[] items = {mApplicationContext.getResources()
                        .getString(R.string.point_type_selector_start_marker_option),
                        mApplicationContext.getResources()
                                .getString(R.string.point_type_selector_end_marker_option)};

                AlertDialog.Builder builder = new AlertDialog.Builder(
                        MainFragment.this.getActivity());
                builder.setTitle(getResources().getString(R.string.point_type_selector_title));
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == 0) {
                            Log.d(tripTag, "setMarker_Start markerdaragglong");
                            setMarker(true, latLngFinal, true, true);
                        } else {
                            setMarker(false, latLngFinal, true, true);
                        }
                        Log.d(tripTag, "onMapLongClickListener");
                        processRequestTrip();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        };
        mMap.setOnMapLongClickListener(onMapLongClickListener);

        OnInfoWindowClickListener onInfoWindowClickListener = new OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                if (mBikeRentalStations != null && mBikeRentalStations.containsKey(marker)){
                    BikeRentalStationInfo bikeRentalStationInfo = mBikeRentalStations.get(marker);
                    Log.d(tripTag, "setMarker_Start oninfowindow");
                    setMarker(true, bikeRentalStationInfo.getLocation(), false, false);
                    setTextBoxLocation(bikeRentalStationInfo.getName(), true);
                }
                Log.d(tripTag, "SAVING OTPBundle");
                saveOTPBundle();
                OTPBundle otpBundle = getFragmentListener().getOTPBundle();
                if (!mLinks.containsKey(marker)) {
                    Matcher matcher = Pattern.compile("\\d+").matcher(marker.getTitle());
                    if (matcher.find()) {
                        String numberString = marker.getTitle().substring(0, matcher.end());
                        //Step indexes shown to the user are in a scale starting by 1 but instructions steps internally start by 0
                        int currentStepIndex = Integer.parseInt(numberString) - 1;
                        otpBundle.setCurrentStepIndex(currentStepIndex);
                        otpBundle.setFromInfoWindow(true);
                        getFragmentListener().setOTPBundle(otpBundle);
                        getFragmentListener().onSwitchedToDirectionFragment();
                    }
                }
                else {
                    String url = mLinks.get(marker);

                    if (url != null) {

                        if (!url.equals("")) {
                            String[] domain = url.split(":");

                            String wikipedia = "";

                            wikipedia = "https://" + domain[0] + ".wikipedia.org/wiki/" + domain[1];
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(wikipedia));
                            startActivity(browserIntent);
                        }
                    }
                }
            }
        };
        mMap.setOnInfoWindowClickListener(onInfoWindowClickListener);

        startLocationPlacesAutoCompleteAdapter = new PlacesAutoCompleteAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item,
                mOTPApp.getSelectedServer());
        mTbStartLocation.setAdapter(startLocationPlacesAutoCompleteAdapter);
        mTbStartLocation.setThreshold(3);

        endLocationPlacesAutoCompleteAdapter = new PlacesAutoCompleteAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item,
                mOTPApp.getSelectedServer());
        mTbStartLocation.setAdapter(startLocationPlacesAutoCompleteAdapter);

        mTbEndLocation.setAdapter(endLocationPlacesAutoCompleteAdapter);
        mTbEndLocation .setThreshold(3);

        OnTouchListener otlStart = new RightDrawableOnTouchListener(mTbStartLocation) {
            @Override
            public boolean onDrawableTouch(final MotionEvent event) {

                final CharSequence[] items = {
                        getResources().getString(R.string.text_box_dialog_location_type_current_location),
                        getResources().getString(R.string.text_box_dialog_location_type_contact),
                        getResources().getString(R.string.text_box_dialog_location_type_map_point)};

                AlertDialog.Builder builder = new AlertDialog.Builder(
                        MainFragment.this.getActivity());
                builder.setTitle(getResources().getString(R.string.text_box_dialog_choose_location_type_start));
                builder.setItems(items, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {
                        if (items[item].equals(getResources()
                                .getString(R.string.text_box_dialog_location_type_current_location))) {
                            LatLng mCurrentLatLng = getLastLocation();
                            if (mCurrentLatLng != null) {
                                SharedPreferences.Editor prefsEditor = mPrefs.edit();
                                setTextBoxLocation(getResources().getString(R.string.text_box_my_location),
                                        true);
                                prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_ORIGIN_IS_MY_LOCATION,
                                        true);

                                if (mStartMarker != null) {
                                    mStartMarker.remove();
                                    mStartMarker = null;
                                }

                                prefsEditor.commit();
                                mIsStartLocationGeocodingCompleted = true;
                                Log.d(tripTag, "onDrawableTouch_Start");
                                processRequestTrip();
                            } else {
                                Toast.makeText(MainFragment.this.mApplicationContext,
                                        mApplicationContext.getResources()
                                                .getString(R.string.toast_tripplanner_current_location_error),
                                        Toast.LENGTH_LONG).show();
                            }
                        } else if (items[item]
                                .equals(getResources().getString(R.string.text_box_dialog_location_type_contact))) {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType(
                                    ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_TYPE);
                            ((MyActivity) MainFragment.this.getActivity())
                                    .setButtonStartLocation(true);

                            MainFragment.this.getActivity()
                                    .startActivityForResult(intent,
                                            OTPApp.CHOOSE_CONTACT_REQUEST_CODE);

                        } else { // Point on Map
                            if (mStartMarker != null) {
                                updateMarkerPosition(mStartMarker.getPosition(), true);
                            } else {
                                setTextBoxLocation("", true);
                                mTbStartLocation.setHint(
                                        getResources().getString(R.string.text_box_need_to_place_marker));
                                mTbStartLocation.requestFocus();
                            }
                        }
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            }

        };

        mTbStartLocation.setOnTouchListener(otlStart);

        OnTouchListener otlEnd = new RightDrawableOnTouchListener(mTbEndLocation) {
            @Override
            public boolean onDrawableTouch(final MotionEvent event) {

                final CharSequence[] items = {
                        getResources().getString(R.string.text_box_dialog_location_type_current_location),
                        getResources().getString(R.string.text_box_dialog_location_type_contact),
                        getResources().getString(R.string.text_box_dialog_location_type_map_point)};

                AlertDialog.Builder builder = new AlertDialog.Builder(
                        MainFragment.this.getActivity());
                builder.setTitle(getResources().getString(R.string.text_box_dialog_choose_location_type_end));
                builder.setItems(items, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {
                        if (items[item].equals(getResources()
                                .getString(R.string.text_box_dialog_location_type_current_location))) {
                            LatLng mCurrentLatLng = getLastLocation();
                            if (mCurrentLatLng != null) {
                                SharedPreferences.Editor prefsEditor = mPrefs.edit();
                                setTextBoxLocation(getResources().getString(R.string.text_box_my_location),
                                        false);
                                prefsEditor.putBoolean(
                                        OTPApp.PREFERENCE_KEY_DESTINATION_IS_MY_LOCATION, true);

                                if (mEndMarker != null) {
                                    mEndMarker.remove();
                                    mEndMarker = null;
                                }

                                prefsEditor.commit();
                                mIsEndLocationGeocodingCompleted = true;
                                Log.d(tripTag, "onDrawableTouch_End");
                                processRequestTrip();
                            } else {
                                Toast.makeText(MainFragment.this.mApplicationContext,
                                        mApplicationContext.getResources()
                                                .getString(R.string.toast_tripplanner_current_location_error),
                                        Toast.LENGTH_LONG).show();
                            }


                        } else if (items[item]
                                .equals(getResources().getString(R.string.text_box_dialog_location_type_contact))) {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType(
                                    ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_TYPE);
                            ((MyActivity) MainFragment.this.getActivity())
                                    .setButtonStartLocation(false);

                            MainFragment.this.getActivity()
                                    .startActivityForResult(intent,
                                            OTPApp.CHOOSE_CONTACT_REQUEST_CODE);

                        } else { // Point on Map
                            if (mEndMarker != null) {
                                updateMarkerPosition(mEndMarker.getPosition(), false);
                            } else {
                                setTextBoxLocation("", false);
                                mTbEndLocation.setHint(
                                        getResources().getString(R.string.text_box_need_to_place_marker));
                                mTbEndLocation.requestFocus();
                            }
                        }
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            }

        };

        mTbEndLocation.setOnTouchListener(otlEnd);

        OnItemClickListener tbAutocompleteOnItemClickListener = new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                changingTextBoxWithAutocomplete = true;
                boolean isStartBox = mTbStartLocation.hasFocus();
                CustomAddress selectedAddress = (CustomAddress) adapterView.getItemAtPosition(position);
                Log.d(tripTag, "UseNewAddress: tbAutocomplete: " + selectedAddress);
                useNewAddress(isStartBox, selectedAddress, false);
            }
        };

        mTbStartLocation.setOnItemClickListener(tbAutocompleteOnItemClickListener);
        mTbEndLocation.setOnItemClickListener(tbAutocompleteOnItemClickListener);
        TextWatcher textWatcherStart = new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!changingTextBoxWithAutocomplete){
                    if (mIsStartLocationChangedByUser) {
                        SharedPreferences.Editor prefsEditor = mPrefs.edit();
                        prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_ORIGIN_IS_MY_LOCATION, false);
                        prefsEditor.commit();
                        mIsStartLocationGeocodingCompleted = false;
                    } else {
                        mIsStartLocationChangedByUser = true;
                    }
                }
            }
        };

        TextWatcher textWatcherEnd = new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!changingTextBoxWithAutocomplete) {
                    if (mIsEndLocationChangedByUser) {
                        SharedPreferences.Editor prefsEditor = mPrefs.edit();
                        prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_DESTINATION_IS_MY_LOCATION, false);
                        prefsEditor.commit();
                        mIsEndLocationGeocodingCompleted = false;
                    } else {
                        mIsEndLocationChangedByUser = true;
                    }
                }
            }
        };

        mTbStartLocation.addTextChangedListener(textWatcherStart);
        mTbEndLocation.addTextChangedListener(textWatcherEnd);

        OnEditorActionListener tbLocationOnEditorActionListener = new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                    KeyEvent event) {
                if (v.getId() == R.id.tbStartLocation) {
                    if (actionId == EditorInfo.IME_ACTION_NEXT
                            || (event != null
                            && event.getAction() == KeyEvent.ACTION_DOWN && event
                            .getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                        if (!mIsStartLocationGeocodingCompleted
                                && !mPrefs
                                .getBoolean(OTPApp.PREFERENCE_KEY_ORIGIN_IS_MY_LOCATION, true)) {
                            CharSequence tvCharSequence = v.getText();
                            if (tvCharSequence != null) {
                                processAddress(true, tvCharSequence.toString(), false);
                            } else {
                                Log.w(OTPApp.TAG,
                                        "User switched to next input, but was not possible to"
                                                + "obtain start/end textbox text");
                            }
                        }
                    }
                } else if (v.getId() == R.id.tbEndLocation) {
                    if (actionId == EditorInfo.IME_ACTION_DONE
                            || (event != null
                            && event.getAction() == KeyEvent.ACTION_DOWN && event
                            .getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                        if (!mIsEndLocationGeocodingCompleted
                                && !mPrefs
                                .getBoolean(OTPApp.PREFERENCE_KEY_DESTINATION_IS_MY_LOCATION, true)) {
                            CharSequence tvCharSequence = v.getText();
                            if (tvCharSequence != null) {
                                processAddress(false, tvCharSequence.toString(), false);
                            } else {
                                Log.w(OTPApp.TAG,
                                        "User pressed done, but was not possible to"
                                                + "obtain start/end textbox text");
                            }
                        }
                        Log.d(tripTag, "tbLocationOnEditorActionListener");
                        processRequestTrip();
                    }
                }
                return false;
            }
        };

        mTbStartLocation
                .setOnEditorActionListener(tbLocationOnEditorActionListener);
        mTbEndLocation
                .setOnEditorActionListener(tbLocationOnEditorActionListener);

        OnClickListener oclDisplayDirection = new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.d(tripTag, "SAVING OTPBundle 10044");
                saveOTPBundle();
                getFragmentListener().onSwitchedToDirectionFragment();
            }
        };
        mBtnDisplayDirection.setOnClickListener(oclDisplayDirection);

        // Do NOT show direction icon if there is no direction yet
        toggleItinerarySelectionSpinner(!getFragmentListener().getCurrentItinerary().isEmpty());

        OnClickListener oclMyLocation = new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                LatLng mCurrentLatLng = getLastLocation();

//                Address current = getCurrentLocation();

//                Log.d("GPS", current.toString());

                if (mCurrentLatLng == null) {
                    Toast.makeText(mApplicationContext,
                            mApplicationContext.getResources()
                                    .getString(R.string.toast_tripplanner_current_location_error),
                            Toast.LENGTH_LONG).show();
                } else {
                    if (!mMapFailed){
                        if (mMap.getCameraPosition().zoom < OTPApp.defaultMyLocationZoomLevel) {
                            mMap.animateCamera(CameraUpdateFactory
                                    .newLatLngZoom(mCurrentLatLng, OTPApp.defaultMyLocationZoomLevel));
                        } else {
                            mMap.animateCamera(CameraUpdateFactory.newLatLng(getLastLocation()));
                        }
                    }
                }
            }
        };
        mBtnMyLocation.setOnClickListener(oclMyLocation);

        OnClickListener oclDateDialog = new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                FragmentTransaction ft = MainFragment.this.getActivity().getSupportFragmentManager()
                        .beginTransaction();
                Fragment prev = MainFragment.this.getActivity().getSupportFragmentManager()
                        .findFragmentByTag(OTPApp.TAG_FRAGMENT_DATE_TIME_DIALOG);
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                // Create and show the dialog.
                DateTimeDialog newFragment = new DateTimeDialog();

                Date dateDialogDate;
                if (mTripDate == null) {
                    dateDialogDate = Calendar.getInstance().getTime();
                } else {
                    dateDialogDate = mTripDate;
                }

                Bundle bundle = new Bundle();
                bundle.putSerializable(OTPApp.BUNDLE_KEY_TRIP_DATE, dateDialogDate);
                bundle.putBoolean(OTPApp.BUNDLE_KEY_ARRIVE_BY, mArriveBy);
                newFragment.setArguments(bundle);
                ft.commit();

                newFragment.show(MainFragment.this.getActivity().getSupportFragmentManager(),
                        OTPApp.TAG_FRAGMENT_DATE_TIME_DIALOG);
            }
        };
        mBtnDateDialog.setOnClickListener(oclDateDialog);

        OnClickListener oclSwapOriginDestination = new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!mMapFailed){
                    boolean tempBoolean;
                    CustomAddress tempAddress;
                    tempBoolean = mIsStartLocationGeocodingCompleted;
                    mIsStartLocationGeocodingCompleted = mIsEndLocationGeocodingCompleted;
                    mIsEndLocationGeocodingCompleted = tempBoolean;
                    tempBoolean = mIsStartLocationChangedByUser;
                    mIsStartLocationChangedByUser = mIsEndLocationChangedByUser;
                    mIsEndLocationChangedByUser = tempBoolean;
                    tempAddress = mStartAddress;
                    mStartAddress = mEndAddress;
                    mEndAddress = tempAddress;
                    SharedPreferences.Editor prefsEditor = mPrefs.edit();
                    Boolean tempPref = mPrefs
                            .getBoolean(OTPApp.PREFERENCE_KEY_ORIGIN_IS_MY_LOCATION, false);
                    prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_ORIGIN_IS_MY_LOCATION, mPrefs
                            .getBoolean(OTPApp.PREFERENCE_KEY_DESTINATION_IS_MY_LOCATION, false));
                    prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_DESTINATION_IS_MY_LOCATION, tempPref);
                    prefsEditor.commit();
                    Marker mOldStartMarker = mStartMarker;
                    MarkerOptions newStartMarkerOptions = new MarkerOptions();
                    MarkerOptions newEndMarkerOptions = new MarkerOptions();
                    if (mEndMarker != null) {
                        newStartMarkerOptions.snippet(mEndMarker.getSnippet());
                        newStartMarkerOptions.title(mEndMarker.getTitle());
                        newStartMarkerOptions.position(mEndMarker.getPosition());
                        newStartMarkerOptions.icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    }
                    if (mStartMarker != null) {
                        mStartMarker.remove();
                    }
                    if (mEndMarker != null){
                        mStartMarker = mMap.addMarker(newStartMarkerOptions);
                        mStartMarkerPosition = mStartMarker.getPosition();
                    }
                    else{
                        mStartMarker = null;
                        mStartMarkerPosition = null;
                    }
                    if (mOldStartMarker != null) {
                        newEndMarkerOptions.snippet(mOldStartMarker.getSnippet());
                        newEndMarkerOptions.title(mOldStartMarker.getTitle());
                        newEndMarkerOptions.position(mOldStartMarker.getPosition());
                        newEndMarkerOptions.icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    }
                    if (mEndMarker != null) {
                        mEndMarker.remove();
                    }
                    if (mOldStartMarker != null){
                        mEndMarker = mMap.addMarker(newEndMarkerOptions);
                        mEndMarkerPosition = mEndMarker.getPosition();
                    }
                    else{
                        mEndMarker = null;
                        mEndMarkerPosition = null;
                    }

                    CharSequence tempCharSequence = mTbStartLocation.getText();
                    setTextBoxLocation(mTbEndLocation.getText().toString(), true);
                    setTextBoxLocation(tempCharSequence.toString(), false);

                    if (TextUtils.isEmpty(mTbStartLocation.getText())) {
                        mTbStartLocation.setHint(getResources()
                                .getString(R.string.text_box_start_location_hint));
                    }
                    if (TextUtils.isEmpty(mTbEndLocation.getText())) {
                        mTbEndLocation.setHint(getResources()
                                .getString(R.string.text_box_end_location_hint));
                    }
                    Log.d(tripTag, "oclSwapOriginDestination");
                    processRequestTrip();
                }
            }
        };
        mBtnSwapOriginDestination.setOnClickListener(oclSwapOriginDestination);

        AdapterView.OnItemSelectedListener itinerarySpinnerListener
                = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (mFragmentListener.getCurrentItinerary() != null){
                    if (mFragmentListener.getCurrentItineraryIndex() != position) {
                        mFragmentListener.onItinerarySelected(position, 2);

                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        final int currentItineraryIndex = mFragmentListener.getCurrentItineraryIndex();

        mItinerarySelectionSpinner.setSelection(currentItineraryIndex);
        mItinerarySelectionSpinner.setOnItemSelectedListener(itinerarySpinnerListener);


        mBtnShowFeatures.setOnClickListener(new OnClickListener() {

            public void onClick(View button) {
                if (button.isSelected()){
                    button.setSelected(false);
                    toggleFeaturesOnMap(false);
                    showFeatures = false;
                } else {
                    button.setSelected(true);
                    toggleFeaturesOnMap(true);
                    showFeatures = true;
                }
            }

        });

        GoogleMap.OnMarkerClickListener onMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
//                if (marker.isInfoWindowShown()) {
//                    marker.hideInfoWindow();
//                    return true;
//                }

                return false;
            }
        };

        mMap.setOnMarkerClickListener(onMarkerClickListener);
    }

    /**
     * Wrapper to call request trip, triggering geocoding processes if it's
     * necessary.
     */
    public void processRequestTrip() {
        Log.d(tripTag, Boolean.toString(mIsStartLocationGeocodingCompleted) + Boolean.toString(mIsEndLocationGeocodingCompleted));
        if (mIsStartLocationGeocodingCompleted && mIsEndLocationGeocodingCompleted){
            itinerariesSelected = null;
            requestTrip();
        }
    }

    /**
     * Sends information of the text boxes to fragment listener class through a
     * bundle.
     * <p>
     * Fragment listener provides intercommunication with other fragments or classes.
     */
    private void saveOTPBundle() {
        Log.d("TRQ", "SAVE BUNDLE");
        OTPBundle bundle = new OTPBundle();
        bundle.setFromText(mResultTripStartLocation);
        bundle.setToText(mResultTripEndLocation);

        this.getFragmentListener().setOTPBundle(bundle);
    }

    private void restoreState(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            Log.d("TRQ", "RESTORE STATE: " + savedInstanceState.toString());
        }
        else {
            Log.d("TRQ", "RESTORE STATE");
        }

        if (savedInstanceState != null) {
            mMap = retrieveMap(mMap);

            if (!mMapFailed) {
                boolean mapFailedBefore = savedInstanceState
                        .getBoolean(OTPApp.BUNDLE_KEY_MAP_FAILED);

                if (mapFailedBefore) {
                    enableUIElements(true);

                    initializeMapInterface(mMap);
                }

                if (!mapFailedBefore) {
                    updateOverlay(ConversionUtils.getOverlayString(mApplicationContext));
                }

                setTextBoxLocation(
                        savedInstanceState.getString(OTPApp.BUNDLE_KEY_TB_START_LOCATION), true);
                setTextBoxLocation(savedInstanceState.getString(OTPApp.BUNDLE_KEY_TB_END_LOCATION),
                        false);
                CameraPosition camPosition = savedInstanceState
                        .getParcelable(OTPApp.BUNDLE_KEY_MAP_CAMERA);
                if (camPosition != null) {
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition));
                }

                if ((mStartMarkerPosition = savedInstanceState
                        .getParcelable(OTPApp.BUNDLE_KEY_MAP_START_MARKER_POSITION)) != null) {
                    mStartMarker = addStartEndMarker(mStartMarkerPosition, true);
                }
                if ((mEndMarkerPosition = savedInstanceState
                        .getParcelable(OTPApp.BUNDLE_KEY_MAP_END_MARKER_POSITION)) != null) {
                    mEndMarker = addStartEndMarker(mEndMarkerPosition, false);
                }

                mIsStartLocationGeocodingCompleted = savedInstanceState
                        .getBoolean(OTPApp.BUNDLE_KEY_IS_START_LOCATION_GEOCODING_PROCESSED);
                mIsEndLocationGeocodingCompleted = savedInstanceState
                        .getBoolean(OTPApp.BUNDLE_KEY_IS_END_LOCATION_GEOCODING_PROCESSED);
                mAppStarts = savedInstanceState.getBoolean(OTPApp.BUNDLE_KEY_APP_STARTS);
                mIsStartLocationChangedByUser = savedInstanceState
                        .getBoolean(OTPApp.BUNDLE_KEY_IS_START_LOCATION_CHANGED_BY_USER);
                mIsEndLocationChangedByUser = savedInstanceState
                        .getBoolean(OTPApp.BUNDLE_KEY_IS_END_LOCATION_CHANGED_BY_USER);

                mSavedLastLocation = savedInstanceState
                        .getParcelable(OTPApp.BUNDLE_KEY_SAVED_LAST_LOCATION);
                mSavedLastLocationCheckedForServer = savedInstanceState
                        .getParcelable(OTPApp.BUNDLE_KEY_SAVED_LAST_LOCATION_CHECKED_FOR_SERVER);

                OTPBundle otpBundle = (OTPBundle) savedInstanceState
                        .getSerializable(OTPApp.BUNDLE_KEY_OTP_BUNDLE);
                Log.d("TRQ", "getSerializable");
                if (otpBundle != null) {

                    List<EnrichedItinerary> itineraries = otpBundle.getItineraryList();
                    getFragmentListener().onItinerariesLoaded(itineraries);
                    getFragmentListener().onItinerarySelected(otpBundle.getCurrentItineraryIndex(), 0);
                    fillItinerariesSpinner(itineraries);
                }

                Date savedTripDate = (Date) savedInstanceState
                        .getSerializable(OTPApp.BUNDLE_KEY_TRIP_DATE);
                if (savedTripDate != null) {
                    mTripDate = savedTripDate;
                }
                mArriveBy = savedInstanceState.getBoolean(OTPApp.BUNDLE_KEY_ARRIVE_BY, false);

                if (savedInstanceState.getString(OTPApp.BUNDLE_KEY_RESULT_TRIP_START_LOCATION)
                        != null) {
                    mResultTripStartLocation = savedInstanceState
                            .getString(OTPApp.BUNDLE_KEY_RESULT_TRIP_START_LOCATION);
                }
                if (savedInstanceState.getString(OTPApp.BUNDLE_KEY_RESULT_TRIP_END_LOCATION)
                        != null) {
                    mResultTripEndLocation = savedInstanceState
                            .getString(OTPApp.BUNDLE_KEY_RESULT_TRIP_END_LOCATION);
                }

                mIsStartLocationChangedByUser = false;
                mIsEndLocationChangedByUser = false;

                mIsAlarmBikeRentalUpdateActive = savedInstanceState
                        .getBoolean(OTPApp.BUNDLE_KEY__IS_ALARM_BIKE_RENTAL_ACTIVE, false);

                previousOptimization = (OptimizeType) savedInstanceState
                        .getSerializable(OTPApp.BUNDLE_KEY_PREVIOUS_OPTIMIZATION);
                mCustomServerMetadata = (GraphMetadata) savedInstanceState
                        .getSerializable(OTPApp.BUNDLE_KEY_CUSTOM_SERVER_METADATA);
            }
        }
    }

    /**
     * Activates/deactivates all the UI, avoiding to take care of the possible
     * listeners functions if the application is in a non working state.
     *
     * @param enable if true elements will be enabled
     */
    private void enableUIElements(boolean enable) {
        int visibility;
        if (enable) {
            setHasOptionsMenu(true);
            visibility = View.VISIBLE;
        } else {
            setHasOptionsMenu(false);
            visibility = View.INVISIBLE;
        }
        mTbStartLocation.setVisibility(visibility);
        mTbEndLocation.setVisibility(visibility);
        mBtnDateDialog.setVisibility(visibility);
        mBtnMyLocation.setVisibility(visibility);
        toggleItinerarySelectionSpinner(enable);
    }

    /**
     * Shows/hides itinerary drop down list of map main view.
     * <p>
     * Moves related buttons for MyLocation and the handle to show the left
     * panel accordingly.
     *
     * @param show if true drop down list will be shown
     */
    private void toggleItinerarySelectionSpinner(boolean show) {
        RelativeLayout.LayoutParams paramsMyLocation
                = (RelativeLayout.LayoutParams) mBtnMyLocation.getLayoutParams();
        if (paramsMyLocation != null) {
            if (show) {
                mPanelDisplayDirection.setVisibility(View.VISIBLE);
                //Workaround, this value proves to be false, but is dirty. This is because removeRule is not defined in early versions of the API
                paramsMyLocation.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
            } else {
                mPanelDisplayDirection.setVisibility(View.INVISIBLE);
                paramsMyLocation.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            }
            mBtnMyLocation.setLayoutParams(paramsMyLocation);
            mBtnMyLocation.requestLayout();
        } else {
            Log.w(OTPApp.TAG, "Not possible to move down itineraries spinner");
        }
    }

    private void requestTrip() {
        LatLng mCurrentLatLng = getLastLocation();
        String startLocationString;
        String endLocationString;

        Boolean isOriginMyLocation = mPrefs
                .getBoolean(OTPApp.PREFERENCE_KEY_ORIGIN_IS_MY_LOCATION, false);
        Boolean isDestinationMyLocation = mPrefs
                .getBoolean(OTPApp.PREFERENCE_KEY_DESTINATION_IS_MY_LOCATION, false);

        toggleItinerarySelectionSpinner(false);

        if (mRoute != null) {
            for (Polyline p : mRoute) {
                p.remove();
            }
            mRoute = null;
        }
        if (mModeMarkers != null) {
            for (Map.Entry<Marker, TripInfo> entry : mModeMarkers.entrySet()) {
                entry.getKey().remove();
            }
            mModeMarkers = null;
        }

        if (isOriginMyLocation && isDestinationMyLocation) {
            Toast.makeText(MainFragment.this.mApplicationContext, mApplicationContext.getResources()
                    .getString(R.string.toast_tripplanner_origin_destination_are_equal), Toast.LENGTH_SHORT)
                    .show();
            return;
        } else if (isOriginMyLocation || isDestinationMyLocation) {
            if (mCurrentLatLng == null) {
                Toast.makeText(MainFragment.this.mApplicationContext,
                        mApplicationContext.getResources()
                                .getString(R.string.toast_tripplanner_current_location_error),
                        Toast.LENGTH_LONG).show();
                return;
            } else {
                if (isOriginMyLocation) {
                    startLocationString = mCurrentLatLng.latitude + "," + mCurrentLatLng.longitude;
                    if (mEndMarker == null) {
                        Toast.makeText(MainFragment.this.mApplicationContext,
                                mApplicationContext.getResources()
                                        .getString(R.string.toast_tripplanner_need_to_place_markers_before_planning),
                                Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        endLocationString = mEndMarker.getPosition().latitude + "," + mEndMarker
                                .getPosition().longitude;
                    }
                } else {
                    endLocationString = mCurrentLatLng.latitude + "," + mCurrentLatLng.longitude;
                    if (mStartMarker == null) {
                        Toast.makeText(MainFragment.this.mApplicationContext,
                                mApplicationContext.getResources()
                                        .getString(R.string.toast_tripplanner_need_to_place_markers_before_planning),
                                Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        startLocationString = mStartMarker.getPosition().latitude + ","
                                + mStartMarker
                                .getPosition().longitude;
                    }
                }
            }
        } else {
            if ((mStartMarker == null) || (mEndMarker == null)) {
                Toast.makeText(MainFragment.this.mApplicationContext,
                        mApplicationContext.getResources()
                                .getString(R.string.toast_tripplanner_need_to_place_markers_before_planning),
                        Toast.LENGTH_SHORT).show();
                return;
            } else {
                startLocationString = mStartMarker.getPosition().latitude + "," + mStartMarker
                        .getPosition().longitude;
                endLocationString = mEndMarker.getPosition().latitude + "," + mEndMarker
                        .getPosition().longitude;
                if (startLocationString.equals(endLocationString)){
                    Toast.makeText(MainFragment.this.mApplicationContext, mApplicationContext.getResources()
                            .getString(R.string.toast_tripplanner_origin_destination_are_equal), Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
            }
        }

        if (!mIsStartLocationGeocodingCompleted && !isOriginMyLocation) {
            Toast.makeText(MainFragment.this.mApplicationContext, mApplicationContext.getResources()
                    .getString(R.string.toast_tripplanner_need_to_place_markers_before_planning), Toast.LENGTH_SHORT)
                    .show();
            return;
        } else if (!mIsEndLocationGeocodingCompleted && !isDestinationMyLocation) {
            Toast.makeText(MainFragment.this.mApplicationContext, mApplicationContext.getResources()
                    .getString(R.string.toast_tripplanner_need_to_place_markers_before_planning), Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        Request request = new Request();

        try {
            request.setFrom(URLEncoder.encode(startLocationString, OTPApp.URL_ENCODING));
            request.setTo(URLEncoder.encode(endLocationString, OTPApp.URL_ENCODING));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        request.setArriveBy(mArriveBy);

        Server selectedServer = mOTPApp.getSelectedServer();

        Integer defaultMaxWalkInt = mApplicationContext.getResources()
                 .getInteger(R.integer.max_walking_distance);

        try {
            Double maxWalk = Double
                    .parseDouble(mPrefs.getString(OTPApp.PREFERENCE_KEY_MAX_WALKING_DISTANCE,
                            defaultMaxWalkInt.toString()));
            request.setMaxWalkDistance(maxWalk);
        } catch (NumberFormatException ex) {
            request.setMaxWalkDistance((double) defaultMaxWalkInt);
        }

        request.setWheelchair(mPrefs.getBoolean(OTPApp.PREFERENCE_KEY_WHEEL_ACCESSIBLE,
                false));

        Date requestTripDate;
        if (mTripDate == null) {
            requestTripDate = Calendar.getInstance().getTime();
        } else {
            requestTripDate = mTripDate;
        }

        request.setDateTime(
                DateFormat.format(OTPApp.FORMAT_OTP_SERVER_DATE_QUERY,
                        requestTripDate.getTime()).toString(),
                DateFormat
                        .format(OTPApp.FORMAT_OTP_SERVER_TIME_QUERY, requestTripDate.getTime())
                        .toString());

        request.setShowIntermediateStops(Boolean.TRUE);

        request.setNumItineraries(5);

        List<Place>  intermediatePlaces        = new ArrayList<>(customTrip.getIntermediatePlaces());
        List<String> intermediatePlacesEncoded = new ArrayList<>();

        try {

            for (Place place : intermediatePlaces) {
                intermediatePlacesEncoded.add(URLEncoder.encode(place.getLat() + "," + place.getLng(), OTPApp.URL_ENCODING));
            }

            request.setIntermediatePlaces(intermediatePlacesEncoded);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        WeakReference<Activity> weakContext = new WeakReference<Activity>(
                MainFragment.this.getActivity());

        new TripRequest(weakContext, MainFragment.this.mApplicationContext, getResources(), mOTPApp
                .getSelectedServer(), MainFragment.this, customTrip)
                .execute(request);

        InputMethodManager imm = (InputMethodManager) MainFragment.this.getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mTbEndLocation.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(mTbStartLocation.getWindowToken(), 0);

        mTripDate = null;
    }



    /**
     * Retrieves a map if the map fragment parameter is null.
     * <p>
     * If there is an error tries to solve it checking if it was because of
     * "Google Play Services" sending the corresponding intent.
     *
     * @param mMap map fragment to check if the map is already initialized
     * @return initialized map fragment
     */
    private GoogleMap retrieveMap(GoogleMap mMap) {
        // Do a null check to confirm that we have not already instantiated the map.
        mMapFailed = false;

        if (mMap == null) {
            mMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap == null) {
                int status = GooglePlayServicesUtil
                        .isGooglePlayServicesAvailable(mApplicationContext);

                if (status != ConnectionResult.SUCCESS) {
                    enableUIElements(false);
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, getActivity(),
                            OTPApp.CHECK_GOOGLE_PLAY_REQUEST_CODE);
                    dialog.show();
                    mMapFailed = true;
                }
            }

        }

        return mMap;
    }


    /**
     * Removes focus from the text box chosen by the parameter and deletes map click listener if
     * none of the text boxes remain focused.
     *
     * @param isStartTextbox to select text box to removes focus from
     */
    private void removeFocus(boolean isStartTextbox) {
        if (!mMapFailed){
            if (isStartTextbox) {
                mTbStartLocation.clearFocus();
                if (!mTbEndLocation.hasFocus()){
                    mMap.setOnMapClickListener(null);
                }
            } else {
                mTbEndLocation.clearFocus();
                if (!mTbStartLocation.hasFocus()){
                    mMap.setOnMapClickListener(null);
                }
            }
        }
    }


    /**
     * Triggers ServerSelector task to retrieve servers list.
     * <p>
     * Server list will be downloaded or retrieved from the database.
     * <p>
     * A valid location should be passed to perform server autodetect if the
     * preference is set. If location is null a toast will be displayed
     * informing of the error.
     * <p>
     * It it's not possible or not requested to autodetect the server list will
     * be displayed.
     *
     * @param mCurrentLatLng location to use if servers should be detected
     */
    public void runAutoDetectServer(LatLng mCurrentLatLng, boolean showDialog) {
        if ((mCurrentLatLng == null) || (mMap == null)) {
            Toast.makeText(mApplicationContext,
                    mApplicationContext.getResources().getString(R.string.toast_tripplanner_current_location_error),
                    Toast.LENGTH_LONG).show();
        } else {
            ServersDataSource dataSource = ServersDataSource.getInstance(mApplicationContext);
            WeakReference<Activity> weakContext = new WeakReference<Activity>(getActivity());

            ServerSelector serverSelector = new ServerSelector(weakContext, mApplicationContext,
                    dataSource, this, mNeedToUpdateServersList, showDialog);
            serverSelector.execute(mCurrentLatLng);
            mSavedLastLocationCheckedForServer = mCurrentLatLng;
        }
        setNeedToRunAutoDetect(false);
        setNeedToUpdateServersList(false);
    }

    /**
     * Triggers ServerSelector task to retrieve servers list.
     * <p>
     * Server list will be downloaded or retrieved from the database.
     * <p>
     * A servers list will be displayed or a toast informing of the error.
     * <p>
     */
    public void runAutoDetectServerNoLocation(boolean showDialog) {
        ServersDataSource dataSource = ServersDataSource.getInstance(mApplicationContext);
        WeakReference<Activity> weakContext = new WeakReference<Activity>(getActivity());

        ServerSelector serverSelector = new ServerSelector(weakContext, mApplicationContext,
                dataSource, this, mNeedToUpdateServersList, showDialog);
        LatLng latLngList[] = new LatLng[1];
        latLngList[0] = null;
        serverSelector.execute(latLngList);
        setNeedToRunAutoDetect(false);
        setNeedToUpdateServersList(false);
    }

    /**
     * Registers the server in the OTPApp class.
     * <p>
     * UI may be restored to avoid presence of all server data, removing all
     * objects from the map and restarting text boxes to default contents.
     * <p>
     * OTPApp can be requested calling to getActivity by other fragments.
     *
     * @param s new server to be set
     * @param restartUI if true UI will be restarted to adapt to new server
     */
    private void setSelectedServer(Server s, boolean restartUI) {
        if (restartUI) {
            restartMap();
            restartTextBoxes();
        }

        mOTPApp.setSelectedServer(s);
    }

    /**
     * Removes all map objects and the global variables that reference them in
     * this fragment.
     */
    private void restartMap() {
        if (mStartMarker != null) {
            mStartMarker.remove();
        }
        if (mEndMarker != null) {
            mEndMarker.remove();
        }
        if (mModeMarkers != null) {
            for (Map.Entry<Marker, TripInfo> entry : mModeMarkers.entrySet()) {
                entry.getKey().remove();
            }
        }
        if (mBikeRentalStations != null) {
            for (Map.Entry<Marker, BikeRentalStationInfo> entry : mBikeRentalStations.entrySet()) {
                entry.getKey().remove();
            }
        }
        if (mRoute != null) {
            for (Polyline p : mRoute) {
                p.remove();
            }
        }
        if (mBoundariesPolyline != null) {
            mBoundariesPolyline.remove();
        }

        mStartMarker = null;
        mStartMarkerPosition = null;
        mEndMarker = null;
        mEndMarkerPosition = null;
        mRoute = null;
        mModeMarkers = null;
        mBikeRentalStations = null;
        mBoundariesPolyline = null;

        toggleItinerarySelectionSpinner(false);
    }

    /**
     * Sets text boxes to initial default locations.
     * <p>
     * MyLocation for start text box and empty for end text box.
     * <p>
     * Accordingly preference with key PREFERENCE_KEY_ORIGIN_IS_MY_LOCATION
     * is set.
     */
    private void restartTextBoxes() {
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        setTextBoxLocation(mApplicationContext.getResources().getString(R.string.text_box_my_location),
                true);
        prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_ORIGIN_IS_MY_LOCATION, true);
        prefsEditor.commit();
        mIsStartLocationGeocodingCompleted = true;
        mIsEndLocationGeocodingCompleted = false;

        mTbEndLocation.requestFocus();
        
        setTextBoxLocation("", false);
    }

    /**
     * Writes coordinates of latlng to the selected text box.
     *
     * @param latlng    object containing the coordinates to set
     * @param isStartTb when true start text box is set otherwise end text box
     */
    private void setLocationTb(LatLng latlng, boolean isStartTb) {
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat(OTPApp.FORMAT_COORDINATES,
                decimalFormatSymbols);
        if (isStartTb) {
            setTextBoxLocation(decimalFormat.format(latlng.latitude) + ", " + decimalFormat
                    .format(latlng.longitude), true);
        } else {
            setTextBoxLocation(decimalFormat.format(latlng.latitude) + ", " + decimalFormat
                    .format(latlng.longitude), false);
        }
    }

    /**
     * Moves or adds (if didn't existed) a start/end marker to latlng position
     * and updates its text box.
     * <p>
     * If preference with key PREFERENCE_KEY_USE_INTELLIGENT_MARKERS is set
     * geocoding will be triggered for text boxes, except if the parameter geocoding is set to
     * false.
     * <p>
     * If the marker does not fit in selected server bounds marker won't be set
     * and a warning will be shown.
     *
     * @param isStartMarker when true start marker will be set
     * @param latlng        the location to move on
     * @param showMessage   whether show or not informative message on success
     * @param geocode       when false, even with the preference set, geocoding won't be triggered.
     */
        private void setMarker(boolean isStartMarker, LatLng latlng, boolean showMessage,
                           boolean geocode) {
        SharedPreferences.Editor prefsEditor = mPrefs.edit();

        if (((mOTPApp.getSelectedServer() != null) && LocationUtil
                .checkPointInBoundingBox(latlng, mOTPApp.getSelectedServer()))
                || (mOTPApp.getSelectedServer() == null)) {
            if (showMessage) {
                String toastText;
                if (isStartMarker) {
                    toastText = mApplicationContext.getResources()
                            .getString(R.string.toast_map_markers_start_marker_activated);
                } else {
                    toastText = mApplicationContext.getResources()
                            .getString(R.string.toast_map_markers_end_marker_activated);
                }
                Toast.makeText(mApplicationContext, toastText, Toast.LENGTH_SHORT).show();
            }

            removeFocus(isStartMarker);

            if (isStartMarker) {
                if (mStartMarker == null) {
                    mStartMarker = addStartEndMarker(latlng, true);
                } else {
                    setMarkerPosition(true, latlng);
                    mStartMarkerPosition = latlng;
                }
                            MainFragment.this.setLocationTb(latlng, true);
                            prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_ORIGIN_IS_MY_LOCATION, false);
                prefsEditor.commit();
                if (mPrefs.getBoolean(OTPApp.PREFERENCE_KEY_USE_INTELLIGENT_MARKERS, true) && geocode) {
                    mIsStartLocationGeocodingCompleted = false;
                    updateMarkerPosition(latlng, true);
                } else {
                    mIsStartLocationGeocodingCompleted = true;
                    Log.d(tripTag, "Sono dentro a setMarker_Start");
                    processRequestTrip();
                }
            } else {
                if (mEndMarker == null) {
                    mEndMarker = addStartEndMarker(latlng, false);
                } else {
                    setMarkerPosition(false, latlng);
                    mEndMarkerPosition = latlng;
                }
                MainFragment.this.setLocationTb(latlng, false);
                prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_DESTINATION_IS_MY_LOCATION, false);
                prefsEditor.commit();
                if (mPrefs.getBoolean(OTPApp.PREFERENCE_KEY_USE_INTELLIGENT_MARKERS, true) && geocode) {
                    mIsEndLocationGeocodingCompleted = false;
                    updateMarkerPosition(latlng, false);
                } else {
                    mIsEndLocationGeocodingCompleted = true;
                    Log.d(tripTag, "setMarker_End");
                    processRequestTrip();
                }
            }
        } else {
            if (showMessage) {
                Toast.makeText(mApplicationContext, mApplicationContext.getResources()
                        .getString(R.string.toast_map_markers_marker_out_of_boundaries), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Updates marker or creates a new one if doesn't exit to the passed latlng
     * <p>
     * Accordingly updates the field used for save/restore purposes.
     *
     * @param isStartMarker if true start marker will be changed, end marker
     *                      otherwise
     * @param latLng        contains the coordinates of the position to be changed to
     */
    private void setMarkerPosition(boolean isStartMarker, LatLng latLng) {
        if (isStartMarker) {
            if (mStartMarker == null) {
                mStartMarker = addStartEndMarker(latLng, true);
            } else {
                mStartMarker.setPosition(latLng);
            }
            mStartMarkerPosition = latLng;
        } else {
            if (mEndMarker == null) {
                mEndMarker = addStartEndMarker(latLng, false);
            } else {
                mEndMarker.setPosition(latLng);
            }
            mEndMarkerPosition = latLng;
        }
    }

    /**
     * Creates and adds to the map a new start/end marker.
     * <p>
     * Accordingly updates the field used for save/restore purposes.
     *
     * @param latLng        the position to initialize the new marker
     * @param isStartMarker if true a start marker will be created
     * @return the new marker created
     */
    private Marker addStartEndMarker(LatLng latLng, boolean isStartMarker) {
        if (!mMapFailed){
            MarkerOptions markerOptions = new MarkerOptions().position(latLng)
                    .draggable(true);
            if (isStartMarker) {
                markerOptions
                        .title(mApplicationContext.getResources()
                                .getString(R.string.map_markers_start_marker_title))
                        .snippet(mApplicationContext.getResources()
                                .getString(R.string.map_markers_start_marker_description))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                mStartMarkerPosition = latLng;
                return mMap.addMarker(markerOptions);
            } else {
                markerOptions
                        .title(mApplicationContext.getResources().getString(R.string.map_markers_end_marker_title))
                        .snippet(mApplicationContext.getResources()
                                .getString(R.string.map_markers_end_marker_description))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                mEndMarkerPosition = latLng;
                return mMap.addMarker(markerOptions);
            }
        }
        return null;
    }


    private String getLocationTbText(boolean isTbStartLocation) {
        if (isTbStartLocation) {
            Editable tbStarLocationEditable = mTbStartLocation.getText();
            if (tbStarLocationEditable != null) {
                return tbStarLocationEditable.toString();
            } else {
                Log.e(OTPApp.TAG, "Not possible to obtain origin from input box");
            }
        } else {
            Editable tbEndLocationEditable = mTbEndLocation.getText();
            if (tbEndLocationEditable != null) {
                return tbEndLocationEditable.toString();
            } else {
                Log.e(OTPApp.TAG, "Not possible to obtain destination from input box");
            }
        }
        return null;
    }

    /**
     * Updates the text box contents to the given location and triggers
     * geocoding for that location to update the text box.
     * <p>
     * This is a wrapper for setLocationTb, processAddress and accordingly change
     * the field to control if the text box was changed by the user.
     */
    private void updateMarkerPosition(LatLng newLatLng, boolean isStartMarker) {
        setLocationTb(newLatLng, isStartMarker);
        String locationText = getLocationTbText(isStartMarker);
        if (isStartMarker) {
            mIsStartLocationChangedByUser = false;
        } else {
            mIsEndLocationChangedByUser = false;
        }
        processAddress(isStartMarker, locationText, newLatLng.latitude, newLatLng.longitude, true);
    }

    @Override
    public void onStart() {
        super.onStart();

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (mMapFailed) {
            mMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                enableUIElements(true);

                initializeMapInterface(mMap);

                runAutoDetectServerNoLocation(true);
            }
        }

        connectLocationClient();
    }

    /**
     * Connects the LocationClient.
     * <p>
     * To avoid errors only tries if is not pending for another connection
     * request or is disconnected.
     */
    public void connectLocationClient() {
        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Disconnects the LocationClient.
     * <p>
     * To avoid errors only tries if it's connected.
     */
    public void disconnectLocationClient() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        bundle.putBoolean(OTPApp.BUNDLE_KEY_MAP_FAILED, mMapFailed);

        if (!mMapFailed) {
            bundle.putParcelable(OTPApp.BUNDLE_KEY_MAP_CAMERA, mMap.getCameraPosition());
            bundle.putParcelable(OTPApp.BUNDLE_KEY_MAP_START_MARKER_POSITION, mStartMarkerPosition);
            bundle.putParcelable(OTPApp.BUNDLE_KEY_MAP_END_MARKER_POSITION, mEndMarkerPosition);
            bundle.putBoolean(OTPApp.BUNDLE_KEY_APP_STARTS, mAppStarts);
            bundle.putBoolean(OTPApp.BUNDLE_KEY_IS_START_LOCATION_GEOCODING_PROCESSED,
                    mIsStartLocationGeocodingCompleted);
            bundle.putBoolean(OTPApp.BUNDLE_KEY_IS_END_LOCATION_GEOCODING_PROCESSED,
                    mIsEndLocationGeocodingCompleted);
            bundle.putBoolean(OTPApp.BUNDLE_KEY_IS_START_LOCATION_CHANGED_BY_USER,
                    mIsStartLocationChangedByUser);
            bundle.putBoolean(OTPApp.BUNDLE_KEY_IS_END_LOCATION_CHANGED_BY_USER,
                    mIsEndLocationChangedByUser);
            Editable tbStarLocationEditable = mTbStartLocation.getText();
            if (tbStarLocationEditable != null) {
                bundle.putString(OTPApp.BUNDLE_KEY_TB_START_LOCATION,
                        tbStarLocationEditable.toString());
            } else {
                Log.e(OTPApp.TAG, "Not possible to obtain origin while saving app bundle");
            }
            Editable tbEndLocationEditable = mTbEndLocation.getText();
            if (tbEndLocationEditable != null) {
                bundle.putString(OTPApp.BUNDLE_KEY_TB_END_LOCATION,
                        tbEndLocationEditable.toString());
            } else {
                Log.e(OTPApp.TAG, "Not possible to obtain destination while saving app bundle");
            }
            bundle.putString(OTPApp.BUNDLE_KEY_TB_END_LOCATION,
                    mTbEndLocation.getText().toString());

            bundle.putParcelable(OTPApp.BUNDLE_KEY_SAVED_LAST_LOCATION, mSavedLastLocation);
            bundle.putParcelable(OTPApp.BUNDLE_KEY_SAVED_LAST_LOCATION_CHECKED_FOR_SERVER,
                    mSavedLastLocationCheckedForServer);

            if (mResultTripStartLocation != null) {
                bundle.putString(OTPApp.BUNDLE_KEY_RESULT_TRIP_START_LOCATION,
                        mResultTripStartLocation);
            }
            if (mResultTripEndLocation != null) {
                bundle.putString(OTPApp.BUNDLE_KEY_RESULT_TRIP_END_LOCATION,
                        mResultTripEndLocation);
            }

            bundle.putSerializable(OTPApp.BUNDLE_KEY_TRIP_DATE, mTripDate);
            bundle.putBoolean(OTPApp.BUNDLE_KEY_ARRIVE_BY, mArriveBy);

            bundle.putBoolean(OTPApp.BUNDLE_KEY__IS_ALARM_BIKE_RENTAL_ACTIVE, mIsAlarmBikeRentalUpdateActive);

            bundle.putSerializable(OTPApp.BUNDLE_KEY_PREVIOUS_OPTIMIZATION, previousOptimization);

            if (!mFragmentListener.getCurrentItineraryList().isEmpty()) {
                OTPBundle otpBundle = new OTPBundle();
                otpBundle.setFromText(mResultTripStartLocation);
                otpBundle.setToText(mResultTripEndLocation);
                otpBundle.setItineraryList(mFragmentListener.getCurrentItineraryList());
                otpBundle.setCurrentItineraryIndex(mFragmentListener.getCurrentItineraryIndex());
                otpBundle.setCurrentItinerary(mFragmentListener.getCurrentItinerary());
                bundle.putSerializable(OTPApp.BUNDLE_KEY_OTP_BUNDLE, otpBundle);

            }
            bundle.putSerializable(OTPApp.BUNDLE_KEY_CUSTOM_SERVER_METADATA, mCustomServerMetadata);

            bundle.putSerializable(OTPApp.BUNDLE_KEY_CUSTOM_TRIP, customTrip);
        }

    }

    /**
     * Triggers geocoding for chosen text box with passed text.
     * <p>
     * If address contents are the String used to identify user's location
     * ("MyLocation" for example) user location is passed to know the
     * corresponding address.
     * In this case user's location shouldn't be null, if it is a toast is
     * shown.
     */
    public void processAddress(final boolean isStartTextBox, String address,
                               boolean geocodingForMarker) {
        processAddress(isStartTextBox, address, null, null, geocodingForMarker);
    }

    /**
     * Triggers geocoding for chosen text box with passed text, offering the possibility of pass
     * original latitude and longitude requested to check reverse geocoding results when
     * geocoding for marker.
     * <p>
     * If address contents are the String used to identify user's location
     * ("MyLocation" for example) user location is passed to know the
     * corresponding address.
     * In this case user's location shouldn't be null, if it is a toast is
     * shown.
     */
    public void processAddress(final boolean isStartTextBox, String address, Double originalLat,
                               Double originalLon, boolean geocodingForMarker) {
        Log.d("TRQ_TriggerGeocoding", "Geocoder");
        WeakReference<Activity> weakContext = new WeakReference<Activity>(getActivity());

        mGeoCodingTask = new OTPGeocoding(weakContext, mApplicationContext,
                isStartTextBox, geocodingForMarker, mOTPApp.getSelectedServer(), this);
        LatLng mCurrentLatLng = getLastLocation();

        if (address.equalsIgnoreCase(this.getResources().getString(R.string.text_box_my_location))) {
            if (mCurrentLatLng != null) {
                if (isStartTextBox){
                    mIsStartLocationGeocodingCompleted = false;
                    mGeoCodingTask.execute(address, String.valueOf(mCurrentLatLng.latitude),
                            String.valueOf(mCurrentLatLng.longitude));
                }
                else{
                    mIsEndLocationGeocodingCompleted = false;
                    mGeoCodingTask.execute(address, String.valueOf(mCurrentLatLng.latitude),
                            String.valueOf(mCurrentLatLng.longitude));
                }
            } else {
                Toast.makeText(mApplicationContext,
                        mApplicationContext.getResources()
                                .getString(R.string.toast_tripplanner_current_location_error),
                        Toast.LENGTH_LONG).show();
            }
        } else {
            String latString, lonString;
            if (originalLat != null && originalLon != null){
                latString = originalLat.toString();
                lonString = originalLon.toString();
            }
            else {
                latString = null;
                lonString = null;
            }
            if (isStartTextBox){
                mIsStartLocationGeocodingCompleted = false;
                mGeoCodingTask.execute(address, latString, lonString);
            }
            else{
                mIsEndLocationGeocodingCompleted = false;
                mGeoCodingTask.execute(address, latString, lonString);
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        Log.d("TRQ", "MainFragment onResume");
        listenForBikeUpdates(mIsAlarmBikeRentalUpdateActive);


    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("TRQ", "MainFragmentPAUSE");
        listenForBikeUpdates(false);
    }

    @Override
    public void onStop() {
        disconnectLocationClient();
        Log.d("TRQ", "MainFragmentSTOP");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        // Release all map-related objects to make sure GPS is shut down when
        // the user leaves the app

        Log.d(OTPApp.TAG, "Released all map objects in MainFragment.onDestroy()");

        super.onDestroy();
    }


    /**
     * Updates server to the new one set in preferences and also makes some UI changes (camera
     * movements) if specified.
     *
     * @param updateUI also updateUI, not useful if changes should occur on slider_green_clip
     */
    public void updateSelectedServer(boolean updateUI) {
        long serverId;
        Server server;
        if (!mMapFailed){
            if (mPrefs.getBoolean(OTPApp.PREFERENCE_KEY_SELECTED_CUSTOM_SERVER, false)) {
                server = new Server(mPrefs.getString(OTPApp.PREFERENCE_KEY_CUSTOM_SERVER_URL, ""),
                        mApplicationContext);
                setSelectedServer(server, updateUI);
                String bounds;
                if ((bounds = mPrefs.getString(OTPApp.PREFERENCE_KEY_CUSTOM_SERVER_BOUNDS, null))
                        != null) {
                    server.setBounds(bounds);
                    addBoundariesRectangle(server);
                }
                WeakReference<Activity> weakContext = new WeakReference<Activity>(getActivity());

                if (mCustomServerMetadata == null){
                    MetadataRequest metaRequest = new MetadataRequest(weakContext, mApplicationContext,
                            this);
                    metaRequest.execute(mPrefs.getString(OTPApp.PREFERENCE_KEY_CUSTOM_SERVER_URL, ""));
                }
                else{
                    onMetadataRequestComplete(mCustomServerMetadata, false);
                }

                Log.d(OTPApp.TAG, "Now using custom OTP server: " + mPrefs
                        .getString(OTPApp.PREFERENCE_KEY_CUSTOM_SERVER_URL, ""));
            } else if ((serverId = mPrefs.getLong(OTPApp.PREFERENCE_KEY_SELECTED_SERVER, 0)) != 0){
                mCustomServerMetadata = null;
                ServersDataSource dataSource = ServersDataSource.getInstance(mApplicationContext);
                dataSource.open();
                server = new Server(dataSource
                        .getServer(mPrefs.getLong(OTPApp.PREFERENCE_KEY_SELECTED_SERVER, 0)));
                dataSource.close();

                setSelectedServer(server, updateUI);
                addBoundariesRectangle(server);

                if (updateUI){
                    LatLng mCurrentLatLng = getLastLocation();

                    if ((mCurrentLatLng != null) && (LocationUtil
                            .checkPointInBoundingBox(mCurrentLatLng, server))) {
                        mMap.animateCamera(CameraUpdateFactory
                                .newLatLngZoom(mCurrentLatLng, getServerInitialZoom(server)));
                    } else {
                        mMap.animateCamera(CameraUpdateFactory
                                .newLatLngZoom(getServerCenter(server), getServerInitialZoom(server)));
                        Log.d(tripTag, "setMarker_Start if");
                        setMarker(true, getServerCenter(server), false, true);
                    }
                }

                Log.d(OTPApp.TAG, "Now using OTP server: " + server.getRegion());
            } else {
                Log.d(OTPApp.TAG, "Server not selected yet, should be first start or app update");
                return;
            }
            if (server != null){
                if (startLocationPlacesAutoCompleteAdapter != null
                        && endLocationPlacesAutoCompleteAdapter != null){
                    startLocationPlacesAutoCompleteAdapter.setSelectedServer(server);
                    endLocationPlacesAutoCompleteAdapter.setSelectedServer(server);
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu pMenu, MenuInflater inflater) {
        super.onCreateOptionsMenu(pMenu, inflater);
        inflater.inflate(R.menu.menu, pMenu);
        mGPS = pMenu.getItem(0);
    }

    @Override
    public void onPrepareOptionsMenu(final Menu pMenu) {
        if (isGPSEnabled()) {
            mGPS.setTitle(R.string.menu_button_disable_gps);
        } else {
            mGPS.setTitle(R.string.menu_button_enable_gps);
        }
        super.onPrepareOptionsMenu(pMenu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem pItem) {
        OTPApp app = ((OTPApp) getActivity().getApplication());
        switch (pItem.getItemId()) {
            case R.id.gps_settings:
                Intent myIntent = new Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
                break;
            case R.id.settings:
                getActivity().startActivityForResult(
                        new Intent(getActivity(), SettingsActivity.class),
                        OTPApp.SETTINGS_REQUEST_CODE);
                break;
            case R.id.feedback:
                Server selectedServer = app.getSelectedServer();

                String[] recipients = {selectedServer.getContactEmail(),
                        getString(R.string.feedback_email_android_developer)};

                String uriText = "mailto:";
                for (String recipient : recipients) {
                    uriText += recipient + ";";
                }

                String subject = "";
                subject += getResources().getString(R.string.menu_button_feedback_subject);
                Date d = Calendar.getInstance().getTime();
                subject += "[" + d.toString() + "]";
                uriText += "?subject=" + subject;

                String content = ((MyActivity) getActivity()).getCurrentRequestString();

                try {
                    uriText += "&body=" + URLEncoder.encode(content, OTPApp.URL_ENCODING);
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                    return false;
                }

                Uri uri = Uri.parse(uriText);

                Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
                sendIntent.setData(uri);
                startActivity(Intent.createChooser(sendIntent,
                        getResources().getString(R.string.menu_button_feedback_send_email)));

                break;
            case R.id.server_info:
                Server server = app.getSelectedServer();

                if (server == null) {
                    Log.w(OTPApp.TAG,
                            "Tried to get server info when no server was selected");
                    Toast.makeText(mApplicationContext, mApplicationContext.getResources()
                            .getString(R.string.toast_no_server_selected_error), Toast.LENGTH_SHORT)
                            .show();
                    break;
                }

                WeakReference<Activity> weakContext = new WeakReference<Activity>(getActivity());

                ServerChecker serverChecker = new ServerChecker(weakContext, mApplicationContext,
                        true);
                serverChecker.execute(server);

                break;
            default:
                break;
        }

        return false;
    }

    private Boolean isGPSEnabled() {
        return sLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Wrapper to other functions: moves the marker to the location included
     * in the address, updates text box and zooms to that position.
     *
     * @param isStartMarker if true start marker will be changed
     * @param address       will location and text information
     */
    public void moveMarker(Boolean isStartMarker, CustomAddress address) {
        if (isStartMarker) {
            mStartAddress = address;
        } else {
            mEndAddress = address;
        }
        LatLng latlng = new LatLng(address.getLatitude(), address.getLongitude());
        setMarkerPosition(isStartMarker, latlng);
        setTextBoxLocation(address.toString(), isStartMarker);
        zoomToGeocodingResult(isStartMarker, address);
    }

    /**
     * Wrapper to other functions: moves the marker to the location included
     * in the address, updates text box and zooms to that position.
     * <p>
     * This only happens if the new location is closer than a constant to
     * marker previous location. Otherwise address is only used as reference
     * and text box is updated to "Marker close to [address]".
     *
     * @param isStartMarker if true start marker will be changed
     * @param address       will location and text information
     */
    public void moveMarkerRelative(Boolean isStartMarker, CustomAddress address) {
        float results[] = new float[1];
        double addressLat = address.getLatitude();
        double addressLon = address.getLongitude();

        Marker marker;
        if (isStartMarker) {
            marker = mStartMarker;
            mStartAddress = address;
        } else {
            marker = mEndMarker;
            mEndAddress = address;
        }

        Location.distanceBetween(marker.getPosition().latitude, marker.getPosition().longitude,
                addressLat, addressLon, results);

        if (results[0] < OTPApp.GEOCODING_MAX_ERROR) {
            LatLng newLatlng = new LatLng(addressLat, addressLon);
            setMarkerPosition(isStartMarker, newLatlng);
            setTextBoxLocation(address.toString(), isStartMarker);
        } else {
            setTextBoxLocation(getResources().getString(R.string.text_box_close_to_marker) + " "
                    + address.toString(), isStartMarker);
        }

    }

    /**
     * Zooms to address or to address and the location of the other marker if it's
     * not the first marker.
     * <p>
     * If the other location is "MyLocation" will also be included in zoom.
     *
     * @param isStartLocation if true address is for start location
     * @param address         with the location to zoom at
     */
    public void zoomToGeocodingResult(boolean isStartLocation, CustomAddress address) {
        LatLng latlng = new LatLng(address.getLatitude(), address.getLongitude());
        LatLng mCurrentLatLng = getLastLocation();

        if (!mMapFailed){
            if (isStartLocation) {
                if (mIsStartLocationChangedByUser) {
                    if (mEndMarker != null) {
                        zoomToTwoPoints(latlng, mEndMarkerPosition);
                    } else if (mPrefs
                            .getBoolean(OTPApp.PREFERENCE_KEY_DESTINATION_IS_MY_LOCATION, false)) {
                        if (mCurrentLatLng == null) {
                            Toast.makeText(mApplicationContext, mApplicationContext.getResources()
                                    .getString(R.string.toast_tripplanner_current_location_error), Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            zoomToTwoPoints(latlng, mCurrentLatLng);
                        }
                    } else {
                        zoomToLocation(latlng);
                    }
                }
            } else {
                if (mIsEndLocationChangedByUser) {
                    if (mStartMarker != null) {
                        zoomToTwoPoints(mStartMarkerPosition, latlng);
                    } else if (mPrefs.getBoolean(OTPApp.PREFERENCE_KEY_ORIGIN_IS_MY_LOCATION, false)) {
                        if (mCurrentLatLng == null) {
                            Toast.makeText(mApplicationContext, mApplicationContext.getResources()
                                    .getString(R.string.toast_tripplanner_current_location_error), Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            zoomToTwoPoints(mCurrentLatLng, latlng);
                        }
                    } else {
                        zoomToLocation(latlng);
                    }
                }
            }
        }
    }

    public void zoomToLocation(LatLng latlng) {
        if (latlng != null) {
            mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(latlng, OTPApp.defaultMediumZoomLevel));
        }
    }

    public void zoomToTwoPoints(LatLng pointA, LatLng pointB) {
        if ((pointA.latitude != pointB.latitude) && (pointA.longitude != pointB.longitude)) {
            LatLngBounds.Builder boundsCreator = LatLngBounds.builder();

            boundsCreator.include(pointA);
            boundsCreator.include(pointB);

            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsCreator.build(),
                    getResources().getInteger(R.integer.route_zoom_padding)));
        }
    }

    /**
     * Updates start/end text box contents to the given text.
     *
     * @param text           contents to insert
     * @param isStartTextBox if true start box will be used
     */
    public void setTextBoxLocation(String text, boolean isStartTextBox) {
        if (isStartTextBox) {
            mIsStartLocationChangedByUser = false;
            mTbStartLocation.setText(text);
        } else {
            mIsEndLocationChangedByUser = false;
            mTbEndLocation.setText(text);
        }
    }

    /**
     * Resets start/end text box to previous valid address.
     *
     * @param isStartTextBox if true start box will be used
     */
    private void restartTextBoxLocation(boolean isStartTextBox) {
        if (isStartTextBox) {
            if (mStartAddress != null) {
                mIsStartLocationChangedByUser = false;
                mTbStartLocation.setText(addressToString(mStartAddress));
            }
        } else {
            if (mEndAddress != null) {
                mIsEndLocationChangedByUser = false;
                mTbEndLocation.setText(addressToString(mEndAddress));
            }
        }
    }

    /**
     * Returns address in string format.
     * <p>
     * Lines used are first and second.
     *
     * @param add the address to transform
     */
    private String addressToString(CustomAddress add) {
        return ((add.getAddressLine(0) != null) ? add.getAddressLine(0) : "")
                + ", "
                + ((add.getAddressLine(1) != null) ? add.getAddressLine(1) : "");
    }

    /**
     * Draws the route on the map.
     * <p>
     * To indicate the full route a polyline will be drawn using all points in
     * itinerary.
     * <p>
     * On each method of transportation change a mode marker will be added.
     * <p>
     * Mode marker for transit step will display stop name, departure time and
     * headsign.
     * Mode marker for walk/bike connection, guidance to next point and distance and time
     * to get there.
     * <p>
     * Previous routes are removed from the map.
     *
     * @param itinerary     the information to be drawn
     * @param animateCamera type of camera animation: - 0 camera wouldn't be animated
     *                                                - 1 animated to fit the route
     *                                                - 2 animated to fit first transit marker if
     *                                                  any, otherwise to route.
     */
    public void showRouteOnMap(List<Leg> itinerary, int animateCamera) {
        Log.d(OTPApp.TAG,
                "(TripRequest) legs size = "
                        + Integer.toString(itinerary.size()));
        if (mRoute != null) {
            for (Polyline legLine : mRoute) {
                legLine.remove();
            }
            mRoute.clear();
        }
        if (mModeMarkers != null) {
            for (Map.Entry<Marker, TripInfo> entry : mModeMarkers.entrySet()) {
                entry.getKey().remove();
            }
        }

        if (mLinks != null) {
            for (Map.Entry<Marker, String> entry : mLinks.entrySet()) {
                entry.getKey().remove();
            }
        }

        mRoute = new ArrayList<Polyline>();
        mModeMarkers = new HashMap<Marker, TripInfo>();
        mLinks = new HashMap<Marker, String>();
        Marker firstTransitMarker = null;

        if (!itinerary.isEmpty() && !mMapFailed) {
            LatLngBounds.Builder boundsCreator = LatLngBounds.builder();

            int stepIndex = 0;

            for (Leg leg : itinerary) {
                stepIndex++;

                List<LatLng> points = LocationUtil.decodePoly(leg.legGeometry
                        .getPoints());

                if (!points.isEmpty()) {
                    MarkerOptions modeMarkerOption = generateModeMarkerOptions(leg, points.get(0),
                            stepIndex);

                    float scaleFactor = getResources().getFraction(R.fraction.scaleFactor, 1, 1);


                    Marker modeMarker = mMap.addMarker(modeMarkerOption);
                    boolean realtime = false;
                    if (TraverseMode.valueOf(leg.mode).isTransit()) {
                        realtime = leg.realTime;
                    }
                    TripInfo tripInfo = new TripInfo(realtime, leg.tripId,
                            generateModeMarkerSnippet(leg), leg.departureDelay);
                    mModeMarkers.put(modeMarker, tripInfo);

                    if (TraverseMode.valueOf(leg.mode).isTransit()) {
                        //because on transit two step-by-step indications are generated (get on / get off)
                        stepIndex++;

                        if (firstTransitMarker == null) {
                            firstTransitMarker = modeMarker;
                        }
                    }
                    PolylineOptions options = new PolylineOptions().addAll(points)
                            .width(5 * scaleFactor)
                            .color(OTPApp.COLOR_ROUTE_LINE);
                    Polyline routeLine = mMap.addPolyline(options);
                    mRoute.add(routeLine);
                    for (LatLng point : points) {
                        boundsCreator.include(point);
                    }
                }
            }
            mCustomInfoWindowAdapter.setMarkers(mModeMarkers);
            mCustomInfoWindowAdapter.setLinks(mLinks);
            mMap.setInfoWindowAdapter(mCustomInfoWindowAdapter);
            if (animateCamera == 1){
                if (firstTransitMarker != null){
                    firstTransitMarker.showInfoWindow();
                }
            }
            if (animateCamera > 0) {
                LatLngBounds routeBounds = boundsCreator.build();
                if (((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map))
                        .getMap()
                        != null){
                    showRouteOnMapAnimateCamera(routeBounds, firstTransitMarker, animateCamera);
                }
            }
        }
    }

    private MarkerOptions generateModeMarkerOptions(Leg leg, LatLng location, int stepIndex){
        MarkerOptions modeMarkerOption = new MarkerOptions().position(location);
        Drawable drawable = getResources().getDrawable(getPathIcon(leg.mode));
        if (drawable != null) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable.getCurrent();
            Bitmap bitmap = bitmapDrawable.getBitmap();
            final float ratio = 1.5f;
            int width = (int) (bitmap.getWidth() * 1.5);
            int height = (int) (bitmap.getHeight() * 1.5);
            Bitmap marker = Bitmap.createScaledBitmap(bitmap, width, height, false);
            modeMarkerOption.icon(
                    BitmapDescriptorFactory.fromBitmap(marker));
        } else {
            Log.e(OTPApp.TAG, "Error obtaining drawable to add mode icons to the map");
        }

        modeMarkerOption.title(generateModeMarkerTitle(leg, stepIndex));

        return modeMarkerOption;
    }

    private String generateModeMarkerTitle(Leg leg, int stepIndex){
        TraverseMode traverseMode = TraverseMode.valueOf(leg.mode);
        String title = "";

        if (traverseMode.isTransit()) {
            title = stepIndex + ". " + ConversionUtils
                    .getRouteShortNameSafe(leg.routeShortName, leg.routeLongName,
                            mApplicationContext)
                    + " " + getResources().getString(R.string.map_markers_connector_before_stop) + " "
                    + DirectionsGenerator.getLocalizedStreetName(leg.from.name,
                    mApplicationContext.getResources());
        }
        else{
            if (traverseMode.equals(TraverseMode.WALK)) {
                title = stepIndex + ". " + getResources()
                        .getString(R.string.map_markers_mode_walk_action)
                        + " " + getResources().getString(R.string.map_markers_connector_before_destination)
                        + " " + DirectionsGenerator.getLocalizedStreetName(leg.to.name,
                        mApplicationContext.getResources());
            } else if (traverseMode.equals(TraverseMode.BICYCLE)) {
                title = stepIndex + ". " + getResources()
                        .getString(R.string.map_markers_mode_bicycle_action)
                        + " " + getResources().getString(R.string.map_markers_connector_before_destination)
                        + " " + DirectionsGenerator.getLocalizedStreetName(leg.to.name,
                        mApplicationContext.getResources());
            }
        }
        return title;
    }

    private CharSequence generateModeMarkerSnippet(Leg leg) {
        CharSequence snippet;
        long legDuration;
        TraverseMode traverseMode = TraverseMode.valueOf(leg.mode);
        legDuration = ConversionUtils.normalizeDuration(leg.duration, mPrefs);
        if (traverseMode.isTransit()) {
            CharSequence spannableSnippet = ConversionUtils
                    .getTimeWithContext(mApplicationContext, leg.agencyTimeZoneOffset,
                            Long.parseLong(leg.startTime), false);
            if (leg.realTime){
                int color = ConversionUtils.getDelayColor(leg.departureDelay, mApplicationContext);
                spannableSnippet = ConversionUtils
                        .getTimeWithContext(mApplicationContext, leg.agencyTimeZoneOffset,
                                Long.parseLong(leg.startTime), false, color);
            }
            if (leg.headsign != null) {
                snippet = TextUtils.concat(spannableSnippet, " ",
                        getResources().getString(R.string.step_by_step_non_transit_to),
                        " ", leg.headsign);
            }
            else{
                snippet = spannableSnippet;
            }
        } else {
            snippet = ConversionUtils
                    .getFormattedDurationTextNoSeconds(legDuration, false,
                            mApplicationContext) + " " + "-" + " "
                    + ConversionUtils
                    .getFormattedDistance(leg.distance, mApplicationContext);
        }
        return  snippet;
    }

    /**
     * Moves the camera to correctly display the route on map. Several options are possible to move
     * the camera according to animateCamera parameter.
     * <p>
     * If the route contains any transit leg, Info Window of start point of the first one will be
     * opened and centered in the screen, to display stop address from where route starts.
     * To fit the whole route in the screen and let the first transit stop in the center, next steps
     * are performed (the objective is to create new bounds with this characteristic):
     * - Measure screen horizontal and vertical distance to northeast and southwest points of route
     * bounds. Obtain the highest.
     * - Add this distance in the other 3 dimensions to the first transit marker screen position in
     * order to obtain new points to have all the route fitted and the transit marker in the middle
     * (approximately due to projection restrictions).
     * - Calculate the coordinates of these new points and add them to the route points to calculate
     * the new bounds.
     * - Pass the new bounds to the camera movement function and correct result will be obtained.
     *
     * @param routeBounds original route bounds
     * @param firstTransitMarker position of first transit stop that will be centered
     * @param animateCamera type of camera animation: - 0 camera wouldn't be animated
     *                                                - 1 animated to fit the route
     *                                                - 2 animated to fit first transit marker if
     *                                                  any, otherwise to route.
     *                                                - 3 moved with no animation to fit first
     *                                                transit marker if any, otherwise to route.
     */
    private void showRouteOnMapAnimateCamera(LatLngBounds routeBounds, Marker firstTransitMarker, int animateCamera){

        int windowWidth = getResources().getDisplayMetrics().widthPixels;
        int windowHeight = getResources().getDisplayMetrics().heightPixels;
        int limitNortheastRight = windowWidth - mMapPaddingRight;
        int limitNortheastTop = mMapPaddingTop;
        int limitSouthwestLeft = mMapPaddingLeft;
        int limitSouthwestBottom = windowHeight - mMapPaddingBottom;
        Point northeastInScreen = mMap.getProjection().toScreenLocation(routeBounds.northeast);
        Point southwestInScreen = mMap.getProjection().toScreenLocation(routeBounds.southwest);

        if ((firstTransitMarker != null) && (animateCamera == 1)){
            Point firstTransitMarkerInScreen = mMap.getProjection().toScreenLocation(firstTransitMarker.getPosition());
            int maxDistanceToRouteEdge = 0;
            int distanceHorizontalNortheast = northeastInScreen.x - firstTransitMarkerInScreen.x;
            int distanceVerticalNortheast = firstTransitMarkerInScreen.y - northeastInScreen.y;
            int distanceHorizontalSouthwest = firstTransitMarkerInScreen.x - southwestInScreen.x;
            int distanceVerticalSouthwest = southwestInScreen.y - firstTransitMarkerInScreen.y;
            if (distanceHorizontalNortheast > maxDistanceToRouteEdge){
                maxDistanceToRouteEdge = distanceHorizontalNortheast;
            }
            if (distanceVerticalNortheast > maxDistanceToRouteEdge){
                maxDistanceToRouteEdge = distanceVerticalNortheast;
            }
            if (distanceHorizontalSouthwest > maxDistanceToRouteEdge){
                maxDistanceToRouteEdge = distanceHorizontalSouthwest;
            }
            if (distanceVerticalSouthwest > maxDistanceToRouteEdge) {
                maxDistanceToRouteEdge = distanceVerticalSouthwest;
            }
            maxDistanceToRouteEdge = Math.abs(maxDistanceToRouteEdge);

            Point newLimitSouthWest = new Point(firstTransitMarkerInScreen.x - maxDistanceToRouteEdge, firstTransitMarkerInScreen.y + maxDistanceToRouteEdge);
            Point newLimitNorthEast = new Point(firstTransitMarkerInScreen.x + maxDistanceToRouteEdge, firstTransitMarkerInScreen.y - maxDistanceToRouteEdge);
            LatLng newLimitSouthWestLatLng = mMap.getProjection().fromScreenLocation(newLimitSouthWest);
            LatLng newLimitNorthEastLatLng = mMap.getProjection().fromScreenLocation(newLimitNorthEast);

            if (newLimitSouthWestLatLng != null && newLimitNorthEastLatLng != null){
                if (newLimitNorthEastLatLng.latitude < newLimitSouthWestLatLng.latitude){
                    newLimitNorthEastLatLng = new LatLng(newLimitSouthWestLatLng.latitude,
                            newLimitNorthEastLatLng.longitude);
                    newLimitSouthWestLatLng = new LatLng(newLimitNorthEastLatLng.latitude,
                            newLimitSouthWestLatLng.longitude);
                }
                if (newLimitNorthEastLatLng.longitude < newLimitSouthWestLatLng.longitude){
                    newLimitNorthEastLatLng = new LatLng(newLimitNorthEastLatLng.latitude,
                            newLimitSouthWestLatLng.longitude);
                    newLimitSouthWestLatLng = new LatLng(newLimitSouthWestLatLng.latitude,
                            newLimitNorthEastLatLng.longitude);
                }

                routeBounds = new LatLngBounds(newLimitSouthWestLatLng, newLimitNorthEastLatLng);
            }
        }

        int routeDefaultPadding = getResources().getInteger(R.integer.route_zoom_padding);
        routeDefaultPadding = Math.round(routeDefaultPadding * getResources().getDisplayMetrics().density);
        int padding = routeDefaultPadding;
        int maxHorizontalPadding = (limitNortheastRight - limitSouthwestLeft) / 2;
        int maxVerticalPadding = (limitSouthwestBottom - limitNortheastTop) / 2;
        if (padding > maxHorizontalPadding){
            padding = maxHorizontalPadding;
        }
        if (padding > maxVerticalPadding){
            padding = maxVerticalPadding;
        }

        if (animateCamera == 3){
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(routeBounds, padding));
        }
        else{
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(routeBounds, padding));
        }
    }

    private int getPathIcon(String modeString) {
        TraverseMode mode = TraverseMode.valueOf(modeString);
        int icon;

        if (mode.compareTo(TraverseMode.BICYCLE) == 0) {
            icon = R.drawable.cycling;
        } else if (mode.compareTo(TraverseMode.CAR) == 0) {
            icon = R.drawable.car;
        } else if ((mode.compareTo(TraverseMode.BUS) == 0) || (mode.compareTo(TraverseMode.BUSISH)
                == 0)) {
            icon = R.drawable.bus;
        } else if ((mode.compareTo(TraverseMode.RAIL) == 0) || (
                mode.compareTo(TraverseMode.TRAINISH) == 0)) {
            icon = R.drawable.train;
        } else if (mode.compareTo(TraverseMode.FERRY) == 0) {
            icon = R.drawable.ferry;
        } else if (mode.compareTo(TraverseMode.GONDOLA) == 0) {
            icon = R.drawable.boat;
        } else if (mode.compareTo(TraverseMode.SUBWAY) == 0) {
            icon = R.drawable.underground;
        } else if (mode.compareTo(TraverseMode.TRAM) == 0) {
            icon = R.drawable.tramway;
        } else if (mode.compareTo(TraverseMode.WALK) == 0) {
            icon = R.drawable.pedestriancrossing;
        } else if (mode.compareTo(TraverseMode.CABLE_CAR) == 0) {
            icon = R.drawable.cablecar;
        } else if (mode.compareTo(TraverseMode.FUNICULAR) == 0) {
            icon = R.drawable.funicolar;
        } else if (mode.compareTo(TraverseMode.TRANSIT) == 0) {
            icon = R.drawable.road;
        } else if (mode.compareTo(TraverseMode.TRANSFER) == 0) {
            icon = R.drawable.caution;
        } else {
            icon = R.drawable.road;
        }

        return icon;
    }

    public OtpFragment getFragmentListener() {
        return mFragmentListener;
    }

    public void setFragmentListener(OtpFragment fragmentListener) {
        this.mFragmentListener = fragmentListener;
    }

    @Override
    public void onServerSelectorComplete(Server server) {
        //Update application server
        if (getActivity() != null) {
            updateSelectedServer(true);
        }
    }

    @Override
    public void onTripRequestComplete(List<EnrichedItinerary> enrichedItineraries, String currentRequestString) {

        if (getActivity() != null) {



            ConversionUtils.fixTimezoneOffsets(enrichedItineraries,
                    mPrefs.getBoolean(OTPApp.PREFERENCE_KEY_USE_DEVICE_TIMEZONE, false));
            fillItinerariesSpinner(enrichedItineraries);
            toggleItinerarySelectionSpinner(!enrichedItineraries.isEmpty());

            OtpFragment ofl = getFragmentListener();

            // onItinerariesLoaded must be invoked before onItinerarySelected(0)
            ofl.onItinerariesLoaded(enrichedItineraries);
            ofl.onItinerarySelected(0, 1);
            MyActivity myActivity = (MyActivity) getActivity();
            myActivity.setCurrentRequestString(currentRequestString);

            if ((mStartAddress != null) && (mPrefs
                    .getBoolean(OTPApp.PREFERENCE_KEY_USE_INTELLIGENT_MARKERS, true))) {
                mResultTripStartLocation = addressToString(mStartAddress);
            } else {
                Editable tbStarLocationEditable = mTbStartLocation.getText();
                if (tbStarLocationEditable != null) {
                    mResultTripStartLocation = tbStarLocationEditable.toString();
                } else {
                    Log.e(OTPApp.TAG,
                            "Not possible to obtain origin from input box while saving it to"
                                    + " step-by-step screen");
                }
            }
            if ((mEndAddress != null) && (mPrefs
                    .getBoolean(OTPApp.PREFERENCE_KEY_USE_INTELLIGENT_MARKERS, true))) {
                mResultTripEndLocation = addressToString(mEndAddress);
            } else {
                Editable tbEndLocationEditable = mTbEndLocation.getText();
                if (tbEndLocationEditable != null) {
                    mResultTripEndLocation = tbEndLocationEditable.toString();
                } else {
                    Log.e(OTPApp.TAG,
                            "Not possible to obtain destination from input box while saving it to"
                                    + " step-by-step screen");
                }
            }
            removeFocus(true);
            removeFocus(false);
        }
    }

    @Override
    public void onTripRequestCanceled() {

        Log.d(OTPApp.TAG, "Cancello");

        restartMap();
        restartTextBoxes();
    }

    private void fillItinerariesSpinner(List<EnrichedItinerary> itineraryList) {

        String[] itinerarySummaryList  = formatItinerarySummary(itineraryList);

        ArrayAdapter<String> itineraryAdapter = new ArrayAdapter<String>(this.getActivity(),
                android.R.layout.simple_spinner_item, itinerarySummaryList);

        itineraryAdapter.setDropDownViewResource(R.layout.custom_item);
        mItinerarySelectionSpinner.setAdapter(itineraryAdapter);
    }

    private String[] formatItinerarySummary(List<EnrichedItinerary> itineraryList) {

        String[] itinerarySummaryList = new String[itineraryList.size()];
        long tripDuration;

        for (int i = 0; i < itinerarySummaryList.length; i++) {
            boolean isTransitIsTagSet = false;
            Itinerary it = itineraryList.get(i).getItinerary();
            tripDuration = ConversionUtils.normalizeDuration(it.duration, mPrefs);
            for (Leg leg : it.legs) {
                TraverseMode traverseMode = TraverseMode.valueOf(leg.mode);
                if (traverseMode.isTransit()) {
                    itinerarySummaryList[i] = ConversionUtils
                            .getTimeWithContext(mApplicationContext, leg.agencyTimeZoneOffset,
                                    Long.parseLong(leg.startTime), false).toString();
//                    itinerarySummaryList[i] += ". " + ConversionUtils
//                            .getRouteShortNameSafe(leg.routeShortName,leg.routeLongName,
//                                    mApplicationContext);

                    itinerarySummaryList[i] += "Bus " + composeItineraryLongName(it);

                    itinerarySummaryList[i] += " - " + ConversionUtils
                            .getFormattedDurationTextNoSeconds(tripDuration, false,
                                    mApplicationContext);
                    if (leg.headsign!= null) {
                        itinerarySummaryList[i] += " - " + leg.headsign;
                    }
                    isTransitIsTagSet = true;
                    break;
                }
            }

            if (isTransitIsTagSet) {
                final int transfers = getItineraryTransfersCount(it);

                if (transfers > 0) {
                    itinerarySummaryList[i] += " - " + transfers + ((transfers > 0) ? " Cambio" : " Cambi");
                }
            }

            if (!isTransitIsTagSet) {
//                itinerarySummaryList[i] = Integer.toString(i + 1)
//                        + ".   ";//Shown index is i + 1, to use 1-based indexes for the UI instead of 0-based
                itinerarySummaryList[i] = "A piedi ";
                itinerarySummaryList[i] +=
                        ConversionUtils.getFormattedDistance(it.walkDistance, mApplicationContext)
                                + " " + "-" + " " + ConversionUtils
                                .getFormattedDurationTextNoSeconds(tripDuration, false,
                                        mApplicationContext);
            }

            EnrichedItinerary enrichedItinerary = itineraryList.get(i);

            int   historic           = enrichedItinerary.getHistoricAggregatedCount();
            int   green              = enrichedItinerary.getGreenAggregatedCount();
            int   open               = enrichedItinerary.getPanoramicAggregatedCount();
            float historicPercentage = (float) historic / (historic + green + open) * 100;
            float greenPercentage    = (float) green    / (historic + green + open) * 100;
            float openPercentage     = (float) open     / (historic + green + open) * 100;
            boolean preset = (customTrip.getMonuments()  == CustomTrip.MAX) ||
                    (customTrip.getGreenAreas() == CustomTrip.MAX) ||
                    (customTrip.getOpenSpaces() == CustomTrip.MAX);
            if (preset) {
                if (customTrip.getMonuments()  == CustomTrip.MAX)
                    itinerarySummaryList[i] += " (Monumenti: " + Math.round(historicPercentage) + "%)";
                else if (customTrip.getGreenAreas()  == CustomTrip.MAX)
                    itinerarySummaryList[i] += " (Aree verdi: " + Math.round(greenPercentage) + "%)";
                else if (customTrip.getOpenSpaces()  == CustomTrip.MAX)
                    itinerarySummaryList[i] += " (Piazze: " + Math.round(openPercentage) + "%)";
            }
            else {
                itinerarySummaryList[i] += " (" +
                        Math.round(historicPercentage * 100) / 100.0f + "%, " +
                        Math.round(greenPercentage * 100) / 100.0f + "%, " +
                        Math.round(openPercentage * 100) / 100.0f + "%)";
            }
        }

        return itinerarySummaryList;
    }

    public static int getItineraryTransfersCount(Itinerary itinerary) {

        int changes = 0;

        for (Leg leg : itinerary.legs) {

            TraverseMode traverseMode = TraverseMode.valueOf(leg.mode);

            if (traverseMode.isTransit()) {
                changes++;
            }
        }

        return changes - 1;
    }

    public String composeItineraryLongName(Itinerary itinerary) {

        String itineraryLongName = "";
        boolean first = true;

        for (Leg leg : itinerary.legs) {

            TraverseMode traverseMode = TraverseMode.valueOf(leg.mode);

            if (traverseMode.isTransit()) {

                if (!first) {
                    itineraryLongName += "-";
                }

                itineraryLongName += ConversionUtils
                        .getRouteShortNameSafe(leg.routeShortName,leg.routeLongName, mApplicationContext)
                        .split(" ")[1];

                first = false;
            }
        }

        return itineraryLongName;
    }

    @Override
    public void onOTPGeocodingComplete(final boolean isStartTextbox,
            ArrayList<CustomAddress> addressesReturn, boolean geocodingForMarker) {
        if (getActivity() != null) {

            try {
                AlertDialog.Builder geocoderAlert = new AlertDialog.Builder(
                        getActivity());
                geocoderAlert.setTitle(R.string.geocoder_results_title)
                        .setMessage(R.string.geocoder_results_no_results_message)
                        .setCancelable(false)
                        .setPositiveButton(getResources().getString(android.R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                });

                if (addressesReturn.isEmpty()) {
                    restartTextBoxLocation(isStartTextbox);
                    AlertDialog alert = geocoderAlert.create();
                    alert.show();
                    return;
                } else if (addressesReturn.size() == 1) {
                    Log.d(tripTag, "otpGeocodingComplete");
                    useNewAddress(isStartTextbox, addressesReturn.get(0), geocodingForMarker);
                    return;
                }

                AlertDialog.Builder geocoderSelector = new AlertDialog.Builder(
                        getActivity());
                geocoderSelector.setTitle(R.string.geocoder_results_title);

                final CharSequence[] addressesText = new CharSequence[addressesReturn
                        .size()];
                for (int i = 0; i < addressesReturn.size(); i++) {
                    CustomAddress address = addressesReturn.get(i);
                    addressesText[i] = address.getStringAddress(true);

                    Log.d(OTPApp.TAG, addressesText[i].toString());
                }

                final ArrayList<CustomAddress> addressesTemp = addressesReturn;
                geocoderSelector.setItems(addressesText,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                CustomAddress address = addressesTemp.get(item);
                                Log.d(OTPApp.TAG, "Chosen: " + addressesText[item]);
                                Log.d(tripTag, "Chosen");
                                useNewAddress(isStartTextbox, address, false);
                            }
                        });
                AlertDialog alertGeocoder = geocoderSelector.create();
                alertGeocoder.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        restartTextBoxLocation(isStartTextbox);
                    }
                });
                alertGeocoder.show();
            } catch (Exception e) {
                Log.e(OTPApp.TAG, "Error in Main Fragment Geocoding callback: " + e);
            }
        }
    }

    public void useNewAddress(final boolean isStartTextbox, CustomAddress newAddress,
                                    boolean geocodingForMarker) {
        removeFocus(isStartTextbox);
        if (isStartTextbox) {
            mIsStartLocationGeocodingCompleted = true;
        } else {
            mIsEndLocationGeocodingCompleted = true;
        }
        if (geocodingForMarker) {
            moveMarkerRelative(isStartTextbox, newAddress);
        } else {
            moveMarker(isStartTextbox, newAddress);
        }
        Log.d(tripTag, "useNewAddress: " + newAddress.toString());
        processRequestTrip();
        changingTextBoxWithAutocomplete = false;
    }


    @Override
    public void onMetadataRequestComplete(GraphMetadata metadata, boolean updateUI) {
        if (getActivity() != null && !mMapFailed) {
            if (metadata != null){
                mCustomServerMetadata = metadata;

                double lowerLeftLatitude = metadata.getLowerLeftLatitude();
                double lowerLeftLongitude = metadata.getLowerLeftLongitude();
                double upperRightLatitude = metadata.getUpperRightLatitude();
                double upperRightLongitude = metadata.getUpperRightLongitude();

                Server selectedServer = mOTPApp.getSelectedServer();

                String bounds = String.valueOf(lowerLeftLatitude) +
                        "," + String.valueOf(lowerLeftLongitude) +
                        "," + String.valueOf(upperRightLatitude) + "," + String
                        .valueOf(upperRightLongitude);
                selectedServer.setBounds(bounds);

                SharedPreferences.Editor prefsEditor = PreferenceManager
                        .getDefaultSharedPreferences(mApplicationContext).edit();
                prefsEditor.putString(OTPApp.PREFERENCE_KEY_CUSTOM_SERVER_BOUNDS, bounds);
                prefsEditor.commit();

                Log.d(OTPApp.TAG, "LowerLeft: " + Double.toString(lowerLeftLatitude) + "," + Double
                        .toString(lowerLeftLongitude));
                Log.d(OTPApp.TAG, "UpperRight" + Double.toString(upperRightLatitude) + "," + Double
                        .toString(upperRightLongitude));

                addBoundariesRectangle(selectedServer);

                LatLng mCurrentLatLng = getLastLocation();

                if (updateUI){
                    if ((mCurrentLatLng != null) && (LocationUtil
                            .checkPointInBoundingBox(mCurrentLatLng, selectedServer))) {
                        mMap.animateCamera(CameraUpdateFactory
                                .newLatLngZoom(mCurrentLatLng, getServerInitialZoom(selectedServer)));
                    } else {
                                mMap.animateCamera(CameraUpdateFactory
                                        .newLatLngZoom(getServerCenter(selectedServer),
                                                getServerInitialZoom(selectedServer)));
                        Log.d(tripTag, "setMarker_Startmetadfata request com");
                        setStartMarkerLocation(false, selectedServer);
//                        setMarker(true, getServerCenter(selectedServer), false, true);
                    }
                }
            }
        }
    }

    /**
     * Changes the tiles used to display the map and sets max zoom level.
     *
     * @param overlayString tiles URL for custom tiles or description for
     *                      Google ones
     */
    public void updateOverlay(String overlayString) {
        if (!mMapFailed){
            int tile_width = OTPApp.CUSTOM_MAP_TILE_SMALL_WIDTH;
            int tile_height = OTPApp.CUSTOM_MAP_TILE_SMALL_HEIGHT;

            if (overlayString == null) {
                overlayString = ConversionUtils.getOverlayString(mApplicationContext);

            }
            if (mSelectedTileOverlay != null) {
                mSelectedTileOverlay.remove();
            }
            if (overlayString.startsWith(OTPApp.MAP_TILE_GOOGLE)) {
                int mapType = GoogleMap.MAP_TYPE_NORMAL;

                if (overlayString.equals(OTPApp.MAP_TILE_GOOGLE_HYBRID)) {
                    mapType = GoogleMap.MAP_TYPE_HYBRID;
                } else if (overlayString.equals(OTPApp.MAP_TILE_GOOGLE_NORMAL)) {
                    mapType = GoogleMap.MAP_TYPE_NORMAL;
                } else if (overlayString.equals(OTPApp.MAP_TILE_GOOGLE_TERRAIN)) {
                    mapType = GoogleMap.MAP_TYPE_TERRAIN;
                } else if (overlayString.equals(OTPApp.MAP_TILE_GOOGLE_SATELLITE)) {
                    mapType = GoogleMap.MAP_TYPE_SATELLITE;
                }
                mMap.setMapType(mapType);
                mMaxZoomLevel = mMap.getMaxZoomLevel();
            } else {
                if (overlayString.equals(getResources().getString(R.string.tiles_mapnik))) {
                    mMaxZoomLevel = getResources().getInteger(R.integer.tiles_mapnik_max_zoom);
                } else if (overlayString.equals(getResources().getString(R.string.tiles_lyrk))) {
                    mMaxZoomLevel = getResources().getInteger(R.integer.tiles_lyrk_max_zoom);
                    tile_width = OTPApp.CUSTOM_MAP_TILE_BIG_WIDTH;
                    tile_height = OTPApp.CUSTOM_MAP_TILE_BIG_HEIGHT;
                } else {
                    mMaxZoomLevel = getResources().getInteger(R.integer.tiles_maquest_max_zoom);
                }

                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                CustomUrlTileProvider mTileProvider = new CustomUrlTileProvider(
                        tile_width,
                        tile_height, overlayString);
                mSelectedTileOverlay = mMap.addTileOverlay(
                        new TileOverlayOptions().tileProvider(mTileProvider)
                                .zIndex(OTPApp.CUSTOM_MAP_TILE_Z_INDEX));

                if (mMap.getCameraPosition().zoom > mMaxZoomLevel) {
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(mMaxZoomLevel));
                }
            }
        }
    }

    /**
     * Returns last location coordinates.
     * <p>
     * This is obtained from the Location Client if it's connected and returns
     * a valid Location. If not saved last location is provided.
     * <p>
     * On successful call to Location Client saved last location is updated.
     *
     * @return a LatLng object with the most updated user coordinates
     */
    public LatLng getLastLocation() {

        Address currentAddress = getCurrentAddress();
        LatLng latLng = null;

        if (currentAddress != null) {
            latLng = new LatLng(currentAddress.getLatitude(), currentAddress.getLongitude());
        }

        return latLng;

//        if (mGoogleApiClient != null) {
//            if (mGoogleApiClient.isConnected()) {
//                Location loc = FusedLocationApi.getLastLocation(mGoogleApiClient);
//
//                if (loc != null) {
//                    LatLng mCurrentLocation = new LatLng(loc.getLatitude(), loc.getLongitude());
//                    mSavedLastLocation = mCurrentLocation;
//                    return mCurrentLocation;
//                }
//            }
//            if (mSavedLastLocation != null) {
//                return mSavedLastLocation;
//            }
//        }
//        return null;
    }

    public Address getCurrentAddress() {

        Location gpsLoc = null;
        double latitude = 0.0, longitude = 0.0;
        String userCountry, userAddress;
        LatLng result;
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);


        if (ActivityCompat.checkSelfPermission(mApplicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(mApplicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(mApplicationContext, Manifest.permission.ACCESS_NETWORK_STATE)   != PackageManager.PERMISSION_GRANTED) {

            return null;
        }

        try {

            gpsLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (gpsLoc != null) {
            latitude = gpsLoc.getLatitude();
            longitude = gpsLoc.getLongitude();
        }

        ActivityCompat.requestPermissions(this.getActivity(), new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE}, 1);
        List<Address> addresses = null;
        try {

            Geocoder geocoder = new Geocoder(mApplicationContext, Locale.getDefault());

            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && addresses.size() > 0) {
                userCountry = addresses.get(0).getCountryName();
                userAddress = addresses.get(0).getAddressLine(0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (addresses.size() > 0)
            return addresses.get(0);

        return null;
    }

    /*
                             * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (!mMapFailed) {
            if (connectionResult.hasResolution()) {
                try {
                    // Start an Activity that tries to resolve the error
                    connectionResult.startResolutionForResult(
                            getActivity(),
                            OTPApp.CONNECTION_FAILURE_RESOLUTION_REQUEST_CODE);
                    /*
                     * Thrown if Google Play services canceled the original
                     * PendingIntent
                     */
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            } else {
                AlertDialog.Builder errorPlay = new AlertDialog.Builder(getActivity());
                errorPlay.setTitle(getResources().getString(R.string.play_services_error_title))
                        .setMessage(getResources().getString(R.string.play_services_error)
                                + connectionResult.getErrorCode())
                        .setNeutralButton(getResources().getString(android.R.string.ok), null)
                        .create()
                        .show();
            }
        }
    }

    /**
     * Called by Google Play Services when this app is connected
     *
     * @param connectionHint
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Location mCurrentLocation = FusedLocationApi.getLastLocation(mGoogleApiClient);
        boolean autodetectServerTriggered = false;

        if ((!mMapFailed)) {
            if (mCurrentLocation != null) {
                double savedLatitude = 0;
                double savedLongitude = 0;
                float distance[] = new float[1];
                distance[0] = 0;
                if (mSavedLastLocationCheckedForServer != null) {
                    savedLatitude = mSavedLastLocationCheckedForServer.latitude;
                    savedLongitude = mSavedLastLocationCheckedForServer.longitude;
                }

                LatLng mCurrentLatLng = new LatLng(mCurrentLocation.getLatitude(),
                        mCurrentLocation.getLongitude());

                Location.distanceBetween(savedLatitude, savedLongitude, mCurrentLatLng.latitude,
                        mCurrentLatLng.longitude, distance);

                if (!checkServersAreUpdated() || mNewAppVersion) {
                    runAutoDetectServer(mCurrentLatLng, false);
                } else {
                    if (mNeedToRunAutoDetect) {
                        autodetectServerTriggered = true;
                        runAutoDetectServer(mCurrentLatLng, true);
                    } else if (mPrefs.getBoolean(OTPApp.PREFERENCE_KEY_AUTO_DETECT_SERVER, true)) {

                        if ((mOTPApp.getSelectedServer() != null)
                                && (!LocationUtil
                                .checkPointInBoundingBox(mCurrentLatLng,
                                        mOTPApp.getSelectedServer()))
                                && (((mSavedLastLocationCheckedForServer != null) && (distance[0]
                                > OTPApp.COORDINATES_IMPORTANT_DIFFERENCE))
                                || (mSavedLastLocationCheckedForServer == null))) {
                            autodetectServerTriggered = true;
                            runAutoDetectServer(mCurrentLatLng, false);
                        } else if (mOTPApp.getSelectedServer() == null) {
                            autodetectServerTriggered = true;
                            runAutoDetectServer(mCurrentLatLng, true);
                        }
                    }
                    if (!autodetectServerTriggered){
                        if (mAppStarts) {
                            Server selectedServer = mOTPApp.getSelectedServer();
                            if ((selectedServer != null) && selectedServer.areBoundsSet()) {
                                if (LocationUtil
                                        .checkPointInBoundingBox(mCurrentLatLng, selectedServer)) {
                                    mMap.animateCamera(CameraUpdateFactory
                                            .newLatLngZoom(mCurrentLatLng,
                                                    getServerInitialZoom(selectedServer)));
                                } else {
                                    mMap.animateCamera(CameraUpdateFactory
                                            .newLatLngZoom(getServerCenter(selectedServer),
                                                    getServerInitialZoom(selectedServer)));

                                    Log.d(tripTag, "setMarker_Start onconnected");

                                    // Originariamente usato per l'origine al centro del  confine dell'OTP Server
//                                     setMarker(true, getServerCenter(selectedServer), false, true);

                                    setStartMarkerLocation( false, selectedServer);
                                }
                            } else if(selectedServer != null) {
                                mMap.animateCamera(CameraUpdateFactory
                                        .newLatLngZoom(mCurrentLatLng,
                                                getServerInitialZoom(selectedServer)));
                            }
                        }
                    }

                    mAppStarts = false;
                }
            } else if (mOTPApp.getSelectedServer() == null || mNewAppVersion
                    || mNeedToUpdateServersList) {
                runAutoDetectServerNoLocation(true);
            }
        }

    }

    private boolean checkServersAreUpdated() {
        ServersDataSource dataSource = ServersDataSource.getInstance(mApplicationContext);
        dataSource.open();
        boolean result;
        Calendar someDaysBefore = Calendar.getInstance();
        someDaysBefore.add(Calendar.DAY_OF_MONTH, -OTPApp.EXPIRATION_DAYS_FOR_SERVER_LIST);
        Long serversUpdateDate = dataSource.getMostRecentDate();
        result = !((serversUpdateDate != null) && (someDaysBefore.getTime().getTime()
                > serversUpdateDate));
        dataSource.close();

        return result;
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    /**
     * Draws rectangle in the map to represent the bounds, using selected
     * server fields for lower left and upper right coordinates.
     *
     * @param server from which coordinates will be pulled
     */
    public void addBoundariesRectangle(Server server) {
        List<LatLng> bounds = new ArrayList<LatLng>();
        bounds.add(new LatLng(server.getLowerLeftLatitude(), server.getLowerLeftLongitude()));
        bounds.add(new LatLng(server.getLowerLeftLatitude(), server.getUpperRightLongitude()));
        bounds.add(new LatLng(server.getUpperRightLatitude(), server.getUpperRightLongitude()));
        bounds.add(new LatLng(server.getUpperRightLatitude(), server.getLowerLeftLongitude()));
        bounds.add(new LatLng(server.getLowerLeftLatitude(), server.getLowerLeftLongitude()));

        PolylineOptions boundariesPolylineOptions = new PolylineOptions()
                .addAll(bounds)
                .color(Color.GRAY);
        if (mBoundariesPolyline != null){
            mBoundariesPolyline.remove();
        }
        mBoundariesPolyline = mMap.addPolyline(boundariesPolylineOptions);
    }

    public float getServerInitialZoom(Server s) {
        if (s.isZoomSet()) {
            return s.getInitialZoom();
        } else {
            return OTPApp.defaultInitialZoomLevel;
        }
    }

    public LatLng getServerCenter(Server s) {
        if (s.isCenterSet()) {
            return new LatLng(s.getCenterLatitude(), s.getCenterLongitude());
        } else {
            return new LatLng(s.getGeometricalCenterLatitude(), s.getGeometricalCenterLongitude());
        }
    }

    @Override
    public void onCameraChange(CameraPosition position) {
        if (position.zoom > mMaxZoomLevel && !mMapFailed) {
            mMap.moveCamera(CameraUpdateFactory.zoomTo(mMaxZoomLevel));
        }
    }


    @Override
    public void onDateComplete(Date tripDate, boolean arriveBy) {
        this.mTripDate = tripDate;
        this.mArriveBy = arriveBy;
        String tripTime = tripDate.toString() + arriveBy;
        Log.d(tripTag, "onDateComplete");
        processRequestTrip();
        Log.d(OTPApp.TAG, tripTime);
    }

    @Override
    public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar,
            Double minValue, Double maxValue) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putFloat(OTPApp.PREFERENCE_KEY_LAST_BIKE_TRIANGLE_MIN_VALUE, minValue.floatValue());
        editor.putFloat(OTPApp.PREFERENCE_KEY_LAST_BIKE_TRIANGLE_MAX_VALUE, maxValue.floatValue());
        editor.commit();
        String bikeParam = minValue.toString() + maxValue.toString();
        Log.d(OTPApp.TAG, bikeParam);
    }


    public void listenForBikeUpdates(boolean enable){
        if (enable){
            mApplicationContext.registerReceiver(new AlarmReceiver(), mIntentFilter);
            mAlarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    OTPApp.DEFAULT_UPDATE_INTERVAL_BIKE_RENTAL,
                    OTPApp.DEFAULT_UPDATE_INTERVAL_BIKE_RENTAL, mAlarmIntentBikeRentalUpdate);
        }
        else{
            if (!mApplicationContext.getPackageManager()
                    .queryBroadcastReceivers(mBikeRentalUpdateIntent, mBikeRentalUpdateIntent.getFlags())
                    .isEmpty()){
                mApplicationContext.unregisterReceiver(mAlarmReceiver);
                if (!mIsAlarmTripTimeUpdateActive){
                    mApplicationContext.unregisterReceiver(mAlarmReceiver);
                }
            }
            mAlarmMgr.cancel(mAlarmIntentBikeRentalUpdate);
        }
    }

    public void listenForTripTimeUpdates(boolean enable, long timeToStartUpdates){
        if (enable){
            Calendar calTimeToStartUpdates = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            calTimeToStartUpdates.setTime(new Date(timeToStartUpdates));
            calTimeToStartUpdates.add(Calendar.HOUR_OF_DAY, -1);
            long timeToStartUpdatesProcessed = calTimeToStartUpdates.getTimeInMillis();
            if (timeToStartUpdatesProcessed < (System.currentTimeMillis() - 1000)){
                timeToStartUpdatesProcessed = System.currentTimeMillis() + 1000;
            }
            mApplicationContext.registerReceiver(new AlarmReceiver(), mIntentFilter);
            mAlarmMgr.setInexactRepeating(AlarmManager.RTC, timeToStartUpdatesProcessed,
                    OTPApp.DEFAULT_UPDATE_INTERVAL_TRIP_TIME, mAlarmIntentTripTimeUpdate);
        }
        else{
            if (!mApplicationContext.getPackageManager()
                    .queryBroadcastReceivers(mTripTimeUpdateIntent, mTripTimeUpdateIntent.getFlags())
                    .isEmpty()){
                if (!mIsAlarmBikeRentalUpdateActive){
                    mApplicationContext.unregisterReceiver(mAlarmReceiver);
                }
            }
            mAlarmMgr.cancel(mAlarmIntentTripTimeUpdate);
        }
    }

    @Override
    public void onUpdateTripTimesComplete(HashMap<String, List<TripTimeShort>> timesUpdatesForTrips) {
        if (getActivity() != null){
            List<EnrichedItinerary> itineraries;
            if ((itineraries = getFragmentListener().getCurrentItineraryList()) != null){
                if ((timesUpdatesForTrips != null) && !timesUpdatesForTrips.isEmpty()){
                    long lastLegTime = 0;
                    Leg lastLeg = itineraries.get(0).getItinerary().legs.get(0);
                    for (EnrichedItinerary itinerary : itineraries){
                        for (Leg leg : itinerary.getItinerary().legs){
                            long legEndTimeLong = Long.parseLong(leg.startTime);
                            if (legEndTimeLong > lastLegTime){
                                lastLegTime = legEndTimeLong;
                                lastLeg = leg;
                            }
                            List<TripTimeShort> tripsTimesUpdates;
                            if ((tripsTimesUpdates
                                    = timesUpdatesForTrips.get(leg.agencyId + ":" + leg.tripId))
                                    != null){
                                TripTimeShort firstStopUpdate = null;
                                TripTimeShort lastStopUpdate = null;
                                for (TripTimeShort tripTimeUdapteForStop : tripsTimesUpdates){
                                    if (tripTimeUdapteForStop.stopId.equals(leg.agencyId + ":" + leg.from.stopCode)){
                                        firstStopUpdate = tripTimeUdapteForStop;
                                    }
                                    if (tripTimeUdapteForStop.stopId.equals(leg.agencyId + ":" + leg.to.stopCode)){
                                        lastStopUpdate = tripTimeUdapteForStop;
                                    }
                                }
                                if ((firstStopUpdate != null) && (lastStopUpdate != null)){
                                    int legsUpdated = updateLeg(leg, firstStopUpdate, lastStopUpdate);
                                    if (legsUpdated != 0){
                                        for (Map.Entry<Marker, TripInfo> entry : mModeMarkers.entrySet()) {
                                            if (leg.tripId.equals(entry.getValue().getTripId())){
                                                entry.getValue().setSnippet(generateModeMarkerSnippet(leg));
                                                entry.getValue().setDelayInSeconds(leg.departureDelay);
                                                if (entry.getKey().isInfoWindowShown()){
                                                    entry.getKey().showInfoWindow();
                                                }
                                            }
                                        }
                                        showNotification(leg, legsUpdated);
                                    }
                                }
                            }
                        }
                    }
                    if (isTripOver(lastLeg)){
                        listenForTripTimeUpdates(false, 0);
                    }
                } else{
                    Toast.makeText(mApplicationContext,
                            getResources()
                                .getString(R.string.toast_realtime_updates_fail),
                                Toast.LENGTH_SHORT).show();
                    listenForTripTimeUpdates(false, 0);
                }
            }
        }
    }

    private boolean isTripOver(Leg leg){
        Calendar calTimeToStopUpdates = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calTimeToStopUpdates.setTime(new Date(Long.parseLong(leg.endTime)));
        calTimeToStopUpdates.add(Calendar.MILLISECOND, leg.agencyTimeZoneOffset);

        Calendar actualTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        actualTime.setTime(new Date(System.currentTimeMillis()));
        actualTime.add(Calendar.MILLISECOND, leg.agencyTimeZoneOffset);

        return actualTime.getTimeInMillis() > calTimeToStopUpdates.getTimeInMillis();
    }

    /**
     * Updates leg fields with departure and arrival new trip times.
     *
     * @param leg the leg to update
     * @param departureTripTimesUpdate departure new trip times
     * @param arrivalTripTimesUpdate arrival new trip times
     * @return 0 if none are updated, 1 if departure is updated, 2 if arrival is updated, 3 if both
     * are updated
     */
    private int updateLeg(Leg leg, TripTimeShort departureTripTimesUpdate,
                              TripTimeShort arrivalTripTimesUpdate){
        int updatedLegs = 0;
        if (leg.departureDelay != departureTripTimesUpdate.departureDelay){
            CharSequence oldDepartureTime = ConversionUtils
                    .getTimeWithContext(mApplicationContext, leg.agencyTimeZoneOffset,
                            Long.parseLong(leg.startTime), false);
            Long scheduledStartTime = Long.parseLong(leg.startTime) - leg.departureDelay * 1000;
            leg.departureDelay = departureTripTimesUpdate.departureDelay;
            leg.startTime = ((Long)(scheduledStartTime + leg.departureDelay * 1000)).toString();
            CharSequence newDepartureTime = ConversionUtils
                    .getTimeWithContext(mApplicationContext, leg.agencyTimeZoneOffset,
                            Long.parseLong(leg.startTime), false);
            if (!oldDepartureTime.equals(newDepartureTime)){
                updatedLegs = 1;
            }
        }
        if (leg.arrivalDelay != arrivalTripTimesUpdate.arrivalDelay){
            CharSequence oldArrivalTime = ConversionUtils
                    .getTimeWithContext(mApplicationContext, leg.agencyTimeZoneOffset,
                            Long.parseLong(leg.endTime), false);
            Long scheduledEndTime = Long.parseLong(leg.endTime) - leg.arrivalDelay * 1000;
            leg.arrivalDelay = arrivalTripTimesUpdate.arrivalDelay;
            leg.endTime = ((Long)(scheduledEndTime + leg.arrivalDelay * 1000)).toString();
            CharSequence newArrivalTime = ConversionUtils
                    .getTimeWithContext(mApplicationContext, leg.agencyTimeZoneOffset,
                            Long.parseLong(leg.endTime), false);
            if (!oldArrivalTime.equals(newArrivalTime)){
                if (updatedLegs == 1){
                    updatedLegs = 3;
                }
                else{
                    updatedLegs = 2;
                }
            }
        }
        return updatedLegs;
    }

    private String generateDelayText(int delay, boolean longFormat){
        String delayText = ConversionUtils.getFormattedDurationTextNoSeconds(delay, longFormat, mApplicationContext);
        if (delay == 0){
            delayText = getResources()
                    .getString(R.string.map_markers_warning_live_upates_on_time);
        }
        else if (delay > 0) {
            delayText += " "
                    + getResources()
                    .getString(R.string.map_markers_warning_live_upates_late_arrival);
        }
        else {
            delayText = delayText.replace("-","");
            delayText += " "
                    + getResources()
                    .getString(R.string.map_markers_warning_live_upates_early_arrival);
        }
        return delayText;
    }

    private void showNotification(Leg leg, int legsUpdated){
        if (!getFragmentListener().getCurrentItinerary().contains(leg)){
            return;
        }
        String delayText;

        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        if (legsUpdated == 1){
            delayText = generateDelayText(leg.departureDelay, false) + " " +
                    getResources().getString(R.string.notification_stop_name_conector) + " "
                    + getResources().getString(R.string.notification_origin);
            inboxStyle.addLine(generateDelayText(leg.departureDelay, false) + " " +
                    getResources().getString(R.string.notification_stop_name_conector) + " "
                    + getResources().getString(R.string.notification_origin) + ", "
                    + leg.from.name);
        }
        else if (legsUpdated == 2){
            delayText = generateDelayText(leg.arrivalDelay, false) + " " +
                    getResources().getString(R.string.notification_stop_name_conector) + " "
                    + getResources().getString(R.string.notification_destination);
            inboxStyle.addLine(generateDelayText(leg.arrivalDelay, false) + " " +
                    getResources().getString(R.string.notification_stop_name_conector) + " "
                    + getResources().getString(R.string.notification_destination) +", "
                    + leg.to.name);
        }
        else if (legsUpdated == 3){
            if (leg.departureDelay == leg.arrivalDelay){
                delayText = generateDelayText(leg.departureDelay, false) + " " +
                        getResources().getString(R.string.notification_stop_name_conector) + " "
                        + getResources().getString(R.string.notification_origin)  + " " +
                        getResources().getString(R.string.notification_two_delays_connector) + " "
                        +
                        getResources().getString(R.string.notification_destination);
                inboxStyle.addLine(generateDelayText(leg.departureDelay, true));
                inboxStyle.addLine(getResources().getString(R.string.notification_stop_name_conector) + " "
                        + getResources().getString(R.string.notification_origin) + ","
                        + " " + leg.from.name);
                inboxStyle.addLine(getResources().getString(R.string.notification_stop_name_conector) + " "
                        + getResources().getString(R.string.notification_destination) + ","
                        + " " + leg.to.name);
            }
            else{
                delayText =  generateDelayText(leg.departureDelay, false) + " " +
                        getResources().getString(R.string.notification_stop_name_conector) + " "
                        + getResources().getString(R.string.notification_origin)  + " " +
                        getResources().getString(R.string.notification_two_delays_connector) + " "
                        +
                        generateDelayText(leg.arrivalDelay, false) + " " +
                        getResources().getString(R.string.notification_stop_name_conector) + " "
                        + getResources().getString(R.string.notification_destination);
                inboxStyle.addLine(generateDelayText(leg.departureDelay, false) + " " +
                        getResources().getString(R.string.notification_stop_name_conector) + " "
                        + leg.from.name);
                inboxStyle.addLine( generateDelayText(leg.arrivalDelay, false) + " " +
                        getResources().getString(R.string.notification_stop_name_conector) + " "
                        + leg.to.name);
            }
        }
        else{
            return;
        }

        Intent notificationIntentOpenApp = new Intent(OTPApp.INTENT_NOTIFICATION_ACTION_OPEN_APP);
        notificationIntentOpenApp.putExtra(OTPApp.BUNDLE_KEY_INTENT_TRIP_ID, leg.tripId);
        PendingIntent notificationOpenAppPendingIntent = PendingIntent
                .getBroadcast(mApplicationContext,
                        0,
                        notificationIntentOpenApp,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        Intent notificationIntentDismissUpdates = new Intent(OTPApp.INTENT_NOTIFICATION_ACTION_DISMISS_UPDATES);
        notificationIntentDismissUpdates.putExtra(OTPApp.BUNDLE_KEY_INTENT_TRIP_ID, leg.tripId);
        PendingIntent notificationPendingIntentDismissUpdates = PendingIntent
                .getBroadcast(mApplicationContext,
                        0,
                        notificationIntentDismissUpdates,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mApplicationContext)
                        .setSmallIcon(R.drawable.notification_opentripplanner)
                        .setContentTitle(ConversionUtils.getRouteShortNameSafe(leg.routeShortName,
                                leg.routeLongName, mApplicationContext))
                        .setContentText(delayText)
                        .setLargeIcon(BitmapFactory
                                .decodeResource(getActivity()
                                                .getResources(),
                                        DirectionsGenerator
                                                .getNotificationIcon(new TraverseModeSet(leg.mode))))
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setContentIntent(notificationOpenAppPendingIntent)
                        .addAction(R.drawable.ic_action_cancel,
                                getResources()
                                        .getString(R.string.notification_disable_updates_button),
                                notificationPendingIntentDismissUpdates);
        mBuilder.setStyle(inboxStyle);
        NotificationManager notificationManager =
                (NotificationManager) mApplicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = mBuilder.build();
        notification.defaults = Notification.DEFAULT_ALL;
        notification.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
        Integer notificationID = Integer.parseInt(leg.tripId);
        notificationManager.notify(notificationID, notification);
    }

    /**
     * Checks to see if this version of OTP Android is higher than the last executed version,
     * and perform any cleanup necessary.
     */
    private void checkAppVersion() {
        try {
            PackageInfo packageInfo = getActivity().getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0);
            int newVersionCode = packageInfo.versionCode;
            int oldVersionCode = mPrefs.getInt(OTPApp.PREFERENCE_KEY_APP_VERSION, newVersionCode);
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putInt(OTPApp.PREFERENCE_KEY_APP_VERSION, newVersionCode);
            editor.commit();
            mNewAppVersion = newVersionCode != oldVersionCode;

            /**
             * Special handling for introduction of PREFERENCE_KEY_APP_VERSION - see #309
             * Otherwise, mNewVersion will be false the first time we execute version_code 13
             * when installed as an update to version_code 12.
             */
            boolean executedVersion13 = mPrefs.getBoolean(OTPApp.PREFERENCE_KEY_EXECUTED_VERSION_CODE_13, false);

            if (mNewAppVersion || !executedVersion13) {
                Log.d(OTPApp.TAG, "Updating from app version " + oldVersionCode + " to " +
                        newVersionCode);
                // Erase selected server, so server selection is run after an app update (#309)
                editor.putLong(OTPApp.PREFERENCE_KEY_SELECTED_SERVER, 0);
                // We've executed version_code 13 or higher once, so set the preference
                editor.putBoolean(OTPApp.PREFERENCE_KEY_EXECUTED_VERSION_CODE_13, true);
                editor.commit();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void openModeMarker(String tripId){
        if (mModeMarkers != null && tripId != null){
            for (Map.Entry<Marker, TripInfo> entry : mModeMarkers.entrySet()) {
                if (tripId.equals(entry.getValue().getTripId())){
                    entry.getKey().showInfoWindow();
                    break;
                }
            }
        }
    }


    @Override
    public void onUpdateTripTimesFail() {
        listenForTripTimeUpdates(false, 0);
    }

    @Override
    public void onBikeRentalStationListLoad(BikeRentalStationList bikeRentalStationCollection) {

    }

    @Override
    public void onBikeRentalStationListUpdate(BikeRentalStationList bikeRentalStationCollection) {

    }

    @Override
    public void onBikeRentalStationListFail() {

    }

    public class AlarmReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            NotificationManager notificationManager;
            if (intent.getAction().equals(OTPApp.INTENT_UPDATE_BIKE_RENTAL_ACTION)){
                BikeRentalLoad bikeRentalLoad = new BikeRentalLoad(mApplicationContext, false, MainFragment.this);
                bikeRentalLoad.execute(mOTPApp.getSelectedServer().getBaseURL());
            }
            else if (intent.getAction().equals(OTPApp.INTENT_UPDATE_TRIP_TIME_ACTION)){
                RequestTimesForTrips requestTimesForTrips =
                        new RequestTimesForTrips(mApplicationContext, MainFragment.this);
                List<String> legsToUpdate = new ArrayList<String>();
                for (EnrichedItinerary itinerary : getFragmentListener().getCurrentItineraryList()){
                    for (Leg leg : itinerary.getItinerary().legs){
                        if (leg.realTime && (TraverseMode.valueOf(leg.mode)).isTransit()){
                            legsToUpdate.add(leg.agencyId + ":" + leg.tripId);
                        }
                    }
                }
                legsToUpdate.add(0, mOTPApp.getSelectedServer().getBaseURL());
                String[] legsToUpdateArray = legsToUpdate.toArray(new String[legsToUpdate.size()]);
                requestTimesForTrips.execute(legsToUpdateArray);
            }
            else if (intent.getAction().equals(OTPApp.INTENT_NOTIFICATION_ACTION_OPEN_APP)){
                Intent activityIntent = new Intent(mApplicationContext, MyActivity.class);
                activityIntent.setAction(OTPApp.INTENT_NOTIFICATION_RESUME_APP_WITH_TRIP_ID);
                activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activityIntent.putExtra(OTPApp.BUNDLE_KEY_INTENT_TRIP_ID, intent.getStringExtra(OTPApp.BUNDLE_KEY_INTENT_TRIP_ID));
                mApplicationContext.startActivity(activityIntent);
            }
            else if (intent.getAction().equals(OTPApp.INTENT_NOTIFICATION_ACTION_DISMISS_UPDATES)){
                notificationManager =
                        (NotificationManager) mApplicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
                if (intent.getStringExtra(OTPApp.BUNDLE_KEY_INTENT_TRIP_ID) != null){
                    notificationManager.cancelAll();
                }
                else{
                    notificationManager.cancel(Integer
                            .parseInt(intent.getStringExtra(OTPApp.BUNDLE_KEY_INTENT_TRIP_ID)));
                }
                Toast.makeText(mApplicationContext,
                        getResources().getString(R.string.notification_disable_updates_info),
                        Toast.LENGTH_SHORT).show();
                listenForTripTimeUpdates(false, 0);
            }
        }
    }

    @Override
    public void onServerCheckerComplete(String result, boolean isCustomServer, boolean isAutoDetected, boolean isWorking) {
        updateSelectedServer(true);
    }

    public GraphMetadata getmCustomServerMetadata() {
        return mCustomServerMetadata;
    }

    public void setmCustomServerMetadata(GraphMetadata mCustomServerMetadata) {
        this.mCustomServerMetadata = mCustomServerMetadata;
    }

    private void setStartMarkerLocation(boolean useServerCenter, Server selectedServer) {

        LatLng latLng;
        boolean insideServerBounds = false;

        if (!mMapFailed) {

            if (selectedServer == null)
                throw new NullPointerException();

            // Uso le coordinate del centro del server
            if (useServerCenter) {

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getServerCenter(selectedServer),
                        getServerInitialZoom(selectedServer)));

                latLng = getServerCenter(selectedServer);
                setMarker(true, latLng, false, true);
            } else {



                if (Build.VERSION.SDK_INT >= 23) {
                    String[] PERMISSIONS = {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION};
                    final int REQUEST = 112;

                    if (!hasPermissions(mApplicationContext, PERMISSIONS)) {
                        ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, REQUEST);
                    }

                    latLng = getLastLocation();

//                    if (address != null) {
//                        latLng = new LatLng(address.getLatitude(), address.getLongitude());
//                    }
//                    else {
//                        latLng = null;
//                    }

                    // Voglio usare la posizione dell'utente ma è nulla: uso Bologna Centrale
                    if (latLng != null) {
                        insideServerBounds = LocationUtil.checkPointInBoundingBox(latLng, mOTPApp.getSelectedServer());
                    }

                    if (latLng == null || !insideServerBounds) {
                        // Uso GeoCoder per trovare Bologna Centrale sulla mappa e piazzare il marker per la partenza
                        CustomAddress customAddress = LocationUtil.processGeocoding(mApplicationContext, selectedServer, false, "Bologna centrale").get(0);
                        latLng = new LatLng(customAddress.getLatitude(), customAddress.getLongitude());
                    }

                    setMarker(true, latLng, false, true);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, getServerInitialZoom(selectedServer)));
                }
            }
        }
    }

        private boolean hasPermissions(Context context, String... permissions){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
                for (String permission : permissions) {
                    if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                        return false;
                    }
                }
            }
            return true;
        }

    private void setInitialCameraLocation(boolean useServerCenter, Server selectedServer) {

        LatLng latLng = null;

        if (selectedServer != null) {
            if (!mMapFailed) {
                if (useServerCenter) {
                    latLng = getServerCenter(selectedServer);
                }
                else {
                    CustomAddress customAddress = LocationUtil.processGeocoding(mApplicationContext, selectedServer, false, "Bologna centrale").get(0);
                    latLng = new LatLng(customAddress.getLatitude(), customAddress.getLongitude());
                }

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, getServerInitialZoom(selectedServer)));
            }
        }
    }

    /**
     * Creates and adds to the map a new marker.
     * <p>
     *
     * @param latLng        the position to initialize the new marker
     * @param featureType   the type of the feature to indicate
     * @return the new marker created
     */
    private Marker addMarker(LatLng latLng, FeatureType featureType, String title, String snippet, String url) {

        if (!mMapFailed) {

            float color = BitmapDescriptorFactory.HUE_MAGENTA;

            MarkerOptions markerOptions = new MarkerOptions().position(latLng);

            switch (featureType) {

                case HISTORIC: color = BitmapDescriptorFactory.HUE_RED;
                    break;

                case GREEN: color = BitmapDescriptorFactory.HUE_GREEN;
                    break;

                case PANORAMIC: color = BitmapDescriptorFactory.HUE_BLUE;
                    break;
            }

            markerOptions
                    .title(title)
                    .icon(BitmapDescriptorFactory.defaultMarker(color))
                    .snippet(snippet)
                    .draggable(false)
                    .visible(false);

            Marker featureMarker = mMap.addMarker(markerOptions);

            mLinks.put(featureMarker, url);

            return featureMarker;
        }

        return null;
    }

    public void tripRequestCanceled() {
        restartMap();
        restartTextBoxes();
        setStartMarkerLocation( false, mOTPApp.getSelectedServer());
    }

    private double getGeometricalCenterLatitude(double lowerLeftLatitude, double upperRightLatitude) {

        return (lowerLeftLatitude + upperRightLatitude) / 2;
    }

    /**
     * @return the geometricalCenterLongitude
     */
    private double getGeometricalCenterLongitude(double lowerLeftLongitude, double upperRightLongitude) {
        return (lowerLeftLongitude + upperRightLongitude) / 2;
    }

    private LatLng getFeatureLatLng(Element feature) {

        LatLng  latLng = null;

        if (feature.type.equalsIgnoreCase("node")) {
            Node node = (Node) feature;
            double lat = node.lat;
            double lng = node.lon;
            latLng = new LatLng(lat, lng);
        } else if (feature.type.equalsIgnoreCase("way")) {
            Way way = (Way) feature;
            double lat = getGeometricalCenterLatitude(way.bounds.minlat, way.bounds.maxlat);
            double lng = getGeometricalCenterLongitude(way.bounds.minlon, way.bounds.maxlon);
            latLng = new LatLng(lat, lng);
        } else if (feature.type.equalsIgnoreCase("relation")) {
            Relation relation = (Relation) feature;
            double lat = getGeometricalCenterLatitude(relation.bounds.minlat, relation.bounds.maxlat);
            double lng = getGeometricalCenterLongitude(relation.bounds.minlon, relation.bounds.maxlon);
            latLng = new LatLng(lat, lng);
        }

        return latLng;
    }

    public void toggleFeaturesOnMap(boolean show) {
        for (Marker marker : featureMarkers) {
            marker.setVisible(show);
        }
    }

    public void setFeaturesOnMap(EnrichedItinerary currentItinerary) {
        setFeaturesOnMap(currentItinerary, true);
    }

    public boolean isShowButtonVisible() {
        return mBtnShowFeatures.isShown();
    }

    public void showButtonVisible(boolean show) {

        if (show)
            mBtnShowFeatures.setVisibility(View.VISIBLE);
        else
            mBtnShowFeatures.setVisibility(View.INVISIBLE);
    }
    public void setFeaturesOnMap(EnrichedItinerary currentItinerary, boolean pedantic) {

        Log.d(tripTag, "Start showing trip " + currentItinerary.getName() + "'s features");

        featureMarkers.clear();

        Element[] historicalFeatures  = currentItinerary.getHistoricalFeatures();
        Element[] greenFeatures       = currentItinerary.getGreenFeatures();
        Element[] panoramicalFeatures = currentItinerary.getPanoramicFeatures();

        for (int i = 0; i < historicalFeatures.length; ++i) {

            Element feature = historicalFeatures[i];
            LatLng  latLng  = null;
            String  title   = "";
            String  snippet = "";

            latLng = getFeatureLatLng(feature);

            if (latLng != null) {

                String[] baloon = getFeatureDescription(feature);

                if (pedantic || !baloon[0].equals("")) {
                    featureMarkers.add(addMarker(latLng, FeatureType.HISTORIC, baloon[0], baloon[1], baloon[2]));
                }
            }

        }

        for (int i = 0; i < greenFeatures.length; ++i) {

            Element feature = greenFeatures[i];
            LatLng  latLng  = null;
            String  title   = "";
            String  snippet = "";

            latLng = getFeatureLatLng(feature);

            if (latLng != null) {

                String[] baloon = getFeatureDescription(feature);

                if (pedantic || !baloon[0].equals("")) {
                    featureMarkers.add(addMarker(latLng, FeatureType.GREEN, baloon[0], baloon[1], baloon[2]));
                }
            }
        }

        for (int i = 0; i < panoramicalFeatures.length; ++i) {

            Element feature = panoramicalFeatures[i];
            LatLng  latLng = null;
            String  title   = "";
            String  snippet = "";

            latLng = getFeatureLatLng(feature);

            if (latLng != null) {

                String[] baloon = getFeatureDescription(feature);

                if (pedantic || !baloon[0].equals("")) {
                    featureMarkers.add(addMarker(latLng, FeatureType.PANORAMIC, baloon[0], baloon[1], baloon[2]));
                }
            }
        }
    }

    private String[] getFeatureDescription(Element feature) {

        String[] description = {"", "", ""};

        if (feature.tags != null) {

            if (feature.tags.containsKey("name")) {
                description[0] = feature.tags.get("name");
            }

            if (feature.tags.containsKey("description"))
                description[1] += feature.tags.get("description") + "\n";

            if (feature.tags.containsKey("inscription"))
                description[1] += feature.tags.get("inscription") + "\n";

            if (feature.tags.containsKey("wikipedia")) {
                description[1] += getResources().getString(R.string.map_markers_wikipedia) + " Wikipedia " + feature.tags.get("wikipedia");
                description[2] = feature.tags.get("wikipedia");
            }
        }

        return description;
    }
}