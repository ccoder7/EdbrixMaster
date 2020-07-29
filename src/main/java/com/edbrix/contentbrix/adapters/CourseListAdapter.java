package com.edbrix.contentbrix.adapters;


import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.edbrix.contentbrix.R;
import com.edbrix.contentbrix.customview.RoundedImageView;
import com.edbrix.contentbrix.data.CoursesData;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CourseListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private Context context;
    private ArrayList<CoursesData> courseList;
    private CourseListActionListener courseListActionListener;
    private OnLoadMoreListener onLoadMoreListener;
    private boolean isLoading;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;



    public interface CourseListActionListener {
        void onCourseItemSelected(CoursesData courses);
    }


    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    public CourseListAdapter(Context context, RecyclerView mRecyclerView, ArrayList<CoursesData> courseList)
    {
        this.context = context;
        this.courseList = courseList;

        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_courses, parent, false);
            return new CourseViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof CourseViewHolder) {
            final CoursesData course = courseList.get(position);
            CourseViewHolder courseViewHolder = (CourseViewHolder) holder;
            courseViewHolder.txtCourseName.setText(course.getTitle());
            courseViewHolder.txtCourseDesc.setText(course.getDescription());

            if (course.getCourseImageUrl() != null && !course.getCourseImageUrl().isEmpty()) {
                Picasso.with(context)
                        .load(course.getCourseImageUrl())
                        .error(R.drawable.icon_image)
                        .into(courseViewHolder.imgCourseBy);
            }

            courseViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    courseListActionListener.onCourseItemSelected(course);
                }
            });
           /* courseViewHolder.btnGoDetail.setOnClickListener(new View.OnClickListener() { //commented by pranav
                @Override
                public void onClick(View view) {
                    courseListActionListener.onCourseItemSelected(course);
                }
            });*/

            if (position % 2 == 1) {
                courseViewHolder.mView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorActionBar));
            } else {
                courseViewHolder.mView.setBackgroundColor(ContextCompat.getColor(context, R.color.ColorsWhite));
            }
        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return courseList == null ? 0 : courseList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return courseList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.onLoadMoreListener = mOnLoadMoreListener;
    }

    public void setCourseListActionListener(CourseListActionListener courseListActionListener) {
        this.courseListActionListener = courseListActionListener;
    }

    public void setLoaded() {
        isLoading = false;
    }

    public class CourseViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private TextView txtCourseName;
        private TextView txtCourseDesc;
       /* private ImageView btnGoDetail;*/ //commented by pranav
        private RoundedImageView imgCourseBy;

        CourseViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            txtCourseName = itemView.findViewById(R.id.txtCourseName);
            txtCourseDesc = itemView.findViewById(R.id.txtCourseDesc);
           /* btnGoDetail = itemView.findViewById(R.id.btnGoDetail);*/ //commented by pranav
            imgCourseBy = itemView.findViewById(R.id.imgCourseBy);

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

    public void updateList(ArrayList<CoursesData> filterList) {
        if (courseList != null) {
            courseList = filterList;
            notifyDataSetChanged();
        }
    }
}
