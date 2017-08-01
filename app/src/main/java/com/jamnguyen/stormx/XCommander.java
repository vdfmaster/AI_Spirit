package com.jamnguyen.stormx;

import org.opencv.core.Point;

public class XCommander
{
    //DEFINES---------------------------------------------------------------------------------------

    //To Arduino
    public static final String MOVE_FORWARD     = "F";
    public static final String TURN_LEFT        = "L";
    public static final String TURN_LEFT_90     = "Q";
    public static final String TURN_RIGHT       = "R";
    public static final String TURN_RIGHT_90    = "E";
    public static final String TURN_AROUND      = "Z";
    public static final String MOVE_BACKWARD    = "B";
    public static final String MOVE_BACKWARD_1S = "V";
    public static final String STOP             = "S";
    public static final String CATCH_BALL       = "C";
    public static final String PUSH_BALL        = "P";

    //From Arduino
    public static final String CANNOT_MOVE      = "K";
    //----------------------------------------------------------------------------------------------
    private XDetector m_Detector;
    private XBluetooth m_Bluetooth;
    private boolean m_isBallHolding;

    public XCommander(XBluetooth BT, XDetector DT)
    {
        m_Bluetooth = BT;
        m_Detector = DT;
        m_isBallHolding = false;
    }

    public void sendCommnand(String command)
    {
		if(!m_Bluetooth.getPrevSentMsg().equals(command))
		{
			m_Bluetooth.send(command);
		}
    }

	public void handleBall(Point centerPoint)
	{
		//This function run when there's ball on screen

        int tX = m_Detector.getTransposedX((int)centerPoint.y);
        int tY = m_Detector.getTransposedY((int)centerPoint.x);
		
		//Ball's catchable
        if(m_Detector.getBallArea()/m_Detector.getScreenArea() >= XDetector.CAUGHT_AREA_RATIO)
        {
            //First: Stop
            if(!m_Bluetooth.getPrevSentMsg().equals(CATCH_BALL))
            {
                sendCommnand(STOP);
            }
            //Second: Catch
            else if(m_Bluetooth.getPrevSentMsg().equals(STOP))
            {
                //IM_Command = Config.CMD_BALL;
                m_isBallHolding = true;
            }
        }
        else
        {
            //Calibrating direction
            if (tX < m_Detector.getMiddleLine() && (m_Detector.getMiddleLine() - tX) > XDetector.MIDDLE_DELTA)
            {
                sendCommnand(TURN_LEFT);
            } 
			else if (tX > m_Detector.getMiddleLine() && (tX - m_Detector.getMiddleLine()) > XDetector.MIDDLE_DELTA)
            {
				sendCommnand(TURN_RIGHT);
            }
            else
            {
                sendCommnand(MOVE_FORWARD);
            }
        }
	}
	
	public void seekForBall()
	{
		sendCommnand(TURN_LEFT);
	}

	public boolean isBallHolding()
    {
        return m_isBallHolding;
    }

    public void setBallHolding(boolean hold)
    {
        m_isBallHolding = hold;
    }
}
