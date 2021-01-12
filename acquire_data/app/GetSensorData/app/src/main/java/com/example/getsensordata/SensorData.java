package com.example.getsensordata;

public class SensorData {
    private double magnetValX, magnetValY, magnetValZ;
    private double accelValX, accelValY, accelValZ;
    private double orientValX, orientValY, orientValZ;
    private double gyroValX, gyroValY, gyroValZ;
    private double pressVal;

//    public String getMagnetVal() {
//        String val;
//        val = String.format("%.3f", magnetValX) + "," + String.format("%.3f", magnetValY)
//                + "," + String.format("%.3f", magnetValZ);
//        return val;
//    }

    public String getMagnetVal() {
        String val;
        val = magnetValX + "," + magnetValY + "," + magnetValZ;
        return val;
    }

    public void setMagnetVal(double magnetValX, double magnetValY, double magnetValZ) {
        this.magnetValX = magnetValX;
        this.magnetValY = magnetValY;
        this.magnetValZ = magnetValZ;
    }

//    public String getAccelVal() {
//        String val;
//        val = String.format("%.3f", accelValX) + "," + String.format("%.3f", accelValY)
//                + "," + String.format("%.3f", accelValZ);
//        return val;
//    }

    public String getAccelVal() {
        String val;
        val = accelValX + "," + accelValY + "," + accelValZ;
        return val;
    }

    public void setAccelVal(double accelValX, double accelValY, double accelValZ) {
        this.accelValX = accelValX;
        this.accelValY = accelValY;
        this.accelValZ = accelValZ;
    }

//    public String getOrientVal() {
//        String val;
//        val = String.format("%.3f", orientValX) + "," + String.format("%.3f", orientValY)
//                + "," + String.format("%.3f", orientValZ);
//        return val;
//    }

    public String getOrientVal() {
        String val;
        val = orientValX + "," + orientValY + "," + orientValZ;
        return val;
    }

    public void setOrientVal(double orientValX, double orientValY, double orientValZ) {
        this.orientValX = orientValX;
        this.orientValY = orientValY;
        this.orientValZ = orientValZ;
    }

//    public String getGyroVal() {
//        String val;
//        val = String.format("%.3f", gyroValX) + "," + String.format("%.3f", gyroValY)
//                + "," + String.format("%.3f", gyroValZ);
//        return val;
//    }

    public String getGyroVal() {
        String val;
        val = gyroValX + "," + gyroValY + "," + gyroValZ;
        return val;
    }

    public void setGyroVal(double gyroValX, double gyroValY, double gyroValZ) {
        this.gyroValX = gyroValX;
        this.gyroValY = gyroValY;
        this.gyroValZ = gyroValZ;
    }

//    public String getPressVal() {
//        String val;
//        val = String.format("%.3f", pressVal);
//        return val;
//    }

    public String getPressVal() {
        String val;
        val = Double.toString(pressVal);
        return val;
    }

    public void setPressVal(double pressVal) { this.pressVal = pressVal; }
}
