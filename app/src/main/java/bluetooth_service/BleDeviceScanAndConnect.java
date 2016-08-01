package bluetooth_service;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import bluetooth_manager.bluetooth.gatt.GattManager;

public class BleDeviceScanAndConnect extends DeviceOperationAbstract
{
	private static final String TAG = "BleDeviceScanAndConnect";
	
	private String device_name;
	private String device_address;
	private DeviceCallBacks mBleDeviceCallBacks;
	private Context mCtx = null;
	
	private static boolean mScanning = false;
	
//	private static final int BLE_SCAN_TIME_MS = 30000; // 30sec total scan time
	
	private static final int BLE_SCAN_TIME_MS = 5000; // 30sec total scan time
	
	// Initializes Bluetooth adapter.
	BluetoothManager bluetoothManager = null;
	BluetoothAdapter mBluetoothAdapter = null;
	
	// start the GattConnection in another thread
	BluetoothGatt mBluetoothGatt = null;
	BluetoothDevice mBluetoothDevice = null;
	GattManager mGattManager = null;
		
	public BleDeviceScanAndConnect(Context ctx, String deviceName, String device_address, GattManager gatt_manager, DeviceCallBacks bleDeviceCallBacks)
	{
		this.mCtx = ctx;
		this.device_name = deviceName;
		this.device_address = device_address;
		this.mBleDeviceCallBacks = bleDeviceCallBacks;
		this.mGattManager = gatt_manager;

		bluetoothManager = (BluetoothManager) mCtx.getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

	}

	@Override
	public BluetoothDevice deviceScan()
	{
		if(mScanning == false)
		{
			// Scan the Ble Devices
			scanLeDevice(true);
		}
		else
		{
			Log.w(TAG, "Wait for previous ble scan to finish");
		}

		return null;
	}

	@Override
	public void deviceConnection(String connectionThreadName, BluetoothDevice device)
	{
		mBluetoothDevice = device;
		startConnectionThread(connectionThreadName);
	}

	@Override
	public void deviceDisconnect(BluetoothGatt mBluetoothGatt)
	{
		if(mBluetoothGatt != null)
		{
			Log.d(TAG, "deviceDisconnect : Disconnecting and clossing GATT");

			mBluetoothGatt.disconnect();
			mBluetoothGatt.close();
		}
		else
		{
			Log.e(TAG, "Bluetooth GATT is NULL");
		}
	}

	@Override
	public byte[] deviceReceiveData(BluetoothDevice device, GattManager gattManager, DeviceCallBacks deviceReadDataCallBack)
	{
		return null;
	}

	@Override
	public void deviceSendData(BluetoothDevice device, GattManager gattManager, byte[] data, DeviceCallBacks deviceSendDataCallBack)
	{

	}


	private static Handler mScanStopHandler = new Handler();
	private Runnable mScanStopRunnable = new Runnable() 
	{
		@Override
		public void run() 
		{
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            
      	  // execute the scan finished callBack for that specific device
      	  mBleDeviceCallBacks.onDeviceScanFinished(false, null);
		}
	};
	
    private void scanLeDevice(boolean enable)
    {
        Log.d(TAG, "scanLeDevice for time_in_ms = " + String.valueOf(BLE_SCAN_TIME_MS));
        if (enable) 
        {
            // Stops scanning after a pre-defined scan period.
        	mScanStopHandler.postDelayed(mScanStopRunnable, BLE_SCAN_TIME_MS);
        	
        	// start scanning
            mScanning = true;
            if(mBluetoothAdapter.startLeScan(mLeScanCallback))
            {
            	Log.d(TAG, "ScanLe Started");
            }
            else
            {
            	Log.w(TAG, "ScanLe start failed");
            }
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        
    }
    
    private BluetoothAdapter.LeScanCallback mLeScanCallback =  new BluetoothAdapter.LeScanCallback() 
    {
       @Override
       public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord)
       {
    	  String scan_device_address = device.getAddress();
          if(scan_device_address.equals(device_address))
          {
        	  Log.i(TAG, "Device found. Address = " + scan_device_address + ". RSSI = " + Integer.toString(rssi)+ ". ScanRecord = " + scanRecord.toString());
        	  Log.d(TAG, "Connecting to device");
        	  
        	  mScanning = false;

        	  // if device found, stop the handler runnable and stop Ble scan 
        	  mScanStopHandler.removeCallbacks(mScanStopRunnable);
        	  mBluetoothAdapter.stopLeScan(mLeScanCallback);
        	  
        	  // execute the scan finished callBack for that specific device
        	  mBleDeviceCallBacks.onDeviceScanFinished(true, device);
          }	
          else
          {
        	  Log.i(TAG, "Other Scanned Device found. Address = " + scan_device_address + ". RSSI = " + Integer.toString(rssi)+ ". ScanRecord = " + scanRecord.toString());
          }
       }
    };
    
 // start the GattConnection in another thread
    private Runnable mRunnable = new Runnable() 
    {
		@Override
		public void run() 
		{
            try
            {
                 Thread.sleep(1000);
            }
            catch (InterruptedException e) 
            {
            	Log.d(TAG, "Stopping the thread. Error = " + e.toString());
            }

			mBluetoothGatt = mGattManager.connectBluetoothDevice(mCtx, mBluetoothDevice, mBleDeviceCallBacks);
		}
	};
    
    private void startConnectionThread(String thread_name)
    {
    	new Thread(mRunnable, thread_name).start();
    }
    
}
