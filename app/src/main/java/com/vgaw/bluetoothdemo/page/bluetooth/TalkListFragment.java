package com.vgaw.bluetoothdemo.page.bluetooth;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by caojin on 2016/6/17.
 */
public class TalkListFragment extends ListFragment {
    private ArrayList<String> dataList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.test_list_item,
                android.R.id.text1, dataList);
        setListAdapter(adapter);
    }

    public void addMsg(String item){
        dataList.add(item);
        adapter.notifyDataSetChanged();
    }
}
