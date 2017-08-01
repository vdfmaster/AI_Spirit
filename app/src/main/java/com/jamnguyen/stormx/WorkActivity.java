package com.jamnguyen.stormx;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.opencv.imgproc.Imgproc.contourArea;

public class WorkActivity extends Activity implements View.OnTouchListener, CvCameraViewListener2
{
    private static final String  TAG              = "OCVSample::Activity";

    private XCameraView     m_OpenCvCameraView;
    private boolean         m_isBallOnScreen = false;

    private XBluetooth      m_XBluetooth;
    private boolean         m_isBluetoothConnected = false;
    private XDetector       m_XDetector;
    private XCommander      m_XCommander;
    private Handler         m_Handler;                  //Main handler that will receive callback notifications
    private String          m_MsgFromArduino;
    private Context         context;
    private Mat             m_Rgba;


	private Gameplay m_Game;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this)
    {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    m_OpenCvCameraView.enableView();
                    m_OpenCvCameraView.setOnTouchListener(WorkActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    //Android States--------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_work);

        m_MsgFromArduino = "<Nothing received>";
		
        //Camera
        m_OpenCvCameraView = (XCameraView) findViewById(R.id.activity_java_surface_view);
        m_OpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        m_OpenCvCameraView.setCvCameraViewListener(this);

        //Bluetooth
        Intent newint = getIntent();
        String address = newint.getStringExtra(SetupActivity.EXTRA_ADDRESS);

        //Init handler for message from XBluetooth
        initHandler();
        context = getApplicationContext();
        m_XBluetooth = new XBluetooth(address, context, m_Handler);
        m_XBluetooth.init();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (m_OpenCvCameraView != null)
            m_OpenCvCameraView.disableView();
        if (m_Game != null)
            m_Game.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug())
        {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        }
        else
        {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        if (m_Game != null)
            m_Game.onResume();
    }

    public void onDestroy()
    {
        super.onDestroy();
        if (m_OpenCvCameraView != null)
            m_OpenCvCameraView.disableView();
        if (m_Game != null)
            m_Game.onDestroy();
    }

    public boolean onTouch(View v, MotionEvent event)
    {
        m_XDetector.getColorOnTouch(m_Rgba, m_OpenCvCameraView.getWidth(), m_OpenCvCameraView.getHeight(), event);
        return false; // don't need subsequent touch events
    }

    //Camera handling-------------------------------------------------------------------------------
    public void onCameraViewStarted(int width, int height)
    {
        m_Rgba = new Mat(height, width, CvType.CV_8UC4);
        m_XDetector = new XDetector(context, width, height);

        m_Game = new Gameplay(m_XBluetooth, m_XDetector, context);
        // m_XCommander = new XCommander(m_XBluetooth, m_XDetector);
    }
    public void onCameraViewStopped()
    {
        m_Rgba.release();
    }


    public Mat onCameraFrame(CvCameraViewFrame inputFrame)
    {
        m_Rgba = inputFrame.rgba();

        //If Bluetooth connection fail then there's nothing to do
//        if(m_isBluetoothConnected)
        {
            //Put all processing here---------------------------------------------------------------
//            m_XDetector.circleDectect(inputFrame);

            //Tap the screen to start
            if(Gameplay.ANDROID_STARTED)
            {
                if(!Gameplay.ANDROID_INITIALIZED)
                {
                    m_Game.STATE_INIT_func();
                }
                else
                {
                    //Show forward range
                    m_XDetector.drawForwardRange(m_Rgba);

                    m_XDetector.colorDetect(m_Rgba);
					m_Game.SetSwitchMessage(Integer.parseInt(m_MsgFromArduino));
                    if (!m_XDetector.isBallOnScreen()) {
                        m_Game.SetColorMessage(Gameplay.COLOR_ZERO);
                    } 
					else 
					{
                        if (m_XDetector.getBallArea() / m_XDetector.getScreenArea() >= XDetector.CAUGHT_AREA_RATIO) 
						{
							m_Game.SetColorMessage(Gameplay.COLOR_NEAR);
						} 
						else 
						{
							int tX = m_XDetector.getTransposedX((int) m_XDetector.getBallCenter().y);
							int tY = m_XDetector.getTransposedY((int) m_XDetector.getBallCenter().x);
							//Calibrating direction
							if (tX < m_XDetector.getMiddleLine() && (m_XDetector.getMiddleLine() - tX) > XDetector.MIDDLE_DELTA) {
								m_Game.SetColorMessage(Gameplay.COLOR_LEFT);
							} else if (tX > m_XDetector.getMiddleLine() && (tX - m_XDetector.getMiddleLine()) > XDetector.MIDDLE_DELTA) {
								m_Game.SetColorMessage(Gameplay.COLOR_RIGHT);
							} else {
								m_Game.SetColorMessage(Gameplay.COLOR_MIDDLE);
							}
						}
                    }
                    m_Game.Run();
                    /* if(!m_XCommander.isBallHolding())
                    {
                        if (m_XDetector.isBallOnScreen())
                        {
                            //Approach ball
                            m_XCommander.handleBall(m_XDetector.getBallCenter());
                        }
                        else
                        {
                            //Look for ball
                            m_XCommander.seekForBall();
                        }
                    }
                    else
                    {
                        //Look for goal
                        if (m_XDetector.isBallOnScreen())
                        {
                            //Approach goal
                            m_XCommander.handleGoal(m_XDetector.getBallCenter());
                        }
                        else
                        {
                            //Look for goal
                            m_XCommander.seekForBall();
                        }
                    } */
                }
            }

            /* if(m_MsgFromArduino.equals("P"))
            {
                //After pushing ball
                m_XCommander.setBallHolding(false);
            } */

            //Showing statuses
            Utils.drawString(m_Rgba, "Arduino: " + m_MsgFromArduino, 20, 40);
            Utils.drawString(m_Rgba, "Command: " + m_XBluetooth.getPrevSentMsg(), 20, 70);
            Utils.drawString(m_Rgba, "Is detecting ball: " + m_XDetector.getDetectBall(), 20, 100);
            Utils.drawString(m_Rgba, "x_org: " + m_Game.getOrientations()[0] + " -- y_org: " +
                    m_Game.getOrientations()[1] + " -- z_org: " + m_Game.getOrientations()[2], 20, 130);//dung.levan thêm để lấy thông tin
            Utils.drawString(m_Rgba, "x: " + (int)m_Game.getX() + " -- y: " + (int)m_Game.getY() + " -- z: " + (int)m_Game.getZ(), 20, 160); //dung.levan thêm để lấy thông tin
            //--------------------------------------------------------------------------------------
        }

        //Test: Color detection


        return m_Rgba;
    }

    //Bluetooth handling----------------------------------------------------------------------------
    private void initHandler()
    {
        m_Handler = new Handler()
        {
            //Catch message from XBluetooth
            public void handleMessage(android.os.Message msg)
            {
                //Receive message
                if(msg.what == XBluetooth.MESSAGE_READ)
                {
                    String readMessage = null;
                    try
                    {
                        readMessage = new String((byte[]) msg.obj, "US-ASCII");
                        m_MsgFromArduino = readMessage;
                    }
                    catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                //Get connecting status
                if(msg.what == XBluetooth.CONNECTING_STATUS)
                {
                    if(msg.arg1 == 1)
                    {
                        Utils.toastShort("Connected to Device: " + (msg.obj),context);
                        m_isBluetoothConnected = true;
                    }
                    else
                    {
                        Utils.toastShort("Connection Failed", context);
                    }
                }
            }
        };
    }
}