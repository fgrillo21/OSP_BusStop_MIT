package edu.usf.cutr.opentripplanner.android;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class RouteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
    }

    public void preset(View view) {
        Intent intent = new Intent(RouteActivity.this, PresetActivity.class);
        startActivity(intent);
    }

    public void custom(View view) {
        Intent intent = new Intent(RouteActivity.this, CustomActivity.class);
        startActivity(intent);
    }
}
