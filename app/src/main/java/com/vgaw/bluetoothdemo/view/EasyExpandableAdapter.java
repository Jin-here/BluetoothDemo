package com.vgaw.bluetoothdemo.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import java.util.List;

/**
 * Created by caojin on 2017/12/11.
 */

public abstract class EasyExpandableAdapter<G, C> extends BaseExpandableListAdapter {
    private Context context;
    private List<G> groupList;
    private List<List<C>> childList;

    public EasyExpandableAdapter(Context context, List<G> groupList, List<List<C>> childList) {
        this.context = context;
        this.groupList = groupList;
        this.childList = childList;
    }

    @Override
    public int getGroupCount() {
        return groupList == null ? 0 : groupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childList.get(groupPosition) == null ? 0 : childList.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childList.get(groupPosition) == null ? null : childList.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        EasyHolder holder = null;
        if (convertView == null || !(convertView.getTag() instanceof EasyHolder)) {
            holder = getGroupHolder(getGroupType(groupPosition));
            holder.init(context);
            convertView = holder.createView(groupPosition);
            convertView.setTag(holder);
        } else {
            holder = (EasyHolder) convertView.getTag();
        }
        holder.refreshView(groupPosition, getGroup(groupPosition));
        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        EasyExpandableHolder holder = null;
        if (convertView == null || !(convertView.getTag() instanceof EasyExpandableHolder)) {
            holder = getChildHolder(getChildType(groupPosition, childPosition));
            holder.init(context);
            convertView = holder.createView(groupPosition, childPosition);
            convertView.setTag(holder);
        } else {
            holder = (EasyExpandableHolder) convertView.getTag();
        }
        holder.refreshView(groupPosition, childPosition, getChild(groupPosition, childPosition));
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    protected abstract EasyHolder getGroupHolder(int type);

    protected abstract EasyExpandableHolder getChildHolder(int type);
}
