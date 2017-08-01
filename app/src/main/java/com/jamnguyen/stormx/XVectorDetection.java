package com.jamnguyen.stormx;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.WindowManager;

/**
 * Created by dung.levan on 27/07/2017.
 */

public class XVectorDetection{
    SensorManager sensorManager = null;
    Sensor rotationVectorSensor = null;
    SensorEventListener rvListener = null;
    float x,y,z;
   // Context context;

    public XVectorDetection(final Context mContext){
		super();
        sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        // Create a listener
        rvListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float[] rotationMatrix = new float[16];
                // Remap coordinate system
                float[] remappedRotationMatrix = new float[16];
                SensorManager.getRotationMatrixFromVector(rotationMatrix, sensorEvent.values);
                int rotation = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
                if(rotation == 0) // Default display rotation is portrait
                    SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, remappedRotationMatrix);
                else   // Default display rotation is landscape
                    SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_Z, SensorManager.AXIS_X, remappedRotationMatrix);
              //  SensorManager.remapCoordinateSystem(rotationMatrix,SensorManager.AXIS_X,SensorManager.AXIS_Z, remappedRotationMatrix);

                // Convert to orientations
                float[] orientations = new float[3];
                SensorManager.getOrientation(remappedRotationMatrix, orientations);
                 for (int i = 0; i < 3; i++) {
                     orientations[i] = (float) (Math.toDegrees(orientations[i])); // Góc -180, 0, 180 độ
                 }
                 x = (int)orientations[0] + 180;// Lấy góc 360 độ
                 y = (int)orientations[1] + 180;// Lấy góc 360 độ
                 z = (int)orientations[2] + 180;// Lấy góc 360 độ

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };
        registerListener();
    }

    public void registerListener(){
        if (sensorManager != null)
            // Register it
            sensorManager.registerListener(rvListener, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
	public void unRegisterListener()
	{
		if (sensorManager != null)
			sensorManager.unregisterListener(rvListener);
	}
    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float getX() {

        return x;
    }
}
