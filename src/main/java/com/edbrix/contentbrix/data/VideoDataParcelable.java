package com.edbrix.contentbrix.data;

import android.os.Parcel;
import android.os.Parcelable;

//import com.vlk.multimager.utils.Image;

/**
 * Created by rajk on 08/08/17.
 */

public class VideoDataParcelable implements Parcelable {

//    private Image imageData;
    private String audFileAbsolutePath;
    private int audioFileDurationSeconds;

  /*  public VideoDataParcelable(Image image, String audFileAbsolutePath, int duration) {
        this.imageData = image;
        this.audFileAbsolutePath = audFileAbsolutePath;
        this.audioFileDurationSeconds = duration;
    }

    public Image getImageData() {
        return imageData;
    }

    public void setImageData(Image imageData) {
        this.imageData = imageData;
    }*/

    public String getAudFileAbsolutePath() {
        return audFileAbsolutePath;
    }

    public void setAudFileAbsolutePath(String audFileAbsolutePath) {
        this.audFileAbsolutePath = audFileAbsolutePath;
    }

    public int getAudioFileDurationSeconds() {
        return audioFileDurationSeconds;
    }

    public void setAudioFileDurationSeconds(int audioFileDuration) {
        this.audioFileDurationSeconds = audioFileDuration;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
//        out.writeParcelable(imageData, flags);
        out.writeString(audFileAbsolutePath);
        out.writeInt(audioFileDurationSeconds);
    }

    public static final Creator<VideoDataParcelable> CREATOR = new Creator<VideoDataParcelable>() {
        public VideoDataParcelable createFromParcel(Parcel in) {
            return new VideoDataParcelable(in);
        }

        public VideoDataParcelable[] newArray(int size) {
            return new VideoDataParcelable[0];
        }
    };

    private VideoDataParcelable(Parcel in) {
//        imageData = in.readParcelable(getClass().getClassLoader());
        audFileAbsolutePath = in.readString();
        audioFileDurationSeconds = in.readInt();
    }

}
