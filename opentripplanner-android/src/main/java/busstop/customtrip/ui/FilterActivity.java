package busstop.customtrip.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import busstop.customtrip.model.CustomTrip;
import busstop.customtrip.model.Place;
import edu.usf.cutr.opentripplanner.android.OTPApp;
import edu.usf.cutr.opentripplanner.android.R;

public class FilterActivity extends AppCompatActivity {

    private static final int MAX_TRIP_DURATION = 70;

    CustomTrip customTrip;
    String fromActivity;
    List<Place> intermediatePlaces = new ArrayList<>();

    CheckBox maxDurationCheckbox;
    CheckBox maxStopsCheckbox;
    ImageButton mBtnDeletePlace;
    ImageButton mBtnEmptyList;
    EditText maxDurationInput;
    EditText maxStopsInput;
    AutoCompleteTextView textViewIntermediatePlaces;
    ListView listViewPlaces;
    List<Place> mPlacesToCustomTrip = new ArrayList<>();
    ArrayAdapter<Place> iPlacesArrayAdapter;
    ArrayAdapter<Place> selectedArrayAdapter;
    int mPlaceSelected;

    @Override
    protected void onNewIntent(Intent intent) {

        fromActivity = (String) intent.getSerializableExtra("fromActivity");

        if(fromActivity == null) {
            /* solo la prima volta che si arriva in questa activity il custom trip viene inizializzato con i valori di default */
            customTrip = CustomTrip.getCustomTripDefaultValues();
        } else {
            /* per adesso qui ci si arriva solo dopo aver applicato i filter (da FilterActivity) */
            customTrip = (CustomTrip) intent.getSerializableExtra("customTrip");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        /* Get here the info about custom trip*/
        Intent intent = getIntent();
        customTrip    = (CustomTrip) intent.getSerializableExtra(OTPApp.BUNDLE_KEY_CUSTOM_TRIP);
        fromActivity  = (String) intent.getSerializableExtra("fromActivity");

        intermediatePlaces = loadIntermediatePlaces(getApplicationContext(), getResources().openRawResource(R.raw.places));

        iPlacesArrayAdapter = new ArrayAdapter<Place>(this, android.R.layout.simple_dropdown_item_1line, intermediatePlaces);
//        selectedArrayAdapter = new ArrayAdapter<Place>(this, android.R.layout.simple_dropdown_item_1line, mPlacesToCustomTrip);
        selectedArrayAdapter = new CustomPlacesAdapter(this, android.R.layout.simple_dropdown_item_1line, mPlacesToCustomTrip);

        maxDurationCheckbox = findViewById(R.id.maxDurationCheckbox);
        maxStopsCheckbox   = findViewById(R.id.maxStopsCheckbox);
        maxDurationInput   = findViewById(R.id.maxDurationInput);
        maxStopsInput      = findViewById(R.id.maxStopsInput);
        textViewIntermediatePlaces = findViewById(R.id.textViewIntermediatePlaces);
        listViewPlaces     = findViewById(R.id.listPlacesChosen);
        mBtnEmptyList      = findViewById(R.id.btnTrashPlaces);

        textViewIntermediatePlaces.setAdapter(iPlacesArrayAdapter);
        listViewPlaces.setAdapter(selectedArrayAdapter);

        /* I setting seguenti servono a mantenere le informazioni selezionate dall'utente */
        /* inizializzazione dei valori per la durata massima del viaggio */
        initMaxDurationSection(customTrip);

        /* inizializzazione dei valori per il numero minimo di cambi */
        initMaxStopsSection(customTrip);

        initIntermediatePlaces(customTrip);

        maxDurationCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
               @Override
               public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                   if(isChecked) {
                       maxDurationInput.setEnabled(true);
                       maxDurationInput.requestFocus();
                   } else {
                       maxDurationInput.setEnabled(false);
                       /* se l'utente deseleziona l'opzione viene risettato il valore di default */
                       maxDurationInput.setHint(R.string.maxDurationInputHint);
                       customTrip = CustomTrip.newCustomTrip(customTrip)
                               .withMaxDurationMinutes(MAX_TRIP_DURATION)
                               .build();
                   }
                   customTrip = CustomTrip.newCustomTrip(customTrip)
                           .withIsMaxDurationOn(isChecked)
                           .build();
               }
           }
        );

        maxDurationInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int durationNumber = TextUtils.isEmpty(maxDurationInput.getText().toString()) ? customTrip.getMaxStops() : Integer.parseInt(maxDurationInput.getText().toString());
                int maxDuration = maxDurationCheckbox.isChecked() ? durationNumber : customTrip.getMaxStops();
                customTrip = CustomTrip.newCustomTrip(customTrip)
                        .withMaxDurationMinutes(maxDuration)
                        .build();
            }
        });

        maxStopsCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
               @Override
               public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                   if(isChecked) {
                       maxStopsInput.setEnabled(true);
                       maxStopsInput.requestFocus();
                   } else {
                       maxStopsInput.setEnabled(false);
                       /* se l'utente deseleziona l'opzione viene risettato il valore di default */
                       maxStopsInput.setHint(R.string.maxStopsInputHint);
                       customTrip = CustomTrip.newCustomTrip(customTrip)
                               .withMaxStops(2)
                               .build();
                   }
                   customTrip = CustomTrip.newCustomTrip(customTrip)
                           .withIsMaxStopsOn(isChecked)
                           .build();
               }
           }
        );

        maxStopsInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int stopsNumber = TextUtils.isEmpty(maxStopsInput.getText().toString()) ? customTrip.getMaxStops() : Integer.parseInt(maxStopsInput.getText().toString());
                int maxStops = maxStopsCheckbox.isChecked() ? stopsNumber : customTrip.getMaxStops();
                customTrip = CustomTrip.newCustomTrip(customTrip)
                        .withMaxStops(maxStops)
                        .build();
            }
        });

        listViewPlaces.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    mPlaceSelected = position;
                    mBtnDeletePlace.setEnabled(true);
            }
        });

        // Aggiungo una tappa solo se non è già presente nell'elenco delle tappe
        textViewIntermediatePlaces.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Place place = (Place) textViewIntermediatePlaces.getAdapter().getItem(position);

                if (!mPlacesToCustomTrip.contains(place)) {
                    selectedArrayAdapter.add(place);
                    selectedArrayAdapter.notifyDataSetChanged();
                }

                textViewIntermediatePlaces.setText("");

                // Tolgo la tastiera
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(textViewIntermediatePlaces.getWindowToken(), 0);
            }
        });

        // Tolgo la tastiera dopo aver aggiunto una tappa
        textViewIntermediatePlaces.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    // hide virtual keyboard
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(textViewIntermediatePlaces.getWindowToken(), 0);
                    return true;
                }

                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void apply(View view) {

        customTrip = CustomTrip.newCustomTrip(customTrip)
                .withIntermediatePlaces(mPlacesToCustomTrip)
                .build();

        if(!fromActivity.equals("Slider")) {
            Intent intent = new Intent(FilterActivity.this, PresetActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra(OTPApp.BUNDLE_KEY_CUSTOM_TRIP, customTrip);
            intent.putExtra("fromActivity", "Filter");
            startActivity(intent);
        } else {
            Intent intent = new Intent(FilterActivity.this, SeekBarActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra(OTPApp.BUNDLE_KEY_CUSTOM_TRIP, customTrip);
            startActivity(intent);
        }
    }

    private void initMaxDurationSection(CustomTrip customTrip) {
        maxDurationCheckbox.setChecked(customTrip.isMaxDurationOn());
        if(customTrip.isMaxDurationOn()) {
            maxDurationInput.setEnabled(true);
            maxDurationInput.setHint(String.valueOf(customTrip.getMaxDurationMinutes()));
        } else {
            maxDurationInput.setEnabled(false);
            maxDurationInput.setHint(R.string.maxDurationInputHint);
        }
    }

    private void initMaxStopsSection(CustomTrip customTrip) {
        maxStopsCheckbox.setChecked(customTrip.isMaxStopsOn());
        if(customTrip.isMaxStopsOn()) {
            maxStopsInput.setEnabled(true);
            maxStopsInput.setHint(String.valueOf(customTrip.getMaxStops()));
        } else {
            maxStopsInput.setEnabled(false);
            maxStopsInput.setHint(R.string.maxStopsInputHint);
        }
    }

    private void initIntermediatePlaces(CustomTrip customTrip) {
        List<Place> chosen = customTrip.getIntermediatePlaces();

        if(chosen.size() > 0) {
            for (Place place : chosen) {
                selectedArrayAdapter.add(place);
                selectedArrayAdapter.notifyDataSetChanged();
            }
        }
    }

    public void deletePlace(View view) {

        if (mPlaceSelected != -1) {

            Place selected = (Place) listViewPlaces.getItemAtPosition(mPlaceSelected);

            selectedArrayAdapter.remove(selected);
            selectedArrayAdapter.notifyDataSetChanged();

            mPlaceSelected = -1;
        }
    }

    public void emptyPlaces(View view) {
        if (iPlacesArrayAdapter.getCount() > 0) {
            selectedArrayAdapter.clear();
        }
    }

    private List<Place> loadIntermediatePlaces(Context context, InputStream resource) {

        List<Place> result = new ArrayList<>();
        String name = "";
        double latitude, longitude;

        String jsonStr = readFromFile(context, resource);
        JSONObject jsonObject=null;
        try {
            jsonObject = new JSONObject((jsonStr));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONArray jArray = null;
        JSONArray jElements = null;
        JSONObject jTags = null;
        String type;
        JSONObject obj;

        try {

            jElements = jsonObject.getJSONArray("elements");

            for (int i = 0; i < jElements.length(); ++i) {

                obj = jElements.getJSONObject(i);
                type = obj.getString("type");
                jTags = obj.getJSONObject("tags");
                name = jTags.getString("name");

                if (type.equals("node")) {
                    latitude = Double.parseDouble(obj.getString("lat"));
                    longitude = Double.parseDouble(obj.getString("lon"));
                }
                else {
                    JSONObject bounds = obj.getJSONObject("bounds");
                    latitude = getGeometricalCenterLatitude(bounds.getDouble("minlat"), bounds.getDouble("maxlat"));
                    longitude = getGeometricalCenterLongitude(bounds.getDouble("minlon"), bounds.getDouble("maxlon"));
                }

                Place newPlace = new Place(name, latitude, longitude);

                if (!result.contains(newPlace)) {
                    result.add(newPlace);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("IPC", "Founded " + result.size() + " features");

        for (Place p : result)
            Log.d("IPC", "Added: " + p.getName() + "(" + p.getLat() + ", " + p.getLng() + ")");

        return result;
    }

    private double getGeometricalCenterLatitude(double lowerLeftLatitude, double upperRightLatitude) {

        return (lowerLeftLatitude + upperRightLatitude) / 2;
    }

    private double getGeometricalCenterLongitude(double lowerLeftLongitude, double upperRightLongitude) {
        return (lowerLeftLongitude + upperRightLongitude) / 2;
    }

    private String readFromFile(Context context, InputStream resource) {

        String      ret         = "";
        InputStream inputStream = null;

        try {

            inputStream = resource;

            if ( inputStream != null ) {

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader    = new BufferedReader(inputStreamReader);
                String            receiveString     = "";
                StringBuilder     stringBuilder     = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }
}
