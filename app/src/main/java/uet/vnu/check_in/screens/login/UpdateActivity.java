package uet.vnu.check_in.screens.login;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
import uet.vnu.check_in.CheckInApplication;
import uet.vnu.check_in.R;
import uet.vnu.check_in.classfier.Classifier;
import uet.vnu.check_in.classfier.TensorFlowImageClassifier;
import uet.vnu.check_in.data.model.Student;
import uet.vnu.check_in.data.source.local.AuthenticationLocalDataSource;
import uet.vnu.check_in.data.source.remote.AuthenticationRemoteDataSource;
import uet.vnu.check_in.data.source.remote.api.response.LoginResponse;
import uet.vnu.check_in.screens.BaseActivity;
import uet.vnu.check_in.screens.home.HomeActivity;
import uet.vnu.check_in.util.ImageUtils;
import uet.vnu.check_in.util.StringUtils;

public class UpdateActivity extends BaseActivity implements View.OnClickListener, TextWatcher {

    private TextInputLayout mInputLayoutName;
    private TextInputLayout mInputLayoutBirthday;
    private TextInputEditText mInputEditName;
    private TextInputEditText mInputEditTextBirthday;
    private ProgressBar mProgressBarLoading;
    private AppCompatImageView mImageViewPicker;

    private Bitmap image1;
    private Bitmap image2;
    private Bitmap image3;

    private StorageReference mStorageRef;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private static final int RC_CAMERA = 3000;

    private ArrayList<ArrayList<Float>> vectors = new ArrayList<>();

    private static final int INPUT_SIZE = 160;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "embeddings";

    private ArrayList<Float> vector1 = new ArrayList<>();
    private ArrayList<Float> vector2 = new ArrayList<>();
    private ArrayList<Float> vector3 = new ArrayList<>();


    private static final String MODEL_FILE = "file:///android_asset/frozen_graph.pb";
    private static final String LABEL_FILE = "file:///android_asset/imagenet_comp_graph_label_strings.txt";

    private Classifier classifier;

    private Boolean check = true;

