package com.example.smartparking;

public class Utility {
    static public final String URL = "http://YOUR_PATH/";
    
    static final public int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    static public String printByteArray(byte[] bytes) {
        // print BLE bytes to array
        String res = "[";
        for (Byte obj : bytes) {
            res += String.format("%02X", obj);
        }
        res += "]";
        return res;
    }
}
