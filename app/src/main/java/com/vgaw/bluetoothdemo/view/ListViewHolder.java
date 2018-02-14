package com.vgaw.bluetoothdemo.view;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by caojin on 2017/12/11.
 */

public abstract class ListViewHolder {
    protected View view;

    public void init(Context mContext) {
        view = LayoutInflater.from(mContext).inflate(getLayout(), null);
    }

    public abstract @LayoutRes
    int getLayout();
}
