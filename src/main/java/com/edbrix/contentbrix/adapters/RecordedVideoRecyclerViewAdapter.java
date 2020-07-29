package com.edbrix.contentbrix.adapters;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.edbrix.contentbrix.MainActivity;
import com.edbrix.contentbrix.R;
import com.edbrix.contentbrix.VideoPlayerActivity;
import com.edbrix.contentbrix.commons.VideoThumbLoader;
import com.edbrix.contentbrix.data.FileData;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by rajk on 21/09/17.
 */

public class RecordedVideoRecyclerViewAdapter extends RecyclerView.Adapter<RecordedVideoRecyclerViewAdapter.VideoViewHolder> {
    private Context adContext;
    private List<FileData> fileDataList;
    private OnButtonActionListener onButtonActionListener;
    private final VideoThumbLoader mVideoThumbLoader;

    public interface OnButtonActionListener {

        public void onDeleteButtonPressed(FileData fileData, int position);

        public void onCardViewClicked(FileData fileData, int position);

        public void onRenameViewClicked(FileData fileData, int position);

    }

    public RecordedVideoRecyclerViewAdapter(Context adContext, List<FileData> filesList, OnButtonActionListener onButtonActionListener) {
        this.fileDataList = filesList;
        this.adContext = adContext;
        this.mVideoThumbLoader = new VideoThumbLoader();
        this.onButtonActionListener = onButtonActionListener;
    }

    @Override
    public RecordedVideoRecyclerViewAdapter.VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.videoitem_card, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecordedVideoRecyclerViewAdapter.VideoViewHolder holder, final int position) {
        holder.fileDataAdpt = fileDataList.get(position);
        holder.thumbnail.setTag(holder.fileDataAdpt.getFileObject().getPath());// binding imageview

        holder.thumbnail.setImageResource(R.drawable.play_circle_grey); //default image
        mVideoThumbLoader.showThumbByAsynctack(holder.fileDataAdpt.getFileObject().getPath(), holder.thumbnail);
        String fName = holder.fileDataAdpt.getFileName();//.replaceAll(".mp4", "");
        holder.title.setText(fName);
        holder.duration.setText(getVideoDuration(adContext, holder.fileDataAdpt.getFileObject()));

        holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonActionListener.onDeleteButtonPressed(holder.fileDataAdpt, position);
            }
        });
        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openVideoPlayer(holder.fileDataAdpt);
            }
        });
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonActionListener.onCardViewClicked(holder.fileDataAdpt, position);
            }
        });
        holder.rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonActionListener.onRenameViewClicked(holder.fileDataAdpt, position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return fileDataList.size();
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder {
        public TextView title, duration;
        public ImageView thumbnail, overflow,rename;
        public CardView cardView;
        public FileData fileDataAdpt;

        public VideoViewHolder(View view) {
            super(view);
            cardView = (CardView) view.findViewById(R.id.card_view);
            title = (TextView) view.findViewById(R.id.title);
            duration = (TextView) view.findViewById(R.id.duration);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            overflow = (ImageView) view.findViewById(R.id.overflow);
            rename = (ImageView) view.findViewById(R.id.rename);
        }
    }

    /**
     * Return video file duration from File Object
     *
     * @param context   Context
     * @param uriOfFile URI of File
     * @return String with min:sec format
     */
    public String getVideoDuration(Context context, File uriOfFile) {
        int duration = 0;
        MediaPlayer mp = MediaPlayer.create(context, Uri.parse(uriOfFile.getPath()));
        try {
            duration = mp.getDuration();
            mp.release();
        } catch (Exception e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        }

/*convert millis to appropriate time*/
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
        );
    }

    public void removeItem(int pos) {
        if (fileDataList != null && fileDataList.size() > 0) {
            fileDataList.remove(pos);
            if(adContext instanceof MainActivity)
            {
                ((MainActivity)adContext).checkIsVedioListEmpty();
            }
            notifyDataSetChanged();
        }
    }

    private void openVideoPlayer(FileData fileData) {
        Intent videoPlayer = new Intent(adContext, VideoPlayerActivity.class);
        videoPlayer.putExtra("FileData", fileData);
        Log.d("File name passed : ",fileData.getFileName());
        adContext.startActivity(videoPlayer);
    }

    public void updateList(List<FileData> list) {
        if (fileDataList != null && fileDataList.size() > 0) {
            fileDataList =list;
            notifyDataSetChanged();
        }
    }

}
