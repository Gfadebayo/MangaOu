package com.exzell.mangaplayground.advancedsearch;

public enum Order {

        A_Z("a-z", "A-Z"),
        RATING("rating", "Rating"),
        UPDATE("update", "Update"),
        CREATE("create", "Create"),
        TOTAL("views_a", "Total"),
        _365DAYS("views_y", "365 Days"),
        _180DAYS("views_s", "180 Days"),
        _90DAYS("views_t", "90 Days"),
        _30DAYS("views_m", "30 Days"),
        _7DAYS("views_w", "7 Days"),
        _24HOURS("views_d", "24 Hours"),
        _12HOURS("views_l", "12 Hours"),
        _6HOURS("views_x", "6 Hours"),
        _60MINUTES("views_h", "60 Minutes");

        Order(String val, String dispName){
            this.val = val;
            this.dispName = dispName;

            group = val.contains("views_") ? "Views" : "";
        }

        public String val;
        public String dispName;
        public String group;
}
