package com.lwons.ecphoto.ui.menu;

import java.util.List;

/**
 * Created by liuwons on 18-12-9.
 */
public class MenuModel {

    public int version;

    public List<MenuGroup> groups;

    public static class MenuGroup {
        public long id;
        public String name;
        public List<MenuItem> items;
    }

    public static class MenuItem {
        public long id;
        public String name;
    }
}
