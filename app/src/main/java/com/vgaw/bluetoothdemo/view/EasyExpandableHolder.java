package com.vgaw.bluetoothdemo.view;

import android.view.View;

/**
 * Created by caojin on 2017/12/11.
 */

public abstract class EasyExpandableHolder extends ListViewHolder {
    public abstract View createView(int groupPosition, int childPosition);

    public abstract void refreshView(int groupPosition, int childPosition, Object item);
}
