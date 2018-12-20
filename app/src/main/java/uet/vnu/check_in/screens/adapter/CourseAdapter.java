package uet.vnu.check_in.screens.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Scheduler;
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
import uet.vnu.check_in.screens.home.CourseActivity;
import uet.vnu.check_in.screens.home.HomeActivity;
import uet.vnu.check_in.screens.login.UpdateActivity;

public class CourseAdapter extends BaseRecyclerViewAdapter<Course, CourseAdapter.ViewHolder> {

    public CourseAdapter(Context context) {
        super(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        // Inflate the custom layout
        View courseView = layoutInflater.inflate(R.layout.item_course_recycleview, parent, false);

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


    public class ViewHolder  extends RecyclerView.ViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

        public TextView mTextViewCourseName;
        private Course tmpCourse;
        private CourseAdapter parentAdapter;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextViewCourseName = itemView.findViewById(R.id.tv_it_course_name);
            itemView.findViewById(R.id.iv_more_course).setOnClickListener(this);
            mTextViewCourseName.setOnClickListener(this);
        }
        public void updateView(Course course, CourseAdapter adapter){
            tmpCourse = course;
            this.parentAdapter = adapter;
            mTextViewCourseName.setText(course.getName());
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.iv_more_course:
                    PopupMenu menu = new PopupMenu(view.getContext(), view);
                    menu.setOnMenuItemClickListener(this);
                    menu.inflate(R.menu.menu_row_course);
                    menu.show();
                    break;
                case R.id.tv_it_course_name:
                    Log.d("cuonghx", "onClick: " );
                    Intent intent = new Intent(view.getContext(), ChatlogActivity.class);
                    intent.putExtra("course_id", tmpCourse.getId());
                    intent.putExtra("course_name", tmpCourse.getName());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    view.getContext().startActivity(intent);
                    break;
            }
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch(menuItem.getItemId()){
                case R.id.item_unrollCourse:
                    Log.d("cuonghx", "onMenuItemClick: " + tmpCourse.getId());
                    AuthenticationRemoteDataSource.getInstance(CheckInApplication.getInstance().getCheckInApi())
                            .unrollCourse(21, tmpCourse.getId())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnSubscribe(new Consumer<Disposable>() {
                                @Override
                                public void accept(Disposable disposable) throws Exception {

                                }
                            }).subscribe(new Consumer<BaseResponse>() {
                        @Override
                        public void accept(BaseResponse baseResponse) throws Exception {
                            if (baseResponse.getStatus() == 1){
                                parentAdapter.removeCourse(tmpCourse);
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            handleErrors(throwable);
                        }
                    });
                    return true;
                default:
                    return false;

            }
        }
        private void handleErrors(Throwable throwable) {
            if (throwable instanceof HttpException) {
                handleHttpExceptions((HttpException) throwable);
                return;
            } else if (throwable instanceof UnknownHostException) {
                Toast.makeText(this.itemView.getContext(), R.string.msg_check_internet_connection, Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this.itemView.getContext(),
                    R.string.msg_something_went_wrong,
                    Toast.LENGTH_SHORT)
                    .show();
        }

        private void handleHttpExceptions(HttpException httpException) {
            switch (httpException.code()) {
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    Toast.makeText(this.itemView.getContext(),
                            R.string.msg_wrong_email_or_password,
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(this.itemView.getContext(), httpException.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
