package uet.vnu.check_in.screens.checkin;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
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
import uet.vnu.check_in.data.source.remote.api.response.BaseResponse;
import uet.vnu.check_in.data.source.remote.api.response.CheckinResponse;
import uet.vnu.check_in.service.GPSTracker;
import uet.vnu.check_in.util.ArrayUtil;
import uet.vnu.check_in.util.ImageUtils;

public class CheckInRealTime extends CameraActivity implements ImageReader.OnImageAvailableListener, GoogleApiClient.OnConnectionFailedListener,  GoogleApiClient.ConnectionCallbacks, LocationListener {

    protected static final boolean SAVE_PREVIEW_BITMAP = false;

    private ResultsView resultsView;

    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Bitmap cropCopyBitmap = null;
    private ArrayList<ArrayList<Float>> vectors = new ArrayList<>();


    private long lastProcessingTimeMs;

    private static final int INPUT_SIZE = 160;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "embeddings";

    private LocationRequest mLocationRequest;
    protected GoogleApiClient mGoogleApiClient;
    private LocationGPS locationGPS;



    private static final String MODEL_FILE = "file:///android_asset/frozen_graph.pb";
    private static final String LABEL_FILE =
            "file:///android_asset/imagenet_comp_graph_label_strings.txt";


    private static final boolean MAINTAIN_ASPECT = true;

    private static Size DESIRED_PREVIEW_SIZE = new Size(720, 640);

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();


    private Integer sensorOrientation;
    private Classifier classifier;
    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;


    private BorderedText borderedText;

    private StorageReference mStorageRef;
    private boolean wait = false;

    private Dialog dialog;

    static boolean active = false;

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        if (toast != null) {
            toast.cancel();
        }
        mCompositeDisposable.clear();
        super.onStop();
        active = false;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.camera_connect_fragment;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        Log.e("Width", "" + width);
        Log.e("height", "" + height);

        DESIRED_PREVIEW_SIZE = new Size(height, width);
        return DESIRED_PREVIEW_SIZE;
    }

    private static final float TEXT_SIZE_DIP = 10;

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        final float textSizePx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        classifier =
                TensorFlowImageClassifier.create(
                        getAssets(),
                        MODEL_FILE,
                        LABEL_FILE,
                        INPUT_SIZE,
                        IMAGE_MEAN,
                        IMAGE_STD,
                        INPUT_NAME,
                        OUTPUT_NAME);
        mStorageRef = FirebaseStorage.getInstance().getReference();

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

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();
//        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

