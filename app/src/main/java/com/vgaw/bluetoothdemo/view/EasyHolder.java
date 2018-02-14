package com.vgaw.bluetoothdemo.view;

import android.view.View;

/**
 * from : Volodymyr
 * to : caojinmail@163.com
 * me : github.com/VolodymyrCj/
 */
public abstract class EasyHolder extends ListViewHolder {
    public abstract View createView(int position);

    public abstract void  refreshView(int position, Object item);
}
