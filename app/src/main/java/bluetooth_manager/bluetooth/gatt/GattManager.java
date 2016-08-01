package bluetooth_manager.bluetooth.gatt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.droidparts.util.L;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import bluetooth_manager.bluetooth.gatt.operations.GattCharacteristicReadOperation;
import bluetooth_manager.bluetooth.gatt.operations.GattDescriptorReadOperation;
import bluetooth_manager.bluetooth.gatt.operations.GattOperation;
import bluetooth_service.DeviceCallBacks;

public class GattManager {

	private static final String TAG = "GattManager"; 
	
    private ConcurrentLinkedQueue<GattOperation> mQueue;
    private ConcurrentHashMap<String, BluetoothGatt> mGatts;
    private GattOperation mCurrentOperation;
    private HashMap<UUID, ArrayList<CharacteristicChangeListener>> mCharacteristicChangeListeners;
    private AsyncTask<Void, Void, Void> mCurrentOperationTimeout;

    public GattManager() {
        mQueue = new ConcurrentLinkedQueue<>();
        mGatts = new ConcurrentHashMap<>();
        mCurrentOperation = null;
        mCharacteristicChangeListeners = new HashMap<>();
    }

    public synchronized void cancelCurrentOperationBundle() {
        L.v("Cancelling current operation. Queue size before: " + mQueue.size());
        if(mCurrentOperation != null && mCurrentOperation.getBundle() != null) {
            for(GattOperation op : mCurrentOperation.getBundle().getOperations()) {
                mQueue.remove(op);
            }
        }
        L.v("Queue size after: " + mQueue.size());
        mCurrentOperation = null;
        drive();
    }

    public synchronized void queue(GattOperation gattOperation) {
        mQueue.add(gattOperation);
        L.v("Queueing Gatt operation, size will now become: " + mQueue.size());
        drive();
    }

    private synchronized void drive() {
        if(mCurrentOperation != null) {
            L.e("tried to drive, but currentOperation was not null, " + mCurrentOperation);
            return;
        }
        if( mQueue.size() == 0) {
            L.v("Queue empty, drive loop stopped.");
            mCurrentOperation = null;
            return;
        }

        final GattOperation operation = mQueue.poll();
        L.v("Driving Gatt queue, size will now become: " + mQueue.size());
        setCurrentOperation(operation);


        if(mCurrentOperationTimeout != null) {
            mCurrentOperationTimeout.cancel(true);
        }
        mCurrentOperationTimeout = new AsyncTask<Void, Void, Void>() {
            @Override
            protected synchronized Void doInBackground(Void... voids) {
                try {
                    L.v("Starting to do a background timeout");
                    wait(operation.getTimoutInMillis());
                } catch (InterruptedException e) {
                    L.v("was interrupted out of the timeout");
                }
                if(isCancelled()) {
                    L.v("The timeout was cancelled, so we do nothing.");
                    return null;
                }
                L.v("Timeout ran to completion, time to cancel the entire operation bundle. Abort, abort!");
                cancelCurrentOperationBundle();
                return null;
            }

            @Override
            protected synchronized void onCancelled() {
                super.onCancelled();
                notify();
            }
        }.execute();

        final BluetoothDevice device = operation.getDevice();
        
        if(mGatts.containsKey(device.getAddress())) 
        {
            execute(mGatts.get(device.getAddress()), operation);
        } else 
        {
        	Log.w(TAG, "Device address = " + device.getAddress() + " isnt in the GATT list");
        	// TODO return the callback to add device
        }
    }

    private void execute(BluetoothGatt gatt, GattOperation operation) {
        if(operation != mCurrentOperation) {
            return;
        }
        operation.execute(gatt);
        if(!operation.hasAvailableCompletionCallback()) {
            setCurrentOperation(null);
            drive();
        }
    }

    public synchronized void setCurrentOperation(GattOperation currentOperation) {
        mCurrentOperation = currentOperation;
    }

