package com.vgaw.bluetoothdemo.util;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by caojin on 2018/2/14.
 */

public class BLEUtil {
    public static boolean isCharacteristicWriteable(int charaProp) {
        return (charaProp & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0;
    }

    public static boolean isCharacterisitcReadable(int charaProp) {
        return ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) != 0);
    }

    public static boolean isCharacterisiticNotifiable(int charaProp) {
        return (charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
    }
}
