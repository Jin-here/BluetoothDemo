package com.vgaw.bluetoothdemo.page.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.vgaw.bluetoothdemo.R;
import com.vgaw.bluetoothdemo.databinding.FragmentServiceListBinding;
import com.vgaw.bluetoothdemo.util.BLEUtil;
import com.vgaw.bluetoothdemo.util.HexTransform;
import com.vgaw.bluetoothdemo.view.EasyExpandableAdapter;
import com.vgaw.bluetoothdemo.view.EasyExpandableHolder;
import com.vgaw.bluetoothdemo.view.EasyHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by caojin on 2018/2/14.
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ServiceListFragment extends Fragment {
    public static final String TAG = "ServiceListFragment";
    private FragmentServiceListBinding binding;
    private BLEActivity mActivity;

    private BluetoothGatt bluetoothGatt;

    private EasyExpandableAdapter mAdapter;
    private List<BluetoothGattService> groupList = new ArrayList<>();
    private List<List<BluetoothGattCharacteristic>> childList = new ArrayList<>();

    public void onServiceDiscovered(BluetoothGatt bluetoothGatt) {
        this.bluetoothGatt = bluetoothGatt;

        List<BluetoothGattService> bluetoothGattServiceList = bluetoothGatt.getServices();
        for (BluetoothGattService service : bluetoothGattServiceList) {
            this.groupList.add(service);
            this.childList.add(service.getCharacteristics());
        }
    }

    public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (BLEActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_service_list, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new EasyExpandableAdapter<BluetoothGattService, BluetoothGattCharacteristic>(mActivity, this.groupList, this.childList) {
            @Override
            protected EasyHolder getGroupHolder(int type) {
                return new EasyHolder() {
                    private TextView tv0;
                    private TextView tv1;

                    @Override
                    public View createView(int position) {
                        tv0 = view.findViewById(R.id.tv0);
                        tv1 = view.findViewById(R.id.tv1);
                        return view;
                    }

                    @Override
                    public void refreshView(int position, Object item) {
                        BluetoothGattService service = (BluetoothGattService) item;
                        tv0.setText(service.getUuid().toString());
                    }

                    @Override
                    public int getLayout() {
                        return R.layout.item_style1;
                    }
                };
            }

            @Override
            protected EasyExpandableHolder getChildHolder(int type) {
                return new EasyExpandableHolder() {
                    private TextView tv0;
                    private TextView tv1;
                    private TextView tv2;
                    private Button btn_notify;
                    private Button btn_write;
                    private EditText et_write;

                    @Override
                    public View createView(int groupPosition, int childPosition) {
                        tv0 = view.findViewById(R.id.tv0);
                        tv1 = view.findViewById(R.id.tv1);
                        tv2 = view.findViewById(R.id.tv2);
                        btn_notify = view.findViewById(R.id.btn_notify);
                        btn_write = view.findViewById(R.id.btn_write);
                        et_write = view.findViewById(R.id.et_write);
                        return view;
                    }

                    @Override
                    public void refreshView(int groupPosition, int childPosition, Object item) {
                        BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) item;
                        tv0.setText("UUID: " + characteristic.getUuid().toString());
                        proPropertyUI(characteristic.getProperties());
                        tv2.setText("Value: " + HexTransform.bytesToHexString(characteristic.getValue()));

                        View.OnClickListener clickListener = new CharacteristicClickListener(characteristic) {
                            @Override
                            public void onClick(View v) {
                                if (v.getId() == R.id.btn_notify) {
                                    // notify
                                    setCharacteristicNotification(mCharacteristic, true);
                                } else {
                                    // write
                                    mCharacteristic.setValue(HexTransform.hexStringToBytes(et_write.getText().toString()));
                                    bluetoothGatt.writeCharacteristic(mCharacteristic);
                                }
                            }
                        };
                        btn_notify.setOnClickListener(clickListener);
                        btn_write.setOnClickListener(clickListener);
                    }

                    @Override
                    public int getLayout() {
                        return R.layout.item_style2;
                    }

                    private void proPropertyUI(int charaProp) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Property: ");
                        if (BLEUtil.isCharacterisitcReadable(charaProp)) {
                            sb.append("READ ");
                        }
                        if (BLEUtil.isCharacterisiticNotifiable(charaProp)) {
                            sb.append("NOTIFY ");
                            btn_notify.setVisibility(View.VISIBLE);
                        } else {
                            btn_notify.setVisibility(View.GONE);
                        }
                        if (BLEUtil.isCharacteristicWriteable(charaProp)) {
                            sb.append("WRITE ");
                            btn_write.setVisibility(View.VISIBLE);
                            et_write.setVisibility(View.VISIBLE);
                        } else {
                            btn_write.setVisibility(View.GONE);
                            et_write.setVisibility(View.GONE);
                        }
                        tv1.setText(sb.toString());
                    }
                };
            }
        };
        binding.elvServiceList.setAdapter(mAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        close();
    }

    private void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        bluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        List<BluetoothGattDescriptor> descriptor = characteristic.getDescriptors();
        descriptor.get(0).setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor.get(0));
    }

    private void close() {
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    public abstract class CharacteristicClickListener implements View.OnClickListener {
        protected BluetoothGattCharacteristic mCharacteristic;

        public CharacteristicClickListener(BluetoothGattCharacteristic characteristic) {
            this.mCharacteristic = characteristic;
        }
    }
}
