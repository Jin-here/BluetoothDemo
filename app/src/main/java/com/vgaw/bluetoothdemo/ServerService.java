package com.vgaw.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
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
public class ServerService {
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private AcceptThread acceptThread;
    private TalkThread talkThread;

    private BluetoothSocket socket;
    private boolean isAcceptStarted = false;
    private boolean isAccepted = false;

    public ServerService(BluetoothAdapter mBluetoothAdapter, Handler handler) {
        this.mBluetoothAdapter = mBluetoothAdapter;
        this.mHandler = handler;
    }

    public synchronized void receive() {
        if (isAccepted) {
            if (talkThread == null || !talkThread.isNormal()) {
                talkThread = null;
                talkThread = new TalkThread(socket, mHandler);
                talkThread.start();
                talkThread.receive();
            }
        }
    }

    public synchronized void accept() {
        if (!isAcceptStarted) {
            isAcceptStarted = true;
            acceptThread = new AcceptThread(mBluetoothAdapter);
            acceptThread.start();
            Log.e("fuck", "开始接收");
        }
    }

    public void release() {
        if (talkThread != null) {
            talkThread.release();
            talkThread = null;
        }
    }

    public class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(BluetoothAdapter mBluetoothAdapter) {
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                UUID uuid = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("caojin", uuid);
            } catch (IOException e) {
            }
            mmServerSocket = tmp;
        }

        public void run() {
            //BluetoothSocket socket = null;
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e1) {
                        }
                    }
                    try {
                        mmServerSocket.close();
                    } catch (IOException e1) {
                    }
                    Log.e("fuck", "服务端接收连接异常，已经关闭");
                    break;
                }

                mHandler.sendEmptyMessage(Statics.MSG_ACCEPTED);
                isAccepted = true;
                Log.e("fuck", "连入新的连接");
                try {
                    mmServerSocket.close();
                } catch (IOException e) {
                }
                Log.e("fuck", "服务端正常关闭");
                break;
            }
        }

        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
            }
        }
    }
}
