package com.klab.sin.robotcommander;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

import com.klab.sin.robotcommander.bluetooth.BluetoothHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RoboConsoleActivity extends AppCompatActivity {
    public static final String BLUETOOTH_MAC_ADDRESS_KEY = "BLUETOOTH_MAC_ADDRESS_KEY";
    private static final String TAG = RoboConsoleActivity.class.getSimpleName();

    private Timer readCommandsFromRobotTimer;
    private Timer updateImageTimer;

    private BluetoothHelper bluetoothHelper;
    private String macAddressBluetoothDevice;

    private BlockingQueue<Command> commandsFromRobotQueue = new LinkedBlockingQueue<>();

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robo_console);

        imageView = (ImageView) findViewById(R.id.imageView);

        bluetoothHelper = new BluetoothHelper(BluetoothAdapter.getDefaultAdapter());
        macAddressBluetoothDevice = bluetoothHelper.getMacAddressFromName(
                getIntent().getStringExtra(BLUETOOTH_MAC_ADDRESS_KEY));
    }

    private void startDialog() {
        final long period = 100;
        readCommandsFromRobotTimer = new Timer();

        readCommandsFromRobotTimer.schedule(new TimerTask() {
            private InputStream in;

            @Override
            public void run() {
                if (in == null) {
                    in = bluetoothHelper.reconnect(macAddressBluetoothDevice);
                }

                if (in != null) {
                    try {
                        while (in.available() > 0) {
                            char command = (char) in.read();
                            char end = (char) in.read();

                            if (end == '.') {
                                switch (command) {
                                    case '1':
                                        commandsFromRobotQueue.put(Command.FORWARD);
                                        break;
                                    case '2':
                                        commandsFromRobotQueue.put(Command.BACK);
                                        break;
                                    case '3':
                                        commandsFromRobotQueue.put(Command.PAUSE);
                                        break;
                                    default:
                                        break;
                                }
                            }
                            Log.d(TAG, "Received message: " + command);
                        }
                    } catch (Exception e) {
                        in = bluetoothHelper.reconnect(macAddressBluetoothDevice);
                    }
                }
            }
        }, 100, period);
    }

    @Override
    public void onResume() {
        super.onResume();

        startDialog();
        startUpdateImage();
        //testImageUpdate();
    }

    private void testImageUpdate() {
        try {
            commandsFromRobotQueue.put(Command.FORWARD);
            commandsFromRobotQueue.put(Command.BACK);
            commandsFromRobotQueue.put(Command.PAUSE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startUpdateImage() {
        final long period = 50;
        updateImageTimer = new Timer();
        updateImageTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Command command = null;
                try {
                    command = commandsFromRobotQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                final Drawable image;
                if (command == Command.FORWARD) {
                    image = getResources().getDrawable(R.drawable.forward);
                } else if (command == Command.BACK) {
                    image = getResources().getDrawable(R.drawable.back);
                } else if (command == Command.PAUSE) {
                    image = getResources().getDrawable(R.drawable.pause);
                } else {
                    return;
                }

                if (image == null) {

                }

                final Drawable finalImage = image;
                runOnUiThread(new Runnable() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void run() {
                        imageView.setImageDrawable(finalImage);
                    }
                });
            }
        }, 100, period);
    }


    @Override
    protected void onPause() {
        super.onPause();
        readCommandsFromRobotTimer.cancel();
        bluetoothHelper.disconnect();
    }

    private enum Command {
        FORWARD,
        BACK,
        PAUSE
    }
}
