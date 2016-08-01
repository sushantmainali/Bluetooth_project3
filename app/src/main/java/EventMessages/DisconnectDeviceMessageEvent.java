package EventMessages;

/**
 * Created by Sushant Mainali on 7/23/2016.
 */
public class DisconnectDeviceMessageEvent
{
    public final String device_name;
    public final String device_address;

    public DisconnectDeviceMessageEvent(String device_name, String device_address)
    {
        this.device_name = device_name;
        this.device_address = device_address;
    }
}
