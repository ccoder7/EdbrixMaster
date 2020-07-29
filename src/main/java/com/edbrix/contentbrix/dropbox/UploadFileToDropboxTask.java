package com.edbrix.contentbrix.dropbox;

import android.os.AsyncTask;

import com.crashlytics.android.Crashlytics;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by rajk on 17/10/17.
 */

public class UploadFileToDropboxTask extends AsyncTask<String, Void, FileMetadata> {

    private DbxClientV2 dbxClient;
    private File file;
    private Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onUploadFileToDropboxComplete(FileMetadata result);
        void onUploadFileToDropboxError(Exception e);
    }

    public UploadFileToDropboxTask(DbxClientV2 dbxClient, File file, Callback callback) {
        this.dbxClient = dbxClient;
        this.file = file;
        this.mCallback = callback;
    }

    @Override
    protected FileMetadata doInBackground(String... params) {
        try {
            // Upload to Dropbox
            InputStream inputStream = new FileInputStream(file);
           return dbxClient.files().uploadBuilder("/" + file.getName()) //Path in the user's Dropbox to save the file.
                    .withMode(WriteMode.OVERWRITE) //always overwrite existing file
                    .uploadAndFinish(inputStream);
        } catch (DbxException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
            mException = e;
        } catch (IOException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
            mException = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(FileMetadata result) {
        super.onPostExecute(result);

        if (mException != null) {
            mCallback.onUploadFileToDropboxError(mException);
        } else if (result == null) {
            mCallback.onUploadFileToDropboxError(null);
        } else {
            mCallback.onUploadFileToDropboxComplete(result);
        }
    }
}

