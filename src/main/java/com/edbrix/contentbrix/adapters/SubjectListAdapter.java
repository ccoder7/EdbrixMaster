package com.edbrix.contentbrix.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.edbrix.contentbrix.R;
import com.edbrix.contentbrix.data.SubjectData;

import java.util.ArrayList;

/**
 * Created by rajk on 11/12/17.
 */

public class SubjectListAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<SubjectData> listData;


    public SubjectListAdapter(Context context, ArrayList<SubjectData> listData) {
        this.mContext =context;
        this.listData =listData;
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return Long.parseLong(listData.get(position).getId());
    }

    @Override
    public View getDropDownView(int position, View view, ViewGroup parent) {
        if (view == null || !view.getTag().toString().equals("DROPDOWN")) {
            view = LayoutInflater.from(mContext).inflate(R.layout.normal_spinner_item_dropdown, parent, false);
            view.setTag("DROPDOWN");
        }


        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setText(listData.get(position).getSubject() + "");
        return view;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null || !view.getTag().toString().equals("NON_DROPDOWN")) {
            view = LayoutInflater.from(mContext).inflate(R.layout.
                    normal_spinner_item_bar, parent, false);
            view.setTag("NON_DROPDOWN");
        }
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setText(listData.get(position).getSubject() + "");
        return view;
    }
}
