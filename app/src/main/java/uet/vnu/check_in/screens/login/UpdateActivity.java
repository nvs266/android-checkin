package uet.vnu.check_in.screens.login;

import android.os.Bundle;
import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.net.HttpURLConnection;
import java.net.UnknownHostException;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.widget.ImageViewCompat;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
import uet.vnu.check_in.CheckInApplication;
import uet.vnu.check_in.R;
import uet.vnu.check_in.data.source.local.AuthenticationLocalDataSource;
import uet.vnu.check_in.data.source.remote.AuthenticationRemoteDataSource;
import uet.vnu.check_in.data.source.remote.api.response.LoginResponse;
import uet.vnu.check_in.screens.BaseActivity;
import uet.vnu.check_in.util.StringUtils;

public class UpdateActivity extends BaseActivity implements View.OnClickListener, TextWatcher {

    private TextInputLayout mInputLayoutName;
    private TextInputLayout mInputLayoutBirthday;
    private TextInputEditText mInputEditName;
    private TextInputEditText mInputEditTextBirthday;
    private ProgressBar mProgressBarLoading;
    private AppCompatImageView mImageViewPicker;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_update;
    }

    @Override
    protected void initComponents(Bundle savedInstanceState) {
        this.setupView();
    }

    @Override
    protected void onStop() {
        mCompositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_continue :
                Log.d("cuoghx", "onClick: continue" );
                this.onclickContinue();
                break;
            case R.id.imv_picker_image :
                Log.d("cuoghx", "onClick: image" );
                break;
            default:
                break;
        }
    }
    private void setupView(){
        this.mInputLayoutName = findViewById(R.id.til_name);
        this.mInputEditName = findViewById(R.id.tiet_name);
        this.mInputLayoutBirthday = findViewById(R.id.til_birthday);
        this.mInputEditTextBirthday = findViewById(R.id.tiet_birthday);
        this.mProgressBarLoading = findViewById(R.id.progress_circular_loading);
        this.mImageViewPicker = findViewById(R.id.imv_picker_image);
        this.mImageViewPicker.setOnClickListener(this);

        this.mInputEditName.addTextChangedListener(this);
        this.mInputEditTextBirthday.addTextChangedListener(this);
        findViewById(R.id.bt_continue).setOnClickListener(this);
    }
    private void onclickContinue() {
        String name = String.valueOf(this.mInputEditName.getText());
        String birthday = String.valueOf(this.mInputEditTextBirthday.getText());
//        int student_id = AuthenticationLocalDataSource.getInstance(CheckInApplication.getInstance().getSharedPrefsApi()).getLoggedStudent().getId();

        if (!validateFormat(name, birthday)){
            return;
        }

        AuthenticationRemoteDataSource.getInstance(CheckInApplication.getInstance().getCheckInApi())
                .updateInformationStudent(name, birthday, "[[1,2,3]]", 1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        showLoadingIndicator();

                    }
                }).subscribe(new Consumer<LoginResponse>() {
            @Override
            public void accept(LoginResponse loginResponse) throws Exception {
                hideLoadingIndicator();
                switch (loginResponse.getStatus()){
                    case 1 :
                        Toast.makeText(UpdateActivity.this, "Success", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(UpdateActivity.this,
                                R.string.msg_something_went_wrong,
                                Toast.LENGTH_SHORT)
                                .show();
                        break;
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                hideLoadingIndicator();
                handleErrors(throwable);
            }
        });
    }
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        mInputLayoutName.setError(null);
        mInputLayoutBirthday.setError(null);
    }

    public void showLoadingIndicator() {
        mNavigator.disableUserInteraction();
        mProgressBarLoading.setVisibility(View.VISIBLE);
    }

    public void hideLoadingIndicator() {
        mNavigator.enableUserInteraction();
        mProgressBarLoading.setVisibility(View.GONE);
    }
    private void handleErrors(Throwable throwable) {
        if (throwable instanceof HttpException) {
            handleHttpExceptions((HttpException) throwable);
            return;
        } else if (throwable instanceof UnknownHostException) {
            Toast.makeText(this, R.string.msg_check_internet_connection, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(UpdateActivity.this,
                R.string.msg_something_went_wrong,
                Toast.LENGTH_SHORT)
                .show();
    }

    private void handleHttpExceptions(HttpException httpException) {
        switch (httpException.code()) {
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                Toast.makeText(UpdateActivity.this,
                        R.string.msg_wrong_email_or_password,
                        Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, httpException.getMessage(), Toast.LENGTH_SHORT).show();
                break;
        }
    }
    private boolean validateFormat(String name, String birthday) {
        boolean validate = true;

        if (StringUtils.checkNullOrEmpty(name)) {
            mInputLayoutName.setError(getString(R.string.msg_name_should_not_empty));
            validate = false;
        }

        if (StringUtils.checkNullOrEmpty(birthday)) {
            mInputLayoutBirthday.setError(getString(R.string.msg_birthday_should_not_empty));
            validate = false;
        }else if (!StringUtils.isValidDateFormat("dd/MM/yyyy", birthday)){
            mInputLayoutBirthday.setError(getString(R.string.msg_birthday_should_not_empty));
            validate = false;
        }

        return validate;
    }

}
