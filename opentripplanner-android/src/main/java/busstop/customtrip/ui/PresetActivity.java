package busstop.customtrip.ui;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.widget.TextView;
import android.widget.LinearLayout;
import java.util.ArrayList;

import edu.usf.cutr.opentripplanner.android.R;

public class PresetActivity extends AppCompatActivity {

    ViewPager viewPager;
    ArrayList<Integer> arrayList;
    LinearLayout layout_dot;
    TextView[] dot;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preset);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        layout_dot = (LinearLayout) findViewById(R.id.layout_dot);
        arrayList = new ArrayList<>();

        arrayList.add(R.color.red);
        arrayList.add(R.color.green);
        arrayList.add(R.color.colorPrimaryDark);

        CustomPagerAdapter pagerAdapter = new CustomPagerAdapter(getApplicationContext(), arrayList);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setPageMargin(60);
        addDot(0);

        // whenever the page changes
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }
            @Override
            public void onPageSelected(int i) {
                addDot(i);
            }
            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }
    public void addDot(int page_position) {
        dot = new TextView[arrayList.size()];
        layout_dot.removeAllViews();

        for (int i = 0; i < dot.length; i++) {;
            dot[i] = new TextView(this);
            dot[i].setText(Html.fromHtml("&#9673;"));
            dot[i].setTextSize(35);
            dot[i].setTextColor(getResources().getColor(R.color.darker_gray));
            layout_dot.addView(dot[i]);
        }
        //active dot
        dot[page_position].setTextColor(getResources().getColor(R.color.red));
    }
}