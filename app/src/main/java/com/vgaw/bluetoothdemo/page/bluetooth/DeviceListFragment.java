package com.vgaw.bluetoothdemo.page.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by caojin on 2016/6/17.
 */
public abstract class DeviceListFragment extends ListFragment {
    private ArrayList<BluetoothDevice> dataList = new ArrayList<>();
    private ArrayAdapter<BluetoothDevice> adapter;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new ArrayAdapter<BluetoothDevice>(getContext(), android.R.layout.test_list_item,
                android.R.id.text1, dataList){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setText(getItem(position).getName());
                return tv;
            }
        };
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        onItemClick(dataList.get(position));
    }

    public abstract void onItemClick(BluetoothDevice device);

    public void addDevice(BluetoothDevice item){
        dataList.add(item);
        adapter.notifyDataSetChanged();
    }

    public void clearDevice(){
        dataList.clear();
        adapter.notifyDataSetChanged();
    }
}
