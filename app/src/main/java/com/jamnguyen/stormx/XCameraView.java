package com.jamnguyen.stormx;

import java.io.FileOutputStream;
import java.util.List;

import org.opencv.android.JavaCameraView;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;

public class XCameraView extends JavaCameraView
{
    private String mPictureFileName;

    public XCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

//    public List<String> getEffectList() {
//        return mCamera.getParameters().getSupportedColorEffects();
//    }

//    public boolean isEffectSupported() {
//        return (mCamera.getParameters().getColorEffect() != null);
//    }

//    public String getEffect() {
//        return mCamera.getParameters().getColorEffect();
//    }

//    public void setEffect(String effect) {
//        Camera.Parameters params = mCamera.getParameters();
//        params.setColorEffect(effect);
//        mCamera.setParameters(params);
//    }

//    public List<Size> getResolutionList() {
//        return mCamera.getParameters().getSupportedPreviewSizes();
//    }

//    public void setResolution(Size resolution) {
//        disconnectCamera();
//        mMaxHeight = resolution.height;
//        mMaxWidth = resolution.width;
//        connectCamera(getWidth(), getHeight());
//    }
//
//    public Size getResolution() {
//        return mCamera.getParameters().getPreviewSize();
//    }

//    public void takePicture(final String fileName) {
//        Log.i(TAG, "Taking picture");
//        this.mPictureFileName = fileName;
//        // Postview and jpeg are sent in the same buffers if the queue is not empty when performing a capture.
//        // Clear up buffers to avoid mCamera.takePicture to be stuck because of a memory issue
//        mCamera.setPreviewCallback(null);
//
//        // PictureCallback is implemented by the current class
//        mCamera.takePicture(null, null, this);
//    }
}
