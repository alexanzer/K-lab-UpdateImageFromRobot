package com.klab.sin.robotcommander;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.klab.sin.robotcommander.bluetooth.BluetoothHelper;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private BluetoothHelper bluetoothHelper;
    private ArrayAdapter<String> devicesListAdapter;
    private Timer updatingDevicesListTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ListView devicesListView = (ListView) findViewById(R.id.devicesListView);
        devicesListAdapter = new ArrayAdapter<>(
                this,
                R.layout.list_device_item,
                R.id.list_device_item,
                new ArrayList<String>());
        devicesListView.setAdapter(devicesListAdapter);
        devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), RoboConsoleActivity.class);
                intent.putExtra(RoboConsoleActivity.BLUETOOTH_MAC_ADDRESS_KEY, devicesListAdapter.getItem(position).toString());
                Log.d(TAG, "Start GraphicActivity.");
                startActivity(intent);
            }
        });

        bluetoothHelper = new BluetoothHelper(BluetoothAdapter.getDefaultAdapter());
    }

    @Override
    protected void onResume() {
        super.onResume();
        startUpdatingDevicesListTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        updatingDevicesListTimer.cancel();
    }

    private void startUpdatingDevicesListTimer() {
        updatingDevicesListTimer = new Timer();
        updatingDevicesListTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                final List<String> devices = bluetoothHelper.getNamesOfDevices();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        devicesListAdapter.clear();
                        devicesListAdapter.addAll(new ArrayList<>(devices));
                    }
                });

            }
        }, 100, 5000);
    }

    private Context getActivity() {
        return this;
    }
}