//        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);

        frameToCropTransform = ImageUtils.getTransformationMatrix(
                previewWidth, previewHeight,
                previewWidth, previewHeight,
                sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        addCallback(
                new OverlayView.DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        renderDebug(canvas);
                    }
                });
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
//        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
//            Log.e("Google API", "Connecting");
//            mGoogleApiClient.connect();
//        }
//        requestRender();
//        readyForNextImage();
        Log.d("cuonghx", "resume: ");
    }

    private Toast toast = null;

    @Override
    public synchronized void onPause() {
        super.onPause();
        Log.d("cuonghx", "onPause: ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("cuonghx", "restart: ");
    }

    @Override
    protected void processImage() {
        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);

        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap);
        }

        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        Log.d("cuonghx", "run: loop");
                        if (toast != null) {
                            toast.cancel();
                            toast = null;
                            requestRender();
                            readyForNextImage();
                            return;
                        }
                        GPSTracker gps = new GPSTracker(CheckInRealTime.this);
                        if (!gps.canGetLocation()) {
//                            toast = Toast.makeText(CheckInRealTime.this, "Không thể lấy vị trí!", Toast.LENGTH_SHORT);
//                            toast.show();
                            reqestLocationTurnOn();
                        } else if ( locationGPS == null ) {
                            toast = Toast.makeText(CheckInRealTime.this, "Xin chờ để lấy vị trí", Toast.LENGTH_SHORT);
                            toast.show();
                        } else if (locationGPS != null && locationGPS.longtitude == 0 &&  locationGPS.lattitude == 0) {
                            Log.d("cuonghx", "run: loop 0");
                            requestRender();
                            readyForNextImage();
                        } else {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ImageUtils.RotateBitmap(rgbFrameBitmap, 0).compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            Bitmap bitmap = Bitmap.createScaledBitmap(ImageUtils.getFaces(getApplicationContext(), baos.toByteArray()),
                                INPUT_SIZE, INPUT_SIZE, true);
                            classifier.recognizeImage(ImageUtils.RotateBitmap(bitmap, 0),getApplicationContext(), (issuccess, embemdings)->{
                                ArrayList<Float> arr = new ArrayList<>();
                                for (float f: embemdings) {
                                    arr.add(f);
                                }
                                Double dis = 10d;
//                              ArrayUtil.l2distance()
                                for (int i = 0; i < vectors.size() ;i++){
                                    Double tmp = ArrayUtil.l2distance(arr, vectors.get(i));
                                    if (tmp < dis && tmp > 0){
                                        dis = tmp;
                                    }
                                }

                                Log.d("cuonghx", "onPictureTaken: " +  dis);

                                if (dis < 0.7){
                                    Log.d("cuonghx", "onCheckedChanged: start checkin" + locationGPS.lattitude);
                                    Log.d("cuonghx", "onCheckedChanged: start checkin" + locationGPS.longtitude);
                                    success(locationGPS.lattitude, locationGPS.longtitude, ImageUtils.RotateBitmap(rgbFrameBitmap, 270));
                                } else {
                                    if (toast != null) {
                                        toast.cancel();
                                        toast = null;
                                    }
                                    if (toast == null || toast.getView().getWindowVisibility() != View.VISIBLE) {
                                        toast = Toast.makeText(CheckInRealTime.this, "Xin chờ !", Toast.LENGTH_SHORT);
                                        toast.show();
                                    }

                                    requestRender();
                                    readyForNextImage();
                                 }
                            });
                        }

                    }
                });
    }

    private void success(double lat , double longtitude, Bitmap image){

        int studentID = AuthenticationLocalDataSource.getInstance(CheckInApplication.getInstance().getSharedPrefsApi()).getLoggedStudent().getId();
        mCompositeDisposable.add(
            AuthenticationRemoteDataSource.getInstance(CheckInApplication.getInstance().getCheckInApi())
                    .checkin(studentID, longtitude, lat)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(new Consumer<Disposable>() {
                        @Override
                        public void accept(Disposable disposable) throws Exception {

                        }
                    }).subscribe(new Consumer<CheckinResponse>() {
                @Override
                public void accept(CheckinResponse checkinResponse) throws Exception {

                    switch (checkinResponse.getStatus()){
                        case -1:
                            if (dialog != null && dialog.isShowing()) {
                                return;
                            }
                            dialog = new Dialog(CheckInRealTime.this);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setCancelable(false);
                            dialog.setContentView(R.layout.dialog_respone_checkin);
                            TextView textView = dialog.findViewById(R.id.tv);
                            textView.setText("Không thể checkin với vị trí này");
                            dialog.findViewById(R.id.tv_ok_dialog).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.cancel();
                                    CheckInRealTime.this.onBackPressed();
                                }
                            });

                            if (CheckInRealTime.active){
                                dialog.show();
                            }
                            break;
                        case -2:
                            if (dialog != null && dialog.isShowing()) {
                                return;
                            }
                            dialog = new Dialog(CheckInRealTime.this);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setCancelable(false);
                            dialog.setContentView(R.layout.dialog_respone_checkin);
                            dialog.findViewById(R.id.tv_ok_dialog).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.cancel();
                                    CheckInRealTime.this.onBackPressed();
                                }
                            });
                            dialog.show();
                            break;
                        case 1:
                            if (dialog != null && dialog.isShowing()) {
                                return;
                            }
    //                        wait = !wait;
                            dialog = new Dialog(CheckInRealTime.this);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setCancelable(false);
                            dialog.setContentView(R.layout.dialog_respone_checkin_sucess);
                            TextView tv = dialog.findViewById(R.id.tv_nameCourse);
                            String text = checkinResponse.getCourseName() != null ? checkinResponse.getCourseName() : Integer.toString(checkinResponse.getCourseID());
                            tv.setText(text);
                            dialog.findViewById(R.id.tv_ok_dialog).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.cancel();
                                    CheckInRealTime.this.onBackPressed();
                                }
                            });
                            dialog.show();
