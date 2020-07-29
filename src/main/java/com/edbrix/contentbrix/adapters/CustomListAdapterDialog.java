package com.edbrix.contentbrix.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.edbrix.contentbrix.R;
import com.edbrix.contentbrix.data.UserOrganizationList;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CustomListAdapterDialog extends BaseAdapter {

    private ArrayList<UserOrganizationList> listData;

    private LayoutInflater layoutInflater;
    private Context context;


    public CustomListAdapterDialog(Context context, ArrayList<UserOrganizationList> listData) {
        this.listData = listData;
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
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
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.organisation_name_item, null);
            holder = new ViewHolder();

            holder.orgnameTextView = (TextView) convertView.findViewById(R.id.org_name);
            holder.orglogoImageView = (ImageView) convertView.findViewById(R.id.org_image);
            holder.orgLinearLayout = (LinearLayout) convertView.findViewById(R.id.org_relative_layout);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.orgnameTextView.setText(listData.get(position).getOrganizationName());
        /*Picasso.with(context)
                .load(listData.get(position).getSchoolLogoUrl())
                .error(R.drawable.app_logo_round_new24).fit()
                .into(holder.orglogoImageView);*/

        Picasso.with(context)
                .load(listData.get(position).getSchoolLogoUrl())
                .error(R.drawable.app_logo_round_new24)
                .into(holder.orglogoImageView);

        //holder.orgnameTextView.setGravity(Gravity.CENTER);
        return convertView;
    }

    static class ViewHolder {
        TextView orgnameTextView;
        ImageView orglogoImageView;
        LinearLayout orgLinearLayout;
    }

}