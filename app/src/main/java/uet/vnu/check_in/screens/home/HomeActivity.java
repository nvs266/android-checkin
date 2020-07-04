package uet.vnu.check_in.screens.home;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.reactivestreams.Subscription;

import java.net.HttpURLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import jp.wasabeef.blurry.Blurry;
import retrofit2.HttpException;
import uet.vnu.check_in.CheckInApplication;
import uet.vnu.check_in.R;
import uet.vnu.check_in.data.model.Course;
import uet.vnu.check_in.data.source.local.AuthenticationLocalDataSource;
import uet.vnu.check_in.data.source.remote.AuthenticationRemoteDataSource;
import uet.vnu.check_in.screens.BaseActivity;
import uet.vnu.check_in.screens.adapter.CourseAdapter;
import uet.vnu.check_in.screens.adapter.CourseAdapterHome;
import uet.vnu.check_in.screens.checkin.CheckInActivity;
import uet.vnu.check_in.screens.checkin.CheckInRealTime;
import uet.vnu.check_in.screens.login.LoginActivity;
import uet.vnu.check_in.screens.login.UpdateActivity;

public class HomeActivity extends BaseActivity implements View.OnClickListener {

    private static final int PERMISSION_REQUESTS = 1240;

    private RecyclerView mRecyclerViewCourse;
    private CourseAdapterHome mCourseAdapterRecycleview;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_home;
    }

    @Override
    protected void initComponents(Bundle savedInstanceState) {
        setupView();
    }

    private void setupView() {
        findViewById(R.id.cv_start_course).setOnClickListener(this);
        findViewById(R.id.cv_start_checkin).setOnClickListener(this);
        findViewById(R.id.cv_start_profile).setOnClickListener(this);
        findViewById(R.id.cv_start_logout).setOnClickListener(this);
        mRecyclerViewCourse = findViewById(R.id.rcv_home);
        this.mCourseAdapterRecycleview = new CourseAdapterHome(this);
        mRecyclerViewCourse.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerViewCourse.setAdapter(this.mCourseAdapterRecycleview);
        mRecyclerViewCourse.addItemDecoration(new DividerItemDecoration(mRecyclerViewCourse.getContext(), DividerItemDecoration.VERTICAL));
        getdata();
        androidx.appcompat.app.ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#32a5d8")));
        Toolbar actionBarToolbar = findViewById(R.id.action_bar);
        actionBarToolbar.getOverflowIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        if (actionBarToolbar != null) actionBarToolbar.setTitleTextColor(Color.WHITE);
        setTitle("Home");

        ImageView courseiv = findViewById(R.id.iv_teacher_list_start);

        courseiv.post(new Runnable() {
            @Override
            public void run() {
                Blurry.with(HomeActivity.this)
                        .color(Color.argb(50, 100, 11, 100))
                        .capture(courseiv)
                        .into(courseiv);
            }
        });

        ImageView checkiniv = findViewById(R.id.iv_subject_start);

        checkiniv.post(new Runnable() {
            @Override
            public void run() {
                Blurry.with(HomeActivity.this)
                        .color(Color.argb(50, 100, 27, 0))
                        .capture(checkiniv)
                        .into(checkiniv);
            }
        });

        ImageView profileiv = findViewById(R.id.iv_time_table_start);

        profileiv.post(new Runnable() {
            @Override
            public void run() {
                Blurry.with(HomeActivity.this)
                        .color(Color.argb(50, 0, 73, 100))
                        .capture(profileiv)
                        .into(profileiv);
            }
        });

        ImageView exitiv = findViewById(R.id.iv_note);


        exitiv.post(new Runnable() {
            @Override
            public void run() {
                Blurry.with(HomeActivity.this)
                        .color(Color.argb(50, 15, 5, 15))
                        .capture(exitiv)
                        .into(exitiv);
            }
        });

    }

    private void getdata() {
        mCompositeDisposable.add(AuthenticationRemoteDataSource.getInstance(CheckInApplication.getInstance().getCheckInApi())
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
                            HomeActivity.this.subscribeToTopics(String.valueOf(course.getId()));
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

    private void subscribeToTopics(String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
    }

    private void unsubscribeToTopics(String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
    }

    private void handleErrors(Throwable throwable) {
        if (throwable instanceof HttpException) {
            handleHttpExceptions((HttpException) throwable);
            return;
        } else if (throwable instanceof UnknownHostException) {
            Toast.makeText(this, R.string.msg_check_internet_connection, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this,
                R.string.msg_something_went_wrong,
                Toast.LENGTH_SHORT)
                .show();
    }

    private void handleHttpExceptions(HttpException httpException) {
        switch (httpException.code()) {
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                Toast.makeText(this,
                        R.string.msg_wrong_email_or_password,
                        Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, httpException.getMessage(), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        HomeActivity.super.onBackPressed();
//        IsFinish("Bạn muốn đăng suất khỏi tài khoản ?");
    }

    @Override
    protected void onStop() {
        mCompositeDisposable.clear();
        super.onStop();
    }

    public void IsFinish(String alertmessage) {
        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                AuthenticationLocalDataSource.getInstance(
                                        CheckInApplication.getInstance().getSharedPrefsApi())
                                        .deleteStudent();

//                        android.os.Process.killProcess(android.os.Process.myPid());
                                HomeActivity.super.onBackPressed();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(alertmessage)
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.cv_start_course:
                Log.d("cuogh", "onClick: course");
                intent = new Intent(HomeActivity.this, CourseActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                break;
            case R.id.cv_start_checkin:
                Log.d("cuonghx", "onClick: 1" );
                if (!allPermissionsGranted()) {
                    getRuntimePermissions();
                } else {
                    Log.d("cuonghx", "onClick: ");
                    mNavigator.startActivity(CheckInRealTime.class, null);
                }
//                mNavigator.startActivity(CheckInRealTime.class, null);
                break;
            case R.id.cv_start_profile:
                intent = new Intent(HomeActivity.this, UpdateActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                Log.d("cuonghx", "onClick: update");
                break;
            case R.id.cv_start_logout:
                intent = new Intent(HomeActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                AuthenticationLocalDataSource.getInstance(
                        CheckInApplication.getInstance().getSharedPrefsApi())
                        .deleteStudent();
//            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                Log.d("cuonghx", "onClick: logout");
                if (mCourseAdapterRecycleview == null || mCourseAdapterRecycleview.getCollection() == null) {
                    return;
                }
                for (Course course: mCourseAdapterRecycleview.getCollection()) {
                    HomeActivity.this.unsubscribeToTopics(String.valueOf(course.getId()));
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (allPermissionsGranted()) {
            mNavigator.startActivity(CheckInRealTime.class, null);
        }
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info = this.getPackageManager()
                    .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private boolean isPermissionGranted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED;
    }
}
