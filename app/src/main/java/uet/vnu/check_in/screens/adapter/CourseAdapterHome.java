package uet.vnu.check_in.screens.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.UnknownHostException;

import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
import uet.vnu.check_in.CheckInApplication;
import uet.vnu.check_in.R;
import uet.vnu.check_in.data.model.Course;
import uet.vnu.check_in.data.source.remote.AuthenticationRemoteDataSource;
import uet.vnu.check_in.data.source.remote.api.response.BaseResponse;
import uet.vnu.check_in.screens.BaseRecyclerViewAdapter;
import uet.vnu.check_in.screens.chat.ChatlogActivity;

public class CourseAdapterHome extends BaseRecyclerViewAdapter<Course, CourseAdapterHome.ViewHolder> {

    public CourseAdapterHome(Context context) {
        super(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        // Inflate the custom layout
        View courseView = layoutInflater.inflate(R.layout.item_course_rcv_home, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(courseView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Course course = this.mCollection.get(position);
        holder.updateView(course, this);
    }
    public void removeCourse(Course course){
        Log.d("cuonghx", "removeCourse: " + course.getId());
        mCollection.remove(course);
        notifyDataSetChanged();
    }


    public class ViewHolder  extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView mTextViewCourseName;
        private Course tmpCourse;
        private CourseAdapterHome parentAdapter;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextViewCourseName = itemView.findViewById(R.id.tv_it_course_name);
            mTextViewCourseName.setOnClickListener(this);
        }

        public void updateView(Course course, CourseAdapterHome adapter) {
            tmpCourse = course;
            this.parentAdapter = adapter;
            mTextViewCourseName.setText(course.getName());
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.tv_it_course_name:
                    Log.d("cuonghx", "onClick: ");
                    Intent intent = new Intent(view.getContext(), ChatlogActivity.class);
                    intent.putExtra("course_id", tmpCourse.getId());
                    intent.putExtra("course_name", tmpCourse.getName());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    view.getContext().startActivity(intent);
                    break;
            }
        }

    }
}
