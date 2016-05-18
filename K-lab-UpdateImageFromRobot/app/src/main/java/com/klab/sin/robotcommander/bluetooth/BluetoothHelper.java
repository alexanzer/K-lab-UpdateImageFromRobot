package com.klab.sin.robotcommander.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by alexanzer on 09/04/16.
 */
public class BluetoothHelper {
    private static final String TAG = BluetoothHelper.class.getSimpleName();
    final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // - -
    private BluetoothAdapter adapter;
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private InputStream in;

    public BluetoothHelper(BluetoothAdapter bluetoothAdapter) {
        this.adapter = bluetoothAdapter;
    }

    public InputStream connect(String macAddress) {
        device = getBluetoothDevice(macAddress);
        try {
            socket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            socket.connect();
            in = socket.getInputStream();

            return in;
        } catch (IOException e) {
            e.printStackTrace();
            disconnect();
        }

        return null;
    }

    public InputStream reconnect(String macAddress) {
        disconnect();
        return connect(macAddress);
    }

    public String getMacAddressFromName(String name) {
        String[] terms = name.split("   ");
        if (terms != null && terms.length == 2) {
            return terms[1];
        }

        return null;
    }

    public List<String> getNamesOfDevices() {
        final Set<BluetoothDevice> devices = getDevices();
        final List<String> names = new ArrayList<>();

        for (BluetoothDevice device : devices) {
            names.add(device.getName() + "   " + device.getAddress());
            Log.d(TAG, "Device: " + device.getName() + "  " + device.getAddress());
        }

        return names;
    }

    private Set<BluetoothDevice> getDevices() {
        adapter.startDiscovery();
        final Set<BluetoothDevice> devices = adapter.getBondedDevices();
        adapter.cancelDiscovery();

        return devices;
    }

    private BluetoothDevice getBluetoothDevice(String macAddress) {
        if (macAddress == null) return null;
        final Set<BluetoothDevice> devices = getDevices();

        for (BluetoothDevice device : devices) {
            if (macAddress.equals(device.getAddress())) {
                return device;
            }
        }

        return null;
    }

    public void disconnect() {
        try {
            if (in != null)
                in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        in = null;
        socket = null;
    }
}
