package com.edbrix.contentbrix.walkthrough;

import android.content.Context;
import androidx.viewpager.widget.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.edbrix.contentbrix.R;
import com.viewpagerindicator.IconPagerAdapter;


/**
 * Created by rajk
 */
class WalkthroughAdapter extends PagerAdapter implements IconPagerAdapter {

    Context mContext;

    LayoutInflater mLayoutInflater;

    protected static final int[] mResources = new int[]{
            R.drawable.walkthroughs_1,
            R.drawable.walkthroughs_2,
            R.drawable.walkthroughs_3,
            R.drawable.walkthroughs_4,
            R.drawable.walkthroughs_5,
            R.drawable.walkthroughs_6,
           /* R.drawable.walkthroughs_7,*/
    };

    public WalkthroughAdapter(Context context) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mResources.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.pager_item, container, false);

        ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);
        imageView.setImageResource(mResources[position]);

        container.addView(itemView);

        return itemView;
    }

    @Override
    public int getIconResId(int index) {
        return mResources[index % mResources.length];
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}