//                            Toast.makeText(CheckInRealTime.this, "Success!", Toast.LENGTH_SHORT).show();
                            putPhotoToStorage(image, studentID, checkinResponse.getCourseID(), checkinResponse.getInsertId());
                            break;
                    }
                }
            })
        );
    }
    private void reqestLocationTurnOn(){
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
//        }
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog_gps_setting);
            dialog.findViewById(R.id.bt_cancel_dialog).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.cancel();
                    Log.d("cuonghx cancel", "onClick: ");
                    wait = !wait;
                }
            });
            dialog.findViewById(R.id.bt_setting_dialog).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.cancel();
                    Log.d("cuonghx cancel", "onClick: ");
                    wait = !wait;
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });

            dialog.show();
        }
    }

    private void putPhotoToStorage(Bitmap image, int studentId, int courseId, int insertId){
        Date currentTime = Calendar.getInstance().getTime();
        int month = currentTime.getMonth() + 1;
        StorageReference photo = mStorageRef.child("course_"
                + courseId + "/"
                + studentId + "_"
                + currentTime.getDate() + "-"
                + (month) + ".jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 25, baos);
        byte[] data = baos.toByteArray();

        photo.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!urlTask.isSuccessful());
                Uri downloadUrl = urlTask.getResult();
                Log.d("cuonghx", "onSuccess: "+ downloadUrl.toString());
                mCompositeDisposable.add(
                AuthenticationRemoteDataSource.getInstance(CheckInApplication.getInstance().getCheckInApi())
                        .pushPhoto(downloadUrl.toString(), insertId)
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
                }));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });
        wait = !wait;
//        onBackPressed();
    }
    private void handleErrors(Throwable throwable) {
        if (throwable instanceof HttpException) {
            handleHttpExceptions((HttpException) throwable);
            return;
        } else if (throwable instanceof UnknownHostException) {
            Toast.makeText(this, R.string.msg_check_internet_connection, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, R.string.msg_something_went_wrong,
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
    private int count = 1;
    @Override
    public void onSetDebug(boolean debug) {
        classifier.enableStatLogging(debug);
    }

    private void renderDebug(final Canvas canvas) {
        if (!isDebug()) {
            return;
        }
        final Bitmap copy = cropCopyBitmap;
        if (copy != null) {
            final Matrix matrix = new Matrix();
            final float scaleFactor = 2;
            matrix.postScale(scaleFactor, scaleFactor);
            matrix.postTranslate(
                    canvas.getWidth() - copy.getWidth() * scaleFactor,
                    canvas.getHeight() - copy.getHeight() * scaleFactor);
            canvas.drawBitmap(copy, matrix, new Paint());

            final Vector<String> lines = new Vector<String>();
            if (classifier != null) {
                String statString = classifier.getStatString();
                String[] statLines = statString.split("\n");
                for (String line : statLines) {
                    lines.add(line);
                }
            }

            lines.add("Frame: " + previewWidth + "x" + previewHeight);
            lines.add("Crop: " + copy.getWidth() + "x" + copy.getHeight());
            lines.add("View: " + canvas.getWidth() + "x" + canvas.getHeight());
            lines.add("Rotation: " + sensorOrientation);
            lines.add("Inference time: " + lastProcessingTimeMs + "ms");

            borderedText.drawLines(canvas, 10, canvas.getHeight() - 10, lines);
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        } else {
            Log.d("cuonghx", "onLocationChanged: 1");
            requestRender();
            readyForNextImage();
            this.locationGPS = new LocationGPS(location.getLatitude(),location.getLongitude());
        }
    }

    @Override
    public synchronized void onDestroy() {
        super.onDestroy();
        if (toast != null) {
            toast.cancel();
        }
        mGoogleApiClient.disconnect();
        classifier.close();
        classifier = null;

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        locationGPS = new LocationGPS(location.getLatitude(), location.getLongitude());
        Log.d("cuonghx", "onLocationChanged: " + locationGPS.toString());
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }




    private class LocationGPS{
        public double lattitude;
        public double longtitude;

        public LocationGPS(double latitude, double longtitude){
            this.lattitude = latitude;
            this.longtitude = longtitude;
        }
    }
}
