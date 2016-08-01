package UI_Fragments;

import com.example.bluetoothle.R;
import com.example.bluetoothle.SystemBroadcastReceiverNames;

import android.app.Activity;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import EventMessages.DisconnectDeviceMessageEvent;

public class FragmentDisconnectDevices extends ListFragment
{
    private static final String TAG = "FragmentDisDevices";

    private View mView;

    private MyArrayAdapter myArrayAdapter;

    private ArrayList<device_info> listView_device_values = new ArrayList<>();

    public class device_info
    {
        String device_name = getActivity().getResources().getString(R.string.default_device_name_unknown);
        String device_address = getActivity().getResources().getString(R.string.default_device_address_unknown);
    };

    private final String combined_device_detail_spliter_with_space = "   ";

    private ArrayList<String> device_list = new ArrayList<String>();
    MyReceiver receiver = new MyReceiver();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
      mView = inflater.inflate(R.layout.fragment_textview, container, false);

      if(getActivity() != null)
      {
          myArrayAdapter = new MyArrayAdapter(getActivity(), R.layout.disconnect_device, listView_device_values);

          setListAdapter(myArrayAdapter);
      }
      else
      {
          Log.e(TAG, "onCreateView : FragmentDisconnectDevice getActivity() is null");
      }

      return mView;
    }

    public interface OnItemSelectedListener
    {
        public void onRssItemSelected(String link);
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
    }

    @Override
    public void onStop()
    {
        Log.d(TAG, "onStop executed");
        getActivity().unregisterReceiver(receiver);
        super.onStop();
    }


    @Override
    public void onResume()
    {
        super.onResume();

        registerBroadcastReceiver();
        sendDeviceAddressRequestIntent();
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }


    public class MyReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context Context, Intent intent)
        {
            String message = intent.getAction();
            Log.d(TAG, "onReceive message = " + message);
            if(message.equals(SystemBroadcastReceiverNames.DisconnectDevices_ReportDeviceList))
            {
                device_list = intent.getStringArrayListExtra(SystemBroadcastReceiverNames.reportDeviceList);
                populateDeviceLists(device_list);
            }
        }
    }

    private void registerBroadcastReceiver()
    {
        IntentFilter filter = new IntentFilter(SystemBroadcastReceiverNames.DisconnectDevices_ReportDeviceList);
        getActivity().registerReceiver(receiver, filter);
    }


    private void sendDeviceAddressRequestIntent()
    {
        Log.d(TAG, "Send Device address Request list from Disconnect fragment");
        Intent intent = new Intent();
        intent.setAction(SystemBroadcastReceiverNames.DisconnectDevices_RequestDeviceList);

        intent.putExtra(SystemBroadcastReceiverNames.requestDeviceList, true);
        getActivity().sendBroadcast(intent);
    }


    private void populateDeviceLists(ArrayList<String> reported_device_lists)
    {
        if(reported_device_lists != null)
        {
            if((reported_device_lists.size()) == 0)
            {
                Log.e(TAG, "Reported device list size = 0 !!!!!!!!!!!!!!!!");
            }
            else
            {
                if(reported_device_lists.size() >= 0)
                {
                    for(int i=0; i<reported_device_lists.size(); i++)
                    {
                        String[] device_detail = reported_device_lists.get(i).split(combined_device_detail_spliter_with_space);

                        final String device_name = device_detail[0];
                        final String device_address = device_detail[1];

                        addScanDevicesDetail(device_name, device_address);
                    }

                }
            }
        }
    }


    private void addScanDevicesDetail(String mDevice_name, String mDevice_address)
    {
        boolean device_already_added = false;

        // Go through the list to check if device is already added
        for (device_info df : listView_device_values)
        {
            if(df.device_address.equals(mDevice_address))
            {
                Log.d(TAG, "addScanDevicesDetail : Device address = " +mDevice_address+ "  already added");

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
                rowView = inflater.inflate(R.layout.disconnect_device, parent,false);
                // configure view holder
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.textView_device_name = (TextView) rowView.findViewById(R.id.disconnect_device_name);
//                viewHolder.textView_device_name.setText(names.get(position).device_name);
                viewHolder.textView_device_address = (TextView) rowView.findViewById(R.id.disconnect_device_address);
//                viewHolder.textView_device_name.setText(names.get(position).device_name);
                viewHolder.add_device_button = (Button) rowView.findViewById(R.id.start_disconnect_device_button);

                Log.d(TAG,"convertView == NULL. ListView changed. Position = "+ position);

                viewHolder.add_device_button.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        //Todo : disconnection code
                        EventBus.getDefault().post(new DisconnectDeviceMessageEvent(names.get(position).device_name,names.get(position).device_address));

                        Log.d(TAG, "OnClick happened for disconnection. Position = " + position);
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
                    //Todo : disconnection code
                    EventBus.getDefault().post(new DisconnectDeviceMessageEvent(names.get(position).device_name,names.get(position).device_address));

                    Log.d(TAG, "OnClick happened for disconnection. Position = " + position);
                }
            });

            return rowView;
        }
    }
}
