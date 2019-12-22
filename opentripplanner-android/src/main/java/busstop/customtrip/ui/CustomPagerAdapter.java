package busstop.customtrip.ui;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.util.ArrayList;

import edu.usf.cutr.opentripplanner.android.R;

public class CustomPagerAdapter extends PagerAdapter {

    Context context;
    ArrayList<Integer> pager;

    public CustomPagerAdapter(Context context, ArrayList<Integer> pager) {
        this.context = context;
        this.pager = pager;
    }

    @Override
    public int getCount() {
        return pager.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == o;
    }

    @Override
    public  Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.pager_item, container, false);
        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        imageView.setBackgroundResource(pager.get(position));
        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }
}