package busstop.customtrip.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;
import android.widget.TextView;

import edu.usf.cutr.opentripplanner.android.R;

public class SeekBarTestActivity extends AppCompatActivity{
    public SeekBar bar1,bar2,bar3;
    public TextView textProgress1,textProgress2,textProgress3, remaningToSelect;
    private static final int TOTAL_AMOUNT = 100; // the maximum amount for all SeekBars
    // stores the current progress for the SeekBars(initially each SeekBar has a progress of 0)
    private int[] mAllProgress = { 0, 0, 0 };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seek_bar_test);

        bar1 = findViewById(R.id.seekBar1);
        bar2 = findViewById(R.id.seekBar2);
        bar3 = findViewById(R.id.seekBar3);

        textProgress1 = findViewById(R.id.textView1);
        textProgress2 = findViewById(R.id.textView2);
        textProgress3 = findViewById(R.id.textView3);
        remaningToSelect = findViewById(R.id.remaning);

        bar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @SuppressLint("SetTextI18n")
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setProgressOfCurrentSeekBar(seekBar, progress);
                int which = whichIsIt(seekBar.getId());
                textProgress1.setText("Monuments: " + mAllProgress[which] +" %");
                remaningToSelect.setText("Remaining % to select: " + remaining());
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        bar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @SuppressLint("SetTextI18n")
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setProgressOfCurrentSeekBar(seekBar, progress);
                int which = whichIsIt(seekBar.getId());
                textProgress2.setText("Monuments: " + mAllProgress[which] +" %");
                remaningToSelect.setText("Remaining % to select: " + remaining());
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        bar3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @SuppressLint("SetTextI18n")
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setProgressOfCurrentSeekBar(seekBar, progress);
                int which = whichIsIt(seekBar.getId());
                textProgress3.setText("Monuments: " + mAllProgress[which] +" %");
                remaningToSelect.setText("Remaining % to select: " + remaining());
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
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
            case R.id.seekBar1:
                return 0; // first position in mAllProgress
            case R.id.seekBar2:
                return 1;
            case R.id.seekBar3:
                return 2;
            default:
                throw new IllegalStateException(
                        "There should be a Seekbar with this id(" + id + ")!");
        }
    }
}