package EventMessages;

/**
 * Created by Sushant Mainali on 7/24/2016.
 */
public class DisconnectedDeviceMessageEvent
{
    public final String device_name;
    public final String device_address;

    public DisconnectedDeviceMessageEvent(String device_name, String device_address)
    {
        this.device_name = device_name;
        this.device_address = device_address;
    }
}
