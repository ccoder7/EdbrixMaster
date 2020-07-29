package com.edbrix.contentbrix.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.edbrix.contentbrix.R;
import com.vlk.multimager.utils.Image;

import java.util.ArrayList;

public class PageTileListAdapter extends RecyclerView.Adapter<PageTileListAdapter.PageTileViewHolder>{

    private Context context;
    private ArrayList<Image> imageTilesList;
    private PageTileListActionListener pageTileListActionListener;


    public interface PageTileListActionListener {
        void onPageItemSelected(int index, Image image);
    }

    public PageTileListAdapter(Context context, ArrayList<Image> imageTilesList, PageTileListActionListener pageTileListActionListener) {
        this.context = context;
        this.imageTilesList = imageTilesList;
        this.pageTileListActionListener = pageTileListActionListener;
    }

    @NonNull
    @Override
    public PageTileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_page_tiles, parent, false);
        return new PageTileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PageTileListAdapter.PageTileViewHolder holder, final int position) {
        holder.txtPageIndex.setText(""+(position+1));
        Bitmap imageBitMap = BitmapFactory.decodeFile(imageTilesList.get(position).imagePath);
        holder.imgTileThumbnail.setImageBitmap(imageBitMap);
        final Image imageData = imageTilesList.get(position);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageTileListActionListener.onPageItemSelected(position,imageData);
            }
        });

    }

    @Override
    public int getItemCount() {
        return imageTilesList.size();
    }

    public class PageTileViewHolder extends RecyclerView.ViewHolder {
        public TextView txtPageIndex;
        public ImageView imgTileThumbnail;
        public final View mView;

        public PageTileViewHolder(View view) {
            super(view);
            mView = view;
            txtPageIndex = (TextView) view.findViewById(R.id.txtPageIndex);
            imgTileThumbnail = (ImageView) view.findViewById(R.id.imgTileThumbnail);
        }
    }
}
