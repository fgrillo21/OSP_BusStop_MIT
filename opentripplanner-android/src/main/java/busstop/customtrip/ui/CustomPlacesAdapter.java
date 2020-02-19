package busstop.customtrip.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import busstop.customtrip.model.Place;
import edu.usf.cutr.opentripplanner.android.R;

public class CustomPlacesAdapter extends ArrayAdapter<Place> {

    private List<Place> list;
    private Context     context;

    public CustomPlacesAdapter(@NonNull Context context, int resource, @NonNull List<Place> objects) {
        super(context, resource, objects);

        this.context = context;
        this.list    = objects;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Place getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        // return list.get(pos).getId(); if Id is available in items
        return pos;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listview_palces_custom_layout, parent, false);
        }

        TextView tVRow = (TextView) view.findViewById(R.id.tViewRow);
        tVRow.setText(list.get(position).getName());

        ImageButton deleteBtn = (ImageButton) view.findViewById(R.id.btnDeleteRow);

        deleteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                deletePlace(position);
            }
        });

        return view;
    }

    public void deletePlace(int position) {

        if (position != -1) {
            list.remove(position);
            this.notifyDataSetChanged();
        }
    }
}