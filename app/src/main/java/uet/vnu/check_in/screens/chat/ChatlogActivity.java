package uet.vnu.check_in.screens.chat;

import android.os.Bundle;
import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.HttpURLConnection;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
import uet.vnu.check_in.CheckInApplication;
import uet.vnu.check_in.R;
import uet.vnu.check_in.data.model.ChatLog;
import uet.vnu.check_in.data.model.Course;
import uet.vnu.check_in.data.source.remote.AuthenticationRemoteDataSource;
import uet.vnu.check_in.data.source.remote.api.response.BaseResponse;
import uet.vnu.check_in.screens.BaseActivity;
import uet.vnu.check_in.screens.adapter.ChatLogAdapter;
import uet.vnu.check_in.screens.home.CourseActivity;

public class ChatlogActivity extends BaseActivity implements View.OnClickListener {

    private ChatLogAdapter mChatlogAdapter;
    private RecyclerView mRecycleView;
    private EditText mEditTextChat;
    private ImageView mImageViewSender;
    private Course currentCourse;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_chatlog;
    }

    @Override
    protected void initComponents(Bundle savedInstanceState) {
        setupView();
    }
    private void setupView(){
        currentCourse = new Course(getIntent().getIntExtra("course_id", 0), getIntent().getStringExtra("course_name"));
        setTitle(currentCourse.getName());
        Log.d("cuonghx", "setupView: " + currentCourse.getId());
        mRecycleView = findViewById(R.id.rcv_text_chat);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecycleView.setLayoutManager(linearLayoutManager);
        mChatlogAdapter = new ChatLogAdapter(this);
        mRecycleView.setAdapter(mChatlogAdapter);
        addObsever();
        mEditTextChat = findViewById(R.id.et_text_chat);
        mImageViewSender = findViewById(R.id.bt_sent_chat);
        mEditTextChat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0){
                    mImageViewSender.setImageResource(R.drawable.ic_sent_chat);
                    mImageViewSender.setClickable(true);
                }else{
                    mImageViewSender.setImageResource(R.drawable.ic_send);
                    mImageViewSender.setClickable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mImageViewSender.setOnClickListener(this);
        mImageViewSender.setClickable(false);
    }

    private void addObsever() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Messenger");
        myRef.child("Course_" + currentCourse.getId()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ChatLog chatLog = dataSnapshot.getValue(ChatLog.class);
//                Log.d("cuonghx", "onChildAdded: " + chatLog.isTeacher);
                mChatlogAdapter.addItem(chatLog);
                mRecycleView.scrollToPosition(mChatlogAdapter.getItemCount() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_sent_chat:
                Log.d("cuonghx", "onClick: send" );
                String message = String.valueOf(mEditTextChat.getText());
                mEditTextChat.setText("");
                AuthenticationRemoteDataSource.getInstance(CheckInApplication.getInstance().getCheckInApi())
                        .sendMessage(currentCourse.getId(), message, String.valueOf(21), "0")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(Disposable disposable) throws Exception {

                            }
                        }).subscribe(new Consumer<BaseResponse>() {
                    @Override
                    public void accept(BaseResponse baseResponse) throws Exception {
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
    private void handleErrors(Throwable throwable) {
        if (throwable instanceof HttpException) {
            handleHttpExceptions((HttpException) throwable);
            return;
        } else if (throwable instanceof UnknownHostException) {
            Toast.makeText(this, R.string.msg_check_internet_connection, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(ChatlogActivity.this,
                R.string.msg_something_went_wrong,
                Toast.LENGTH_SHORT)
                .show();
    }

    private void handleHttpExceptions(HttpException httpException) {
        switch (httpException.code()) {
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                Toast.makeText(ChatlogActivity.this,
                        R.string.msg_wrong_email_or_password,
                        Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, httpException.getMessage(), Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
