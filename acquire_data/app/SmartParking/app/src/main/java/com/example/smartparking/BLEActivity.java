package com.example.smartparking;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BLEActivity extends AppCompatActivity {
    // BLE
    private BluetoothLeScanner mBLEScanner;
    private ScanSettings settings;
    private BluetoothAdapter mBluetoothAdapter;
    private List<ScanFilter> filterList;

    private static final int REQUEST_ENABLE_BT = 1;

    private static Map<String, Integer> beaconMacAddress = new HashMap<String, Integer>();

    // 비콘 맥 주소 수정
    private static void setBeaconMacAddress() {
        beaconMacAddress.put("C1:00:47:00:33:26", 1); // beacon1 ID(13094)
        beaconMacAddress.put("C1:00:47:00:33:2A", 2); // beacon2 ID(13098)
        beaconMacAddress.put("C1:00:47:00:33:24", 3); // beacon3 ID(13092)
    }

    private static String cellNum = "0"; // cell number
    private static int buttonType = 0; // 1: reset, 2: query, 3: save, 4: learn
    private static int modelType = 0; // 5: model A - car in, 6: model B - car out
    private static int mode = 0; // 7: train, 8:test

    private static int setNum = 0; // the number of sets to collect
    private static int setNumForLearning = 0;

    // 버튼 상수화
    // model A: car in, model B: car out
    private final int BTN_RESET = 1;
    private final int BTN_QUERY = 2;
    private final int BTN_SAVE = 3;
    private final int BTN_LEARN = 4;
    private final int BTN_MODEL_A = 5;
    private final int BTN_MODEL_B = 6;
    private final int BTN_TRAIN = 7;
    private final int BTN_TEST = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setBeaconMacAddress();

        // Need Location Permission
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                Utility.PERMISSION_REQUEST_COARSE_LOCATION);

        setupBLE();
        cellNum = "0";
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
            settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
            filterList = new ArrayList<ScanFilter>();
        }
    }

    @Override
    public void onPause() { super.onPause(); }

    @Override
    public void onDestroy() { super.onDestroy(); }

    private void setupBLE() {
        // Use this check to determine whether BLE is supported on the device.
        // Then you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "이 디바이스는 BLE를 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter. For API level 18 and above,
        // get a reference to BluetoothAdapter through BlueToothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "이 디바이스는 BLE를 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // 블루투스 신호 스캔
    protected boolean scanLeDevice(final boolean enable) {
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            if(enable) {
                mBLEScanner.startScan(filterList, settings, mScanCallback);
            }
            else {
                mBLEScanner.stopScan(mScanCallback);
            }
        }
        return enable;
    }

    // BLE signal 수신 시 호출되는 함수
    private ScanCallback mScanCallback = new ScanCallback() {
        // Save signals
        // 0 => model type, 1 ~ 3 => beacon1 ~ beacon3
        SparseIntArray signal = new SparseIntArray();

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            final BluetoothDevice device = result.getDevice();
            Log.i("SCAN1", "[" + device.getName() + "], ByteArray:" +
                    Utility.printByteArray(result.getScanRecord().getBytes()));

            // model 선택 버튼은 반드시 눌렸다고 가정
            if (getModelType() == BTN_MODEL_A || getModelType() == BTN_MODEL_B) {
                signal.put(0, getModelType());
            }

            // 비콘 신호값 저장
            if (beaconMacAddress.containsKey(device.getAddress())) {
                signal.put(beaconMacAddress.get(device.getAddress()), result.getRssi());
            }

            // if all signals are received, then you will access server
            if (signal.size() == beaconMacAddress.size() + 1) {
                Log.i("model_type", "[model]: " + signal.get(0));
                Log.i("beacon1", "[b1]: " + signal.get(1));
                Log.i("beacon2", "[b2]: " + signal.get(2));
                Log.i("beacon3", "[b3]: " + signal.get(3));

                String rssiToUrl = "modelType=" + modelType + "&mode=" + mode + "&b1Rssi=" + signal.get(1)
                        + "&b2Rssi=" + signal.get(2) + "&b3Rssi=" + signal.get(3);

                signal.clear();

                String URL;
                switch(buttonType) {
                    case BTN_QUERY:
                        URL = Utility.URL + "query?" + rssiToUrl;
                        new MainActivity.NetworkTask(URL, null).execute();
                        break;
                    case BTN_SAVE:
                        if (setNum > 0) {
                            setNum--;
                            URL = Utility.URL + "save?" + rssiToUrl + "&cellNum=" + cellNum + "&setNumForLearning="
                                    + setNumForLearning;
                            Log.i("url", "url: " + URL);
                            new MainActivity.NetworkTask(URL, null).execute();
                        }
                        else {
                            URL = Utility.URL + "learn?modelType=" + modelType + "&setNumForLearning=" + setNumForLearning;
                            scanLeDevice(false);
                            setNumForLearning = 0;
//                            new MainActivity.NetworkTask(URL, null).execute();
                        }
                        break;
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("SCAN2", "ScanResult - Results: " + sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("SCAN", "Scan Failed:Error Code: " + errorCode);
        }
    };

    // setter : Cell number & set number
    protected void setCellAndSetNumber(String cellNum, int setNum) {
        this.cellNum = cellNum;
        this.setNum = setNum;
        this.setNumForLearning = setNum;
    }

    protected void setModelType(int modelType) { this.modelType = modelType; }

    public static int getModelType() { return modelType; }

    protected void setMode(int mode) { this.mode = mode; }

    public static int getMode() { return mode; }

    protected void setButtonType(int buttonType) { this.buttonType = buttonType; }

    public static int getButtonType() { return buttonType; }

    public static int getSetNum() { return setNum; }

    public static int getSetNumForLearning() { return setNumForLearning; }

    // Need Location Permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Utility.PERMISSION_REQUEST_COARSE_LOCATION) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission granted
            }
            else {
                // Alert the user that this application requires the location permission to perform the scan.
            }
        }
    }
}
