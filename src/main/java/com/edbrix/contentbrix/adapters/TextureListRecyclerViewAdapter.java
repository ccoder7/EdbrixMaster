package com.edbrix.contentbrix.adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.edbrix.contentbrix.R;

import java.util.ArrayList;

/**
 * Created by rajk on 19/09/17.
 */

public class TextureListRecyclerViewAdapter extends RecyclerView.Adapter<TextureListRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Integer> resourcesIdList;
    private Context mContext;
    private OnButtonActionListener onButtonActionListener;

    public interface OnButtonActionListener {
        public void onTextureSelected(int drawableResource);
    }

    public TextureListRecyclerViewAdapter(Context mContext, OnButtonActionListener onButtonActionListener) {
        this.mContext = mContext;
        this.onButtonActionListener = onButtonActionListener;
        this.resourcesIdList = new ArrayList<>();
        this.resourcesIdList.add(R.drawable.grey_grid);
        this.resourcesIdList.add(R.drawable.journal_yellow_line_page);
        this.resourcesIdList.add(R.drawable.journal_yellow_line_page_2);
        this.resourcesIdList.add(R.drawable.graphpng);
        this.resourcesIdList.add(R.drawable.light_yellow_square);
        this.resourcesIdList.add(R.drawable.light_green_square);
        this.resourcesIdList.add(R.drawable.light_red_square);
        this.resourcesIdList.add(R.drawable.light_blue_square);
        this.resourcesIdList.add(R.drawable.white_square);
    }

    @Override
    public TextureListRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.background_texture_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TextureListRecyclerViewAdapter.ViewHolder holder, final int position) {

        holder.textureImg.setImageDrawable(mContext.getDrawable(resourcesIdList.get(position)));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               onButtonActionListener.onTextureSelected(resourcesIdList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return resourcesIdList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View mView;
        public TextView textureName;
        public ImageView textureImg;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            textureName = (TextView) view.findViewById(R.id.textureName);
            textureImg = (ImageView) view.findViewById(R.id.textureImg);
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }
}
