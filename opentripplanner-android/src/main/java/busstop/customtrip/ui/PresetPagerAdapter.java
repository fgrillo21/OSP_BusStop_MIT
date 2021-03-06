package busstop.customtrip.ui;




import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.util.List;

import edu.usf.cutr.opentripplanner.android.R;

public class PresetPagerAdapter extends PagerAdapter {

    private Context context;
    private List<Integer> imagesArray;

    PresetPagerAdapter(Context context, List<Integer> imagesArray){

        this.context = context;
        this.imagesArray = imagesArray;
    }

    @Override
    public int getCount() {
        return imagesArray.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == o;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        //creating  xml file for custom viewpager
        View view = LayoutInflater.from(context).inflate(R.layout.pager_item, container, false);

        //finding id
        ImageView imageView =  view.findViewById(R.id.image);

        //setting data
        imageView.setImageDrawable(ContextCompat.getDrawable(context, imagesArray.get(position)));

        if (position != 3) {
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        else {
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }

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