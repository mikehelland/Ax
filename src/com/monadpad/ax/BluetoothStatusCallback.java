package com.monadpad.ax;

/**
 * User: m
 * Date: 6/26/13
 * Time: 8:05 AM
 */
public abstract class BluetoothStatusCallback {

    public abstract void newStatus(String status, int deviceI);
    public abstract void newData(String data, int deviceI);
    public abstract void onConnected(BluetoothFactory.ConnectedThread connection);


}