    public BluetoothGatt getGatt(BluetoothDevice device) {
        return mGatts.get(device);
    }

    public void addCharacteristicChangeListener(UUID characteristicUuid, CharacteristicChangeListener characteristicChangeListener) {
        if(!mCharacteristicChangeListeners.containsKey(characteristicUuid)) {
            mCharacteristicChangeListeners.put(characteristicUuid, new ArrayList<CharacteristicChangeListener>());
        }
        mCharacteristicChangeListeners.get(characteristicUuid).add(characteristicChangeListener);
    }

    public void queue(GattOperationBundle bundle) {
        for(GattOperation operation : bundle.getOperations()) {
            queue(operation);
        }
    }

    public class ConnectionStateChangedBundle {
        public final int mNewState;
        public final String mAddress;

        public ConnectionStateChangedBundle(String address, int newState) {
            mAddress = address;
            mNewState = newState;
        }
    }
    
    
    public BluetoothGatt connectBluetoothDevice(Context ctx, BluetoothDevice device, final DeviceCallBacks mBleDeviceCallBacks)
    {
    	BluetoothGatt mBluetoothGatt = device.connectGatt(ctx, true, new BluetoothGattCallback() 
    	{
	            @Override
	            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
	                super.onConnectionStateChange(gatt, status, newState);
	                
	                BluetoothDevice device = gatt.getDevice();
	
	                if (status == 133) 
	                {
	                    L.e("Got the status 133 bug, closing gatt");
	                    return;
	                }
	
	                if (newState == BluetoothProfile.STATE_CONNECTED) 
	                {
	                    Log.i(TAG,"Gatt connected to device " + device.getAddress());
	                    mGatts.put(device.getAddress(), gatt);
	                    gatt.discoverServices();
	                } 
	                else if (newState == BluetoothProfile.STATE_DISCONNECTED) 
	                {
	                    Log.i(TAG,"Disconnected from gatt server " + device.getAddress() + ", newState: " + newState);
	                    mGatts.remove(device.getAddress());
	                    setCurrentOperation(null);
	                    mBleDeviceCallBacks.onDeviceDisconnected();
	                }
	            }
	
	            @Override
	            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
	                super.onDescriptorRead(gatt, descriptor, status);
	                ((GattDescriptorReadOperation) mCurrentOperation).onRead(descriptor);
	                setCurrentOperation(null);
	            }
	
	            @Override
	            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
	                super.onDescriptorWrite(gatt, descriptor, status);
	                setCurrentOperation(null);
	            }
	
	            @Override
	            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
	                super.onCharacteristicRead(gatt, characteristic, status);
	                ((GattCharacteristicReadOperation) mCurrentOperation).onRead(characteristic);
	                setCurrentOperation(null);
	            }
	
	            @Override
	            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
	                super.onServicesDiscovered(gatt, status);
	                Log.d(TAG,"services discovered, status: " + status);
	                mBleDeviceCallBacks.onDeviceConnected(gatt);
	            }
	
	
	            @Override
	            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
	                super.onCharacteristicWrite(gatt, characteristic, status);
	                BluetoothDevice device = gatt.getDevice();
	                Log.d(TAG, "Characteristic " + characteristic.getUuid() + "written to on device " + device.getAddress());
	                setCurrentOperation(null);
	            }
	
	            @Override
	            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
	                super.onCharacteristicChanged(gatt, characteristic);
	                BluetoothDevice device = gatt.getDevice();
	                Log.e(TAG,"Characteristic " + characteristic.getUuid() + "was changed, device: " + device.getAddress());
	                if (mCharacteristicChangeListeners.containsKey(characteristic.getUuid())) {
	                    for (CharacteristicChangeListener listener : mCharacteristicChangeListeners.get(characteristic.getUuid())) {
	                        listener.onCharacteristicChanged(device.getAddress(), characteristic);
	                    }
	                }
	            }
	        });
    	return mBluetoothGatt;
    }
}
