package com.example.bluetoothle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import EventMessages.ScanDeviceDetailsMessageEvent;

/**
 * Created by Sushant Mainali on 4/17/2016.
 */
public class ScanDevices
{

    private static String TAG = ScanDevices.class.getName().toString();

    private static int scan_time = MainActivity.DEFAULT_SCAN_TIME;

    ScanDevices(int scan_time)
    {
        this.scan_time = scan_time;
    }

    public void startScan()
    {
        // if scan is not in progress then directly start scan
        if(mScanning == false)
        {
            scanLeDevice(true);
        }
        else
        {
            // previous scan in progress. Stop it first
            scanLeDevice(false);

            // start another scan
            scanLeDevice(true);
        }
    }


    private Handler mHandler = new Handler();
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private boolean mScanning = false;

    private synchronized void scanLeDevice(final boolean enable)
    {
        if (enable)
        {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, scan_time);
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }
        else
        {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback()
    {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes)
        {
            String device_address = bluetoothDevice.getAddress();
            String device_name = bluetoothDevice.getName();

            EventBus.getDefault().post(new ScanDeviceDetailsMessageEvent(device_name,device_address));

            Log.d(TAG, "Device found, Name = " + device_name + "  .Device Address = " + device_address + "  . RSSI = " + i + ".  scanRecord = " + bytes);

        }
    };
}
