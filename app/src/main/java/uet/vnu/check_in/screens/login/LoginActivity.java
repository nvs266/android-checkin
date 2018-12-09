package uet.vnu.check_in.screens.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.net.HttpURLConnection;
import java.net.UnknownHostException;

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
import uet.vnu.check_in.screens.BaseActivity;
import uet.vnu.check_in.util.StringUtils;

public class LoginActivity extends BaseActivity implements TextWatcher, View.OnClickListener {

    private TextInputLayout mInputLayoutEmail;
    private TextInputLayout mInputLayoutPassword;
    private TextInputEditText mInputEditTextEmail;
    private TextInputEditText mInputEditTextPassword;
    private ProgressBar mProgressBarLoading;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_login;
    }

    @Override
    protected void initComponents(Bundle savedInstanceState) {
        setupViews();
    }

    @Override
    protected void onStop() {
        mCompositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        mInputLayoutEmail.setError(null);
        mInputLayoutPassword.setError(null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_login:
                Student student = AuthenticationLocalDataSource
                        .getInstance(CheckInApplication.getInstance().getSharedPrefsApi())
                        .getLoggedStudent();
                if (student != null) {
                    Toast.makeText(this, student.getEmail() + " logged-in", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                login();
                break;
        }
    }

    public void showLoadingIndicator() {
        mNavigator.disableUserInteraction();
        mProgressBarLoading.setVisibility(View.VISIBLE);
    }

    public void hideLoadingIndicator() {
        mNavigator.enableUserInteraction();
        mProgressBarLoading.setVisibility(View.GONE);
    }

    private void login() {
        String email = String.valueOf(mInputEditTextEmail.getText());
        String password = String.valueOf(mInputEditTextPassword.getText());

        if (!validateEmailAndPassword(email, password)) {
            return;
        }

        mCompositeDisposable.add(
                AuthenticationRemoteDataSource
                        .getInstance(CheckInApplication.getInstance().getCheckInApi())
                        .loginByEmailAndPassword(email, password)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(Disposable disposable) {
                                showLoadingIndicator();
                            }
                        })
                        .subscribe(new Consumer<LoginResponse>() {
                            @Override
                            public void accept(LoginResponse loginResponse) {
                                hideLoadingIndicator();
                                switch (loginResponse.getStatus()) {
                                    case -1:
                                        Toast.makeText(LoginActivity.this,
                                                R.string.msg_wrong_email_or_password,
                                                Toast.LENGTH_SHORT).show();
                                        break;
                                    case 1:
                                        notifyLoginSuccessful(loginResponse.getStudent());
                                        break;
                                    default:
                                        Toast.makeText(LoginActivity.this,
                                                R.string.msg_something_went_wrong,
                                                Toast.LENGTH_SHORT)
                                                .show();
                                        break;
                                }
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {
                                hideLoadingIndicator();
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

        Toast.makeText(LoginActivity.this,
                R.string.msg_something_went_wrong,
                Toast.LENGTH_SHORT)
                .show();
    }

    private void handleHttpExceptions(HttpException httpException) {
        switch (httpException.code()) {
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                Toast.makeText(LoginActivity.this,
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

    private boolean validateEmailAndPassword(String email, String password) {
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
        }

        return validate;
    }

    private void setupViews() {
        mInputLayoutEmail = findViewById(R.id.til_email);
        mInputEditTextEmail = findViewById(R.id.tiet_email);
        mInputEditTextEmail.addTextChangedListener(this);

        mInputLayoutPassword = findViewById(R.id.til_password);
        mInputEditTextPassword = findViewById(R.id.tiet_password);
        mInputEditTextPassword.addTextChangedListener(this);

        findViewById(R.id.bt_login).setOnClickListener(this);

        mProgressBarLoading = findViewById(R.id.progress_circular_loading);
    }
}
