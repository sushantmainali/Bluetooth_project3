package bluetooth_service;

import java.util.ArrayList;
import java.util.HashMap;

import org.greenrobot.eventbus.EventBus;

import EventMessages.DisconnectedDeviceMessageEvent;
import ble_device.TemperatureDeviceService;
import EventMessages.ToastMessageEvent;
import bluetooth_manager.bluetooth.gatt.GattManager;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.StringDef;
import android.util.Log;

import com.example.bluetoothle.MainActivity;
import com.example.bluetoothle.R;

/**
 * Singleton class to connect one device at a time
 */
public class BluetoothServiceManager
{
    private static BluetoothServiceManager mBluetoothServiceManager = null;

    private final static String TAG = "BluetoothServiceManager";

    protected BluetoothServiceManager()
    {
        // Exist only to default instantiation
    }

    /**
     * Get the instance of singleton class. If not created before then
     * create first and return the instance.
     * @return : return BluetoothServiceManager
     */
    public static BluetoothServiceManager  getInstance()
    {
        if(mBluetoothServiceManager == null)
        {
            mBluetoothServiceManager = new BluetoothServiceManager();
        }

        return mBluetoothServiceManager;
    }

    /**
     * Add the device info. Variable use to Stop service and gatt
     */
    public static class BluetoothSeviceInfo
    {
        public String device_address = "XX:XX:XX:XX:XX";
        public String device_name = "Unknown";
        public BluetoothDevice device = null;
        public Service started_service = null;
        public ServiceConnection service_connection = null;
        public ComponentName mComponentName = null;
        public Context cxts = null;

    }


    public interface ServiceManagerCallback
    {
        // called when ble device is connected
        public void onBLEDeviceConnected();

        // called when ble device is disconnected
        public void onBLEDeviceDisconnected();

        // called when service is disconnected by calling onUnbindService()
        public void onBLEDeviceServiceDisconnected(String device_name, String device_address);
    };

    private static volatile boolean service_being_created = false;


    // Manager keeping track of services and devices
    public static ArrayList<BluetoothSeviceInfo> started_device_service_list = new ArrayList<>();


    public static void startDeviceServiceAndConnect(final Context cxt, final String device_name, final String device_address, final GattManager gatt_manager)
    {
        Log.i(TAG, "startDeviceServiceAndConnect");

        // check if previous connection is in progress
        if(service_being_created == false)
        {
            int array_size = started_device_service_list.size();
            boolean device_connected_before = false;

            for(int i=0; i<array_size; i++)
            {
                // Check if current device address is connected before
                if(started_device_service_list.get(i).device_address.equals(device_address))
                {
                    device_connected_before = true;

                    //No need to go further. return from loop
                    break;
                }
            }

            // Only start connection if device isn't connected before
            if(device_connected_before == false)
            {
                service_being_created = true;

                // Start service here
                // ServiceManagerCallback is implemented to make sure that service is unbind after disconnection happened
                //
                startTemperatureService(cxt, device_name, device_address, gatt_manager, new ServiceManagerCallback()
                {
                    @Override
                    public void onBLEDeviceConnected()
                    {
                        Log.d(TAG, "onBLEDeviceConnected : Bluetooth device is connected");
                    }

                    @Override
                    public void onBLEDeviceDisconnected()
                    {
                        Log.d(TAG, "onBLEDeviceConnected : Bluetooth device is disconnected");
                    }

                    @Override
                    public void onBLEDeviceServiceDisconnected(String device_name, String device_address)
                    {
                        Log.d(TAG, "onBLEDeviceConnected : Sevice for Bluetooth device is disconnected");

                        // Remove the service from the list
                        int list_size = started_device_service_list.size();

                        if(list_size > 0)
                        {
                            for(int i=0; i<list_size; i++)
                            {
                                // If the device address matches the added service than add services reference to array
                                if(started_device_service_list.get(i).device_address.equals(device_address))
                                {
                                    Log.d(TAG, "Service removed to the BluetoothServiceManager list");

                                    EventBus.getDefault().post(new DisconnectedDeviceMessageEvent(started_device_service_list.get(i).device_name,started_device_service_list.get(i).device_address));

                                    started_device_service_list.remove(i);
                                }
                                else
                                {
                                    Log.e(TAG, "Service NOT removed to the BluetoothServiceManager list");
                                }
                            }
                        }
                    }
                });
            }
            else
            {
                EventBus.getDefault().post(new ToastMessageEvent("Bluetooth Device is already connected"));
            }
        }
        else
        {
            EventBus.getDefault().post(new ToastMessageEvent("Bluetooth service allocation in progress"));
        }
    }


