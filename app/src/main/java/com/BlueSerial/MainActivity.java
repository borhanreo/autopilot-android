package com.BlueSerial;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends Activity implements SensorEventListener{
    private static final String TAG = "BlueTest5-MainActivity";
    private int mMaxChars = 50000;//Default
    private UUID mDeviceUUID;
    private BluetoothSocket mBTSocket;
    private ReadInput mReadThread = null;

    private boolean mIsUserInitiatedDisconnect = false;

    // Accelerometer X, Y, and Z values

    // Orientation X, Y, and Z values
    private TextView orientXValue;
    private TextView orientYValue;
    private TextView orientZValue;
    private SensorManager sensorManager = null;

    // All controls here
    private TextView mTxtReceive;
    private EditText mEditSend;
    private Button mBtnDisconnect;
    private Button mBtnSend;
    private Button mBtnClear;
    private Button mBtnClearInput;
    private ScrollView scrollView;
    private CheckBox chkScroll;
    private CheckBox chkReceiveText;
    private CheckBox voiceCommand;
    private Button upBtn,leftBtn,rightBtn,downBtn,upLeftBtn,upRightBtn,leftDownBtn,rightDownBtn,stopBtn;
    private boolean mIsBluetoothConnected = false;
    private TextView accelXValue;
    private BluetoothDevice mDevice;
private TextView tv;
    private ProgressDialog progressDialog;

    private static SensorManager sensorService;
    private Sensor sensor;


    private TextView txtSpeechInput;
    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityHelper.initialize(this);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        mDevice = b.getParcelable(Homescreen.DEVICE_EXTRA);
        mDeviceUUID = UUID.fromString(b.getString(Homescreen.DEVICE_UUID));
        mMaxChars = b.getInt(Homescreen.BUFFER_SIZE);

        Log.d(TAG, "Ready");
            /*VOICE COMMAND MODE START*/
        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
        getActionBar().hide();
        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
        /*VOICE COMMAND MODE end*/
        mBtnDisconnect = (Button) findViewById(R.id.btnDisconnect);
        mBtnSend = (Button) findViewById(R.id.btnSend);
        mBtnClear = (Button) findViewById(R.id.btnClear);
        mTxtReceive = (TextView) findViewById(R.id.txtReceive);
        mEditSend = (EditText) findViewById(R.id.editSend);
        scrollView = (ScrollView) findViewById(R.id.viewScroll);
        chkScroll = (CheckBox) findViewById(R.id.chkScroll);
        chkReceiveText = (CheckBox) findViewById(R.id.chkReceiveText);
        voiceCommand = (CheckBox) findViewById(R.id.voiceCommand);
        mBtnClearInput = (Button) findViewById(R.id.btnClearInput);
        mTxtReceive.setMovementMethod(new ScrollingMovementMethod());
        mBtnClear = (Button) findViewById(R.id.btnClear);
        upBtn = (Button)findViewById(R.id.up);
        upBtn.setVisibility(View.GONE);
        stopBtn = (Button)findViewById(R.id.stop);
        stopBtn.setVisibility(View.GONE);
        leftBtn = (Button)findViewById(R.id.left);
        leftBtn.setVisibility(View.GONE);
        rightBtn = (Button)findViewById(R.id.right);
        rightBtn.setVisibility(View.GONE);
        downBtn = (Button)findViewById(R.id.down);
        downBtn.setVisibility(View.GONE);
        upLeftBtn = (Button)findViewById(R.id.upleft);
        upLeftBtn.setVisibility(View.GONE);
        upRightBtn = (Button)findViewById(R.id.upright);
        upRightBtn.setVisibility(View.GONE);
        leftDownBtn = (Button)findViewById(R.id.leftdown);
        leftDownBtn.setVisibility(View.GONE);
        rightDownBtn = (Button)findViewById(R.id.rightdown);
        rightDownBtn.setVisibility(View.GONE);
        tv = (TextView)findViewById(R.id.setVal);

        accelXValue = (TextView) findViewById(R.id.accel_x_label);
        sensorService = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorService.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        if (sensor != null) {
            sensorService.registerListener(mySensorEventListener, sensor,SensorManager.SENSOR_DELAY_NORMAL);
            Log.i("Compass MainActivity", "Registerered for ORIENTATION Sensor");
        } else {
            Log.e("Compass MainActivity", "Registerered for ORIENTATION Sensor");
            Toast.makeText(this, "ORIENTATION Sensor not found",
                    Toast.LENGTH_LONG).show();
            finish();
        }
        accelXValue.setText("0.00");
        upBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String sendVal="A";
                try {
                    mBTSocket.getOutputStream().write(sendVal.getBytes());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        stopBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String sendVal="S";
                try {
                    mBTSocket.getOutputStream().write(sendVal.getBytes());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        leftBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String sendVal="B";
                try {
                    mBTSocket.getOutputStream().write(sendVal.getBytes());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        rightBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String sendVal="C";
                try {
                    mBTSocket.getOutputStream().write(sendVal.getBytes());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        downBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String sendVal="D";
                try {
                    mBTSocket.getOutputStream().write(sendVal.getBytes());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        upLeftBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String sendVal="E";
                try {
                    mBTSocket.getOutputStream().write(sendVal.getBytes());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        upRightBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String sendVal="F";
                try {
                    mBTSocket.getOutputStream().write(sendVal.getBytes());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        leftDownBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String sendVal="G";
                try {
                    mBTSocket.getOutputStream().write(sendVal.getBytes());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        rightDownBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String sendVal="H";
                try {
                    mBTSocket.getOutputStream().write(sendVal.getBytes());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        mBtnClear.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mEditSend.setText("");
            }
        });
        mBtnDisconnect.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mIsUserInitiatedDisconnect = true;
                new DisConnectBT().execute();
            }
        });

        mBtnSend.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                try {
                    mBTSocket.getOutputStream().write(mEditSend.getText().toString().getBytes());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }
    private SensorEventListener mySensorEventListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            // angle between the magnetic north direction
            // 0=North, 90=East, 180=South, 270=West
            float azimuth = event.values[0];
            float yval = event.values[1];
            accelXValue.setText(Float.toString(event.values[1]));
            //compassView.updateData(azimuth);

            float xval=event.values[0];
            if(yval>3)
            {
                if(xval>1 && xval<9)
                {
                    String sendVal="B"; //left
                    try {
                        mBTSocket.getOutputStream().write(sendVal.getBytes());
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }else if(xval<-1 && xval>-9)
                {
                    String sendVal="C"; //right
                    try {
                        mBTSocket.getOutputStream().write(sendVal.getBytes());
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }else if(xval<=1 && xval>=-1)
                {
                    String sendVal="A"; //up
                    try {
                        mBTSocket.getOutputStream().write(sendVal.getBytes());
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }else{
                String sendVal="S"; //up
                try {
                    mBTSocket.getOutputStream().write(sendVal.getBytes());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensor != null) {
            sensorService.unregisterListener(mySensorEventListener);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        synchronized (this) {
            if (voiceCommand.isChecked()==false){
                if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                    accelXValue.setText(Float.toString(sensorEvent.values[0]));
                    float xval=sensorEvent.values[0];
                    if(xval>2 && xval<9)
                    {
                        String sendVal="E"; //left
                        try {
                            mBTSocket.getOutputStream().write(sendVal.getBytes());
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }else if(xval<-2 && xval>-9)
                    {
                        String sendVal="F"; //right
                        try {
                            mBTSocket.getOutputStream().write(sendVal.getBytes());
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }else if(xval<=2 && xval>=-2)
                    {
                        String sendVal="A"; //up
                        try {
                            mBTSocket.getOutputStream().write(sendVal.getBytes());
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }


                }
                if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                    //orientXValue.setText(Float.toString(sensorEvent.values[0]));
                    //orientYValue.setText(Float.toString(sensorEvent.values[1]));
                    //orientZValue.setText(Float.toString(sensorEvent.values[2]));
                }
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private class ReadInput implements Runnable {

        private boolean bStop = false;
        private Thread t;

        public ReadInput() {
            t = new Thread(this, "Input Thread");
            t.start();
        }

        public boolean isRunning() {
            return t.isAlive();
        }

        @Override
        public void run() {
            InputStream inputStream;

            try {
                inputStream = mBTSocket.getInputStream();



                while (!bStop) {
                    byte[] buffer = new byte[256];
                    if (inputStream.available() > 0) {
                        inputStream.read(buffer);

                        int i = 0;
                        int k=0;
						/*
						 * This is needed because new String(buffer) is taking the entire buffer i.e. 256 chars on Android 2.3.4 http://stackoverflow.com/a/8843462/1287554
						 */

                        for (i = 0; i < buffer.length && buffer[i] != 0; i++) {

                        }
                        sensorManager  = (SensorManager) getSystemService(SENSOR_SERVICE);

                        final TextView textView = (TextView)findViewById(R.id.textView);
                        // this is the view on which you will listen for touch events
                        textView.setText(inputStream.toString());
                        /*final View touchView = findViewById(R.id.touchView);
                        touchView.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                textView.setText("Touch coordinates : " +
                                        String.valueOf(event.getX()) + "x" + String.valueOf(event.getY()));
                                String fg = String.valueOf(event.getX());
                               if(event.getX()>240 )
                               {
                                   String sendVal="E";
                                   try {
                                       mBTSocket.getOutputStream().write(sendVal.getBytes());
                                   } catch (IOException e) {
                                       // TODO Auto-generated catch block
                                       e.printStackTrace();
                                   }
                               }else if(event.getX()<240)
                               {
                                   String sendVal="F";
                                   try {
                                       mBTSocket.getOutputStream().write(sendVal.getBytes());
                                   } catch (IOException e) {
                                       // TODO Auto-generated catch block
                                       e.printStackTrace();
                                   }
                               }
                                return true;
                            }
                        });*/


                        final String strInput = new String(buffer, 0, i);
                        //final String strInput = new String(buffer, 0, i);
                        //final String actStr = new String(buffer,i-2,i);
						/*
						 * If checked then receive text, better design would probably be to stop thread if unchecked and free resources, but this is a quick fix
						 */

                        if (chkReceiveText.isChecked()) {
                            mTxtReceive.post(new Runnable() {
                                @Override
                                public void run() {
                                    mTxtReceive.append(strInput);
                                    tv.setText("Device Run....");
                                    //Uncomment below for testing
                                    //mTxtReceive.append("\n");
                                    //mTxtReceive.append("Chars: " + strInput.length() + " Lines: " + mTxtReceive.getLineCount() + "\n");

                                    int txtLength = mTxtReceive.getEditableText().length();
                                    if(txtLength > mMaxChars){
                                        mTxtReceive.getEditableText().delete(0, txtLength - mMaxChars);
                                    }

                                    if (chkScroll.isChecked()) { // Scroll only if this is checked
                                        scrollView.post(new Runnable() { // Snippet from http://stackoverflow.com/a/4612082/1287554
                                            @Override
                                            public void run() {
                                                scrollView.fullScroll(View.FOCUS_DOWN);
                                            }
                                        });
                                    }
                                }
                            });
                        }

                    }
                    Thread.sleep(500);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        public void stop() {
            bStop = true;
        }

    }
    private class DisConnectBT extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {

            if (mReadThread != null) {
                mReadThread.stop();
                while (mReadThread.isRunning())
                    ; // Wait until it stops
                mReadThread = null;

            }

            try {
                mBTSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mIsBluetoothConnected = false;
            if (mIsUserInitiatedDisconnect) {
                finish();
            }
        }

    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        /*if (mBTSocket != null && mIsBluetoothConnected) {
            new DisConnectBT().execute();
        }
        Log.d(TAG, "Paused");*/

        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mBTSocket == null || !mIsBluetoothConnected) {
            new ConnectBT().execute();
        }

        Log.d(TAG, "Resumed");
        super.onResume();

    }

    @Override
    protected void onStop() {
        //sensorManager.unregisterListener(this);
        Log.d(TAG, "Stopped");
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean mConnectSuccessful = true;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MainActivity.this, "Hold on", "Connecting");// http://stackoverflow.com/a/11130220/1287554
        }

        @Override
        protected Void doInBackground(Void... devices) {

            try {
                if (mBTSocket == null || !mIsBluetoothConnected) {
                    mBTSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mDeviceUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    mBTSocket.connect();
                }
            } catch (IOException e) {
                // Unable to connect to device
                e.printStackTrace();
                mConnectSuccessful = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!mConnectSuccessful) {
                Toast.makeText(getApplicationContext(), "Could not connect to device. Is it a Serial device? Also check if the UUID is correct in the settings", Toast.LENGTH_LONG).show();
                finish();
            } else {
                msg("Connected to device");
                mIsBluetoothConnected = true;
                mReadThread = new ReadInput(); // Kick off input reader
            }

            progressDialog.dismiss();
        }

    }
    /*VOICE COMMAND MODE START*/
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }
    /*VOICE COMMAND MODE END*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (voiceCommand.isChecked()==true){
            switch (requestCode) {
                case REQ_CODE_SPEECH_INPUT: {
                    if (resultCode == RESULT_OK && null != data) {

                        ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        txtSpeechInput.setText(result.get(0));
                        String getStr = result.get(0);
                        if (result.get(0).contains("up") || result.get(0).contains("Up")|| result.get(0).contains("yes")|| result.get(0).contains("go")) {
                            String sendVal="A";
                            try {
                                mBTSocket.getOutputStream().write(sendVal.getBytes());
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            //txtSpeechInput.setText(result.get(0));
                        } else if(result.get(0).contains("left") || result.get(0).contains("Left")) {
                            String sendVal="B";
                            try {
                                mBTSocket.getOutputStream().write(sendVal.getBytes());
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            //txtSpeechInput.setText(result.get(0));
                        }else if(result.get(0).contains("right") || result.get(0).contains("Right")||result.get(0).contains("write") || result.get(0).contains("Write")) {
                            String sendVal="C";
                            try {
                                mBTSocket.getOutputStream().write(sendVal.getBytes());
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        else if(result.get(0).contains("Stop") || result.get(0).contains("stop")||result.get(0).contains("no") || result.get(0).contains("No")) {
                            String sendVal="C";
                            try {
                                mBTSocket.getOutputStream().write(sendVal.getBytes());
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                        else if(result.get(0).contains("30") || result.get(0).contains("thirty")||result.get(0).contains("Thirty")) {
                            String sendVal="M";
                            try {
                                mBTSocket.getOutputStream().write(sendVal.getBytes());
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }else if(result.get(0).contains("60") || result.get(0).contains("sixty")||result.get(0).contains("Sixty")) {
                            String sendVal="N";
                            try {
                                mBTSocket.getOutputStream().write(sendVal.getBytes());
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }else if(result.get(0).contains("90") || result.get(0).contains("ninety")||result.get(0).contains("Ninety")) {
                            String sendVal="O";
                            try {
                                mBTSocket.getOutputStream().write(sendVal.getBytes());
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }else if(result.get(0).contains("20") || result.get(0).contains("twenty")||result.get(0).contains("Twenty")) {
                            String sendVal="P";
                            try {
                                mBTSocket.getOutputStream().write(sendVal.getBytes());
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        else if(result.get(0).contains("50") || result.get(0).contains("fifty")||result.get(0).contains("Fifty")) {
                            String sendVal="Q";
                            try {
                                mBTSocket.getOutputStream().write(sendVal.getBytes());
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        else if(result.get(0).contains("80") || result.get(0).contains("eighty")||result.get(0).contains("Eighty")) {
                            String sendVal="R";
                            try {
                                mBTSocket.getOutputStream().write(sendVal.getBytes());
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }else if(result.get(0).contains("close") || result.get(0).contains("Close")||result.get(0).contains("gun")||result.get(0).contains("Gun")) {
                            String sendVal="T";
                            try {
                                mBTSocket.getOutputStream().write(sendVal.getBytes());
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            //txtSpeechInput.setText("Not match");
                        }
                    }
                    break;
                }

            }
        }

    }
}
