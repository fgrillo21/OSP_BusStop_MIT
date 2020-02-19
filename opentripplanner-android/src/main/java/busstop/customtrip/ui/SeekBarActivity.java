package busstop.customtrip.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import busstop.customtrip.model.CustomTrip;
import edu.usf.cutr.opentripplanner.android.MyActivity;
import edu.usf.cutr.opentripplanner.android.R;

public class SeekBarActivity extends AppCompatActivity {
    private CustomView viewHistoric, viewGreen, viewOpen;
    private TextView textProgress0, textProgress1, textProgress2;

    // stores the current progress for the SeekBars
    private float[]   realProgress    = {100.0f/3, 100.0f/3, 100.0f/3};

    // Graphic progress rounded from real progress that is a float percentage
    private int[] graphicProgress;

    private CustomTrip customTrip;

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
        customTrip = (CustomTrip) i.getSerializableExtra("customTrip");
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

        textProgress0.setText("Monumenti: " + graphicProgress[0] + " %");
        textProgress1.setText("Aree verdi: " + graphicProgress[1] + " %");
        textProgress2.setText("Spazi aperti: " + graphicProgress[2] + " %");

        /*---------------------------------------*/
        viewHistoric.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                realProgress[0] = getPercentage(viewHistoric, event);
                graphicProgress[0] = (int) Math.ceil(realProgress[0]);

                viewHistoric.getBackground().setLevel(10000 - graphicProgress[0] * 100);

                textProgress0.setText("Monumenti: " + graphicProgress[0] + " %");
                textProgress1.setText("Aree verdi: " + graphicProgress[1] + " %");
                textProgress2.setText("Spazi aperti: " + graphicProgress[2] + " %");

                return true;
            }

        });

        viewGreen.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                realProgress[1] = getPercentage(viewGreen, event);
                graphicProgress[1] = (int) Math.ceil(realProgress[1]);

                viewGreen.getBackground().setLevel(10000 - graphicProgress[1] * 100);

                textProgress0.setText("Monumenti: " + graphicProgress[0] + " %");
                textProgress1.setText("Aree verdi: " + graphicProgress[1] + " %");
                textProgress2.setText("Spazi aperti: " + graphicProgress[2] + " %");

                return true;
            }
        });

        viewOpen.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                realProgress[2] = getPercentage(viewOpen, event);
                graphicProgress[2] = (int) Math.ceil(realProgress[2]);

                viewOpen.getBackground().setLevel(10000 - graphicProgress[2] * 100);

                textProgress0.setText("Monumenti: " + graphicProgress[0] + " %");
                textProgress1.setText("Aree verdi: " + graphicProgress[1] + " %");
                textProgress2.setText("Spazi aperti: " + graphicProgress[2] + " %");

                return true;
            }
        });
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
            .withMonuments(graphicProgress[0])
            .withGreenAreas(graphicProgress[1])
            .withOpenSpaces(graphicProgress[2])
            .build();

        Intent intent = new Intent(SeekBarActivity.this, MyActivity.class);
            intent.putExtra("customTrip", customTrip);
            intent.putExtra("fromActivity", "Slider");
            startActivity(intent);
    }

    public void filter(View view) {
        Intent intent = new Intent(SeekBarActivity.this, FilterActivity.class);
        intent.putExtra("customTrip", customTrip);
        intent.putExtra("fromActivity", "Preset");
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
}