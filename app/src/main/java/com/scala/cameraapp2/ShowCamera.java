package com.scala.cameraapp2;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;


import java.io.IOException;

public class ShowCamera extends Activity implements CameraBridgeViewBase.CvCameraViewListener2{
    private static final String TAG="MainActivity";

    private Mat mRgba;
    private Mat mGray;
    private CameraBridgeViewBase mOpenCvCameraView;
    private Detector Detector;
    private BaseLoaderCallback mLoaderCallback =new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface
                        .SUCCESS:{
                    mOpenCvCameraView.enableView();
                }
                default:
                {
                    super.onManagerConnected(status);

                }
                break;
            }
        }
    };

    public ShowCamera(){
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       requestWindowFeature(Window.FEATURE_NO_TITLE);
       getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        int MY_PERMISSIONS_REQUEST_CAMERA=0;
        if (ContextCompat.checkSelfPermission(ShowCamera.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(ShowCamera.this, new String[] {Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }

        setContentView(R.layout.activity_camera);

        mOpenCvCameraView=(CameraBridgeViewBase) findViewById(R.id.frame_Surface);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        try{
            Detector=new Detector(getAssets(),"ssd_mobilenet.tflite","labelmap.txt",300);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()){
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else{
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,this,mLoaderCallback);
        }
    }

    @Override    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView !=null){
            mOpenCvCameraView.disableView();
        }
    }

    public void onDestroy(){
        super.onDestroy();
        if(mOpenCvCameraView !=null){
            mOpenCvCameraView.disableView();
        }

    }

    public void onCameraViewStarted(int width ,int height){
        mRgba=new Mat(height,width, CvType.CV_8UC4);
        mGray =new Mat(height,width,CvType.CV_8UC1);
    }
    public void onCameraViewStopped(){
        mRgba.release();
    }
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame){
        mRgba=inputFrame.rgba();
        mGray=inputFrame.gray();
        Mat out;
        out=Detector.recognizeImage(mRgba);

        return out;
    }

}