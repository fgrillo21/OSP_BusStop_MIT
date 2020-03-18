package busstop.customtrip.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import busstop.customtrip.model.CustomTrip;
import edu.usf.cutr.opentripplanner.android.AboutActivity;
import edu.usf.cutr.opentripplanner.android.MyActivity;
import edu.usf.cutr.opentripplanner.android.OTPApp;
import edu.usf.cutr.opentripplanner.android.R;

public class PresetActivity extends AppCompatActivity {

    ViewPager viewPager;
    LinearLayout layout_dot;
    List<Integer> imageId = Arrays.asList(R.drawable.monuments_preset, R.drawable.greenareas_preset, R.drawable.open_preset, R.drawable.mixed_elements);
    CustomTrip customTrip;
    String fromActivity;
    Button button;
    TextView tTitle;

    private int dotscount;
    private ImageView[] dots;

    private int pageSelected = 0;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.preset_title_historic);
        }

        setContentView(R.layout.activity_preset);

        /* Get here the info about custom trip*/
        Intent intent = getIntent();
        fromActivity = (String) intent.getSerializableExtra("fromActivity");

        if(fromActivity == null) {
            /* solo la prima volta che si arriva in questa activity il custom trip viene inizializzato con i valori di default */
            customTrip = CustomTrip.getCustomTripDefaultValues();
        } else {
            /* per adesso qui ci si arriva solo dopo aver applicato i filter (da FilterActivity) */
            customTrip = (CustomTrip) intent.getSerializableExtra(OTPApp.BUNDLE_KEY_CUSTOM_TRIP);
        }

        layout_dot = findViewById(R.id.layout_dot);
        viewPager  = findViewById(R.id.viewpager);
        button     = findViewById(R.id.button_choose);

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
                final TextView myTitleText = findViewById(R.id.myDescription);
                switch (position) {
                    default:
                    case 0: {
                        myTitleText.setText(R.string.description_monuments);
                        button.setText(R.string.choose);
                        actionBar.setTitle(R.string.preset_title_historic);
                        break;
                    }
                    case 1: {
                        myTitleText.setText(R.string.description_greenareas);
                        button.setText(R.string.choose);
                        actionBar.setTitle(R.string.preset_title_green);
                        break;
                    }
                    case 2: {
                        myTitleText.setText(R.string.description_openspaces);
                        button.setText(R.string.choose);
                        actionBar.setTitle(R.string.preset_title_open);
                        break;
                    }
                    case 3: {
                        myTitleText.setText(R.string.description_choose_percentage);
                        button.setText(R.string.customize);
                        actionBar.setTitle(R.string.preset_title_custom);
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

        if (item.getItemId() == R.id.simple_menu_about) {
            Intent intent = new Intent(PresetActivity.this, AboutActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.simple_menu,menu);

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
                        .withMonuments(100.0f/3)
                        .withGreenAreas(100.0f/3)
                        .withOpenSpaces(100.0f/3)
                        .build();
            }
        }
        if(pageSelected == 3) {
            Intent intent = new Intent(PresetActivity.this, SeekBarActivity.class);
            intent.putExtra(OTPApp.BUNDLE_KEY_CUSTOM_TRIP, customTrip);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        } else {
            Intent intent = new Intent(PresetActivity.this, MyActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra(OTPApp.BUNDLE_KEY_CUSTOM_TRIP, customTrip);
            startActivity(intent);
        }
    }

    public void filter(View view) {
        Intent intent = new Intent(PresetActivity.this, FilterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(OTPApp.BUNDLE_KEY_CUSTOM_TRIP, customTrip);
        intent.putExtra("fromActivity", "Preset");
        startActivity(intent);
    }
}