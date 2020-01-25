package busstop.customtrip.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import busstop.customtrip.model.CustomTrip;
import edu.usf.cutr.opentripplanner.android.MyActivity;
import edu.usf.cutr.opentripplanner.android.R;

public class SeekBarActivity extends AppCompatActivity {
    public SeekBar bar0, bar1, bar2;
    private SeekBar[] seekBars = new SeekBar[3];
    public TextView textProgress0, textProgress1, textProgress2;
    private static final float TOTAL_AMOUNT = 100.0f; // the maximum amount for all SeekBars

    // stores the current progress for the SeekBars
    private float[]   realProgress    = {100.0f/3, 100.0f/3, 100.0f/3};

    // Graphic progress rounded from real progress that is a float percentage
    private int[] graphicProgress;

    private int currentTouchId = -1;
    private int previousTouchId = -1;
    private boolean touchedBar0, touchedBar1, touchedBar2 = false;

    private CustomTrip customTrip;

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setContentView(R.layout.seek_bar);

        /* inizializzazione default */
        bar0 = seekBars[0] = findViewById(R.id.seekBar0);
        bar1 = seekBars[1] = findViewById(R.id.seekBar1);
        bar2 = seekBars[2] = findViewById(R.id.seekBar2);

        Intent i = getIntent();
        customTrip = (CustomTrip)i.getSerializableExtra("customTrip");
        graphicProgress = new int[]{customTrip.getMonuments(), customTrip.getGreenAreas(), customTrip.getOpenSpaces()};

        bar0.setProgress(graphicProgress[0]);
        bar1.setProgress(graphicProgress[1]);
        bar2.setProgress(graphicProgress[2]);

        textProgress0 = findViewById(R.id.textView0);
        textProgress1 = findViewById(R.id.textView1);
        textProgress2 = findViewById(R.id.textView2);

        textProgress0.setText("Monuments: " + realProgress[0] +" % (" + graphicProgress[0] + ")");
        textProgress1.setText("Green Areas: " + realProgress[1] +" % (" + graphicProgress[1] + ")");
        textProgress2.setText("Open Spaces: " + realProgress[2] +" % (" + graphicProgress[2]  + ")");

        /*---------------------------------------*/

