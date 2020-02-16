package busstop.customtrip.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;

import edu.usf.cutr.opentripplanner.android.R;

public class CustomActivityTest extends AppCompatActivity {

    private CustomView viewHistoric, viewGreen, viewOpen;

    private float mHistoricRealPercentage    = 100 / 3.0f;
    private int   mHistoricGraphicPercentage = 33;
    private float mGreenRealPercentage       = 100 / 3.0f;
    private int   mGreenGraphicPercentage    = 33;
    private float mOpenRealPercentage        = 100 / 3.0f;
    private int   mOpenGraphicPercentage     = 33;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setContentView(R.layout.custom_slider_test);

        viewHistoric = findViewById(R.id.viewHistoric);
        viewGreen    = findViewById(R.id.viewGreen);
        viewOpen     = findViewById(R.id.viewOpen);


        viewHistoric.getBackground().setLevel(mHistoricGraphicPercentage * 100);
        viewGreen.getBackground().setLevel(mGreenGraphicPercentage * 100);
        viewOpen.getBackground().setLevel(mOpenGraphicPercentage * 100);

        viewHistoric.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                mHistoricRealPercentage    = getPercentage(viewHistoric, event);
                mHistoricGraphicPercentage = (int) Math.ceil(mHistoricRealPercentage);

                viewHistoric.getBackground().setLevel(10000 - mHistoricGraphicPercentage * 100);

                return true;
            }

        });

        viewGreen.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                mGreenRealPercentage    = getPercentage(viewGreen, event);
                mGreenGraphicPercentage = (int) Math.ceil(mGreenRealPercentage);

                viewGreen.getBackground().setLevel(10000 - mGreenGraphicPercentage * 100);

                return true;
            }
        });

        viewOpen.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                mOpenRealPercentage    = getPercentage(viewOpen, event);
                mOpenGraphicPercentage = (int) Math.ceil(mOpenRealPercentage);

                viewOpen.getBackground().setLevel(10000 - mOpenGraphicPercentage * 100);

                return true;
            }
        });
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