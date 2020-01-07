package busstop.customtrip.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import edu.usf.cutr.opentripplanner.android.R;


public class ImageGridAdapter extends BaseAdapter {

    private ArrayList<DataGrid> dataGrid;
    private LayoutInflater mInflaterCatalogListItems;

    ImageGridAdapter(Context context, ArrayList<DataGrid> dataGrid) {
        this.dataGrid = dataGrid;
        mInflaterCatalogListItems = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    //This function will determine how many items to be displayed
    @Override
    public int getCount() {
        return dataGrid.size();
    }

    @Override
    public Object getItem(int position) {
        return dataGrid.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //This function will iterate through each object in the Data Set. This function will form each item in a Grid View
    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {

            holder = new ViewHolder();
            convertView = mInflaterCatalogListItems.inflate(R.layout.grid_item,
                    null);
            holder.imageName = convertView.findViewById(R.id.textView);
            holder.imagePath = convertView.findViewById(R.id.photoView);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        //Change the content here
        if (dataGrid.get(position) != null) {
            holder.imageName.setText(dataGrid.get(position).getPhotoName());
            holder.imagePath.setImageResource(dataGrid.get(position).getPhotoPath());
        }

        return convertView;
    }

    //View Holder class used for reusing the same inflated view. It will decrease the inflation overhead @getView
    private static class ViewHolder {
        TextView imageName;
        ImageView imagePath;
    }

}