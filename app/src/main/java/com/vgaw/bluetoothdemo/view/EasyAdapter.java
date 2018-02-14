package com.vgaw.bluetoothdemo.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * from : Volodymyr
 * to : caojinmail@163.com
 * me : github.com/VolodymyrCj/
 */
public abstract class EasyAdapter extends BaseAdapter {
    protected Context context;
    private List dataList;

    public EasyAdapter(Context context, List dataList){
        this.context = context;
        this.dataList = dataList;
    }

    @Override
    public int getCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList == null ? null : dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        EasyHolder holder = null;
        if (convertView == null){
            holder = getHolder(getItemViewType(position));
            holder.init(context);
            convertView = holder.createView(position);
            convertView.setTag(holder);
        }else {
            holder = (EasyHolder) convertView.getTag();
        }
        holder.refreshView(position, getItem(position));
        return convertView;
    }

    public abstract EasyHolder getHolder(int type);

}
