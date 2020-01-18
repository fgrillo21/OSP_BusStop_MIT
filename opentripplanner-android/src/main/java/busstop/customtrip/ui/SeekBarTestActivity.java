package busstop.customtrip.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import edu.usf.cutr.opentripplanner.android.R;

public class SeekBarTestActivity extends AppCompatActivity {
    public SeekBar bar0, bar1, bar2;
    public TextView textProgress0, textProgress1, textProgress2, remaningToSelect;
    private static final int TOTAL_AMOUNT = 100; // the maximum amount for all SeekBars
    // stores the current progress for the SeekBars(initially each SeekBar has a progress of 0)
    private int[] mAllProgress = { 34, 33, 33};
    private int currentTouchId = -1;
    private int previousTouchId = -1;
    private boolean touchedBar0, touchedBar1, touchedBar2 = false;

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seek_bar_test);

        /* inizializzazione default */
        bar0 = findViewById(R.id.seekBar0);
        bar1 = findViewById(R.id.seekBar1);
        bar2 = findViewById(R.id.seekBar2);

        bar0.setProgress(mAllProgress[0]);
        bar1.setProgress(mAllProgress[1]);
        bar2.setProgress(mAllProgress[2]);

        textProgress0 = findViewById(R.id.textView0);
        textProgress1 = findViewById(R.id.textView1);
        textProgress2 = findViewById(R.id.textView2);

        textProgress0.setText("Monuments: " + mAllProgress[0] +" %");
        textProgress1.setText("Green Areas: " + mAllProgress[1] +" %");
        textProgress2.setText("Open Spaces: " + mAllProgress[2] +" %");

        remaningToSelect = findViewById(R.id.remaning);
        remaningToSelect.setText("Remaning % to select:" + remaining() + "%");

        /*---------------------------------------*/

        bar0.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                setProgressOfCurrentSeekBar(seekBar, progress);
                textProgress0.setText("Monuments: " + mAllProgress[0] + " %");
                remaningToSelect.setText("Remaning % to select:" + remaining() + "%");

            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                previousTouchId = currentTouchId;
                currentTouchId = 0;
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

                setProgressOfCurrentSeekBar(seekBar, progress);
                textProgress1.setText("Green Areas: " + mAllProgress[1] + " %");
                remaningToSelect.setText("Remaning % to select:" + remaining() + "%");
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                previousTouchId = currentTouchId;
                currentTouchId = 1;
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

                setProgressOfCurrentSeekBar(seekBar, progress);
                textProgress2.setText("Open Spaces: " + mAllProgress[2] + " %");
                remaningToSelect.setText("Remaning % to select:" + remaining() + "%");
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                previousTouchId = currentTouchId;
                currentTouchId = 2;
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
        int which = whichIsIt(seekBar.getId());
        // the stored progress for this SeekBar
        int storedProgress = mAllProgress[which];
        // we basically have two cases, the user either goes to the left or to
        // the right with the thumb. If he goes to the right we must check to
        // see how much he's allowed to go in that direction(based on the other
        // SeekBar values) and stop him if he the available progress was used. If
        // he goes to the left use that progress as going back
        // and freeing the track isn't a problem.
        if (progress > storedProgress) {
            // how much is currently available based on all SeekBar progress
            int remaining = remaining();
            // if there's no progress remaining then simply set the progress at
            // the stored progress(so the user can't move the thumb further)
            if (remaining == 0) {
                seekBar.setProgress(storedProgress);
                return;
            } else {
                // we still have some progress available so check that available
                // progress and let the user move the thumb as long as the
                // progress is at most as the sum between the stored progress
                // and the maximum still available progress
                if (storedProgress + remaining >= progress) {
                    mAllProgress[which] = progress;
                } else {
                    // the current progress is bigger then the available
                    // progress so restrict the value
                    mAllProgress[which] = storedProgress + remaining;
                }
            }
        } else {
            // (progress <= storedProgress)
            // the user goes left so simply save the new progress(space will be
            // available to other SeekBars)
            mAllProgress[which] = progress;
        }
    }

    /**
     * Returns the still available progress after the difference between the
     * maximum value(TOTAL_AMOUNT = 100) and the sum of the store progresses of
     * all SeekBars.
     *
     * @return the available progress.
     */
    private int remaining() {
        int remaining = TOTAL_AMOUNT;
        for (int i = 0; i < 3; i++) {
            remaining -= mAllProgress[i];
        }
        if (remaining >= 100) {
            remaining = 100;
        } else if (remaining <= 0) {
            remaining = 0;
        }
        return remaining;
    }

    private int whichIsIt(int id) {
        switch (id) {
            case R.id.seekBar0:
                return 0; // first position in mAllProgress
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
}