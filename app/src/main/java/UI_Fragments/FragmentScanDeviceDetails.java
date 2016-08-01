package UI_Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.bluetoothle.R;
/**
 * Created by Sushant Mainali on 6/1/2016.
 */
public class FragmentScanDeviceDetails extends Fragment
{

    private static final String TAG = "ScanDeviceDetails";

    View mView;

    public TextView textView_device_name;
    public TextView textView_device_address;
    public Button button_add_device;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.scan_device_detail, container, false);

        textView_device_address = (TextView) mView.findViewById(R.id.scan_device_address);
        textView_device_name = (TextView) mView.findViewById(R.id.scan_device_name);
        button_add_device = (Button) mView.findViewById(R.id.add_scanned_device_button);

        return mView;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public void onStop()
    {
        Log.d(TAG, "onStop executed");
        super.onStop();
    }


    @Override
    public void onResume()
    {
        Log.d(TAG, "onResume executed");
        super.onResume();
    }
}
