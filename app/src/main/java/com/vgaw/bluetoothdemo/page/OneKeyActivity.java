package com.vgaw.bluetoothdemo.page;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import com.vgaw.bluetoothdemo.R;
import com.vgaw.bluetoothdemo.databinding.ActivityOneKeyBinding;
import com.vgaw.bluetoothdemo.util.HexTransform;

import java.util.List;
import java.util.UUID;

/**
 * Created by caojin on 2018/2/14.
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class OneKeyActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback, View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "OneKeyActivity";
    private static final int STATUS_BLE_NOT_SUPPORT = 0;
    private static final int STATUS_BLE_DISABLED = 1;
    private static final int STATUS_BLE_ENABLED = 2;
    private static final int STATUS_BLE_SEARCHING = 3;
    private static final int STATUS_BLE_CONNECTED = 4;
    private static final int STATUS_BLE_DISCONNECTED = 5;
    private static final int STATUS_BLE_SEARCH_TIME_OUT = 6;

    private static final int SCAN_TIME_OUT = 3000;

    private static final String UUID_SERVICE_NOTIFY = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private static final String UUID_SERVICE_WRITE = "0000ffe5-0000-1000-8000-00805f9b34fb";
    private static final String UUID_CHARACTERISTIC_NOTIFY = "0000ffe4-0000-1000-8000-00805f9b34fb";
    private static final String UUID_CHARACTERISTIC_WRITE = "0000ffe9-0000-1000-8000-00805f9b34fb";

    private static final int REQUEST_ENABLE_BT = 0x01;

    private ActivityOneKeyBinding binding;
    private Handler mHandler;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattService notifyService;
    private BluetoothGattService writeService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_one_key);

        initView();
        mHandler = new Handler();
        if (!initBLE()) {
            return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            notifyStatusChange(STATUS_BLE_ENABLED);

            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();

            startSearch();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSearch();
        close();
    }

    private void close() {
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    private void initView() {

        binding.btnSearch.setOnClickListener(this);
        binding.btnWrite.setOnClickListener(this);
        binding.cbNotify.setOnCheckedChangeListener(this);
    }

    private void startSearch() {
        if (mBluetoothAdapter != null) {
            binding.btnSearch.setEnabled(false);

            notifyStatusChange(STATUS_BLE_SEARCHING);

            mHandler.removeCallbacks(scanRunnable);
            mHandler.postDelayed(scanRunnable, SCAN_TIME_OUT);
            mBluetoothAdapter.startLeScan(this);
        }
    }

    private void stopSearch() {
        mHandler.removeCallbacks(scanRunnable);
        mBluetoothAdapter.stopLeScan(this);

        binding.btnSearch.setEnabled(true);
    }

    private Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            notifyStatusChange(STATUS_BLE_SEARCH_TIME_OUT);
            stopSearch();
        }
    };

    private boolean initBLE() {
        if (bleSupported()) {
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
            // TODO: 2018/2/14 null
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                notifyStatusChange(STATUS_BLE_DISABLED);

                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                notifyStatusChange(STATUS_BLE_ENABLED);

                startSearch();
            }
            return true;
        }
        return false;
    }

    private boolean bleSupported() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            notifyStatusChange(STATUS_BLE_NOT_SUPPORT);
            return false;
        }
        return true;
    }

    private void notifyStatusChange(int status) {
        switch (status) {
            case STATUS_BLE_NOT_SUPPORT:
                binding.tvStatus.setText("设备不支持BLE");
                break;
            case STATUS_BLE_DISABLED:
                binding.tvStatus.setText("蓝牙未打开");
                break;
            case STATUS_BLE_ENABLED:
                binding.tvStatus.setText("蓝牙已打开");
                break;
            case STATUS_BLE_SEARCHING:
                binding.tvStatus.setText("正在搜索设备");
                break;
            case STATUS_BLE_CONNECTED:
                binding.tvStatus.setText("已连接");
                break;
            case STATUS_BLE_DISCONNECTED:
                binding.tvStatus.setText("连接已断开");
                break;
            case STATUS_BLE_SEARCH_TIME_OUT:
                binding.tvStatus.setText("搜索设备超时");
                break;
        }
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        stopSearch();

        device.connectGatt(this, false, callback);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_search) {
            startSearch();
        } else {
            write();
        }
    }

    private void write() {
        if (writeService != null) {
            BluetoothGattCharacteristic characteristic = writeService.getCharacteristic(UUID.fromString(UUID_CHARACTERISTIC_WRITE));
            if (characteristic != null) {
                String raw = binding.etWrite.getText().toString();
                if (raw.length() % 2 != 0) {
                    raw = "0" + raw;
                }
                characteristic.setValue(HexTransform.hexStringToBytes(raw));
                bluetoothGatt.writeCharacteristic(characteristic);
            }
        }
    }

    private BluetoothGattCallback callback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();

                Log.d(TAG, "Connected to GATT server.");
                Log.d(TAG, "Attempting to start service discovery:");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT server.");

                OneKeyActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyStatusChange(STATUS_BLE_DISCONNECTED);
                    }
                });
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onServicesDiscovered: ");
                onServiceDiscovered(gatt);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            notifyCharacteristicChanged(characteristic);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onCharacteristicRead: " + HexTransform.bytesToHexString(characteristic.getValue()));
            }
        }
    };

    private void notifyCharacteristicChanged(final BluetoothGattCharacteristic characteristic) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.tvValue.setText(HexTransform.bytesToHexString(characteristic.getValue()));
            }
        });
    }

    private void onServiceDiscovered(BluetoothGatt gatt) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyStatusChange(STATUS_BLE_CONNECTED);
            }
        });

        this.bluetoothGatt = gatt;
        notifyService = gatt.getService(UUID.fromString(UUID_SERVICE_NOTIFY));
        writeService = gatt.getService(UUID.fromString(UUID_SERVICE_WRITE));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setCharacteristicNotification(isChecked);
    }

    private void setCharacteristicNotification(boolean enabled) {
        if (notifyService != null) {
            BluetoothGattCharacteristic characteristic = notifyService.getCharacteristic(UUID.fromString(UUID_CHARACTERISTIC_NOTIFY));
            if (characteristic != null) {
                bluetoothGatt.setCharacteristicNotification(characteristic, enabled);
                List<BluetoothGattDescriptor> descriptor = characteristic.getDescriptors();
                descriptor.get(0).setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                bluetoothGatt.writeDescriptor(descriptor.get(0));
            }
        }
    }
}
