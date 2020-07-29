package com.edbrix.contentbrix.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.edbrix.contentbrix.R;


/**
 * Created by rajk on 19/09/17.
 */

public class WhiteBoardRecyclerViewAdapter extends RecyclerView.Adapter<WhiteBoardRecyclerViewAdapter.ViewHolder> {

    @Override
    public WhiteBoardRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_white_board_page, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(WhiteBoardRecyclerViewAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 10;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View mView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }
}
