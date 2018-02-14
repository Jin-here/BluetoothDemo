package com.vgaw.bluetoothdemo.page.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.vgaw.bluetoothdemo.R;
import com.vgaw.bluetoothdemo.databinding.FragmentBleDeviceListBinding;
import com.vgaw.bluetoothdemo.view.EasyAdapter;
import com.vgaw.bluetoothdemo.view.EasyHolder;

import java.util.ArrayList;

/**
 * Created by caojin on 2018/2/13.
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BLEDeviceListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, BluetoothAdapter.LeScanCallback, AdapterView.OnItemClickListener {
    public static final String TAG = "BLEDeviceListFragment";

    private static final int SCAN_TIME_OUT = 3000;
    private FragmentBleDeviceListBinding binding;
    private BLEActivity mActivity;
    private Handler mHandler;

    private EasyAdapter mAdapter;
    private ArrayList<BluetoothDevice> dataList = new ArrayList<>();

    private BLEDeviceListListener listener;

    public void setBLEDeviceListListener(BLEDeviceListListener listener) {
        this.listener = listener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (BLEActivity) getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_ble_device_list, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.srlBleDevice.setColorSchemeResources(android.R.color.black,
                android.R.color.holo_red_light,
                android.R.color.holo_blue_dark,
                android.R.color.holo_orange_dark);
        binding.srlBleDevice.setOnRefreshListener(this);

        mAdapter = new EasyAdapter(mActivity, this.dataList) {
            @Override
            public EasyHolder getHolder(int type) {
                return new EasyHolder() {
                    @Override
                    public View createView(int position) {
                        return view;
                    }

                    @Override
                    public void refreshView(int position, Object item) {
                        BluetoothDevice device = (BluetoothDevice) item;
                        ((TextView) view.findViewById(android.R.id.text1)).setText(device.getName());
                    }

                    @Override
                    public int getLayout() {
                        return android.R.layout.test_list_item;
                    }
                };
            }
        };
        binding.lvBleDevice.setAdapter(mAdapter);
        binding.lvBleDevice.setOnItemClickListener(this);

        onRefresh();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopSearch();
    }

    @Override
    public void onRefresh() {
        startSearch();
    }

    private BluetoothAdapter getBluetoothAdapter() {
        return this.mActivity.getBluetoothAdapter();
    }

    private void startSearch() {
        this.dataList.clear();

        mHandler.removeCallbacks(scanRunnable);
        mHandler.postDelayed(scanRunnable, SCAN_TIME_OUT);
        getBluetoothAdapter().startLeScan(this);
    }

    private void stopSearch() {
        mHandler.removeCallbacks(scanRunnable);
        getBluetoothAdapter().stopLeScan(BLEDeviceListFragment.this);

        binding.srlBleDevice.setRefreshing(false);
    }

    private Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            stopSearch();
        }
    };

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        stopSearch();

        this.dataList.add(device);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (this.listener != null) {
            this.listener.onDeviceChosen(this.dataList.get(position));
        }
    }

    public interface BLEDeviceListListener {
        void onDeviceChosen(BluetoothDevice device);
    }
}
