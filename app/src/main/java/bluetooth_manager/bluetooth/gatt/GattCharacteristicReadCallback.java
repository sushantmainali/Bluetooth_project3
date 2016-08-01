package bluetooth_manager.bluetooth.gatt;

public interface GattCharacteristicReadCallback {
    void call(byte[] characteristic);
}
