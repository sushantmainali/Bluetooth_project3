package bluetooth_service;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

import bluetooth_manager.bluetooth.gatt.GattManager;

public abstract class DeviceOperationAbstract {

	public abstract BluetoothDevice deviceScan();
	
//	public abstract BluetoothGatt deviceConnection(BluetoothDevice device, GattManager gattManager, DeviceCallBacks deviceConnectionCallBack);
	/**
	 * Wait for the callBack of deviceScan before starting scan
	 * @return BluetoothGatt
	 */
	public abstract void deviceConnection(String connectionThreadName, BluetoothDevice device);
	
	public abstract void deviceDisconnect(BluetoothGatt mBluetoothGatt);
	
	public abstract void deviceSendData(BluetoothDevice device, GattManager gattManager, byte[] data, DeviceCallBacks deviceSendDataCallBack);
	
	public abstract byte[] deviceReceiveData(BluetoothDevice device, GattManager gattManager, DeviceCallBacks deviceReadDataCallBack);
	
}
