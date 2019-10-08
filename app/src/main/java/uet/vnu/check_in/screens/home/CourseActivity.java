package uet.vnu.check_in.screens.home;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
import uet.vnu.check_in.CheckInApplication;
import uet.vnu.check_in.R;
import uet.vnu.check_in.data.model.Course;
import uet.vnu.check_in.data.model.Student;
import uet.vnu.check_in.data.source.local.AuthenticationLocalDataSource;
import uet.vnu.check_in.data.source.remote.AuthenticationRemoteDataSource;
import uet.vnu.check_in.data.source.remote.api.response.BaseResponse;
import uet.vnu.check_in.data.source.remote.api.response.GetCourseByCourseResponse;
import uet.vnu.check_in.screens.BaseActivity;
import uet.vnu.check_in.screens.adapter.CourseAdapter;
import uet.vnu.check_in.screens.checkin.CheckInActivity;
import uet.vnu.check_in.util.StringUtils;

public class CourseActivity extends BaseActivity implements View.OnClickListener{

    private RecyclerView mRecyclerViewCourse;
    private CourseAdapter mCourseAdapterRecycleview;
    private Dialog dialogAddCourse;
    private EditText mEditextCode;
    private Course  tmpCourse;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_course;
    }

    @Override
    protected void initComponents(Bundle savedInstanceState) {
        setupView();
        getdata();
    }

    @Override
    protected void onStop() {
        mCompositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fab_add_course :
                dialogAddCourse = new Dialog(this);
                dialogAddCourse.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogAddCourse.setCancelable(false);
                dialogAddCourse.setContentView(R.layout.dialog_enroll_course);
                mEditextCode = dialogAddCourse.findViewById(R.id.edt_code_dialog);
                dialogAddCourse.findViewById(R.id.tv_enroll_dialog).setOnClickListener(this);
                dialogAddCourse.findViewById(R.id.tv_cancel_dialog).setOnClickListener(this);
                dialogAddCourse.show();
                break;
            case R.id.tv_enroll_dialog :
                String code = String.valueOf(mEditextCode.getText());
                if (StringUtils.checkNullOrEmpty(code)){
                    Toast.makeText(this, "Hãy nhập code và thử lại", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialogAddCourse.cancel();
                mCompositeDisposable.add(
                    AuthenticationRemoteDataSource.getInstance(CheckInApplication.getInstance().getCheckInApi())
                            .getCourseByCode(code)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnSubscribe(new Consumer<Disposable>() {
                                @Override
                                public void accept(Disposable disposable) throws Exception {

                                }
                            }).subscribe(new Consumer<GetCourseByCourseResponse>() {
                        @Override
                        public void accept(GetCourseByCourseResponse getCourseByCourseResponse) throws Exception {
                            if (getCourseByCourseResponse.getStatus() == 1){
                                tmpCourse = new Course(getCourseByCourseResponse.getCourseId(), getCourseByCourseResponse.getCourseName());
                                dialogAddCourse = new Dialog(CourseActivity.this);
                                dialogAddCourse.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                dialogAddCourse.setCancelable(false);
                                dialogAddCourse.setContentView(R.layout.dialog_confirm_enroll);
                                TextView textView = dialogAddCourse.findViewById(R.id.tv_name_course_dialog);
                                textView.setText(getCourseByCourseResponse.getCourseName());
                                dialogAddCourse.findViewById(R.id.tv_confirmenroll_dialog).setOnClickListener(CourseActivity.this);
                                dialogAddCourse.findViewById(R.id.tv_cancel_dialog).setOnClickListener(CourseActivity.this);
                                dialogAddCourse.show();
                            }else {
                                Toast.makeText(CourseActivity.this, "Code không chính xác nhập và thử lại", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            handleErrors(throwable);
                        }
                    })
                );
                break;
            case R.id.tv_cancel_dialog :
                dialogAddCourse.cancel();
                break;
            case R.id.tv_confirmenroll_dialog :
                dialogAddCourse.cancel();
                Student student = AuthenticationLocalDataSource.getInstance(CheckInApplication.getInstance().getSharedPrefsApi()).getLoggedStudent();
                if (student == null){
                    Toast.makeText(this, R.string.msg_something_went_wrong, Toast.LENGTH_SHORT).show();
                    return;
                }
                AuthenticationRemoteDataSource.getInstance(CheckInApplication.getInstance().getCheckInApi())
                        .enrollCourse(student.getId(),tmpCourse.getId())
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
                            Toast.makeText(CourseActivity.this, "Thành công!", Toast.LENGTH_SHORT).show();
                            mCourseAdapterRecycleview.addItem(tmpCourse);
                        }else{
                            Toast.makeText(CourseActivity.this, "Bạn đã tham gia vào khoá học rồi!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        handleErrors(throwable);
                    }
                });
                break;
        }
    }
    private void setupView(){
        mRecyclerViewCourse = findViewById(R.id.rcv_course);
        findViewById(R.id.fab_add_course).setOnClickListener(this);
        this.mCourseAdapterRecycleview = new CourseAdapter(this);

        mRecyclerViewCourse.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerViewCourse.setAdapter(this.mCourseAdapterRecycleview);
        mRecyclerViewCourse.addItemDecoration(new DividerItemDecoration(mRecyclerViewCourse.getContext(), DividerItemDecoration.VERTICAL));

        setTitle("Khoá học");
        androidx.appcompat.app.ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#32a5d8")));
        Toolbar actionBarToolbar = findViewById(R.id.action_bar);
        actionBarToolbar.getOverflowIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        if (actionBarToolbar != null) actionBarToolbar.setTitleTextColor(Color.WHITE);
    }
    private void getdata() {
        mCompositeDisposable.add(
            AuthenticationRemoteDataSource.getInstance(CheckInApplication.getInstance().getCheckInApi())
                    .enrolledCourse(AuthenticationLocalDataSource.getInstance(CheckInApplication.getInstance().getSharedPrefsApi()).getLoggedStudent().getId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(new Consumer<Disposable>() {
                        @Override
                        public void accept(Disposable disposable) throws Exception {

                        }
                    }).subscribe(new Consumer<List<Course>>() {
                @Override
                public void accept(List<Course> courses) throws Exception {
                    Log.d("cuonghx", "accept: " + courses.size() );
                    for (Course course: courses) {
                        mCourseAdapterRecycleview.addItem(course);
                    }
                }

            }, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) throws Exception {
                    Log.d("cuonghx", "accept: err" + throwable.getLocalizedMessage());
                    handleErrors(throwable);
                }
            })
        );
    }
    private void handleErrors(Throwable throwable) {
        if (throwable instanceof HttpException) {
            handleHttpExceptions((HttpException) throwable);
            return;
        } else if (throwable instanceof UnknownHostException) {
            Toast.makeText(this, R.string.msg_check_internet_connection, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(CourseActivity.this,
                R.string.msg_something_went_wrong,
                Toast.LENGTH_SHORT)
                .show();
    }

    private void handleHttpExceptions(HttpException httpException) {
        switch (httpException.code()) {
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                Toast.makeText(CourseActivity.this,
                        R.string.msg_wrong_email_or_password,
                        Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, httpException.getMessage(), Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
