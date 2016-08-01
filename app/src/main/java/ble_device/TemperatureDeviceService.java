package ble_device;

import org.greenrobot.eventbus.EventBus;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import EventMessages.ChangeFragmentEvent;
import EventMessages.ToastMessageEvent;
import UI_Fragments.FragmentDisconnectDevices;
import bluetooth_manager.bluetooth.gatt.GattManager;
import bluetooth_service.BleDeviceScanAndConnect;
import bluetooth_service.BluetoothServiceManager;
import bluetooth_service.DeviceCallBacks;

public class TemperatureDeviceService extends Service
{
	private static final String TAG = "TemperatureService";
	
	public String device_name;
	public String device_address;
	private Context cxt;
    private BluetoothServiceManager.ServiceManagerCallback mServiceManagerCallback;
	
	private BleDeviceScanAndConnect thisBleDevice = null;
	private BluetoothGatt mBluetoothGatt = null;
	
	private static final String connectionThreadName = "CONNECTION_THREAD_TEMPERATURE";
	
	public void TemperatureDeviceService_init(Context cnxt, final String deviceName, final String deviceAddress, final GattManager mGattManager, final BluetoothServiceManager.ServiceManagerCallback mServiceManagerCallback)
	{
        Log.d(TAG, "TemperatureDeviceService_init");

        //EventBus.getDefault().register(this);

        this.device_name = deviceName;
        this.device_address = deviceAddress;
        this.cxt = cnxt;
        this.mServiceManagerCallback = mServiceManagerCallback;

//        char divisionChar = ':';//change to '-' if you want your mac to be like 00-15-5D-03-8D-01
//        String UpperCase_address = deviceAddress.toUpperCase();
//        this.device_address = UpperCase_address.replaceAll("(.{2})", "$1"+divisionChar).substring(0,17);

        Log.d(TAG, "TemperatureDeviceService_init : formatted device_address = " + device_address);

        /**
         * Ble device implements all the generic functions for a bluetooth device.
         */
        thisBleDevice = new BleDeviceScanAndConnect(cxt, device_name, device_address, mGattManager, new DeviceCallBacks()
        {

            @Override
            public void onDeviceWriteCharacteristic()
            {
                Log.d(TAG, "onDeviceWriteCharacteristic");
            }

//            @Override
//            public void onDeviceScanFinished(boolean device_found, BluetoothDevice device)
//            {
//                Log.d(TAG, "onDeviceScanFinished");
//                // Scan finished. Now connect to GattServices
//
//                if(device_found == true)
//                {
//                    Log.d(TAG, "Device found. Name = " + device_name + " . Address = " + device_address);
//                    thisBleDevice.deviceConnection();
//                }
//                else
//                {
//                    // Send toast message to UI and stop the connection procedure
//                    EventBus.getDefault().post(new ToastMessageEvent("Device not Found"));
//                }
//
//            }

            @Override
            public void onDeviceReadCharacteristic()
            {
                Log.d(TAG, "onDeviceReadCharacteristic");
            }

            @Override
            public void onDeviceDisconnected()
            {
                Log.d(TAG, "onDeviceDisconnected");

                mServiceManagerCallback.onBLEDeviceServiceDisconnected(deviceName, deviceAddress);
            }

            @Override
            public void onDeviceConnected(BluetoothGatt bluetooth_gatt)
            {
                Log.d(TAG, "onDeviceConnected");
                mBluetoothGatt = bluetooth_gatt;
                mServiceManagerCallback.onBLEDeviceConnected();
            }

            @Override
            public void onDeviceScanFinished(boolean device_found, BluetoothDevice device)
            {
                Log.d(TAG, "onDeviceScanFinished");
                // Scan finished. Now connect to GattServices

                if(device_found)
                {
                    Log.d(TAG, "Device found. Name = " + device_name + " . Address = " + device_address);
                    EventBus.getDefault().post(new ToastMessageEvent("Scan Complete. BLE Device Found"));
                    thisBleDevice.deviceConnection(connectionThreadName, device);
                }
                else
                {
                    // Send toast message to UI and stop the connection procedure
                    EventBus.getDefault().post(new ToastMessageEvent("Device not Found"));
                }
            }
        });

        addDevice(cxt,device_name,device_address);
    }


    private void addDevice(Context cxt, String device_name, String device_address)
    {
        thisBleDevice.deviceScan();
    }



	@Override
	public IBinder onBind(Intent intent) 
	{
		Log.d(TAG, "temperature_device_service onBind");
		
		return mBinder;
	}
	
	
	@Override
	public void onCreate() 
	{
		super.onCreate();
	}
	
	
	@Override
	public void onDestroy() 
	{
		super.onDestroy();
	}
	
	
    @Override
    public boolean onUnbind(Intent intent)
    {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly. In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        disconnectBLE_Device();

        Log.d(TAG, "onUnbind service received");

        return super.onUnbind(intent);
    }
	
	
	@Override
	public void onTaskRemoved(Intent rootIntent) {
		// TODO Auto-generated method stub
		super.onTaskRemoved(rootIntent);
	}
	
	private final IBinder mBinder = new LocalBinder();
	
    public class LocalBinder extends Binder
    {
    	public TemperatureDeviceService getService()
        {
            return TemperatureDeviceService.this;
        }
    }
    
    public void disconnectBLE_Device()
    {
        thisBleDevice.deviceDisconnect(mBluetoothGatt);
    }

}
