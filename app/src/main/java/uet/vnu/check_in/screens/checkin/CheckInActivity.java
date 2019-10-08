package uet.vnu.check_in.screens.checkin;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
import uet.vnu.check_in.CheckInApplication;
import uet.vnu.check_in.R;
import uet.vnu.check_in.classfier.CameraSourcePreview;
import uet.vnu.check_in.classfier.Classifier;
import uet.vnu.check_in.classfier.GraphicOverlay;
import uet.vnu.check_in.classfier.TensorFlowImageClassifier;
import uet.vnu.check_in.data.model.Student;
import uet.vnu.check_in.data.source.local.AuthenticationLocalDataSource;
import uet.vnu.check_in.data.source.remote.AuthenticationRemoteDataSource;
import uet.vnu.check_in.data.source.remote.api.response.BaseResponse;
import uet.vnu.check_in.data.source.remote.api.response.CheckinResponse;
import uet.vnu.check_in.data.source.remote.api.response.LoginResponse;
import uet.vnu.check_in.screens.BaseActivity;
import uet.vnu.check_in.screens.login.UpdateActivity;
import uet.vnu.check_in.service.GPSTracker;
import uet.vnu.check_in.util.ArrayUtil;
import uet.vnu.check_in.util.ImageUtils;
import uet.vnu.check_in.util.StringUtils;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CheckInActivity extends BaseActivity implements ActivityCompat.OnRequestPermissionsResultCallback, CompoundButton.OnCheckedChangeListener, CameraSource.PictureCallback {

    private static final String TAG = "CHECKIN:LivePreview";
    private static final int PERMISSION_REQUESTS = 1;

    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;

    private static final int INPUT_SIZE = 160;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "embeddings";

    private ArrayList<ArrayList<Float>> vectors = new ArrayList<>();

    private LocationManager mLocationManager;
    private StorageReference mStorageRef;

    private LocationRequest mLocationRequest;
    protected GoogleApiClient mGoogleApiClient;


    private static final String MODEL_FILE = "file:///android_asset/facenet.pb";
    private static final String LABEL_FILE =
            "file:///android_asset/imagenet_comp_graph_label_strings.txt";

    private Classifier classifier;

//    private final LocationListener mLocationListener = new LocationListener() {
//        @Override
//        public void onLocationChanged(final Location location) {
//            Log.d(TAG, "onLocationChanged: long" + location.getLongitude() );
//            Log.d(TAG, "onLocationChanged: lat" + location.getLatitude());
//        }
//
//        @Override
//        public void onStatusChanged(String s, int i, Bundle bundle) {
//
//        }
//
//        @Override
//        public void onProviderEnabled(String s) {
//
//        }
//
//        @Override
//        public void onProviderDisabled(String s) {
//
//        }
//    };

    private Camera.PreviewCallback  previewCallback= new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data,Camera cam)
        {
            Camera.Size previewSize = cam.getParameters().getPreviewSize();
            YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21,previewSize.width,previewSize.height, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0,0,previewSize.width,previewSize.height),80,baos);
            byte[] jdata = baos.toByteArray();
            Bitmap bitmap = BitmapFactory.decodeByteArray(jdata,0,jdata.length);
        }
    };

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_check_in;
    }

    @Override
    protected void initComponents(Bundle savedInstanceState) {
        preview = findViewById(R.id.preview);
        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }
        graphicOverlay = findViewById(R.id.faceOverlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }

//        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//
//        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, mLocationListener);

        ToggleButton facingSwitch = findViewById(R.id.facingswitch);
