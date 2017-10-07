package com.example.itstaff.bleexemplo;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IT Staff on 25/08/2017.
 */

public class DeviceAdapter extends ArrayAdapter<BluetoothDevice> {

    private Context context;
    private ArrayList<BluetoothDevice> devices;


    public DeviceAdapter(Context context, ArrayList<BluetoothDevice> devices) {
        super(context, 0, devices);

        this.devices = devices;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        BluetoothDevice device = devices.get(position);

        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.layout_row,null);
        }

        TextView tvMac = (TextView) convertView.findViewById(R.id.tvMac);
        tvMac.setText(device.getAddress());

        return convertView;
    }
}
