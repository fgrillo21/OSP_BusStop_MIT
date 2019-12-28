package busstop.customtrip.ui;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import edu.usf.cutr.opentripplanner.android.R;

public class PresetActivity extends AppCompatActivity {

    ViewPager viewPager;
    LinearLayout layout_dot;
    List<Integer> imageId = Arrays.asList(R.drawable.monuments, R.drawable.greenareas, R.drawable.openspaces);

    private int dotscount;
    private ImageView[] dots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preset);

        layout_dot = findViewById(R.id.layout_dot);
        viewPager = findViewById(R.id.viewpager);
        CustomPagerAdapter adapter = new CustomPagerAdapter(getApplicationContext(),imageId);
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
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}