    private ArrayList<String> imageURL = new ArrayList<>();

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
            case R.id.aciv_img1 :
                this.mImageViewPicker = findViewById(R.id.aciv_img1);
                Log.d("cuoghx", "onClick: image" );
                captureImage();
                break;
            case R.id.aciv_img2 :
                this.mImageViewPicker = findViewById(R.id.aciv_img2);
                Log.d("cuoghx", "onClick: image" );
                captureImage();
                break;
            case R.id.aciv_img3 :
                this.mImageViewPicker = findViewById(R.id.aciv_img3);
                Log.d("cuoghx", "onClick: image" );
                captureImage();
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == RC_CAMERA) {
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureImage();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void captureImage() {
        ImagePicker.cameraOnly().start(this);
//        ImagePicker.create(this).showCamera(true).limit(3).returnMode(ReturnMode.NONE).start();
    }
    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            // Get a list of picked images
            List<Image> images = ImagePicker.getImages(data);
            Image image = ImagePicker.getFirstImageOrNull(data);
            File imgFile = new  File(images.get(0).getPath());

            for (Image i: images) {
                Bitmap src = BitmapFactory.decodeFile(i.getPath());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                src.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                Bitmap bitmap = Bitmap.createScaledBitmap(ImageUtils.getFaces(getApplicationContext(), baos.toByteArray()),
                        INPUT_SIZE, INPUT_SIZE, true);
                if (check){
                    vectors = new ArrayList<>();
                    check = false;
                }

                classifier.recognizeImage(bitmap,getApplicationContext(), (issuccess, embemdings)->{
                    ArrayList<Float> arr = new ArrayList<>();
                    for (float f: embemdings) {
                        arr.add(f);
                    }
                    vectors.add(arr);
                });
            }

            switch (mImageViewPicker.getId()){
                case R.id.aciv_img1:
                    AuthenticationLocalDataSource.getInstance(CheckInApplication.getInstance().getSharedPrefsApi())
                            .saveImage1(getBase64String(image.getPath()));
                    break;
                case R.id.aciv_img2:
                    AuthenticationLocalDataSource.getInstance(CheckInApplication.getInstance().getSharedPrefsApi())
                            .saveImage2(getBase64String(image.getPath()));
                    break;
                case R.id.aciv_img3:
                    AuthenticationLocalDataSource.getInstance(CheckInApplication.getInstance().getSharedPrefsApi())
                            .saveImage3(getBase64String(image.getPath()));
                    break;
            }

            Picasso.get().load(imgFile).into(mImageViewPicker);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    public Uri getImageUri(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        if (inImage == null) { return null; }
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private String getBase64String(String  path) {

        Bitmap bitmap = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private Bitmap decodeBase64(String completeImageData) {

        String imageDataBytes = completeImageData.substring(completeImageData.indexOf(",")+1);

        InputStream stream = new ByteArrayInputStream(Base64.decode(imageDataBytes.getBytes(), Base64.DEFAULT));

        Bitmap bitmap = BitmapFactory.decodeStream(stream);

        return rotateBitmap(bitmap, 270);
    }

    public Bitmap rotateBitmap(Bitmap source, float angle) {
        if ( source == null ) {
            return source;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private void setupView(){
        this.mInputLayoutName = findViewById(R.id.til_name);
        this.mInputEditName = findViewById(R.id.tiet_name);
        this.mInputLayoutBirthday = findViewById(R.id.til_birthday);
        this.mInputEditTextBirthday = findViewById(R.id.tiet_birthday);
        this.mProgressBarLoading = findViewById(R.id.progress_circular_loading);

        findViewById(R.id.aciv_img1).setOnClickListener(this);
        findViewById(R.id.aciv_img2).setOnClickListener(this);
        findViewById(R.id.aciv_img3).setOnClickListener(this);

        this.mStorageRef = FirebaseStorage.getInstance().getReference();
        this.mInputEditName.addTextChangedListener(this);
        this.mInputEditTextBirthday.addTextChangedListener(this);
        findViewById(R.id.bt_continue).setOnClickListener(this);

        androidx.appcompat.app.ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#32a5d8")));
        Toolbar actionBarToolbar = findViewById(R.id.action_bar);
        actionBarToolbar.getOverflowIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        if (actionBarToolbar != null) actionBarToolbar.setTitleTextColor(Color.WHITE);
        setTitle("Update");

        Student student = AuthenticationLocalDataSource.getInstance(CheckInApplication.getInstance().getSharedPrefsApi()).getLoggedStudent();
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(student.getVectors());
            JSONArray array = (JSONArray)obj;
            for (int i = 0; i < array.size(); i++) {
                JSONArray a = (JSONArray)array.get(i);
                ArrayList<Float> vector = new ArrayList<>();
                for (int j = 0 ;j < a.size();j++){
                    vector.add(Float.parseFloat(a.get(j).toString()));
                }
                vectors.add(vector);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Bitmap avatar1 = decodeBase64(AuthenticationLocalDataSource.getInstance(CheckInApplication.getInstance()
                .getSharedPrefsApi())
                .getImage1());
        AppCompatImageView imageView1 = findViewById(R.id.aciv_img1);

        Picasso.get().load(getImageUri(avatar1)).placeholder(R.drawable.avatar).into(imageView1);

        Bitmap avatar2 = decodeBase64(AuthenticationLocalDataSource.getInstance(CheckInApplication.getInstance()
                .getSharedPrefsApi())
                .getImage2());
        AppCompatImageView imageView2 = findViewById(R.id.aciv_img2);
        Picasso.get().load(getImageUri(avatar2)).placeholder(R.drawable.avatar).into(imageView2);

        Bitmap avatar3 = decodeBase64(AuthenticationLocalDataSource.getInstance(CheckInApplication.getInstance()
                .getSharedPrefsApi())
                .getImage3());
        AppCompatImageView imageView3 = findViewById(R.id.aciv_img3);
        Picasso.get().load(getImageUri(avatar3)).placeholder(R.drawable.avatar).into(imageView3);


        this.mInputEditName.setText(student.getName());
        mInputEditTextBirthday.setText(student.getBirthday());

        classifier = TensorFlowImageClassifier.create(getAssets(),
                MODEL_FILE,
                LABEL_FILE,
                INPUT_SIZE,
                IMAGE_MEAN,
                IMAGE_STD,
                INPUT_NAME,
                OUTPUT_NAME);
    }
    private void onclickContinue() {
        String name = String.valueOf(this.mInputEditName.getText());
        String birthday = String.valueOf(this.mInputEditTextBirthday.getText());

        if (!validateFormat(name, birthday)){
            return;
        }

        Student student = AuthenticationLocalDataSource.getInstance(CheckInApplication.getInstance().getSharedPrefsApi()).getLoggedStudent();
        ArrayList<ArrayList<Float>> vectors = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(student.getVectors());
            JSONArray array = (JSONArray)obj;
            for (int i = 0; i < array.size(); i++) {
                JSONArray a = (JSONArray)array.get(i);
                ArrayList<Float> vector = new ArrayList<>();
                for (int j = 0 ;j < a.size();j++){
                    vector.add(Float.parseFloat(a.get(j).toString()));
                }
                vectors.add(vector);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (this.vectors.size() == 0 && vectors.size() > 0){
            Toast.makeText(this, "Phải chọn ảnh mới để tiếp tục", Toast.LENGTH_SHORT).show();
            return;
        }else if (this.vectors.size() == 0) {
            Toast.makeText(this, "Phải chọn ít nhất 1 ảnh.", Toast.LENGTH_SHORT).show();
            return;
        }
        mCompositeDisposable.add(
            AuthenticationRemoteDataSource.getInstance(CheckInApplication.getInstance().getCheckInApi())
                    .updateInformationStudent(name, birthday, this.vectors.toString(), student.getId())
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
                            if (image1 != null){
                                StorageReference photo1 = mStorageRef.child("image_avatar/" + student.getId() + "1.jpg");
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                ImageUtils.RotateBitmap(image1, 270).compress(Bitmap.CompressFormat.JPEG, 50, baos);
                                byte[] data = baos.toByteArray();
                                photo1.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                                        while (!urlTask.isSuccessful());
                                        Uri downloadUrl = urlTask.getResult();
                                        Log.d("cuonghx", "onSuccess: "+ downloadUrl.toString());
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                    }
                                });
                            }
                            if(image2 != null){
                                StorageReference photo2 = mStorageRef.child("image_avatar/" + student.getId() + "2.jpg");
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                ImageUtils.RotateBitmap(image2, 270).compress(Bitmap.CompressFormat.JPEG, 50, baos);
                                byte[] data = baos.toByteArray();
                                photo2.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                                        while (!urlTask.isSuccessful());
                                        Uri downloadUrl = urlTask.getResult();
                                        Log.d("cuonghx", "onSuccess: "+ downloadUrl.toString());
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                    }
                                });
                            }
                            if(image3 != null){
                                StorageReference photo3 = mStorageRef.child("image_avatar/" + student.getId() + "3.jpg");
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                ImageUtils.RotateBitmap(image3, 270).compress(Bitmap.CompressFormat.JPEG, 50, baos);
                                byte[] data = baos.toByteArray();
                                photo3.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                                        while (!urlTask.isSuccessful());
                                        Uri downloadUrl = urlTask.getResult();
                                        Log.d("cuonghx", "onSuccess: "+ downloadUrl.toString());
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                    }
                                });
                            }
                            Toast.makeText(UpdateActivity.this, "Success", Toast.LENGTH_SHORT).show();
                            AuthenticationLocalDataSource.getInstance(CheckInApplication.getInstance().getSharedPrefsApi()).saveStudent(
                                    new Student(student.getId(), name, birthday, student.getEmail(), UpdateActivity.this.vectors.toString(), student.getOS()
                                            , student.getDeviceToken()));
                            Intent intent = new Intent(UpdateActivity.this, HomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(intent);
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
            })
        );
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
            mInputLayoutBirthday.setError(getString(R.string.birthday_should_match_dateformat));
            validate = false;
        }

        return validate;
    }

}
