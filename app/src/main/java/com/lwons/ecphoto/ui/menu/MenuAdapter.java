package com.lwons.ecphoto.ui.menu;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lwons.ecphoto.R;
import com.lwons.ecphoto.util.FileUtils;

/**
 * Created by liuwons on 18-12-9.
 */
public class MenuAdapter extends BaseExpandableListAdapter {
    private static final String PRESET_MENU_PATH = "json/preset_menu.json";

    public static MenuModel getDefaultMenu(Context context) {
        String json = FileUtils.getAssetContent(context, PRESET_MENU_PATH);
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        Gson gson = new Gson();
        return gson.fromJson(json, MenuModel.class);
    }

    private MenuModel mMenuModel;

    public void loadMenu(MenuModel model) {
        mMenuModel = model;
    }

    @Override
    public int getGroupCount() {
        return mMenuModel.groups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mMenuModel.groups.get(groupPosition).items.size();
    }

    @Override
    public MenuModel.MenuGroup getGroup(int groupPosition) {
        return mMenuModel.groups.get(groupPosition);
    }

    @Override
    public MenuModel.MenuItem getChild(int groupPosition, int childPosition) {
        return getGroup(groupPosition).items.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return getGroup(groupPosition).id;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return getChild(groupPosition, childPosition).id;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View groupView = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_group, null);
        View bottomDivider = groupView.findViewById(R.id.bottom_divider);
        ImageView arrowView = groupView.findViewById(R.id.arrow_iv);
        TextView title = groupView.findViewById(R.id.group_title_tv);
        if (groupPosition == getGroupCount() -1) {
            bottomDivider.setVisibility(View.GONE);
        }
        if (isExpanded) {
            arrowView.setImageResource(R.drawable.menu_group_conceal);
        } else {
            arrowView.setImageResource(R.drawable.menu_group_unfold);
        }
        title.setText(getGroup(groupPosition).name);
        return groupView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item, null);
        TextView title = itemView.findViewById(R.id.item_tv);
        title.setText(getChild(groupPosition, childPosition).name);
        return itemView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
