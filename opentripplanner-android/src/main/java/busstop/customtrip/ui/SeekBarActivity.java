package busstop.customtrip.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import busstop.customtrip.model.CustomTrip;
import edu.usf.cutr.opentripplanner.android.MyActivity;
import edu.usf.cutr.opentripplanner.android.OTPApp;
import edu.usf.cutr.opentripplanner.android.R;
import edu.usf.cutr.opentripplanner.android.fragments.MainFragment;

public class SeekBarActivity extends AppCompatActivity {
    private CustomView viewHistoric, viewGreen, viewOpen;
    private TextView textProgress0, textProgress1, textProgress2;

    // stores the current progress for the SeekBars
    private float[]   realProgress    = {100.0f/3, 100.0f/3, 100.0f/3};

    // Graphic progress rounded from real progress that is a float percentage
    private int[] graphicProgress;

    private CustomTrip customTrip;
    String fromActivity;

    @Override
    protected void onNewIntent(Intent intent) {

        fromActivity = (String) intent.getSerializableExtra("fromActivity");

        if(fromActivity == null) {
            /* solo la prima volta che si arriva in questa activity il custom trip viene inizializzato con i valori di default */
            customTrip = CustomTrip.getCustomTripDefaultValues();
        } else {
            /* per adesso qui ci si arriva solo dopo aver applicato i filter (da FilterActivity) */
            customTrip = (CustomTrip) intent.getSerializableExtra(OTPApp.BUNDLE_KEY_CUSTOM_TRIP);
        }
    }

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setContentView(R.layout.seek_bar);

        Intent i = getIntent();
        customTrip = (CustomTrip) i.getSerializableExtra(OTPApp.BUNDLE_KEY_CUSTOM_TRIP);
        graphicProgress = new int[]{Math.round(customTrip.getMonuments()), Math.round(customTrip.getGreenAreas()), Math.round(customTrip.getOpenSpaces())};

        /* inizializzazione default */
        viewHistoric = findViewById(R.id.viewHistoric);
        viewGreen = findViewById(R.id.viewGreen);
        viewOpen = findViewById(R.id.viewOpen);


        viewHistoric.getBackground().setLevel(graphicProgress[0] * 100);
        viewGreen.getBackground().setLevel(graphicProgress[1] * 100);
        viewOpen.getBackground().setLevel(graphicProgress[2] * 100);

        textProgress0 = findViewById(R.id.textView0);
        textProgress1 = findViewById(R.id.textView1);
        textProgress2 = findViewById(R.id.textView2);

       /* updatePercentages(); */

        /*---------------------------------------*/
        viewHistoric.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                float percentage = getPercentage(viewHistoric, event);
                realProgress[0] = 100.0f - percentage;
                graphicProgress[0] = (int) Math.ceil(percentage);

                viewHistoric.getBackground().setLevel(10000 - (graphicProgress[0]) * 100);

                updatePercentages();

                return true;
            }

        });

        viewGreen.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                float percentage = getPercentage(viewGreen, event);
                realProgress[1] = 100.0f - percentage;
                graphicProgress[1] = (int) Math.ceil(percentage);

                viewGreen.getBackground().setLevel(10000 - (graphicProgress[1]) * 100);

                updatePercentages();

                return true;
            }
        });

        viewOpen.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                float percentage = getPercentage(viewOpen, event);
                realProgress[2] = 100.0f - percentage;
                graphicProgress[2] = (int) Math.ceil(percentage);

                viewOpen.getBackground().setLevel(10000 - (graphicProgress[2]) * 100);

                updatePercentages();

                return true;
            }
        });
    }

    private void updatePercentages() {
        textProgress0.setText("Monumenti: "    + realProgress[0] + " %");
        textProgress1.setText("Aree verdi: "   + realProgress[1] + " %");
        textProgress2.setText("Spazi aperti: " + realProgress[2] + " %");
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

    public void end_button(View view) {
        customTrip = CustomTrip.newCustomTrip(customTrip)
            .withMonuments(getFinalHistoricPercentage())
            .withGreenAreas(getFinalGreenPercentage())
            .withOpenSpaces(getFinalOpenPercentage())
            .build();

//        AlertDialog.Builder builder = new AlertDialog.Builder(
//                this);
//
//        builder.setTitle("Percentuali");
//        builder.setMessage("Monumenti: " + getFinalHistoricPercentage() + "\nGreen: " + getFinalGreenPercentage() + "\nOpen: " + getFinalOpenPercentage());
//
//        AlertDialog alert = builder.create();
//        alert.show();


        Intent intent = new Intent(SeekBarActivity.this, MyActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(OTPApp.BUNDLE_KEY_CUSTOM_TRIP, customTrip);
        intent.putExtra("fromActivity", "Slider");
        startActivity(intent);
    }

    public void filter(View view) {
        Intent intent = new Intent(SeekBarActivity.this, FilterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(OTPApp.BUNDLE_KEY_CUSTOM_TRIP, customTrip);
        intent.putExtra("fromActivity", "Slider");
        startActivity(intent);
    }

    private float getPercentage(View v, MotionEvent event) {

        float yTouch = event.getY();
        float viewHeight = v.getHeight();

        float touchPercentage = yTouch * 100 / viewHeight;

        if (touchPercentage >= 100.0f) {
            touchPercentage = 100.0f;
        }
        else if(touchPercentage <= 0.0f) {
            touchPercentage = 0.0f;
        }

        return touchPercentage;
    }

    private float getFinalHistoricPercentage() {
        float sum = getPercentagesSum();

        if (sum > 0.0f)
            return realProgress[0] * 100 / sum;

        return 0.0f;
    }

    private float getFinalGreenPercentage() {
        float sum = getPercentagesSum();

        if (sum > 0.0f)
            return realProgress[1] * 100 / sum;

        return 0.0f;
    }

    private float getFinalOpenPercentage() {
        float sum = getPercentagesSum();

        if (sum > 0.0f)
            return realProgress[2] * 100 / sum;

        return 0.0f;
    }

    private float getPercentagesSum() {
        // Sempre aggiornati tramite getPercentage
        return realProgress[0] + realProgress[1] + realProgress[2];
    }
}