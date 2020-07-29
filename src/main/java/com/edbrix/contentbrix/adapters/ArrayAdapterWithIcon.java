package com.edbrix.contentbrix.adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.edbrix.contentbrix.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayAdapterWithIcon extends BaseAdapter {

    private List<Integer> images;
    private String[] items;
    private Context context;
    private LayoutInflater inflter;

//    public ArrayAdapterWithIcon(Context context, List<String> items, List<Integer> images) {
//        super(context, R.layout.item_image_text, items);
//        this.images = images;
//    }

    public ArrayAdapterWithIcon(Context context, String[] items, Integer[] images) {
        this.images = Arrays.asList(images);
        this.items =items;
        this.context = context;
        inflter = (LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public String getItem(int position) {
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflter.inflate(R.layout.item_image_text, null);//set layout for displaying items
        ImageView imageView = (ImageView) convertView.findViewById(R.id.imgType);
        TextView textView = (TextView) convertView.findViewById(R.id.txtTypeName);

        imageView.setImageDrawable(ContextCompat.getDrawable(context,images.get(position)));
        textView.setText(items[position].toString());

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(images.get(position), 0, 0, 0);
//        } else {
//            textView.setCompoundDrawablesWithIntrinsicBounds(images.get(position), 0, 0, 0);
//        }
//        textView.setCompoundDrawablePadding(10);
//                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getContext().getResources().getDisplayMetrics()));
        return convertView;
    }

}