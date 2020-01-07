package busstop.customtrip.ui;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;

import edu.usf.cutr.opentripplanner.android.R;

public class CustomActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    ArrayList<DataGrid> dataGrid = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setContentView(R.layout.activity_custom);

        //Prepare DataSet
        dataGrid = prepareDataSet();

        //Initialize Grid View for programming
        GridView gridview = findViewById(R.id.gridView);

        //Connect DataSet to Adapter
        ImageGridAdapter imageGridAdapter = new ImageGridAdapter(this, dataGrid);

        //Now Connect Adapter To GridView
        gridview.setAdapter(imageGridAdapter);

        //Add Listener For Grid View Item Click
        gridview.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

        //Show Name Of The Flower
        Toast.makeText(CustomActivity.this, dataGrid.get(position).getPhotoName(),
                Toast.LENGTH_SHORT).show();
    }

    private ArrayList<DataGrid> prepareDataSet() {

        ArrayList<DataGrid> dataGrid = new ArrayList<>();

        DataGrid item;

        //1st Item
        item = new DataGrid();
        item.setPhotoName("Monumenti");
        item.setPhotoPath(R.drawable.monuments);
        dataGrid.add(item);

        //2nd Item
        item = new DataGrid();
        item.setPhotoName("Aree Verdi");
        item.setPhotoPath(R.drawable.greenareas);
        dataGrid.add(item);


        //3rd Item
        item = new DataGrid();
        item.setPhotoName("Spazi aperti");
        item.setPhotoPath(R.drawable.openspaces);
        dataGrid.add(item);

        return dataGrid;
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
}
