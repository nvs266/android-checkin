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

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
import uet.vnu.check_in.CheckInApplication;
import uet.vnu.check_in.R;
import uet.vnu.check_in.data.model.Student;
import uet.vnu.check_in.data.source.local.AuthenticationLocalDataSource;
import uet.vnu.check_in.data.source.remote.AuthenticationRemoteDataSource;
import uet.vnu.check_in.data.source.remote.api.response.LoginResponse;
import uet.vnu.check_in.data.source.remote.api.response.RegisterResponse;
import uet.vnu.check_in.screens.BaseActivity;
import uet.vnu.check_in.util.StringUtils;

public class RegisterActivity extends BaseActivity implements View.OnClickListener, TextWatcher {

    private TextInputLayout mInputLayoutEmail;
    private TextInputLayout mInputLayoutPassword;
    private TextInputLayout mInputLayoutConfirmPassword;
    private TextInputEditText mInputEditTextEmail;
    private TextInputEditText mInputEditTextPassword;
    private TextInputEditText mInputEditTextConfirmPassword;
    private ProgressBar mProgressBarLoading;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_register;
    }

    @Override
    protected void initComponents(Bundle savedInstanceState) {
        this.setUpView();
    }

    @Override
    protected void onStop() {
        mCompositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_register:
                Toast.makeText(this, "login", Toast.LENGTH_SHORT).show();
                register();
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        mInputLayoutEmail.setError(null);
        mInputLayoutPassword.setError(null);
        mInputLayoutConfirmPassword.setError(null);
    }

    public void showLoadingIndicator() {
        mNavigator.disableUserInteraction();
        mProgressBarLoading.setVisibility(View.VISIBLE);
    }

    public void hideLoadingIndicator() {
        mNavigator.enableUserInteraction();
        mProgressBarLoading.setVisibility(View.GONE);
    }

    private void register() {
        String email = String.valueOf(mInputEditTextEmail.getText());
        String password = String.valueOf(mInputEditTextPassword.getText());
        String confirmPassword = String.valueOf(mInputEditTextConfirmPassword.getText());
        if (!validateEmailAndPassword(email, password, confirmPassword)) {
            return;
        }
        AuthenticationRemoteDataSource.getInstance(CheckInApplication.getInstance().getCheckInApi())
                .registerByEmailAndPassword(email, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        showLoadingIndicator();
                    }
                }).subscribe(new Consumer<RegisterResponse>() {
            @Override
            public void accept(RegisterResponse registerResponse) throws Exception {
                hideLoadingIndicator();
                switch (registerResponse.getStatus()) {
                    case -1:
                        Toast.makeText(RegisterActivity.this,
                                "Email đã tồn tại!",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        notifyLoginSuccessful(registerResponse.getStudent());
                        break;
                    default:
                        Toast.makeText(RegisterActivity.this,
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

    private void handleErrors(Throwable throwable) {
        if (throwable instanceof HttpException) {
            handleHttpExceptions((HttpException) throwable);
            return;
        } else if (throwable instanceof UnknownHostException) {
            Toast.makeText(this, R.string.msg_check_internet_connection, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(RegisterActivity.this,
                R.string.msg_something_went_wrong,
                Toast.LENGTH_SHORT)
                .show();
    }

    private void handleHttpExceptions(HttpException httpException) {
        switch (httpException.code()) {
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                Toast.makeText(RegisterActivity.this,
                        R.string.msg_wrong_email_or_password,
                        Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, httpException.getMessage(), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void notifyLoginSuccessful(Student student) {
        Toast.makeText(this, R.string.msg_login_successful, Toast.LENGTH_SHORT).show();
        AuthenticationLocalDataSource
                .getInstance(CheckInApplication.getInstance()
                        .getSharedPrefsApi())
                .saveStudent(student);
    }

    private void setUpView() {
        mInputLayoutEmail = findViewById(R.id.til_email);
        mInputEditTextEmail = findViewById(R.id.tiet_email);
        mInputEditTextEmail.addTextChangedListener(this);

        mInputLayoutPassword = findViewById(R.id.til_password);
        mInputEditTextPassword = findViewById(R.id.tiet_password);
        mInputEditTextPassword.addTextChangedListener(this);

        mInputLayoutConfirmPassword = findViewById(R.id.til_confirm_password);
        mInputEditTextConfirmPassword = findViewById(R.id.tiet_confirm_password);
        mInputEditTextConfirmPassword.addTextChangedListener(this);

        findViewById(R.id.bt_comeback_login).setOnClickListener(this);
        findViewById(R.id.bt_register).setOnClickListener(this);

        mProgressBarLoading = findViewById(R.id.progress_circular_loading);
    }

    private boolean validateEmailAndPassword(String email, String password, String confirmPassword) {
        boolean validate = true;

        if (StringUtils.checkNullOrEmpty(email)) {
            mInputLayoutEmail.setError(getString(R.string.msg_email_should_not_empty));
            validate = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mInputLayoutEmail.setError(getString(R.string.msg_email_not_match));
            validate = false;
        }

        if (StringUtils.checkNullOrEmpty(password)) {
            mInputLayoutPassword.setError(getString(R.string.msg_password_should_not_empty));
            validate = false;
        } else if (!password.equals(confirmPassword)) {
            mInputLayoutConfirmPassword.setError(getString(R.string.msg_confimpassword_should_match_password));
        }

        return validate;
    }

}
