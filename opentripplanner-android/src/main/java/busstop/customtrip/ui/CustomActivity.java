package busstop.customtrip.ui;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.ArrayList;

import edu.usf.cutr.opentripplanner.android.R;

public class CustomActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    ArrayList<DataGrid> dataGrid = new ArrayList<>();
    private SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        /* le seguenti righe commentate sono relative alla griglia con le 3 immagini
        * per ora il test viene fatto su un layout temporaneo con una solo foto */

        //setContentView(R.layout.activity_custom);

        //Prepare DataSet
        //dataGrid = prepareDataSet();

        //Initialize Grid View for programming
        //GridView gridview = findViewById(R.id.gridView);

        //Connect DataSet to Adapter
        //ImageGridAdapter imageGridAdapter = new ImageGridAdapter(this, dataGrid);

        //Now Connect Adapter To GridView
        //gridview.setAdapter(imageGridAdapter);

        //Add Listener For Grid View Item Click
        //gridview.setOnItemClickListener(this);

        /* --------------------------------------------------------------------------------*/

        setContentView(R.layout.tmp_layout);

        seekBar = findViewById(R.id.seekBar1);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int progress = 0;

            @Override

            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                FrameLayout target = (FrameLayout) findViewById(R.id.target);

                progress = progresValue;

                ViewGroup.LayoutParams lp = target.getLayoutParams();
                lp.height = progress;
                target.setLayoutParams(lp);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /* metodo seguente da non considerare, relativo alla griglia (nel layout grid_item.xml) che per ora non è usata
    * non cancellare potrebbe servire una volta implementata la logica su una foto, per riadattarla alla griglia con tre foto
    *  */

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

    }

    private ArrayList<DataGrid> prepareDataSet() {

        ArrayList<DataGrid> dataGrid = new ArrayList<>();

        DataGrid item;

        //1st Item
        item = new DataGrid();
        item.setPhotoName("Monumenti");
        item.setPhotoPath(R.drawable.monuments_bw);
        dataGrid.add(item);

        //2nd Item
        item = new DataGrid();
        item.setPhotoName("Aree Verdi");
        item.setPhotoPath(R.drawable.greenareas_bw);
        dataGrid.add(item);


        //3rd Item
        item = new DataGrid();
        item.setPhotoName("Spazi aperti");
        item.setPhotoPath(R.drawable.openspaces_bw);
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
