package com.edbrix.contentbrix.data;


//import com.vlk.multimager.utils.Image;

import java.io.Serializable;

/**
 * Created by rajk on 08/08/17.
 */

public class VideoData implements Serializable {
//    public Image imageData;
    public String audFileAbsolutePath;
    public int duration;


   /* public VideoData(Image imageData, String audFileAbsolutePath, int duration) {
        super();
        this.imageData = imageData;
        this.audFileAbsolutePath = audFileAbsolutePath;
        this.duration = duration;
    }
*/
  /*  public Image getImageData() {
        return imageData;
    }

    public void setImageData(Image imageData) {
        this.imageData = imageData;
    }
*/
    public String getAudFileAbsolutePath() {
        return audFileAbsolutePath;
    }

    public void setAudFileAbsolutePath(String audFileAbsolutePath) {
        this.audFileAbsolutePath = audFileAbsolutePath;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
