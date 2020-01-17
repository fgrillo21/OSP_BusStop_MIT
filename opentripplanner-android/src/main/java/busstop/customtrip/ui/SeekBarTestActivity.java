package busstop.customtrip.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;
import android.widget.TextView;

import edu.usf.cutr.opentripplanner.android.R;

public class SeekBarTestActivity extends AppCompatActivity{

    SeekBar bar1;
    SeekBar bar2;
    SeekBar bar3;
    TextView textProgress1;
    TextView textProgress2;
    TextView textProgress3;

    int progressChangedValueBar1 = 0;
    int progressChangedValueBar2 = 0;
    int progressChangedValueBar3 = 0;

    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seek_bar_test);
        bar1=findViewById(R.id.seekBar1);
        bar2=findViewById(R.id.seekBar2);
        bar3=findViewById(R.id.seekBar3);

        textProgress1=findViewById(R.id.textView1);
        textProgress2=findViewById(R.id.textView2);
        textProgress3=findViewById(R.id.textView3);

        bar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValueBar1 = progress;
                textProgress1.setText("Monuments: " + progress+" %");
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        bar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValueBar2 = progress;
                textProgress2.setText("Green Areas: " + progress +" %");
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        bar3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValueBar3 = progress;
                textProgress3.setText("Open Spaces: " + progress + " %");

            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
