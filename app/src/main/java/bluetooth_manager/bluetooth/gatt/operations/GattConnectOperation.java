package bluetooth_manager.bluetooth.gatt.operations;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

public class GattConnectOperation extends GattOperation{

	public GattConnectOperation(BluetoothDevice device) 
	{
		super(device);
		
	}

	@Override
	public void execute(BluetoothGatt bluetoothGatt) 
	{
		bluetoothGatt.connect();
	}

	@Override
	public boolean hasAvailableCompletionCallback() 
	{
		return true;
	}

}
