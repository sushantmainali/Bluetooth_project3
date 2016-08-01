package UI_Fragments;

import com.example.bluetoothle.R;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import EventMessages.AddDeviceDetailsMessageEvent;
import EventMessages.ScanDeviceDetailsMessageEvent;

public class FragmentScanBleDevices extends ListFragment
{
    // Logging TAG strig
    private final static String TAG = FragmentScanBleDevices.class.getName().toString();

    // ArrayAdapter for device name and device list
    private ArrayAdapter scanDeviceListView;

    // View instance for initializing textview
    private View mView;

    private MyArrayAdapter myArrayAdapter;

    private ArrayList<device_info> listView_device_values = new ArrayList<>();


    public class device_info
    {
        String device_name = getActivity().getResources().getString(R.string.default_device_name_unknown);
        String device_address = getActivity().getResources().getString(R.string.default_device_address_unknown);
    };


	  @Override
	  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
      {
          mView = inflater.inflate(R.layout.fragment_textview, container, false);

          if(getActivity() != null)
          {
              myArrayAdapter = new MyArrayAdapter(getActivity(), R.layout.scan_device_detail, listView_device_values);

              setListAdapter(myArrayAdapter);
          }
          else
          {
              Log.e(TAG, "onCreateView : FragmentDeviceConnection getActivity() is null");
          }

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
    public void onResume()
    {
        super.onResume();

        //register for the ScanDeviceDetailsMessageEvent
        EventBus.getDefault().register(this);
    }

    @Override
	  public void onDetach() {
	    super.onDetach();
	  }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        //register for the ScanDeviceDetailsMessageEvent
        EventBus.getDefault().unregister(this);
    }

    public class MyArrayAdapter extends ArrayAdapter<device_info>
    {
        private Context mContext;
        private ArrayList<device_info> names;

        class ViewHolder
        {
            public TextView textView_device_name;
            public TextView textView_device_address;
            public Button add_device_button;
        }

        public MyArrayAdapter(Context context, int textViewResourceId, ArrayList<device_info> names)
        {
            super(context,textViewResourceId,names);

            this.mContext = context;
            this.names = names;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent)
        {
            View rowView = convertView;
            // reuse views
            if (rowView == null)
            {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.scan_device_detail, parent,false);
                // configure view holder
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.textView_device_name = (TextView) rowView.findViewById(R.id.scan_device_name);
//                viewHolder.textView_device_name.setText(names.get(position).device_name);
                viewHolder.textView_device_address = (TextView) rowView.findViewById(R.id.scan_device_address);
//                viewHolder.textView_device_name.setText(names.get(position).device_name);
                viewHolder.add_device_button = (Button) rowView.findViewById(R.id.add_scanned_device_button);

                Log.d(TAG,"convertView == NULL. ListView changed. Position = "+ position);

                viewHolder.add_device_button.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        EventBus.getDefault().post(new AddDeviceDetailsMessageEvent(names.get(position).device_name,names.get(position).device_address));

                        Log.d(TAG, "OnClick happened for device position = " + position);
                    }
                });

                rowView.setTag(viewHolder);
            }
            else
            {
                Log.e(TAG,"convertView != NULL. ListView changed. Position = "+ position);
            }

            // fill data
            device_info s = names.get(position);
            ViewHolder holder = (ViewHolder) rowView.getTag();
            holder.textView_device_name.setText(s.device_name);
            holder.textView_device_address.setText(s.device_address);

            holder.add_device_button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    EventBus.getDefault().post(new AddDeviceDetailsMessageEvent(names.get(position).device_name,names.get(position).device_address));

                    Log.d(TAG, "OnClick happened for device position = " + position);
                }
            });

            return rowView;
        }
    }


    public void addScanDevicesDetail(String mDevice_name, String mDevice_address)
    {
        boolean device_already_added = false;

        // Go through the list to check if device is already added
        for (device_info df : listView_device_values)
        {
            if(df.device_address.equals(mDevice_address))
            {
                Log.d(TAG, "Device address = " +mDevice_address+ "  already added");

                // set device already added
                device_already_added = true;
            }
        }

        // only add the new address
        if(device_already_added == false)
        {
            device_info new_device_info = new device_info();
            new_device_info.device_name = mDevice_name;
            new_device_info.device_address = mDevice_address;

            listView_device_values.add(new_device_info);
            myArrayAdapter.notifyDataSetChanged();
        }
    }

    // This method will be called when a MessageEvent is posted
    @Subscribe(threadMode= ThreadMode.MAIN)
    public void onMessageEvent(ScanDeviceDetailsMessageEvent event)
    {
        addScanDevicesDetail(event.device_name,event.device_address);
    }
}
