package busstop.customtrip.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import edu.usf.cutr.opentripplanner.android.R;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
    }

    public void preset(View view) {
        Intent intent = new Intent(IntroActivity.this, PresetActivity.class);
        startActivity(intent);
    }
}
