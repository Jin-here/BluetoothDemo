package com.vgaw.bluetoothdemo.page.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.vgaw.bluetoothdemo.R;
import com.vgaw.bluetoothdemo.databinding.ActivityBleBinding;
import com.vgaw.bluetoothdemo.util.HexTransform;

/**
 * Created by caojin on 2018/2/13.
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BLEActivity extends AppCompatActivity implements BLEDeviceListFragment.BLEDeviceListListener {
    private static final String TAG = "BLEActivity";
    private static final int REQUEST_ENABLE_BT = 0x01;

    private ActivityBleBinding binding;
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ble);

        if (!initBLE()) {
            return;
        }
        initView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            Log.d(TAG, "onActivityResult bluetooth enabled");
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(ServiceListFragment.TAG);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .remove(fragment)
                    .commit();
        } else {
            super.onBackPressed();
        }
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return this.mBluetoothAdapter;
    }

    private void initView() {
        BLEDeviceListFragment listFragment = new BLEDeviceListFragment();
        listFragment.setBLEDeviceListListener(this);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container_ble, listFragment, BLEDeviceListFragment.TAG)
                .commit();
    }

    private boolean initBLE() {
        if (bleSupported()) {
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
            // TODO: 2018/2/14 null
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            return true;
        }
        return false;
    }

    private boolean bleSupported() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "该设备不支持BLE", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onDeviceChosen(BluetoothDevice device) {
        device.connectGatt(this, false, callback);
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

    private void notifyCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
        ServiceListFragment fragment = (ServiceListFragment) getSupportFragmentManager().findFragmentByTag(ServiceListFragment.TAG);
        if (fragment != null) {
            fragment.onCharacteristicChanged(characteristic);
        }
    }

    private void onServiceDiscovered(BluetoothGatt bluetoothGatt) {
        ServiceListFragment serviceListFragment = new ServiceListFragment();
        serviceListFragment.onServiceDiscovered(bluetoothGatt);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container_ble, serviceListFragment, ServiceListFragment.TAG)
                .commit();
    }
}
