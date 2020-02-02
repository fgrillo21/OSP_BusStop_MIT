package busstop.customtrip.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import busstop.customtrip.model.CustomTrip;
import edu.usf.cutr.opentripplanner.android.MyActivity;
import edu.usf.cutr.opentripplanner.android.R;

public class PresetActivity extends AppCompatActivity {

    ViewPager viewPager;
    LinearLayout layout_dot;
    List<Integer> imageId = Arrays.asList(R.drawable.monuments, R.drawable.greenareas, R.drawable.openspaces, R.drawable.mixed_elements);
    CustomTrip customTrip = CustomTrip.getCustomTripDefaultValues();

    private int dotscount;
    private ImageView[] dots;

    private int pageSelected = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setContentView(R.layout.activity_preset);

        layout_dot = findViewById(R.id.layout_dot);
        viewPager = findViewById(R.id.viewpager);
        PresetPagerAdapter adapter = new PresetPagerAdapter(getApplicationContext(),imageId);
        viewPager.setAdapter(adapter);
        viewPager.setPageMargin(60);
        dotscount = adapter.getCount();
        dots = new ImageView[dotscount];

        for(int i = 0; i < dotscount; i++){
            dots[i] = new ImageView(this);
            dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.non_active_dot));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            layout_dot.addView(dots[i], params);

        }
        dots[0].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.active_dot));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                pageSelected = position;

                for(int i = 0; i< dotscount; i++){
                    dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.non_active_dot));
                }
                dots[position].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.active_dot));

                /* Initialization different descriptions related to different sections */
                final TextView myTitleText = findViewById(R.id.myTitle);
                switch (position) {
                    default:
                    case 0: {
                        myTitleText.setText(R.string.description_monuments);
                        break;
                    }
                    case 1: {
                        myTitleText.setText(R.string.description_greenareas);
                        break;
                    }
                    case 2: {
                        myTitleText.setText(R.string.description_openspaces);
                        break;
                    }
                    case 3: {
                        myTitleText.setText(R.string.description_choose_percentage);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

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

    public void choose(View view) {
        switch (pageSelected) {
            default:
            case 0: {
                customTrip = CustomTrip.newCustomTrip(customTrip)
                        .withMonuments(100)
                        .withGreenAreas(0)
                        .withOpenSpaces(0)
                        .build();
                break;
            }
            case 1: {
                customTrip =  CustomTrip.newCustomTrip(customTrip)
                        .withMonuments(0)
                        .withGreenAreas(100)
                        .withOpenSpaces(0)
                        .build();
                break;
            }
            case 2: {
                customTrip = CustomTrip.newCustomTrip(customTrip)
                        .withMonuments(0)
                        .withGreenAreas(0)
                        .withOpenSpaces(100)
                        .build();
                break;
            }
            case 3: {
                customTrip = CustomTrip.newCustomTrip(customTrip)
                        .withMonuments(34)
                        .withGreenAreas(33)
                        .withOpenSpaces(33)
                        .build();
            }
        }
        if(pageSelected == 3) {
            Intent intent = new Intent(PresetActivity.this, SeekBarActivity.class);
            intent.putExtra("customTrip", customTrip);
            startActivity(intent);
        } else {
            Intent intent = new Intent(PresetActivity.this, MyActivity.class);
            intent.putExtra("customTrip", customTrip);
            startActivity(intent);
        }
    }
}