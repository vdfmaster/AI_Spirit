package com.jamnguyen.stormx;

import org.opencv.core.Scalar;

public class ColorObject
{
    private int     m_x;
    private int     m_y;
    private String  m_type;
    private Scalar  m_HSVmin;
    private Scalar  m_HSVmax;
    private Scalar  m_Color;

    public ColorObject(String name)
    {
        setType(name);

        if(name.toLowerCase().equals("blue"))
        {
            //Use "calibration mode" to find HSV min and HSV max values
            setHSVmin(new Scalar(92,0,0));
            setHSVmax(new Scalar(124,256,256));

            //BGR value for Blue:
            setColor(new Scalar(255,0,0));

        }
        if(name.toLowerCase().equals("green"))
        {
            //Use "calibration mode" to find HSV min and HSV max values
            setHSVmin(new Scalar(34,50,50));
            setHSVmax(new Scalar(80,220,200));

            //BGR value for Green:
            setColor(new Scalar(0,255,0));

        }
        if(name.toLowerCase().equals("yellow"))
        {
            //Use "calibration mode" to find HSV min and HSV max values
            setHSVmin(new Scalar(20,124,123));
            setHSVmax(new Scalar(30,256,256));

            //BGR value for Yellow:
            setColor(new Scalar(0,255,255));

        }
        if(name.toLowerCase().equals("red"))
        {
            //Use "calibration mode" to find HSV min and HSV max values
            setHSVmin(new Scalar(0,200,0));
            setHSVmax(new Scalar(19,255,255));

            //BGR value for Red:
            setColor(new Scalar(0,0,255));

        }
    }

    public int getX()
    {
        return m_x;
    }
    public int getY()
    {
        return m_y;
    }
    public void setX(int x)
    {
        m_x = x;
    }
    public void setY(int y)
    {
        m_y = y;
    }

    public Scalar getHSVmin()
    {
        return m_HSVmin;
    }
    public Scalar getHSVmax()
    {
        return m_HSVmax;
    }

    public void setHSVmin(Scalar min)
    {
        m_HSVmin = min;
    }
    public void setHSVmax(Scalar max)
    {
        m_HSVmax = max;
    }

    public String getType()
    {
        return m_type;
    }
    public void setType(String t)
    {
        m_type = t;
    }

    public Scalar getColor()
    {
        return m_Color;
    }
    public void setColor(Scalar c)
    {
        m_Color = c;
    }
}
