package bluetooth_service;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

public interface DeviceCallBacks 
{
	public void onDeviceScanFinished(boolean devicefound, BluetoothDevice device);
	
	public void onDeviceConnected(BluetoothGatt bluetooth_gatt);
	
	public void onDeviceDisconnected();
	
	public void onDeviceReadCharacteristic();
	
	public void onDeviceWriteCharacteristic();
}
