package com.example.getsensordata;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    SensorManager sensorManager;

    SensorEventListener magnetListener;
    SensorEventListener accelListener;
    SensorEventListener orientListener;
    SensorEventListener gyroListener;
    SensorEventListener pressListener;

    Sensor magneticSensor;
    Sensor accelSensor;
    Sensor orientSensor;
    Sensor gyroSensor;
    Sensor pressSensor;

    TextView timeVal;
    TextView magnetValX, magnetValY, magnetValZ;
    TextView accelValX, accelValY, accelValZ;
    TextView orientValX, orientValY, orientValZ;
    TextView gyroValX, gyroValY, gyroValZ;
    TextView pressVal;

    EditText editFileNum;

    // TODO: 없어도 되긴 하는뎅
    private static int count = 0;  // 데이터 번호

    // 저장 전 delay time (ms)
    private int time = 4000;

    private SensorData sensorData = new SensorData();

    private boolean flag = true;  // true: Start, false: Stop

    private static String fileName;
    private static String otherFileName;

    private Button btn_in2out;
    private Button btn_out2in;
    private Button btn_in;
    private Button btn_out;
    private Button btn_round;
    private Button btn_start;
    private Button btn_stop;

    private static int mode = 0;  // 1: btn_in2out, 2: btn_out2in, 3: btn_in, 4: btn_out, 5: btn_round
    private static int state = 0;  // 6: btn_start, 7: btn_stop

    // 버튼 상수화
    private static final int BTN_IN2OUT = 1;
    private static final int BTN_OUT2IN = 2;
    private static final int BTN_IN = 3;
    private static final int BTN_OUT = 4;
    private static final int BTN_ROUND = 5;
    private static final int BTN_START = 6;
    private static final int BTN_STOP = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initScreen();

        // storage access permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        orientSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        pressSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);


        magnetListener = new MagnetListener();  // 자기장 센서 리스너 인스턴스
        accelListener = new AccelListener();  // 가속도 센서 리스너 인스턴스
        orientListener = new OrientListener();  // 방향 센서 리스너 인스턴스
        gyroListener = new GyroListener();  // 자이로 센서 리스너 인스턴스
        pressListener = new PressListener();  // 기압 센서 리스너 인스턴스
    }

    private void initScreen() {
        timeVal = findViewById(R.id.time_val);

        magnetValX = findViewById(R.id.magnet_x_val);
        magnetValY = findViewById(R.id.magnet_y_val);
        magnetValZ = findViewById(R.id.magnet_z_val);

        accelValX = findViewById(R.id.accel_x_val);
        accelValY = findViewById(R.id.accel_y_val);
        accelValZ = findViewById(R.id.accel_z_val);

        orientValX = findViewById(R.id.orient_x_val);
        orientValY = findViewById(R.id.orient_y_val);
        orientValZ = findViewById(R.id.orient_z_val);

        gyroValX = findViewById(R.id.gyro_x_val);
        gyroValY = findViewById(R.id.gyro_y_val);
        gyroValZ = findViewById(R.id.gyro_z_val);

        pressVal = findViewById(R.id.press_val);

        editFileNum = findViewById(R.id.edit_file_num);

        btn_in2out = findViewById(R.id.btn_in2out);
        btn_out2in = findViewById(R.id.btn_out2in);
        btn_in = findViewById(R.id.btn_in);
        btn_out = findViewById(R.id.btn_out);
        btn_round = findViewById(R.id.btn_round);
        btn_start = findViewById(R.id.btn_start);
        btn_stop = findViewById(R.id.btn_stop);

        // clickListener
        btn_in2out.setOnClickListener(clickListener);
        btn_out2in.setOnClickListener(clickListener);
        btn_in.setOnClickListener(clickListener);
        btn_out.setOnClickListener(clickListener);
        btn_round.setOnClickListener(clickListener);
        btn_start.setOnClickListener(clickListener);
        btn_stop.setOnClickListener(clickListener);

        defaultButtonColor();

        timeVal.setText(String.format("( START %.1f초 뒤에 저장 시작 )", (double)time / 1000));
    }

    // 버튼 타입에 따라서 정수형으로 리턴
    private int returnButtonType(View v) {
        if (v == btn_in2out) return BTN_IN2OUT;
        else if (v == btn_out2in) return BTN_OUT2IN;
        else if (v == btn_in) return BTN_IN;
        else if (v == btn_out) return BTN_OUT;
        else if (v == btn_round) return BTN_ROUND;
        else if (v == btn_start) return BTN_START;
        else return BTN_STOP;
    }

    // 버튼 기본 색 설정
    private void defaultButtonColor() {
        btn_in2out.setBackgroundColor(Color.GRAY);
        btn_out2in.setBackgroundColor(Color.GRAY);
        btn_in.setBackgroundColor(Color.GRAY);
        btn_out.setBackgroundColor(Color.GRAY);
        btn_round.setBackgroundColor(Color.GRAY);
        btn_start.setBackgroundColor(Color.GRAY);
        btn_stop.setBackgroundColor(Color.GRAY);
    }

    // 버튼 색 설정
    private void setButtonColor(View view) {
        defaultButtonColor();

        switch (getMode()) {
            case BTN_IN2OUT:
                btn_in2out.setBackgroundColor(Color.RED);
                break;

            case BTN_OUT2IN:
                btn_out2in.setBackgroundColor(Color.RED);
                break;

            case BTN_IN:
                btn_in.setBackgroundColor(Color.RED);
                break;

            case BTN_OUT:
                btn_out.setBackgroundColor(Color.RED);
                break;

            case BTN_ROUND:
                btn_round.setBackgroundColor(Color.RED);
                break;
        }
        switch (getState()) {
            case BTN_START:
                btn_start.setBackgroundColor(Color.RED);
                break;

            case BTN_STOP:
                btn_stop.setBackgroundColor(Color.RED);
                break;
        }
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(returnButtonType(v)) {
                case BTN_IN2OUT:
                    setMode(BTN_IN2OUT);
                    setButtonColor(v);
                    Toast.makeText(MainActivity.this, "[ IN -> OUT ] is selected!", Toast.LENGTH_SHORT).show();
                    fileName = "in_out_";
                    break;

                case BTN_OUT2IN:
                    setMode(BTN_OUT2IN);
                    setButtonColor(v);
                    Toast.makeText(MainActivity.this, "[ OUT -> IN ] is selected!", Toast.LENGTH_SHORT).show();
                    fileName = "out_in_";
                    break;

                case BTN_IN:
                    setMode(BTN_IN);
                    setButtonColor(v);
                    Toast.makeText(MainActivity.this, "[ IN ] is selected!", Toast.LENGTH_SHORT).show();
                    fileName = "in_";
                    break;

                case BTN_OUT:
                    setMode(BTN_OUT);
                    setButtonColor(v);
                    Toast.makeText(MainActivity.this, "[ OUT ] is selected!", Toast.LENGTH_SHORT).show();
                    fileName = "out_";
                    break;

                case BTN_ROUND:
                    setMode(BTN_ROUND);
                    setButtonColor(v);
                    Toast.makeText(MainActivity.this, "[ ROUND ] is selected!", Toast.LENGTH_SHORT).show();
                    fileName = "round_";
                    break;

                case BTN_START:
                    setState(BTN_START);
                    setButtonColor(v);
                    Toast.makeText(MainActivity.this, "Start saving!", Toast.LENGTH_SHORT).show();

                    fileName = fileName + editFileNum.getText().toString();
                    otherFileName = fileName + "_others.csv";
                    fileName = fileName + ".csv";

                    try {
                        startSensorDetection();
                    } catch (IOException e) {
                        Log.i("BTN_START error: ", "IOException");
                        e.printStackTrace();
                    }

                    break;

                case BTN_STOP:
                    setState(BTN_STOP);
                    setButtonColor(v);
                    Toast.makeText(MainActivity.this, "Stop saving!", Toast.LENGTH_SHORT).show();
                    stopSensorDetection();
                    break;
            }
        }
    };

    private void startSensorDetection() throws IOException {
        flag = true;
        count = 0;
        sensorManager.registerListener(magnetListener, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(accelListener, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(orientListener, orientSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(gyroListener, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(pressListener, pressSensor, SensorManager.SENSOR_DELAY_NORMAL);
        timerTask();
    }

    private void stopSensorDetection() {
        flag = false;
        sensorManager.unregisterListener(magnetListener);
        sensorManager.unregisterListener(accelListener);
        sensorManager.unregisterListener(orientListener);
        sensorManager.unregisterListener(gyroListener);
        sensorManager.unregisterListener(pressListener);
    }

    // 특정 시간에 1번씩 TimerTask가 실행
    private void timerTask() {
        final Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                // BTN_STOP 누를 때 timer 해제
                if(flag == false)
                    timer.cancel();
                else {
                    count++;
                    String val = count + "," + sensorData.getMagnetVal() + "\n";
                    String otherVal = count + "," + sensorData.getAccelVal() + "," + sensorData.getOrientVal()
                            + "," + sensorData.getGyroVal() + "," + sensorData.getPressVal() + "\n";
                    try {
                        saveData(val, fileName);
                        saveData(otherVal, otherFileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        // time ms 뒤에 시작 후 0.01초마다 갱신
        timer.scheduleAtFixedRate(timerTask, time, 10);
    }

    // 자기장 센서 값이 바뀔 때마다 호출됨
    private class MagnetListener implements SensorEventListener {
        public void onSensorChanged(SensorEvent event) {
            double xVal = event.values[0];
            double yVal = event.values[1];
            double zVal = event.values[2];

            magnetValX.setText(String.format("%.3f", xVal));
            magnetValY.setText(String.format("%.3f", yVal));
            magnetValZ.setText(String.format("%.3f", zVal));

            sensorData.setMagnetVal(xVal, yVal, zVal);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) { }
    }

    // 가속도 센서 값이 바뀔 때마다 호출됨
    private class AccelListener implements SensorEventListener {
        public void onSensorChanged(SensorEvent event) {
            double xVal = event.values[0];
            double yVal = event.values[1];
            double zVal = event.values[2];

            accelValX.setText(String.format("%.3f", xVal));
            accelValY.setText(String.format("%.3f", yVal));
            accelValZ.setText(String.format("%.3f", zVal));

            sensorData.setAccelVal(xVal, yVal, zVal);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) { }
    }

    // 방향 센서 값이 바뀔 때마다 호출됨
    private class OrientListener implements SensorEventListener {
        public void onSensorChanged(SensorEvent event) {
            double xVal = event.values[0];
            double yVal = event.values[1];
            double zVal = event.values[2];

            orientValX.setText(String.format("%.3f", xVal));
            orientValY.setText(String.format("%.3f", yVal));
            orientValZ.setText(String.format("%.3f", zVal));

            sensorData.setOrientVal(xVal, yVal, zVal);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) { }
    }

    // 자이로 센서 값이 바뀔 때마다 호출됨
    private class GyroListener implements SensorEventListener {
        public void onSensorChanged(SensorEvent event) {
            double xVal = event.values[0];
            double yVal = event.values[1];
            double zVal = event.values[2];

            gyroValX.setText(String.format("%.3f", xVal));
            gyroValY.setText(String.format("%.3f", yVal));
            gyroValZ.setText(String.format("%.3f", zVal));

            sensorData.setGyroVal(xVal, yVal, zVal);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) { }
    }

    // 기압 센서 값이 바뀔 때마다 호출됨
    private class PressListener implements SensorEventListener {
        public void onSensorChanged(SensorEvent event) {
            double val = event.values[0];

            pressVal.setText(String.format("%.3f", val));

            sensorData.setPressVal(val);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) { }
    }

    private void saveData(String val, String fileName) throws IOException {
        File file = new File(getExternalFilesDir(null), fileName);
        FileOutputStream stream = new FileOutputStream(file, true);
        try {
            stream.write(val.getBytes());
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getState() { return state; }

    protected void setState(int state) { this.state = state; }

    public static int getMode() { return mode; }

    protected void setMode(int mode) { this.mode = mode; }
}
