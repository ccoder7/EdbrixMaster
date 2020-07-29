package com.edbrix.contentbrix.adapters;

import android.content.Context;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.edbrix.contentbrix.fragments.PDFPageFragment;
import com.vlk.multimager.utils.Image;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PDFViewPagerAdapter extends FragmentPagerAdapter {

    private List<Image> imageAdList;
    private FragmentManager mFragmentManager;
    private PdfRenderer pdfRenderer;
    private Context mContext;
    private int autoNo = 1001;
    private Random generator;
    private String pdfFileName;
    private int pageCount;
    public String TAG = PDFViewPagerAdapter.class.getName();
    private boolean isItemRemoved;

    public PDFViewPagerAdapter(FragmentManager fm, File pdfFileObj, Context context,ArrayList<Image> imageList) {
        super(fm);
        generator = new Random();
        this.imageAdList = imageList;
        this.mFragmentManager = fm;
        this.mContext = context;
        this.pdfFileName = pdfFileObj.getName();
        this.isItemRemoved = false;
        initializePDFRenderer(pdfFileObj);
        Log.e(TAG,"Constructor PDFViewPagerAdapter");
    }


    //09
    public void initializePDFRenderer(File pdfFile) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                this.pdfRenderer = new PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY));
                this.pageCount = this.pdfRenderer.getPageCount();//+20;
                Log.e(TAG,"initializePDFRenderer "+pdfFile.getName()+"\npge_count"+this.pageCount);
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        }
    }

    @Override
    public Fragment getItem(int position){
        //Create a new instance of the fragment and return it.
        PDFPageFragment pdfPageFragment = (PDFPageFragment) PDFPageFragment.getInstance();
        //We will not pass the data through bundle because it will not gets updated by calling notifyDataSetChanged()  method. We will do it through getter and setter.

      /*  if(imageAdList.size() <0){

        }*/
        pdfPageFragment.setImageResource(imageAdList.get(position));
        Log.e(TAG,"pdfPageFragment setImageResource : "+position);
        pdfPageFragment.setFragmentId(position);
        //autoNo = generator.nextInt(10000);//009
        //Bitmap tmpBitmap = getBitmapFromPDFByIndex(position);
        /*if (tmpBitmap != null) {
            *//*File tmpfile = saveImageFromBitmap(tmpBitmap, "" + autoNo);
            imageAdList.add(position, new Image(autoNo, Uri.fromFile(tmpfile), tmpfile.getPath(), false));*//*
            //pdfPageFragment.setPdfImageBitmap(getBitmapFromPDFByIndex(position));
           //pdfPageFragment.setWidthHeight(tmpBitmap.getWidth(), tmpBitmap.getHeight());
            //pdfPageFragment.setImageResource(new Image(autoNo, Uri.fromFile(tmpfile), tmpfile.getPath(), false));

            tmpBitmap.recycle();
            tmpBitmap=null;
        }*/
        Log.e(TAG,"getItem() "+position);
        return pdfPageFragment;
    }

    @Override
    public int getCount() {
        return this.imageAdList.size();
    }

    public List<Fragment> getFragmentsList() {
        Log.e(TAG,"getFragmentsList()"+mFragmentManager.getFragments().size());
        return mFragmentManager.getFragments();
    }

    @Override
    public int getItemPosition(Object object) {
        List<Fragment> fragmentsList = mFragmentManager.getFragments();
        PDFPageFragment fragment = (PDFPageFragment) object;
//        fragment.setRotateButtonVisibility(isRotateButtonVisible);
        Image dummyItem = fragment.getImageResource();
        int position = imageAdList.indexOf(dummyItem);

        /*_____________________________________________*/
        /*Only for logging purpose*/
        int size = fragmentsList.size();
        Image dummyItemNew = null;
        if (position >= 0) {
            dummyItemNew = imageAdList.get(position);
        }
        Log.i("ImgViewPgAdpter", "************getItemPosition Old_Item: Position:" + position + " size:" + size + " id:" + dummyItem._id + " url:" + dummyItem.imagePath);
        if (dummyItemNew != null) {
            Log.i("ImgViewPgAdpter", "************getItemPosition New_Item: Position:" + position + " size:" + size + " id:" + dummyItemNew._id + " url:" + dummyItemNew.imagePath);
        }
        /*_____________________________________________*/

        if (isItemRemoved) {
            // Returning POSITION_NONE means the current data does not matches the data this fragment is showing right now.  Returning POSITION_NONE constant will force the fragment to redraw its view layout all over again and show new data.
            return POSITION_NONE;
        } else {
            if (position >= 0) {
                // The current data matches the data in this active fragment, so let it be as it is.
                return position;
            } else {
                // Returning POSITION_NONE means the current data does not matches the data this fragment is showing right now.  Returning POSITION_NONE constant will force the fragment to redraw its view layout all over again and show new data.
                return POSITION_NONE;
            }
        }
//        Log.e(TAG,"getItemPosition()"+position);
//        return position;
    }

    /*public ArrayList<Image> getPDFImageList() {
        if (imageAdList != null && imageAdList.size() > 0) {
            return (ArrayList<Image>)imageAdList;
        } else {
            return null;
        }
    }*/

    public void reOrderList(List<Image> reorderedList) {
        Log.e("123",""+reorderedList.size());
        try{
            imageAdList.clear();
            Log.e("123"," imageAdList size clear : "+imageAdList.size());
        } catch (Exception e){Log.e(TAG,e.getMessage().toString());}
        imageAdList.addAll(reorderedList);
        Log.e("123","imageAdList size after added : "+imageAdList.size());
        notifyDataSetChanged();
    }


}