        bar0.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    setProgressOfCurrentSeekBar(seekBar, progress);

                }

                textProgress0.setText("Monuments: " + realProgress[0] +" % (" + graphicProgress[0] + ")");
                textProgress1.setText("Green Areas: " + realProgress[1] +" % (" + graphicProgress[1] + ")");
                textProgress2.setText("Open Spaces: " + realProgress[2] +" % (" + graphicProgress[2]  + ")");

            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                if(currentTouchId != 0) {
                    previousTouchId = currentTouchId;
                    currentTouchId = 0;
                }
                touchedBar0 = true;
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                touchedBar0 = false;
            }
        });

        bar0.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return canTouch(0);
                }
            }
        );

        bar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    setProgressOfCurrentSeekBar(seekBar, progress);
                }
                textProgress0.setText("Monuments: " + realProgress[0] +" % (" + graphicProgress[0] + ")");
                textProgress1.setText("Green Areas: " + realProgress[1] +" % (" + graphicProgress[1] + ")");
                textProgress2.setText("Open Spaces: " + realProgress[2] +" % (" + graphicProgress[2]  + ")");
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                if(currentTouchId != 1) {
                    previousTouchId = currentTouchId;
                    currentTouchId = 1;
                }
                touchedBar1 = true;
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                touchedBar1 = false;
            }
        });

        bar1.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return canTouch(1);
                }
            }
        );

        bar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    setProgressOfCurrentSeekBar(seekBar, progress);
                }
                textProgress0.setText("Monuments: " + realProgress[0] +" % (" + graphicProgress[0] + ")");
                textProgress1.setText("Green Areas: " + realProgress[1] +" % (" + graphicProgress[1] + ")");
                textProgress2.setText("Open Spaces: " + realProgress[2] +" % (" + graphicProgress[2]  + ")");
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                if(currentTouchId != 2) {
                    previousTouchId = currentTouchId;
                    currentTouchId = 2;
                }
                touchedBar2 = true;
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                touchedBar2 = false;
            }
        });

        bar2.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return canTouch(2);
                }
            }
        );
    }

    private void setProgressOfCurrentSeekBar(SeekBar seekBar, int progress) {
        // find out which SeekBar triggered the event so we can retrieve its saved current
        // progress

        int notMeId1 = -1, notMeId2 = -1, slaveId = -1;
        int which = whichIsIt(seekBar.getId());

        if (previousTouchId == -1) {

            switch(which) {
                case 0: notMeId1 = 1; notMeId2 = 2;
                        break;

                case 1: notMeId1 = 0; notMeId2 = 2;
                        break;

                case 2: notMeId1 = 0; notMeId2 = 1;
                        break;
            }
        }
        else {

            if ((currentTouchId == 0 && previousTouchId == 1) || (currentTouchId == 1 && previousTouchId == 0)) {
                slaveId = 2;
            }
            else if ((currentTouchId == 0 && previousTouchId == 2) || (currentTouchId == 2 && previousTouchId == 0)) {
                slaveId = 1;
            }
            else if ((currentTouchId == 1 && previousTouchId == 2) || (currentTouchId == 2 && previousTouchId == 1)) {
                slaveId = 0;
            }
        }

        Log.d("SEEK", String.valueOf(progress));
        Log.d("SEEK", "{" + graphicProgress[0] + ", " + graphicProgress[1] + ", " + graphicProgress[2] +"}");

        float increment        = progress - realProgress[which];
        realProgress[which]    = realProgress[which] + increment;
        graphicProgress[which] = Math.round(realProgress[which]);
        float newRemaining     = remaining();

        if (previousTouchId == -1) {
            // target = (100.0f - realProgress[which] - realProgress[notMeId1] - realProgress[notMeId2]) / 2.0f;
            realProgress[notMeId1]    = realProgress[notMeId1] + newRemaining / 2;
            realProgress[notMeId2]    = realProgress[notMeId2] + newRemaining / 2;
            graphicProgress[notMeId1] = Math.round(realProgress[notMeId1]);
            graphicProgress[notMeId2] = Math.round(realProgress[notMeId2]);
            seekBars[notMeId1].setProgress(graphicProgress[notMeId1]);
            seekBars[notMeId2].setProgress(graphicProgress[notMeId2]);
        }
        else {
            if (realProgress[slaveId] + newRemaining >= 0.0f) {
                realProgress[slaveId] = realProgress[slaveId] + newRemaining;
                graphicProgress[slaveId] = Math.round(realProgress[slaveId]);
                seekBars[slaveId].setProgress(graphicProgress[slaveId]);
            }
            else {
                float excess = realProgress[slaveId] + newRemaining;
                realProgress[slaveId] = 0.0f;
                realProgress[previousTouchId] = realProgress[previousTouchId] + excess;
                graphicProgress[slaveId] = 0;
                graphicProgress[previousTouchId] = Math.round(realProgress[previousTouchId]);
                seekBars[slaveId].setProgress(graphicProgress[slaveId]);
                seekBars[previousTouchId].setProgress(graphicProgress[previousTouchId]);
            }
        }
    }

    /**
     * Returns the still available progress after the difference between the
     * maximum value(TOTAL_AMOUNT = TOTAL_AMOUNT) and the sum of the store progresses of
     * all SeekBars.
     *
     * @return the available progress.
     */
    private float remaining() {

        float remaining = TOTAL_AMOUNT;

        for (int i = 0; i < 3; i++) {
            remaining -= realProgress[i];
        }

        return remaining;
    }

    private int whichIsIt(int id) {
        switch (id) {
            case R.id.seekBar0:
                return 0; // first position in graphicProgress
            case R.id.seekBar1:
                return 1;
            case R.id.seekBar2:
                return 2;
            default:
                throw new IllegalStateException(
                        "There should be a Seekbar with this id(" + id + ")!");
        }
    }

    private boolean canTouch(int which) {
        switch (which) {
            case 0:
                return (touchedBar1 || touchedBar2);

            case 1:
                return (touchedBar0 || touchedBar2);

            case 2:
                return (touchedBar0 || touchedBar1);

            default:
                throw new IllegalStateException(
                        "There should be a Seekbar with this id(" + which + ")!");
        }
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
        customTrip = CustomTrip.newActivityGroup()
            .withMonuments(graphicProgress[0])
            .withGreenAreas(graphicProgress[1])
            .withOpenSpaces(graphicProgress[2])
            .build();

        Intent intent = new Intent(SeekBarActivity.this, MyActivity.class);
            intent.putExtra("customTrip", customTrip);
            startActivity(intent);
    }
}