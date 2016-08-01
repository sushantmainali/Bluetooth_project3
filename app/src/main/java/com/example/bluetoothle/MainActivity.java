package com.example.bluetoothle;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import EventMessages.AddDeviceDetailsMessageEvent;
import EventMessages.DisconnectDeviceMessageEvent;
import EventMessages.DisconnectedDeviceMessageEvent;
import bluetooth_manager.bluetooth.gatt.GattManager;
import bluetooth_service.BluetoothServiceManager;

import UI_Fragments.FragmentAddDevice;
import UI_Fragments.FragmentConnectDeviceList;
import UI_Fragments.FragmentDisconnectDevices;
import UI_Fragments.FragmentRemoveDevicesList;
import UI_Fragments.FragmentScanBleDevices;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends Activity 
{

    /**
     * Hash map to store device Device Name and MAC address.
     * Device name is the Key for each entries. MAC address is associated with each key "Device Name"
     * Hash Map is populate in onResume function and other fragments refer to this global variable
     */
    public static HashMap<String, String> device_info_hashMap = new HashMap<String, String>();

    /**
     * This string ArrayList contains the "Device Name" and "Device MAC Address" separated by just "   "
     * This is sent to the FragmentDeviceList and FragmentRemoveDeviceList as an intent
     */
    public static ArrayList<String> listView_device_lists = new ArrayList<String>();

    private static final String TAG = MainActivity.class.getName().toString();

    private Button addDeviceButton;
    private Button closeButton;
    private Button connectButton;
    private Button disconnectButton;
    private Button removeDevicesButton;
    private Button scanDeviceButton;

    public static GattManager mGattManager = new GattManager();

    public static int DEFAULT_SCAN_TIME = 10000;
    public ScanDevices mScanDevice = new ScanDevices(DEFAULT_SCAN_TIME);

    final MyReceiver receiver = new MyReceiver();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // getting the reference of buttons from main activity
        closeButton = (Button) findViewById(R.id.button_close);
        addDeviceButton = (Button) findViewById(R.id.button_setting);
        connectButton = (Button) findViewById(R.id.button_connect);
        disconnectButton = (Button) findViewById(R.id.button_disconnect);
        removeDevicesButton = (Button) findViewById(R.id.button_remove_devices);
        scanDeviceButton = (Button) findViewById(R.id.button_scan);

        EventBus.getDefault().register(this);
        // Register for the intent coming from FragmentRemoveDeviceList and FragmentDeviceList
        registerBroadcastReceiver();

        Log.d(TAG, "device inforamtion hashmap size is " + device_info_hashMap.size());

        // register for on button clicked
        buttonClicklistener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();

        unregisterReceiver(receiver);

        EventBus.getDefault().unregister(this);

        Log.i(TAG, "onDestroy : executing OnDestroy");
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.i(TAG, "onResume : executing onResume");
        getDeviceAddressFromPreferences();
    }

    @Override
    public void onBackPressed()
    {
        Log.e(TAG, "onBackPressed: Back button is disabled");
    }

    /**
     * Add the onClickListener to buttons
     */
    private void buttonClicklistener()
    {
        // When pressed open the fragmentAddDevice to add the devices
        addDeviceButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onCreate (setOnClickListener) : Setting_button is pressed ");

                FragmentAddDevice fr = (FragmentAddDevice) new FragmentAddDevice();
                setMainFragment(fr);
            }
        });

        // When pressed close the application but keep running the services
        closeButton.setOnClickListener(new View.OnClickListener() {

            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onCreate (setOnClickListener) : Close_button is pressed ");
                addDeviceButton.setTextColor(getResources().getColor(R.color.Red));
                finish();
            }
        });

        // When pressed open the fragmentEmptyText(Dog Picture)
        disconnectButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Fragment fr = new FragmentDisconnectDevices();
                setMainFragment(fr);
            }
        });

        // When pressed open the FragmentDeviceList to connect to the added devices
        connectButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {

                FragmentConnectDeviceList fr = new FragmentConnectDeviceList();
                setMainFragment(fr);
                String key = "temp";
                if(device_info_hashMap.isEmpty())
                {
                    Log.e(TAG, "connectButton.setOnClickListener : HashMap is EMPTY !!!!!!!!!");
                }
                else
                {
                    Log.i(TAG, "connectButton.setOnClickListener : HashMap Stored Device name is " + device_info_hashMap.get(key));
                }
            }
        });

        // When pressed open the fragmentRemoveDeviceList to remove added devices
        removeDevicesButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                FragmentRemoveDevicesList fr = new FragmentRemoveDevicesList();
                setMainFragment(fr);
                String key = "temp";
                if(device_info_hashMap.isEmpty())
                {
                    Log.e(TAG, "connectButton.setOnClickListener : HashMap is EMPTY !!!!!!!!!");
                }
                else
                {
                    Log.i(TAG, "connectButton.setOnClickListener : HashMap Stored Device name is " + device_info_hashMap.get(key));
                }
            }
        });

        scanDeviceButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                // Scan devices and add to main_listview
                FragmentScanBleDevices fr = new FragmentScanBleDevices();
                setMainFragment(fr);

                mScanDevice.startScan();
            }
        });

    }


    private void setMainFragment(Fragment inflate_fragment)
    {
        if(inflate_fragment != null)
        {
            try {
                android.app.FragmentManager fm = getFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(R.id.main_fragment, inflate_fragment);
                fragmentTransaction.commit();

            } catch (Exception e)
            {
                Log.e(TAG, "Exception Error :- " + e.toString());
            }
        }
        else
        {
            Log.e(TAG, "setMainFragment : Given fragment is not initialized");
        }
    }

  
  public class MyReceiver extends BroadcastReceiver
  {
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String message = intent.getAction();

        Log.d(TAG, "intert with message " + message);

        if(message.equals(SystemBroadcastReceiverNames.ConnectDevices_RequestDeviceList))
        {
            boolean request_message = intent.getBooleanExtra(SystemBroadcastReceiverNames.requestDeviceList, false);

            Log.i(TAG, "Succesfully received intent bluetoothle.MainActivity.RequestDeviceList");

            if(request_message == true)
            {
                getDeviceAddressFromPreferences();
                reportConnectDeviceAddressRequestIntent(listView_device_lists);
            }
        }
        else if(message.equals(SystemBroadcastReceiverNames.DisconnectDevices_RequestDeviceList))
        {
            boolean request_message = intent.getBooleanExtra(SystemBroadcastReceiverNames.requestDeviceList, false);

            Log.i(TAG, "Succesfully received intent bluetoothle.MainActivity.DisconnectDevices_RequestDeviceList");

            if(request_message == true)
            {
                ArrayList<String> connected_devices = getConnectedDevices();
                reportDisconnectDeviceAddressRequestIntent(connected_devices);
            }
        }
        else if(message.equals(SystemBroadcastReceiverNames.RemoveDevices_RequestDeviceList))
        {
            boolean request_message = intent.getBooleanExtra(SystemBroadcastReceiverNames.requestDeviceList, false);

            Log.i(TAG, "Succesfully received intent bluetoothle.MainActivity.RemoveDeivesRequestDeviceList");

            if(request_message == true)
            {
                getDeviceAddressFromPreferences();
                reportRemoveDeviceAddressRequestIntent(listView_device_lists);
            }
        }
        else if(message.equals(SystemBroadcastReceiverNames.ConnectDeivces_StartServiceRequest))
        {
            Log.i(TAG, "Intent received SystemBroadcastReceiverNames.ConnectDeivces_StartServiceRequest");

            String device_address = intent.getStringExtra(SystemBroadcastReceiverNames.ConnectDeivces_StartServiceRequest_DeviceAddress);
            String device_name = intent.getStringExtra(SystemBroadcastReceiverNames.ConnectDeivces_StartServiceRequest_DeviceName);

            BluetoothServiceManager.getInstance().startDeviceServiceAndConnect(getApplicationContext(), device_name, device_address, mGattManager);

            Log.d(TAG, "ConnectDeivces_StartServiceRequest : Device Name = " + device_name + "  . Device address = " + device_address);
        }
        else
        {
            Log.e(TAG, "Unregistered Broadcast received message = " + message);
        }
    }
  }
  
  
  private void registerBroadcastReceiver()
  {
      IntentFilter filter = new IntentFilter(SystemBroadcastReceiverNames.ConnectDevices_RequestDeviceList);
      filter.addAction(SystemBroadcastReceiverNames.DisconnectDevices_RequestDeviceList);
      filter.addAction(SystemBroadcastReceiverNames.RemoveDevices_RequestDeviceList);
      filter.addAction(SystemBroadcastReceiverNames.ConnectDeivces_StartServiceRequest);

      registerReceiver(receiver, filter);
  }
  
  
  private void reportConnectDeviceAddressRequestIntent(ArrayList<String> deviceArrayLists)
  {
      Log.d(TAG, "Report Device address Request list size = " + deviceArrayLists.size());
      Intent intent = new Intent();
      intent.setAction(SystemBroadcastReceiverNames.ConnectDevices_ReportDeviceList);

      intent.putExtra(SystemBroadcastReceiverNames.reportDeviceList, deviceArrayLists);
      sendBroadcast(intent);
  }


    private void reportDisconnectDeviceAddressRequestIntent(ArrayList<String> deviceArrayLists)
    {
        Log.d(TAG, "Report Device address Request list size = " + deviceArrayLists.size());
        Intent intent = new Intent();
        intent.setAction(SystemBroadcastReceiverNames.DisconnectDevices_ReportDeviceList);

        intent.putExtra(SystemBroadcastReceiverNames.reportDeviceList, deviceArrayLists);
        sendBroadcast(intent);
    }
  
  
  private void reportRemoveDeviceAddressRequestIntent(ArrayList<String> deviceArrayLists)
  {
      Log.d(TAG, "Report Device address Request list size = " + deviceArrayLists.size());
      Intent intent = new Intent();
      intent.setAction(SystemBroadcastReceiverNames.RemoveDevices_ReportDeviceList);

      intent.putExtra(SystemBroadcastReceiverNames.reportDeviceList, deviceArrayLists);
      sendBroadcast(intent);
  }
  
    /**
    * OnResume get the saved devices Name and MAC Address
    */
    private void getDeviceAddressFromPreferences()
    {
        // "Ble_app" is the reference for getting saved sharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("Ble_app",0);

        // Map consists of "Device Name" as a key and "Device Name and Device MAC Address" as the main entry
        @SuppressWarnings("unchecked")
        Map<String, String> allEntries = (Map<String, String>) sharedPref.getAll();
        Log.d(TAG, "onCreate : size of the stored device info is " + allEntries.size());

        for(Entry<String, ?> entry : allEntries.entrySet())
        {
            // device name is key and combined detail is "deviceName__deviceAddress"
            String key_device_name = entry.getKey();
            String combined_device_detail = (String) entry.getValue();
            Log.d(TAG, "Map key value is " + key_device_name + ". Combined Device detail is " + combined_device_detail);

            // separating the device name and device address
            String[] device_detail = combined_device_detail.split("__");

            String device_name = device_detail[0];
            String device_address = device_detail[1];

            // checking if the device detail's name matches with key_device_name (verification)
            if(key_device_name.equals(device_name))
            {
                // if already added to the device_info_hashmap then dont add it
                if(device_info_hashMap.containsKey(key_device_name))
                {
                    Log.e(TAG, "Device " + key_device_name + " is already present in the list");
                }
                else
                {
                    Log.i(TAG, "ADDED Device name = " + device_name + ". Device address = " + device_address);
                    device_info_hashMap.put(device_name, device_address);
                    // Added extra spaces to display in connect device fragment.
                    String listview_string = device_name.concat("   " + device_address);
                    listView_device_lists.add(listview_string);
                }
            }
            else
            {
                Log.e(TAG, "Device key name " + key_device_name + " is not equal to the device actual name " + device_name);
            }
        }
    }

    /**
     * Function to get the connected devices from connectionManager
     */
    private ArrayList<String> getConnectedDevices()
    {
        ArrayList<String> connected_device_list = new ArrayList<String >();

        int connected_device_list_size = BluetoothServiceManager.getInstance().started_device_service_list.size();

        for(int i=0; i<connected_device_list_size; i++)
        {
            String device_name = BluetoothServiceManager.getInstance().started_device_service_list.get(i).device_name;
            String device_address = BluetoothServiceManager.getInstance().started_device_service_list.get(i).device_address;

            Log.d(TAG, "Connected Device name = " + device_name + ".  Device Address = " + device_address);

            device_info_hashMap.put(device_name, device_address);
            // Added extra spaces to display in connect device fragment.
            String listview_string = device_name.concat("   " + device_address);

            connected_device_list.add(listview_string);
        }

        return connected_device_list;
    }

    public void disconnectionTriggered(String device_name, String device_address)
    {
        Log.d(TAG, "disconnectionTriggered : device name = " + device_name + " . Device address = " + device_address);

        ArrayList<String> connected_devices = getConnectedDevices();
        reportDisconnectDeviceAddressRequestIntent(connected_devices);
    }

    // This method will be called when a MessageEvent is posted
    @Subscribe(threadMode= ThreadMode.MAIN)
    public void onMessageEvent(Fragment mfragment)
    {
        setMainFragment(mfragment);
    }

    // This method will be called when a MessageEvent is posted
    @Subscribe(threadMode= ThreadMode.MAIN)
    public void onMessageEvent(DisconnectDeviceMessageEvent mDisconnectDeviceMessageEvent)
    {
        Log.d(TAG, "onMessageEvent : DisconnectDeviceMessageEvent Device name = " + mDisconnectDeviceMessageEvent.device_name +
                ". Device address = " + mDisconnectDeviceMessageEvent.device_address);

        // Disconnect device from BluetoothServiceManager
        BluetoothServiceManager.getInstance().disconnectDevices(mDisconnectDeviceMessageEvent.device_address);
    }


    // This method will be called when a MessageEvent is posted
    @Subscribe(threadMode= ThreadMode.MAIN)
    public void onMessageEvent(DisconnectedDeviceMessageEvent mDisconnectedDeviceMessageEvent)
    {
        Log.d(TAG, "onMessageEvent : DisconnectedDeviceMessageEvent Device name = " + mDisconnectedDeviceMessageEvent.device_name +
                ". Device address = " + mDisconnectedDeviceMessageEvent.device_address);

        ArrayList<String> connected_devices = getConnectedDevices();
        reportDisconnectDeviceAddressRequestIntent(connected_devices);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(AddDeviceDetailsMessageEvent mAddDeviceDetailsMessageEvent)
    {
        String device_name = mAddDeviceDetailsMessageEvent.device_name;
        String device_address = mAddDeviceDetailsMessageEvent.device_address;

        try
        {
            if(device_info_hashMap.containsValue(device_address))
            {
                Log.d(TAG, "onMessageEvent : Device address = " + device_address + "  is already added");

                Toast.makeText(this, "Device ALREADY added", Toast.LENGTH_LONG).show();
            }
            else
            {
                MainActivity.device_info_hashMap.put(device_name, device_address);

                String listview_string = device_name.concat("   " + device_address);
                MainActivity.listView_device_lists.add(listview_string);

                String shared_string = new StringBuilder().append(device_name).append("__").append(device_address).toString();
                Log.i(TAG, "set_button pressed : shared_string is " + shared_string);

                // Creating the instance of the shared preference
                SharedPreferences sharedPref = getSharedPreferences("Ble_app",0);
                // getting the editor for the shared preference
                SharedPreferences.Editor editor = sharedPref.edit();
                // Storing the value of devices
                editor.putString(device_name, shared_string);
                //committing the device information
                editor.commit();

                Toast.makeText(this, "Device Successfully added", Toast.LENGTH_LONG).show();
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "Exception happened. Error = " + e.toString());
        }

        Log.d(TAG, "onMessageEvent : Adding BLE Device name = " + device_name + " . Device address = " + device_address);
    }
}
