package busstop.customtrip.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import busstop.customtrip.model.CustomTrip;
import edu.usf.cutr.opentripplanner.android.R;

public class FilterActivity extends AppCompatActivity {
    CustomTrip customTrip;
    String fromActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        /* Get here the info about custom trip*/
        Intent intent = getIntent();
        customTrip = (CustomTrip) intent.getSerializableExtra("customTrip");
        fromActivity = (String) intent.getSerializableExtra("fromActivity");
    }

    public void apply(View view) {
        if(!fromActivity.equals("Slider")) {
            Intent intent = new Intent(FilterActivity.this, PresetActivity.class);
            intent.putExtra("customTrip", customTrip);
            intent.putExtra("fromActivity", "Filter");
            startActivity(intent);
        } else {
            Intent intent = new Intent(FilterActivity.this, SeekBarActivity.class);
            intent.putExtra("customTrip", customTrip);
            startActivity(intent);
        }
    }
}