//        facingSwitch.setOnCheckedChangeListener(this);
        facingSwitch.setOnClickListener(v -> {
//            DialogUtils.showProgressDialog(FaceTrackerActivity.this);
            cameraSource.takePicture(null, this);
        });
        mStorageRef = FirebaseStorage.getInstance().getReference();

        if (allPermissionsGranted()) {
            createCameraSource();
        } else {
            getRuntimePermissions();
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "Set facing");
        if (cameraSource != null) {
            if (isChecked) {
//                cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
            } else {
//                cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
            }
        }
        preview.stop();
        startCameraSource();
    }

    private void createCameraSource() {
//        if (cameraSource == null) {
//            cameraSource = new CameraSource(this, graphicOverlay);
//        }
//
//        Log.i(TAG, "Using Face Detector Processor");
//        cameraSource.setMachineLearningFrameProcessor(new FaceDetectionProcessor());
        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        cameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();


        classifier = TensorFlowImageClassifier.create(getAssets(),
                MODEL_FILE,
                LABEL_FILE,
                INPUT_SIZE,
                IMAGE_MEAN,
                IMAGE_STD,
                INPUT_NAME,
                OUTPUT_NAME);

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
                Log.d(TAG, "createCameraSource: " + vector.size());
                vectors.add(vector);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }


    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(graphicOverlay);
        }
    }



    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
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

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        Log.i(TAG, "Permission granted!");
        if (allPermissionsGranted()) {
            createCameraSource();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private boolean isPermissionGranted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onPictureTaken(byte[] bytes) {
        Bitmap bitmap = Bitmap.createScaledBitmap(ImageUtils.getFaces(getApplicationContext(), bytes),
                INPUT_SIZE, INPUT_SIZE, true);
//        putPhotoToStorage(bytes, 21, 21 , 48);
        classifier.recognizeImage(bitmap, this, ((isSuccess, embeddings) -> {
            ArrayList<Float> arr = new ArrayList<>();
            for (float f: embeddings) {
                arr.add(f);
            }
            Double dis = 10d;
            for (int i = 0; i < vectors.size() ;i++){
                Double tmp = ArrayUtil.l2distance(arr, vectors.get(i));
                if (tmp < dis && tmp > 0){
                    dis = tmp;
                }
            }
            Log.d("cuonghx", "run: " + dis);
            GPSTracker gps = new GPSTracker(this);
            if(gps.canGetLocation() && dis < 0.7){
                Log.d(TAG, "onCheckedChanged: start checkin" + gps.getLatitude());
                Log.d(TAG, "onCheckedChanged: start checkin" + gps.getLongitude());
                success(gps.getLatitude(), gps.getLongitude(), bytes);

            }else{
                Log.d(TAG, "onPictureTaken: error" );
                Toast.makeText(this, "Thử lại !", Toast.LENGTH_SHORT).show();
            }
        }));
    }

    private void success(double lat , double longtitude, byte[] image){

        int studentID = AuthenticationLocalDataSource.getInstance(CheckInApplication.getInstance().getSharedPrefsApi()).getLoggedStudent().getId();
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
                        Toast.makeText(CheckInActivity.this, "Không thể checkin với vị trí này", Toast.LENGTH_SHORT).show();
                        break;
                    case -2:
                        Toast.makeText(CheckInActivity.this, "Bạn đã checkin rồi.", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        putPhotoToStorage(image, studentID, checkinResponse.getCourseID(), checkinResponse.getInsertId());
                        break;
                }
            }
        });
    }

    private void putPhotoToStorage(byte[] image, int studentId, int courseId, int insertId){
        Date currentTime = Calendar.getInstance().getTime();
        int month = currentTime.getMonth() + 1;
        StorageReference photo = mStorageRef.child("course_"
                + courseId + "/"
                + studentId + "_"
                + currentTime.getDate() + "-"
                + (month) + ".jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitmapFactory.decodeByteArray(image, 0, image.length).compress(Bitmap.CompressFormat.JPEG, 25, baos);
        byte[] data = baos.toByteArray();

        photo.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!urlTask.isSuccessful());
                        Uri downloadUrl = urlTask.getResult();
                        Log.d(TAG, "onSuccess: "+ downloadUrl.toString());

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
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
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

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay, bitmap -> {
//                    new GetVector().execute(bitmap);
//                    image.setImageBitmap(bitmap);
//                    classifier.recognizeImage(bitmap, getApplicationContext());


            });
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
//            Bitmap bitmap = ImageUtils.getBitmapFromView(mOverlay);
            mFaceGraphic.updateFace(face);
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }
}
