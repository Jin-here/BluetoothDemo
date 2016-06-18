package com.vgaw.bluetoothdemo.activity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.vgaw.bluetoothdemo.ClientService;
import com.vgaw.bluetoothdemo.R;
import com.vgaw.bluetoothdemo.ServerService;
import com.vgaw.bluetoothdemo.Statics;
import com.vgaw.bluetoothdemo.fragment.DeviceListFragment;
import com.vgaw.bluetoothdemo.fragment.TalkListFragment;

/**
 * Created by caojin on 2016/6/10.
 */
public class MainActivity extends AppCompatActivity {
    private static final int DICOVERY_DURATION = 300;

    private static final int REQUEST_ENABLE_BT = 0x70;

    private String[] serverState = new String[]{"允许被发现", "开始接收客户端连入", "开始接收消息"};
    private String[] clientState = new String[]{"打开蓝牙", "开始搜索"};

    private BluetoothAdapter mBluetoothAdapter;
    private TextView tv_title;
    private EditText et_msg;
    private Button btn_send;

    private View v_client;
    private Button btn_search;
    private Button btn_cancel;

    private View v_server;
    private Button btn_server;

    private ServerService serverService;
    private ClientService clientService;

    private DeviceListFragment deviceListFragment;
    private TalkListFragment talkListFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        tv_title = (TextView) findViewById(R.id.tv_title);
        et_msg = (EditText) findViewById(R.id.et_msg);
        btn_send = (Button) findViewById(R.id.btn_send);
        v_client = findViewById(R.id.v_client);
        btn_search = (Button) findViewById(R.id.btn_search);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        v_server = findViewById(R.id.v_server);
        btn_server = (Button) findViewById(R.id.btn_server);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(MainActivity.this, "设备不支持Bluetooth", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        makeChoice();
    }

