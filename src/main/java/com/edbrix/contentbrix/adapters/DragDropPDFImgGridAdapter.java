package com.edbrix.contentbrix.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.edbrix.contentbrix.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.vlk.multimager.utils.Image;

import org.askerov.dynamicgrid.BaseDynamicGridAdapter;

import java.util.List;

public class DragDropPDFImgGridAdapter extends BaseDynamicGridAdapter {
    private ImageLoader imageLoaderNostra;
    private DisplayImageOptions options;
    private Context mContext;

    public DragDropPDFImgGridAdapter(Context context, List<?> items, int columnCount, ImageLoader imageLoaderN) {
        super(context, items, columnCount);
        this.mContext = context;
        this.imageLoaderNostra = imageLoaderN;
        this.options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.image_processing)
                .showImageForEmptyUri(R.drawable.image_placeholder)
                .showImageOnFail(R.drawable.no_image)
                .cacheInMemory(false)
                .cacheOnDisk(false)
                .considerExifParams(true)
                .displayer(new CircleBitmapDisplayer(Color.WHITE, 5))
                .build();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImagesViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_grid_drag_drop_img, null);
            holder = new ImagesViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ImagesViewHolder) convertView.getTag();
        }
        holder.build(getItem(position));
        return convertView;
    }

    private class ImagesViewHolder {
        private ImageView image;
        private ImageView closeBtnImageView;

        private ImagesViewHolder(View view) {
            image = (ImageView) view.findViewById(R.id.item_img);
            closeBtnImageView = (ImageView) view.findViewById(R.id.close_img);

        }

        void build(Object listItem) {
            final Image imageObject = (Image) listItem;
            imageLoaderNostra.displayImage(imageObject.uri.toString(), image, options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    image.setImageBitmap(loadedImage);
                }
            });
            closeBtnImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    remove(imageObject);
                    notifyDataSetChanged();
                }
            });
        }
    }

    public Bitmap getImgBimapByPos(int pos){
        Image imageObject = (Image) getItem(pos);
        Bitmap icon = BitmapFactory.decodeFile(imageObject.imagePath);
        return icon;
    }

    public Image getImgByPos(int pos){
        Image imageObject = (Image) getItem(pos);
        return imageObject;
    }

    public void addNewPage(int pos,Image img) {
        add(pos,img);
        notifyDataSetChanged();
    }
}