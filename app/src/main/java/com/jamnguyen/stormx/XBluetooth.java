package com.jamnguyen.stormx;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.InputMismatchException;
import java.util.UUID;

public class XBluetooth
{
    private Context             m_appContext;
    private Handler             m_appHandler;           //Handler from activity
    private String              m_deviceAddress = null;

    private ConnectedThread     m_ConnectedThread;      //Bluetooth background worker thread to send and receive data
    private BluetoothSocket     m_BTSocket = null;      //Bi-directional client-to-client data path
    private BluetoothAdapter    m_BTAdapter;

    private String              m_prevSentMsg;

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //"random" unique identifier

    public final static int REQUEST_ENABLE_BT = 1;     //Used to identify adding bluetooth names
    public final static int MESSAGE_READ = 2;          //Used in bluetooth handler to identify message update
    public final static int CONNECTING_STATUS = 3;     //Used in bluetooth handler to identify message status

    public XBluetooth(String Address, Context context, Handler appHandler)
    {
        m_appContext = context;
        m_deviceAddress = Address;
        m_appHandler = appHandler;
        m_prevSentMsg = "Nothing sent";
    }

    public void init()
    {
        //Get a handle on the bluetooth radio
        m_BTAdapter = BluetoothAdapter.getDefaultAdapter();

        Utils.toastShort("Connecting...", m_appContext);

        //Spawn a new thread to avoid blocking the GUI one
        new Thread()
        {
            public void run()
            {
                boolean fail = false;

                BluetoothDevice device = m_BTAdapter.getRemoteDevice(m_deviceAddress);

                try
                {
                    m_BTSocket = createBluetoothSocket(device);
                } catch (IOException e)
                {
                    fail = true;
                    Utils.toastShort("Socket creation failed.", m_appContext);
                }

                //Establish the Bluetooth socket connection.
                try
                {
                    m_BTSocket.connect();
                }
                catch (IOException e)
                {
                    try
                    {
                        fail = true;
                        m_BTSocket.close();
                        m_appHandler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                    }
                    catch (IOException e2)
                    {
                        Utils.toastShort("Socket creation failed.", m_appContext);
                    }
                }
                if(!fail)
                {
                    m_ConnectedThread = new ConnectedThread(m_BTSocket);
                    m_ConnectedThread.start();
                    m_appHandler.obtainMessage(CONNECTING_STATUS, 1, -1, m_deviceAddress).sendToTarget();
                }
            }
        }.start();
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
    {
        //Creates secure outgoing connection with BT device using UUID
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    private class ConnectedThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //Get the input and output streams
            //Using temp objects because member streams are final
            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }
            catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            final byte delimiter = 10;          //New line character

            byte[] readBuffer = new byte[1024]; //Buffer store for the stream
            int bytesAvailable;                 //Bytes returned from read()
            int readBufferPosition = 0;         //Reading current position

            //Keep listening to the InputStream until an exception occurs
            while (true)
            {
                try
                {
                    //How many bytes are ready to be read?
                    bytesAvailable = mmInStream.available();
                    if(bytesAvailable > 0)
                    {

                        byte[] packetBytes = new byte[bytesAvailable];
                        mmInStream.read(packetBytes);
                        for(int i=0; i<bytesAvailable; i++)
                        {
                            byte b = packetBytes[i];
                            if(b == delimiter)
                            {
                                readBufferPosition--;
                                byte[] encodedBytes = new byte[readBufferPosition];
                                System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
//                                final String data = new String(encodedBytes, "US-ASCII");
                                readBufferPosition = 0;

                                m_appHandler.obtainMessage(MESSAGE_READ, readBufferPosition, -1, encodedBytes).sendToTarget();
                                break;
                            }
                            else
                            {
                                readBuffer[readBufferPosition++] = b;
                            }
                        }

                        //Pause and wait for rest of data.
                        //Adjust this depending on sending speed.
//                        SystemClock.sleep(100);
                        //Record how many bytes we actually read
//                        bytes = mmInStream.available();
//                        bytes = mmInStream.read(buffer, 0, bytes);
                        //Send the obtained bytes to the UI activity
//                        m_appHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();

                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input)
        {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try
            {
                mmOutStream.write(bytes);
            }
            catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    public void Disconnect()
    {
        if (m_BTSocket != null) //If the BTSocket is busy
        {
            try {
                m_BTSocket.close(); //close connection
            } catch (IOException e) {
                Utils.toastLong("Error", m_appContext);
            }
        }
    }

    //For testing
    public void TurnOffLed()
    {
        send("TF\n");
    }

    public void TurnOnLed()
    {
        send("TO\n");
    }

    public void send(String s)
    {
        //First check to make sure thread created

        if (m_ConnectedThread != null)
        {
            m_ConnectedThread.write(s);
        }

        m_prevSentMsg = s;
    }

    public String getPrevSentMsg()
    {
        return m_prevSentMsg;
    }
}
