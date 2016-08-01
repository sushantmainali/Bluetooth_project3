package UI_Fragments;

import com.example.bluetoothle.MainActivity;
import com.example.bluetoothle.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentAddDevice extends Fragment
{

	private final String TAG = "FragmentAddDevice";
	
	View mView;
	
	private Button set_button;
	private EditText Mac_address_edittext;
	private EditText device_name_edittext;
	private TextView Mac_address_textview;
	private TextView device_name_textview;
	
	public final static String ACTION_SET_DEVICE_MAC_ADDRESS_AND_NAME = "com.example.bluetoothle.ACTION_MAC_ADDRESS_AND_DEVICE_NAME";
	public final static String MESSAGE_MAC_ADDRESS = "com.example.bluetoothle.ACTION_MAC_ADDRESS";
	public final static String MESSAGE_DEVICE_NAME = "com.example.bluetoothle.ACTION_DEVICE_NAME";
	
	private static final int toast_longLength = 2000;
	
	  @Override
	  public View onCreateView(LayoutInflater inflater, ViewGroup container,
	      Bundle savedInstanceState) {
		  Log.d(TAG, "onCreate(FrqgmentAddDevice) : onCreateView");
		  mView = inflater.inflate(R.layout.add_device_fragment, container, false);
	      return mView;
	  }

	  
	  public interface OnItemSelectedListener {
	    public void onRssItemSelected(String link);
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
		  Log.d(TAG, "FragmentAddDevice : OnResume()");
		  Mac_address_edittext = (EditText) mView.findViewById(R.id.add_device_MAC_Address_editText);
		  Mac_address_textview = (TextView) mView.findViewById(R.id.add_device_MAC_Address_textView);
		  device_name_edittext = (EditText) mView.findViewById(R.id.add_device_Device_Name_editText);
		  device_name_textview = (TextView) mView.findViewById(R.id.add_device_Device_Name_textView);
		  set_button = (Button) mView.findViewById(R.id.add_device_set_button);
		  set_button_listener();
		  
	      super.onResume();
	  }

	private void set_button_listener()
	{
		
		set_button.setOnClickListener(new View.OnClickListener() 
		{
			
			@Override
			public void onClick(View v) 
			{
				// TODO Auto-generated method stub
				Log.d(TAG, "onClick(add_device_set_button) : on click of set button pressed");
				
				if(Mac_address_edittext.getText().length() == 12 && device_name_edittext.getText().length() >= 3)
				{
					String mac_address = Mac_address_edittext.getText().toString();
					String device_name = device_name_edittext.getText().toString();
					String text_for_mac_address_textview = "Added MAC Address = " ;
					text_for_mac_address_textview = text_for_mac_address_textview.concat(mac_address);
					Mac_address_textview.setText(text_for_mac_address_textview);
					
					String text_for_device_name_textview = "Added Device = " ;
					text_for_device_name_textview = text_for_device_name_textview.concat(device_name);
					device_name_textview.setText(text_for_device_name_textview);
					if(MainActivity.device_info_hashMap.containsKey(device_name))
					{
						Log.e(TAG, "onClick(add_device_set_button) : on click of set button pressed without proper MAC address and Device Name");
						Toast.makeText(getActivity(), "Device Already Added", Toast.LENGTH_LONG).show();
					}
					else
					{
						try
						{
							MainActivity.device_info_hashMap.put(device_name, mac_address);
							String listview_string = device_name.concat("   " + mac_address);
							MainActivity.listView_device_lists.add(listview_string);
							String shared_string = new StringBuilder().append(device_name).append("__").append(mac_address).toString();
							Log.i(TAG, "set_button pressed : shared_string is " + shared_string);
							
							// Creating the instance of the shared preference  
							SharedPreferences sharedPref = getActivity().getSharedPreferences("Ble_app",0);
							// getting the editor for the shared preference
							SharedPreferences.Editor editor = sharedPref.edit();
							// Storing the value of devices
							editor.putString(device_name, shared_string);
							//committing the device information
							editor.commit();
							
							Log.e(TAG, "onClick(add_device_set_button) : on click of set button pressed without proper MAC address and Device Name");
							Toast.makeText(getActivity(), "Device Successfully added", Toast.LENGTH_LONG).show();
						}
						catch (Exception e) 
						{
							Log.e(TAG, "Exception happened. Error = " + e.toString());
						}
						
//						Intent intent = new Intent(Activity_Add_Device.this, MainActivity.class);
//						startActivity(intent);
					}
//						boradcastUpdate(ACTION_SET_DEVICE_MAC_ADDRESS_AND_NAME,device_address,mac_address);
				}
				else
				{
					Log.e(TAG, "onClick(add_device_set_button) : on click of set button pressed without proper MAC address and Device Name");
					Toast.makeText(getActivity(), "MAC address or Device Name is not Valid", Toast.LENGTH_LONG).show();
				}
			}
		 });
		
	}
	
}
