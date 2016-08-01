package EventMessages;

/**
 * Created by Sushant on 22/07/2016.
 * Send device address and name to connect and remove device
 */
public class AddDeviceDetailsMessageEvent
{
    public final String device_name;
    public final String device_address;

    public AddDeviceDetailsMessageEvent(String device_name, String device_address)
    {
        this.device_name = device_name;
        this.device_address = device_address;
    }
}
