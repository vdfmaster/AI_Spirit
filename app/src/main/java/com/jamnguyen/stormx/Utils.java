package com.jamnguyen.stormx;

import android.content.Context;
import android.widget.Toast;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class Utils
{
    //Fast way to call Toast
    public static void toastLong(String s, Context context)
    {
        Toast.makeText(context, s, Toast.LENGTH_LONG).show();
    }

    public static void toastShort(String s, Context context)
    {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }

    //Fast way to put text on Mat
    public static void drawString(Mat frame, String content, int x, int y)
    {
        Imgproc.putText(frame, content, new Point(x, y), Core.FONT_HERSHEY_PLAIN, 2.0, new Scalar(255, 255, 0));
    }
}
