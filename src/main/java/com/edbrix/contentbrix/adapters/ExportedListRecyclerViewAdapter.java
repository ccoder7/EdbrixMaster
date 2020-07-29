package com.edbrix.contentbrix.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.edbrix.contentbrix.BuildConfig;
import com.edbrix.contentbrix.MyExportedFilesActivity;
import com.edbrix.contentbrix.R;
import com.edbrix.contentbrix.RecordPDFOnBoardActivity;
import com.edbrix.contentbrix.data.FileData;

import java.io.File;
import java.util.List;

public class ExportedListRecyclerViewAdapter extends RecyclerView.Adapter<ExportedListRecyclerViewAdapter.VideoViewHolder> {
    private Context adContext;
    private List<FileData> fileDataList;
    private OnButtonActionListener onButtonActionListener;
    private boolean isPDFExport;

    public interface OnButtonActionListener {
        public void onDeleteButtonPressed(FileData fileData, int position);
    }
    public ExportedListRecyclerViewAdapter(Context adContext, boolean isPDFExport, List<FileData> filesList, OnButtonActionListener onButtonActionListener) {
        this.fileDataList = filesList;
        this.adContext = adContext;
        this.onButtonActionListener = onButtonActionListener;
        this.isPDFExport = isPDFExport;
    }
    @Override
    public ExportedListRecyclerViewAdapter.VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fileitem_card, parent, false);
        return new VideoViewHolder(view);
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder {
        public TextView title, duration;
        public ImageView thumbnail, overflow,upload;
        public CardView cardView;
        public FileData fileDataAdpt;
        public LinearLayout container;

        public VideoViewHolder(View view) {
            super(view);
            cardView = (CardView) view.findViewById(R.id.card_view);
            title = (TextView) view.findViewById(R.id.title);
            duration = (TextView) view.findViewById(R.id.duration);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            overflow = (ImageView) view.findViewById(R.id.overflow);
            upload = (ImageView) view.findViewById(R.id.upload);
            container = (LinearLayout)view.findViewById(R.id.container);

            if(isPDFExport){
                upload.setVisibility(View.INVISIBLE);
                overflow.setVisibility(View.INVISIBLE);
            }else{
                upload.setVisibility(View.VISIBLE);
                overflow.setVisibility(View.VISIBLE);
            }
        }
    }
    @Override
    public int getItemCount() {
        return fileDataList.size();
    }

    @Override
    public void onBindViewHolder(final ExportedListRecyclerViewAdapter.VideoViewHolder holder, final int position) {
        holder.fileDataAdpt = fileDataList.get(position);
        holder.thumbnail.setTag(holder.fileDataAdpt.getFileObject().getPath());// binding imageview

        String fName = holder.fileDataAdpt.getFileName().replaceAll(".mp4", "");
        holder.title.setText(fName);
        //holder.duration.setText("Size : "+(int)holder.fileDataAdpt.getFileSizeMB()+" MB");//getVideoDuration(adContext, holder.fileDataAdpt.getFileObject()));
        //holder.duration.setText("Size : "+holder.fileDataAdpt.getFileSizeMB()+"\nType : "+getMimeType(holder.fileDataAdpt.getFileObject().getAbsolutePath()));
        holder.duration.setText("Size: "+(holder.fileDataAdpt.getFileSizeMB())+ "MB");
        holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonActionListener.onDeleteButtonPressed(holder.fileDataAdpt, position);
            }
        });
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPDFExport){
                    openPDFRecordingScreen(holder.fileDataAdpt.getFileObject());
                }else {
                    openFile(holder.fileDataAdpt.getFileObject());
                }

               // Toast.makeText(adContext,"Hii",Toast.LENGTH_LONG).show();
            }
        });
        holder.upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareFile(holder.fileDataAdpt.getFileObject().getPath());
            }
        });
    }

    private void shareFile(String myFilePath) {
        Intent intentShareFile = new Intent(Intent.ACTION_SEND);
        File fileWithinMyDir = new File(myFilePath);

        if(fileWithinMyDir.exists()) {

            Uri fileUri = FileProvider.getUriForFile(adContext, BuildConfig.APPLICATION_ID + ".provider",fileWithinMyDir);
            intentShareFile.setType(adContext.getContentResolver().getType(fileUri));
            if (android.os.Build.VERSION.SDK_INT > 23) {
                intentShareFile.putExtra(Intent.EXTRA_STREAM, fileUri);
            }else{
                intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+myFilePath));
            }

           // resultIntent.setDataAndType(fileUri, getContentResolver().getType(myFilePath));

            intentShareFile.putExtra(Intent.EXTRA_SUBJECT,
                    "Sharing File...");
            intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...");
            intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            adContext.startActivity(Intent.createChooser(intentShareFile, "Share File"));
        }
    }

    public void removeItem(int pos) {
        if (fileDataList != null && fileDataList.size() > 0) {
            fileDataList.remove(pos);
            if(adContext instanceof MyExportedFilesActivity){
                ((MyExportedFilesActivity)adContext).checkIsExportFileListEmpty();
            }
            notifyDataSetChanged();
        }
    }

    public void updateList(List<FileData> list) {
        if (fileDataList != null && fileDataList.size() > 0) {
            fileDataList =list;
            notifyDataSetChanged();
        }
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public void openFile(File file){
        /*try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            //intent.setDataAndType(Uri.fromFile(file), getMimeType(file.getAbsolutePath()));
            intent.setDataAndType(FileProvider.getUriForFile(adContext, BuildConfig.APPLICATION_ID + ".provider", file),getMimeType(file.getAbsolutePath()));
            adContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // no Activity to handle this kind of files
            Toast.makeText(adContext,"No Related Application Found\nto open this File !!",Toast.LENGTH_LONG).show();
        }*/

        // Get URI and MIME type of file
        Uri uri = FileProvider.getUriForFile(adContext, BuildConfig.APPLICATION_ID + ".provider", file);
        String mime = adContext.getContentResolver().getType(uri);

        // Open file with user selected app
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mime);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        adContext.startActivity(intent);

    }

    private void openPDFRecordingScreen(File pdfFile) {
        Intent pdfRecordingIntent = new Intent(adContext, RecordPDFOnBoardActivity.class);
        pdfRecordingIntent.putExtra("pdf", pdfFile);
        adContext.startActivity(pdfRecordingIntent);
        ((Activity)adContext).finish();
    }
}