package uet.vnu.check_in.screens.home;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import uet.vnu.check_in.CheckInApplication;
import uet.vnu.check_in.R;
import uet.vnu.check_in.data.source.local.AuthenticationLocalDataSource;
import uet.vnu.check_in.screens.BaseActivity;
import uet.vnu.check_in.screens.login.LoginActivity;
import uet.vnu.check_in.screens.login.UpdateActivity;

public class HomeActivity extends BaseActivity implements View.OnClickListener {



    @Override
    protected int getLayoutResource() {
        return R.layout.activity_home;
    }

    @Override
    protected void initComponents(Bundle savedInstanceState) {
        setupView();
    }
    private void setupView(){
        findViewById(R.id.bt_mycourse).setOnClickListener(this);
        findViewById(R.id.bt_checkin).setOnClickListener(this);
        findViewById(R.id.bt_update).setOnClickListener(this);
        findViewById(R.id.bt_logout).setOnClickListener(this);

        CheckInApplication.getInstance().subscribeMessageToTopic(true);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        IsFinish("Bạn muốn đăng suất khỏi tài khoản ?");
    }

    public void IsFinish(String alertmessage) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        AuthenticationLocalDataSource.getInstance(CheckInApplication.getInstance().getSharedPrefsApi()).deleteStudent();

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
        switch (view.getId()){
            case R.id.bt_mycourse:
                Log.d("cuogh", "onClick: course" );
                 intent = new Intent(HomeActivity.this, CourseActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                break;
            case R.id.bt_checkin:

                Log.d("cuonghx", "onClick: checkin");
                break;
            case R.id.bt_update:
                intent = new Intent(HomeActivity.this, UpdateActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                Log.d("cuonghx", "onClick: update");
                break;
            case R.id.bt_logout:
                Log.d("cuonghx", "onClick: logout");
                onBackPressed();
                break;
        }
    }
}
