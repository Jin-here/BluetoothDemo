package com.vgaw.bluetoothdemo;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * from : Volodymyr
 * to : caojinmail@163.com
 * me : github.com/VolodymyrCj/
 */

/**
 * 出现异常自动关闭
 */
public class TalkThread extends Thread {
    private static final int BEHAVIOUR_NONE = 0;
    private static final int BEHAVIOUR_RECEIVE = 1;
    private static final int BEHAVIOUR_SEND = 2;
    private static final int BEHAVIOUR_RECEIVE_SEND = 3;

    private int behaviour = BEHAVIOUR_NONE;

    private BluetoothSocket socket;
    private Handler handler;

    private BufferedReader reader = null;
    private BufferedWriter writer = null;

    private AtomicBoolean isStop = new AtomicBoolean(false);
    private AtomicBoolean isNormal = new AtomicBoolean(true);

    private String msg = null;

    public TalkThread(BluetoothSocket socket, Handler handler){
        this.socket = socket;
        this.handler = handler;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            cancel();
        }
    }

    public boolean isNormal(){
        return isNormal.get();
    }

    @Override
    public void run() {
        super.run();
        while (!isStop.get()){
            switch (behaviour){
                case BEHAVIOUR_RECEIVE:
                    try {
                        msg = reader.readLine();
                        handler.sendMessage(handler.obtainMessage(Statics.MSG_RECEIVED, msg));
                        Log.e("fuck", msg);
                        behaviour = BEHAVIOUR_RECEIVE_SEND;
                    } catch (IOException e) {
                        cancel();
                    }
                    break;
                case BEHAVIOUR_SEND:
                    sendMsgInternal();
                    behaviour = BEHAVIOUR_NONE;
                    break;
                case BEHAVIOUR_RECEIVE_SEND:
                    sendMsgInternal();
                    behaviour = BEHAVIOUR_RECEIVE;
                    break;
            }
        }
    }

    public void receive(){
        behaviour = BEHAVIOUR_RECEIVE;
    }

    public void sendMsg(String msg){
        if (msg != null){
            this.msg = msg;
            behaviour = BEHAVIOUR_SEND;
        }
    }

    private void sendMsgInternal(){
        if (writer != null){
            try {
                writer.write(msg);
                writer.flush();
                handler.sendMessage(handler.obtainMessage(Statics.MSG_SENT, msg));
                Log.e("fuck", "消息发送成功");
            } catch (IOException e) {
                cancel();
            }
        }
    }

    public void cancel(){
        isStop = new AtomicBoolean(true);
        isNormal.getAndSet(false);
        release();
    }

    public void release(){
        if (reader != null){
            try {
                reader.close();
            } catch (IOException e) {
            }
        }
        if (writer != null){
            try {
                writer.close();
            } catch (IOException e) {
            }
        }
    }
}
