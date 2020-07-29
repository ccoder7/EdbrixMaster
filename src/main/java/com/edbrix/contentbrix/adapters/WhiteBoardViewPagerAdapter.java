package com.edbrix.contentbrix.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.edbrix.contentbrix.fragments.WhiteBoardPageFragment;

import java.util.List;

/**
 * Created by rajk on 31/07/17.
 */

public class WhiteBoardViewPagerAdapter extends FragmentStatePagerAdapter {

    private FragmentManager mFragmentManager;
    private int pageCount;
    private boolean isItemRemoved;

    public WhiteBoardViewPagerAdapter(FragmentManager fm, int count) {
        super(fm);
        this.mFragmentManager = fm;
        this.pageCount = count;
        this.isItemRemoved = false;
    }

    @Override
    public Fragment getItem(int position) {
        //Create a new instance of the fragment and return it.
        WhiteBoardPageFragment whiteBoardPageFragment = (WhiteBoardPageFragment) WhiteBoardPageFragment.getInstance(/*imageAdList.get(position)*/);
        whiteBoardPageFragment.setFragmentId(position);

        //We will not pass the data through bundle because it will not gets updated by calling notifyDataSetChanged()  method. We will do it through getter and setter.
        Log.e(WhiteBoardViewPagerAdapter.class.getName(),"getItem() "+position);
        return whiteBoardPageFragment;
    }

    @Override
    public int getCount() {
        return pageCount;
    }

    public List<Fragment> getFragmentsList() {
        Log.e(WhiteBoardViewPagerAdapter.class.getName(),"getFragmentsList()"+mFragmentManager.getFragments().size());
        return mFragmentManager.getFragments();
    }

    @Override
    public int getItemPosition(Object object) {
        List<Fragment> fragmentsList = mFragmentManager.getFragments();
        WhiteBoardPageFragment fragment = (WhiteBoardPageFragment) object;
//        fragment.setRotateButtonVisibility(isRotateButtonVisible);
//        Image dummyItem = fragment.getImageResource();

        int position = fragment.getFragmentId();//0;

        /*_____________________________________________*/
        /*Only for logging purpose*/
        int size = fragmentsList.size();
////        Image dummyItemNew = null;
////        if (position >= 0) {
////            dummyItemNew = imageAdList.get(position);
////        }
//        Log.i("ImgViewPgAdpter", "************getItemPosition Old_Item: Position:" + position + " size:" + size + " id:" + dummyItem._id + " url:" + dummyItem.imagePath);
//        if (dummyItemNew != null) {
//            Log.i("ImgViewPgAdpter", "************getItemPosition New_Item: Position:" + position + " size:" + size + " id:" + dummyItemNew._id + " url:" + dummyItemNew.imagePath);
//
//        }
//        /*_____________________________________________*/
//
//        if (isItemRemoved) {
//            // Returning POSITION_NONE means the current data does not matches the data this fragment is showing right now.  Returning POSITION_NONE constant will force the fragment to redraw its view layout all over again and show new data.
//            return POSITION_NONE;
//        } else {
//            if (position >= 0) {
//                // The current data matches the data in this active fragment, so let it be as it is.
//                return position;
//            } else {
//                // Returning POSITION_NONE means the current data does not matches the data this fragment is showing right now.  Returning POSITION_NONE constant will force the fragment to redraw its view layout all over again and show new data.
//                return POSITION_NONE;
//            }
//        }
        return position;
    }
}
