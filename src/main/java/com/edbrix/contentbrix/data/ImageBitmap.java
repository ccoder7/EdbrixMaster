package com.edbrix.contentbrix.data;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by rajk on 18/08/17.
 */

public class ImageBitmap implements Parcelable {

    private int imageId;
    private String imagePath;
    private Bitmap imageBitmap;


    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(imageId);
        out.writeString(imagePath);
        out.writeParcelable(imageBitmap, flags);
    }

    public static final Creator<ImageBitmap> CREATOR = new Creator<ImageBitmap>() {
        public ImageBitmap createFromParcel(Parcel in) {
            return new ImageBitmap(in);
        }

        public ImageBitmap[] newArray(int size) {
            return new ImageBitmap[0];
        }
    };

    private ImageBitmap(Parcel in) {
        imageId = in.readInt();
        imagePath = in.readString();
        imageBitmap = in.readParcelable(getClass().getClassLoader());
    }
}