    public void disconnectDevices(String device_address)
    {
        Service disconnectingService = null;
        ServiceConnection mServiceConnection = null;
        Context mContext = null;

        int array_size = started_device_service_list.size();

        for(int i=0; i<array_size; i++)
        {
            if(started_device_service_list.get(i).device_address.equals(device_address))
            {
                disconnectingService = started_device_service_list.get(i).started_service;
                mServiceConnection = started_device_service_list.get(i).service_connection;
                mContext = started_device_service_list.get(i).cxts;

                Log.d(TAG, "Disconnecting device name = " + started_device_service_list.get(i).device_name +
                        ".  Device address = " + started_device_service_list.get(i).device_address);

                break;
            }
        }

        if((mServiceConnection != null) && (mContext != null))
        {
            Log.d(TAG, "Unbind service service for connector = " + mServiceConnection);
            mContext.unbindService(mServiceConnection);
        }
        else
        {
            Log.e(TAG, "disconnectDevices : ALERT ALERT device service connection reference is null");
        }
    }


    private static void startTemperatureService(final Context cxt, final String device_name, final String device_address, final GattManager gatt_manager, final ServiceManagerCallback mServiceManagerCallback)
    {
        ServiceConnection mTemperatureServiceConnection = new ServiceConnection()
        {
            private boolean bound = false;

            @Override
            public void onServiceDisconnected(ComponentName name)
            {
                Log.d(TAG, "onServiceDisconnected : Service disconnected. Class name = " + name.getClassName());
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service)
            {
                Log.d(TAG, "onServiceConnected : service bonded");

                TemperatureDeviceService mTemperatureDeviceService = ((TemperatureDeviceService.LocalBinder) service).getService();

                service_being_created = false;

                // start the service and connection procedure
                mTemperatureDeviceService.TemperatureDeviceService_init(cxt, device_name, device_address, gatt_manager, mServiceManagerCallback);

                int list_size = started_device_service_list.size();

                bound = true;

                // Added the service reference based on the devicec address
                if(list_size > 0)
                {
                    for(int i=0; i<list_size; i++)
                    {
                        // If the device address matches the added service than add services reference to array
                        if(started_device_service_list.get(i).device_address.equals(mTemperatureDeviceService.device_address))
                        {
                            Log.d(TAG, "Service added to the BluetoothServiceManager list");
                            started_device_service_list.get(i).started_service = mTemperatureDeviceService;
                            started_device_service_list.get(i).mComponentName = name;
                        }
                        else
                        {
                            Log.e(TAG, "Service NOT added to the BluetoothServiceManager list");
                        }
                    }
                }
            }
        };

        // add the Service in the manager and manipulate it later
        BluetoothSeviceInfo device_temperature = new BluetoothSeviceInfo();
        device_temperature.device_address = device_address;
        device_temperature.device_name = device_name;
        device_temperature.service_connection = mTemperatureServiceConnection;
        device_temperature.cxts = cxt;
        started_device_service_list.add(device_temperature);

        // Start the service for ble device
        Intent gattServiceIntent = new Intent(cxt, TemperatureDeviceService.class);
        boolean service_status = cxt.bindService(gattServiceIntent, mTemperatureServiceConnection, Context.BIND_AUTO_CREATE);

        if(service_status)
        {
            Log.d(TAG, "service CREATED");
        }
        else
        {
            Log.d(TAG, "service NOT CREATED");
        }
    }
}