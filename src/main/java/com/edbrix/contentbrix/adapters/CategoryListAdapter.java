package com.edbrix.contentbrix.adapters;

import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.edbrix.contentbrix.R;
import com.edbrix.contentbrix.customview.RoundedImageView;
import com.edbrix.contentbrix.data.CategoryListData;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CategoryListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private Context context;
    private ArrayList<CategoryListData> categoryArrayList;
    private CategoryListActionListener categoryListActionListener;
    private OnLoadMoreListener onLoadMoreListener;
    private boolean isLoading;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;

    public interface CategoryListActionListener {
        void onCategoryItemSelected(CategoryListData categoryListData);
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    public CategoryListAdapter(Context context, RecyclerView categoryListRecyclerView, ArrayList<CategoryListData> categoryArrayList) {
        this.context = context;
        this.categoryArrayList = categoryArrayList;

        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) categoryListRecyclerView.getLayoutManager();
        categoryListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.onLoadMore();
                    }
                    isLoading = true;
                }
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_item_list_category, parent, false);
            return new CategoryViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof CategoryViewHolder) {
            final CategoryListData category = categoryArrayList.get(position);
            CategoryViewHolder categoryViewHolder = (CategoryViewHolder) holder;
            categoryViewHolder.txtCategoryName.setText(category.getTitle());
            categoryViewHolder.txtCategoryDesc.setText(category.getDescription());

            /*if (course.getCourseImageUrl() != null && !course.getCourseImageUrl().isEmpty()) {
                Picasso.with(context)
                        .load(course.getCourseImageUrl())
                        .error(R.drawable.icon_image)
                        .into(courseViewHolder.imgCourseBy);
            }*/

            categoryViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    categoryListActionListener.onCategoryItemSelected(category);
                }
            });
            categoryViewHolder.btnGoDetailCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    categoryListActionListener.onCategoryItemSelected(category);
                }
            });

            if (position % 2 == 1) {
                categoryViewHolder.mView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorActionBar));
            } else {
                categoryViewHolder.mView.setBackgroundColor(ContextCompat.getColor(context, R.color.ColorsWhite));
            }
        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return categoryArrayList == null ? 0 : categoryArrayList.size();
    }

    public void setLoaded() {
        isLoading = false;
    }

    @Override
    public int getItemViewType(int position) {
        return categoryArrayList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.onLoadMoreListener = mOnLoadMoreListener;
    }

    public void setCategoryListActionListener(CategoryListActionListener categoryListActionListener) {
        this.categoryListActionListener = categoryListActionListener;
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private TextView txtCategoryName;
        private TextView txtCategoryDesc;
        private ImageView btnGoDetailCategory;
        private RoundedImageView imgCategoryBy;

        CategoryViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            txtCategoryName = itemView.findViewById(R.id.txtCategoryName);
            txtCategoryDesc = itemView.findViewById(R.id.txtCategoryDesc);
            btnGoDetailCategory = itemView.findViewById(R.id.btnGoDetailCategory);
            imgCategoryBy = itemView.findViewById(R.id.imgCategoryBy);

        }
    }

    // "Loading item" ViewHolder
    private class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View view) {
            super(view);
            progressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
        }
    }

    public void updateList(ArrayList<CategoryListData> filterList) {
        if (categoryArrayList != null) {
            categoryArrayList = filterList;
            notifyDataSetChanged();
        }
    }
}