    private void makeChoice() {
        new AlertDialog.Builder(this)
                .setTitle("请选择")
                .setItems(new String[]{"服务端", "客户端"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        init(which);
                    }
                }).create().show();
    }

    private void init(int which) {
        // 服务端
        if (which == 0) {
            serverService = new ServerService(mBluetoothAdapter, mHandler);
            btn_server.setOnClickListener(listener);
            if (mBluetoothAdapter.getScanMode() !=
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                btn_server.setText(serverState[0]);
            }else {
                btn_server.setText(serverState[1]);
            }
            v_server.setVisibility(View.VISIBLE);
            changeFragment(true);
        }
        // 客户端
        else {
            btn_search.setOnClickListener(listener);
            if (!mBluetoothAdapter.isEnabled()) {
                btn_search.setText(clientState[0]);
            }else {
                btn_search.setText(clientState[1]);
            }
            btn_cancel.setOnClickListener(listener);
            btn_send.setOnClickListener(listener);
            v_client.setVisibility(View.VISIBLE);
            changeFragment(false);
        }
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // 客户端：搜索
                case R.id.btn_search:
                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                        return;
                    }
                    if (!mBluetoothAdapter.startDiscovery()) {
                        Toast.makeText(MainActivity.this, "开始搜索出现异常", Toast.LENGTH_SHORT).show();
                    } else {
                        deviceListFragment.clearDevice();
                        Toast.makeText(MainActivity.this, "正在进行搜索", Toast.LENGTH_SHORT).show();
                    }
                    break;
                // 客户端：停止搜索
                case R.id.btn_cancel:
                    if (mBluetoothAdapter.isDiscovering()) {
                        if (mBluetoothAdapter.cancelDiscovery()) {
                            Toast.makeText(MainActivity.this, "已经停止", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "停止出现异常", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                // 客户端：发送消息
                case R.id.btn_send:
                    clientService.sendMsg(et_msg.getText().toString() + "\n");
                    break;
                // 服务端：设置为服务器
                case R.id.btn_server:
                    // 请求允许被其他蓝牙设备发现
                    if (mBluetoothAdapter.getScanMode() !=
                            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                        Intent discoverableIntent = new
                                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        // 默认120s
                        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DICOVERY_DURATION);
                        startActivityForResult(discoverableIntent, 0x00);
                        return;
                    }
                    // 开始接收客户端连接
                    serverService.accept();
                    break;
            }
        }
    };

    private void changeFragment(boolean isTalk){
        if (isTalk){
            if (talkListFragment == null){
                talkListFragment = new TalkListFragment();
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.container, talkListFragment).commit();
            tv_title.setText("消息列表");
        }else {
            if (deviceListFragment == null){
                deviceListFragment = new DeviceListFragment() {
                    @Override
                    public void onItemClick(BluetoothDevice device) {
                        // 暂停搜索
                        mBluetoothAdapter.cancelDiscovery();
                        clientService = new ClientService(mBluetoothAdapter, device, mHandler);
                        clientService.connect();
                    }
                };
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.container, deviceListFragment).commit();
            tv_title.setText("设备列表");
        }
    }

    private void testBluetooth() {
        /*// 显示bounded设备
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                testList.add(device);
            }
            adapter.notifyDataSetChanged();
        }*/

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            return;
        }
        if (resultCode == DICOVERY_DURATION){
            btn_server.setText(serverState[1]);
        }
        if (requestCode == REQUEST_ENABLE_BT){
            btn_search.setText(clientState[1]);
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.e("fuck", "找到蓝牙设备");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                deviceListFragment.addDevice(device);
            } else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
                switch (mBluetoothAdapter.getScanMode()) {
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.e("fuck", "scan mode:none");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.e("fuck", "scan mode:connectable");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.e("fuck", "scan mode:connectable and discoverable");
                        break;
                }
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                switch (mBluetoothAdapter.getState()) {
                    case BluetoothAdapter.STATE_ON:
                        Log.e("fuck", "蓝牙打开");
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        Log.e("fuck", "蓝牙关闭");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.e("fuck", "蓝牙已经连接");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.e("fuck", "蓝牙正在连接");
                        break;
                    case BluetoothAdapter.STATE_DISCONNECTED:
                        Log.e("fuck", "蓝牙已断开连接");
                        break;
                    case BluetoothAdapter.STATE_DISCONNECTING:
                        Log.e("fuck", "蓝牙正在断开连接");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.e("fuck", "蓝牙正在关闭");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.e("fuck", "蓝牙正在打开");
                        break;
                }
            }
            // 开始搜索
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.e("fuck", "开始搜索");
            }
            // 搜索结束
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.e("fuck", "搜索结束");
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        // 注册搜索设备监听器
        IntentFilter discoveryFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, discoveryFilter);

        // 蓝牙scan mode监听器
        IntentFilter modeFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mReceiver, modeFilter);

        // 蓝牙状态监听
        IntentFilter stateChangedFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, stateChangedFilter);

        // 监听开始进行搜索
        IntentFilter discoveryStartedFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(mReceiver, discoveryStartedFilter);

        // 监听搜索结束
        IntentFilter discoveryFinishedFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, discoveryFinishedFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 连接服务器成功
                case Statics.MSG_CONNECTED:
                    changeFragment(true);
                    break;
                // 接收客户端成功
                case Statics.MSG_ACCEPTED:
                    serverService.receive();
                    btn_server.setText(serverState[2]);
                    break;
                // 收到信息
                case Statics.MSG_RECEIVED:
                    talkListFragment.addMsg("other:" + String.valueOf(msg.obj));
                    break;
                // 发送消息
                case Statics.MSG_SENT:
                    talkListFragment.addMsg("me:" + String.valueOf(msg.obj));
                    et_msg.setText("");
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serverService != null){
            serverService.release();
            serverService = null;
        }
        if (clientService != null){
            clientService.release();
            clientService = null;
        }
    }
}
