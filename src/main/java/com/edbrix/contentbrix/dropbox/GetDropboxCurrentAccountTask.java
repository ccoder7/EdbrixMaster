package com.edbrix.contentbrix.dropbox;

import android.os.AsyncTask;

import com.crashlytics.android.Crashlytics;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;

/**
 * Created by rajk on 17/10/17.
 */

public class GetDropboxCurrentAccountTask extends AsyncTask<Void, Void, FullAccount> {

    private DbxClientV2 dbxClient;
    private Callback  mCallback;
    private Exception error;

    public interface Callback {
        void onAccountReceived(FullAccount account);
        void onError(Exception error);
    }

    public GetDropboxCurrentAccountTask(DbxClientV2 dbxClient, Callback callback){
        this.dbxClient =dbxClient;
        this.mCallback = callback;
    }

    @Override
    protected FullAccount doInBackground(Void... params) {
        try {
            //get the users FullAccount
            return dbxClient.users().getCurrentAccount();
        } catch (DbxException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
            error = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(FullAccount account) {
        super.onPostExecute(account);

        if (account != null && error == null){
            //User Account received successfully
            mCallback.onAccountReceived(account);
        }
        else {
            // Something went wrong
            mCallback.onError(error);
        }
    }
}
