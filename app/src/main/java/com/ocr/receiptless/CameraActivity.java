package com.ocr.receiptless;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Size;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.ocr.receiptless.util.Util;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;


public class CameraActivity extends AppCompatActivity implements OnClickListener{

    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    //private TextView textView;
    private Button btnTakePicture, btnRetake, btnOk;
    private RelativeLayout rlOverlay;
    private ImageView ivPicture;
    private ImageCapture imageCapture;
    private Executor cameraExecutor;

    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // hiding action bar
        getSupportActionBar().hide();

        previewView = findViewById(R.id.previewView);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        //textView = findViewById(R.id.orientation);
        btnTakePicture = findViewById(R.id.btn_take_picture);
        btnRetake = findViewById(R.id.btn_retake);
        btnOk = findViewById(R.id.btn_ok);
        rlOverlay = findViewById(R.id.rl_overlay);
        ivPicture = findViewById(R.id.iv_picture);

        showOverlay(false);
        cameraExecutor = ContextCompat.getMainExecutor(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindImageAnalysis(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, cameraExecutor);

        imageCapture = new ImageCapture.Builder()
                .setTargetRotation(Surface.ROTATION_0)
                .build();

        btnTakePicture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onCaptureClick();
            }
        });

        btnRetake.setOnClickListener(this);
        btnOk.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_retake:
                File fdelete = new File(imagePath);
                if (fdelete.exists()) {
                    if (fdelete.delete()) {
                        Log.d(Util.LOG,imagePath + " deleted successfully");
                    } else {
                        Log.d(Util.LOG,imagePath + " delete failed");
                    }
                }
                showOverlay(false);
                break;
            case R.id.btn_ok:
                Intent intent = new Intent(CameraActivity.this, CategoryActivity.class);
                intent.putExtra(Util.IMAGE_URI_PATH,imagePath);
                intent.putExtra(Util.FROM_CAMERA,true);
                startActivity(intent);
                finish();
                break;
        }
    }

    private void showOverlay(boolean isShow){
        if(isShow){
            rlOverlay.setVisibility(View.VISIBLE);
            btnTakePicture.setVisibility(View.GONE);
        }else{
            rlOverlay.setVisibility(View.GONE);
            btnTakePicture.setVisibility(View.VISIBLE);
        }
    }

    private void bindImageAnalysis(@NonNull ProcessCameraProvider cameraProvider) {
        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder().setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                image.close();
            }
        });
        OrientationEventListener orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                int rotation = 0;
                if(orientation >= 45 && orientation <= 134){
                    rotation = Surface.ROTATION_270;
                }else if(orientation >= 135 && orientation <= 224){
                    rotation = Surface.ROTATION_180;
                }else if(orientation >= 225 && orientation <= 314){
                    rotation = Surface.ROTATION_90;
                }else{
                    rotation = Surface.ROTATION_0;
                }

                //textView.setText(Integer.toString(orientation));
                imageCapture.setTargetRotation(rotation);
            }
        };
        orientationEventListener.enable();
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(previewView.createSurfaceProvider());

        cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageCapture,
                imageAnalysis, preview);
    }

    public void onCaptureClick() {
        final File file = fileWithDirectoryAssurance( getExternalFilesDir(Environment.DIRECTORY_PICTURES)+ "/Receiptless/",System.currentTimeMillis()+".jpg");
        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(file).build();
        imageCapture.takePicture(outputFileOptions,cameraExecutor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                        // insert your code here.
                        Uri uri = Uri.fromFile(file);
                        imagePath = uri.getPath();
                        showOverlay(true);
                        ivPicture.setImageURI(uri);

                    }
                    @Override
                    public void onError(ImageCaptureException error) {
                        // insert your code here.
                        error.printStackTrace();
                        Toast.makeText(CameraActivity.this, "error", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private static File fileWithDirectoryAssurance(String directory, String filename) {
        File dir = new File(directory);
        if (!dir.exists()) dir.mkdirs();
        return new File(directory + "/" + filename);
    }
}
