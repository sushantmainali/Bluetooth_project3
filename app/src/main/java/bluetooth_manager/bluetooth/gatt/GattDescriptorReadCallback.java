package bluetooth_manager.bluetooth.gatt;

public interface GattDescriptorReadCallback {
    void call(byte[] value);
}
