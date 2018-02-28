package com.example.sais.mocklocation;

/**
 * Created by ywq on 2017-09-20.
 */
public class Common {
    public static final String MY_PACKAGE_NAME = Common.class.getPackage().getName();
    public static final String ACTION_PERMISSIONS = "update_permissions";
    public static final String PREFS = "ModSettings";
    public static final String PREFS_LATITUDE = "/latitude";
    public static final String PREFS_LONGITUDE = "/longitude";
    public static final String PREFS_MNC = "/mnc";
    public static final String PREFS_LAC = "/lac";
    public static final String PREFS_CID = "/cid";
    public static final String PREFS_ACTIVE = "/active";
    public static final String PREFS_GPS_ACTIVE = "/gps_active";
    public static final String PREFS_CELL_ACTIVE = "/cell_active";
    public static final String PREFS_CELL_TYPE = "/cell_type";

    public static final String PREFS_DEFAULT = "/default";

    public static final int CELL_TYPE_GSM = 0;
    public static final int CELL_TYPE_LTE = 1;
    public static final int CELL_TYPE_CDMA = 2;
    public static final int CELL_TYPE_WCDMA = 3;
}
