package com.vgaw.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * from : Volodymyr
 * to : caojinmail@163.com
 * me : github.com/VolodymyrCj/
 */
public class ClientService {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice device;
    private Handler mHandler;
    private ConnectThread connectThread;
    private TalkThread talkThread;

    private BluetoothSocket mmSocket;

    private boolean isConnected = false;

    public ClientService(BluetoothAdapter mBluetoothAdapter, BluetoothDevice device, Handler handler){
        this.mBluetoothAdapter = mBluetoothAdapter;
        this.device = device;
        this.mHandler = handler;
    }

    public synchronized void connect(){
        if (!isConnected){
            connectThread = new ConnectThread();
            connectThread.start();
        }
    }

    public synchronized void sendMsg(String msg){
        if (isConnected){
            if (talkThread == null || !talkThread.isNormal()) {
                talkThread = null;
                talkThread = new TalkThread(mmSocket, mHandler);
                talkThread.start();
            }
            talkThread.sendMsg(msg);
        }
    }

    public void release(){
        talkThread.release();
        talkThread = null;
    }

    public class ConnectThread extends Thread {
        public ConnectThread() {
            try {
                mmSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66"));
            } catch (IOException e) {
                mmSocket = null;
            }
        }

        public void run() {
            mBluetoothAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                } catch (IOException closeException) {}
                Log.e("fuck", "连接服务器出现异常,已经关闭");
                return;
            }
            isConnected = true;
            mHandler.sendEmptyMessage(Statics.MSG_CONNECTED);
            Log.e("fuck", "连接成功");
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}
