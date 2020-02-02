package busstop.customtrip.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import busstop.customtrip.model.CustomTrip;
import edu.usf.cutr.opentripplanner.android.R;

public class FilterActivity extends AppCompatActivity {
    CustomTrip customTrip;
    String fromActivity;

    CheckBox maxDurationCheckbox;
    CheckBox maxStopsCheckbox;
    EditText maxDurationInput;
    EditText maxStopsInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        /* Get here the info about custom trip*/
        Intent intent = getIntent();
        customTrip = (CustomTrip) intent.getSerializableExtra("customTrip");
        fromActivity = (String) intent.getSerializableExtra("fromActivity");

        maxDurationCheckbox = findViewById(R.id.maxDurationCheckbox);
        maxStopsCheckbox = findViewById(R.id.maxStopsCheckbox);
        maxDurationInput = findViewById(R.id.maxDurationInput);
        maxStopsInput = findViewById(R.id.maxStopsInput);

        /* I setting seguenti servono a mantenere le informazioni selezionate dall'utente */
        /* inizializzazione dei valori per la durata massima del viaggio */
        initMaxDurationSection(customTrip);

        /* inizializzazione dei valori per il numero minimo di cambi */
        initMaxStopsSection(customTrip);

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
                               .withMaxDurationMinutes(70)
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
                int maxDuration = maxDurationCheckbox.isChecked() ? Integer.parseInt(maxDurationInput.getText().toString()) : customTrip.getMaxStops();
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
                int maxStops = maxStopsCheckbox.isChecked() ? Integer.parseInt(maxStopsInput.getText().toString()) : customTrip.getMaxStops();
                customTrip = CustomTrip.newCustomTrip(customTrip)
                        .withMaxStops(maxStops)
                        .build();
            }
        });
    }

    public void apply(View view) {
        if(!fromActivity.equals("Slider")) {
            Intent intent = new Intent(FilterActivity.this, PresetActivity.class);
            intent.putExtra("customTrip", customTrip);
            intent.putExtra("fromActivity", "Filter");
            startActivity(intent);
        } else {
            Intent intent = new Intent(FilterActivity.this, SeekBarActivity.class);
            intent.putExtra("customTrip", customTrip);
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
}
