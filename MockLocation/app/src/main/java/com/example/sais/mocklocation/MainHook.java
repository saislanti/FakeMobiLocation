package com.example.sais.mocklocation;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by ywq on 2017-08-09.
 */
public class MainHook implements IXposedHookLoadPackage {

    public static XSharedPreferences prefs;

    public static final String this_package = MainHook.class.getPackage().getName();

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable{
        loadPrefs();

        if(this_package.equals(lpparam.packageName)){
            XposedHelpers.findAndHookMethod("com.example.sais.mocklocation.MainActivity",
                    lpparam.classLoader, "isModActive", XC_MethodReplacement.returnConstant(true));
        }

        try{
            if(isGpsActive(lpparam.packageName)){
                final Double lat = Double.parseDouble(prefs.getString(lpparam.packageName + Common.PREFS_LATITUDE, "0.0"));
                final Double log = Double.parseDouble(prefs.getString(lpparam.packageName + Common.PREFS_LONGITUDE, "0.0"));

                XposedHelpers.findAndHookMethod(LocationManager.class, "getLastLocation", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Location l = new Location(LocationManager.GPS_PROVIDER);
                        l.setLatitude(lat);
                        l.setLongitude(log);
                        l.setAccuracy(100f);
                        l.setTime(System.currentTimeMillis());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            l.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                        }
                        param.setResult(l);
                    }
                });

                XposedHelpers.findAndHookMethod(LocationManager.class, "getLastKnownLocation", String.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Location l = new Location(LocationManager.GPS_PROVIDER);
                        l.setLatitude(lat);
                        l.setLongitude(log);
                        l.setAccuracy(100f);
                        l.setTime(System.currentTimeMillis());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            l.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                        }
                        param.setResult(l);
                    }
                });

                XposedBridge.hookAllMethods(LocationManager.class, "getProviders", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        ArrayList<String> arrayList = new ArrayList<>();
                        arrayList.add("gps");
                        param.setResult(arrayList);
                    }
                });

                XposedHelpers.findAndHookMethod(LocationManager.class, "getBestProvider", Criteria.class, Boolean.TYPE, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult("gps");
                    }
                });

                XposedHelpers.findAndHookMethod(LocationManager.class, "addGpsStatusListener", GpsStatus.Listener.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (param.args[0] != null) {
                            XposedHelpers.callMethod(param.args[0], "onGpsStatusChanged", 1);
                            XposedHelpers.callMethod(param.args[0], "onGpsStatusChanged", 3);
                        }
                    }
                });

                XposedHelpers.findAndHookMethod(LocationManager.class, "addNmeaListener", GpsStatus.NmeaListener.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(false);
                    }
                });

                XposedHelpers.findAndHookMethod("android.location.LocationManager", lpparam.classLoader,
                        "getGpsStatus", GpsStatus.class, new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                GpsStatus gss = (GpsStatus) param.getResult();
                                if (gss == null)
                                    return;

                                Class<?> clazz = GpsStatus.class;
                                Method m = null;
                                for (Method method : clazz.getDeclaredMethods()) {
                                    if (method.getName().equals("setStatus")) {
                                        if (method.getParameterTypes().length > 1) {
                                            m = method;
                                            break;
                                        }
                                    }
                                }
                                if (m == null)
                                    return;

                                //access the private setStatus function of GpsStatus
                                m.setAccessible(true);

                                //make the apps belive GPS works fine now
                                int svCount = 5;
                                int[] prns = {1, 2, 3, 4, 5};
                                float[] snrs = {0, 0, 0, 0, 0};
                                float[] elevations = {0, 0, 0, 0, 0};
                                float[] azimuths = {0, 0, 0, 0, 0};
                                int ephemerisMask = 0x1f;
                                int almanacMask = 0x1f;

                                //5 satellites are fixed
                                int usedInFixMask = 0x1f;

                                XposedHelpers.callMethod(gss, "setStatus", svCount, prns, snrs, elevations, azimuths, ephemerisMask, almanacMask, usedInFixMask);
                                param.args[0] = gss;
                                param.setResult(gss);
                                try {
                                    m.invoke(gss, svCount, prns, snrs, elevations, azimuths, ephemerisMask, almanacMask, usedInFixMask);
                                    param.setResult(gss);
                                } catch (Exception e) {
                                    XposedBridge.log(e);
                                }
                            }
                        });

                for (Method method : LocationManager.class.getDeclaredMethods()) {
                    if (method.getName().equals("requestLocationUpdates")
                            && !Modifier.isAbstract(method.getModifiers())
                            && Modifier.isPublic(method.getModifiers())) {
                        XposedBridge.hookMethod(method, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                if (param.args.length >= 4 && (param.args[3] instanceof LocationListener)) {

                                    LocationListener ll = (LocationListener) param.args[3];

                                    Class<?> clazz = LocationListener.class;
                                    Method m = null;
                                    for (Method method : clazz.getDeclaredMethods()) {
                                        if (method.getName().equals("onLocationChanged") && !Modifier.isAbstract(method.getModifiers())) {
                                            m = method;
                                            break;
                                        }
                                    }
                                    Location l = new Location(LocationManager.GPS_PROVIDER);
                                    l.setLatitude(lat);
                                    l.setLongitude(log);
                                    l.setAccuracy(10.00f);
                                    l.setTime(System.currentTimeMillis());
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                        l.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                                    }
                                    XposedHelpers.callMethod(ll, "onLocationChanged", l);
                                    try {
                                        if (m != null) {
                                            m.invoke(ll, l);
                                        }
                                    } catch (Exception e) {
                                        XposedBridge.log(e);
                                    }
                                }
                            }
                        });
                    }

                    if (method.getName().equals("requestSingleUpdate ")
                            && !Modifier.isAbstract(method.getModifiers())
                            && Modifier.isPublic(method.getModifiers())) {
                        XposedBridge.hookMethod(method, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                if (param.args.length >= 3 && (param.args[1] instanceof LocationListener)) {

                                    LocationListener ll = (LocationListener) param.args[3];

                                    Class<?> clazz = LocationListener.class;
                                    Method m = null;
                                    for (Method method : clazz.getDeclaredMethods()) {
                                        if (method.getName().equals("onLocationChanged") && !Modifier.isAbstract(method.getModifiers())) {
                                            m = method;
                                            break;
                                        }
                                    }

                                    try {
                                        if (m != null) {
                                            Location l = new Location(LocationManager.GPS_PROVIDER);
                                            l.setLatitude(lat);
                                            l.setLongitude(log);
                                            l.setAccuracy(100f);
                                            l.setTime(System.currentTimeMillis());
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                                l.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                                            }
                                            m.invoke(ll, l);
                                        }
                                    } catch (Exception e) {
                                        XposedBridge.log(e);
                                    }
                                }
                            }
                        });
                    }
                }
            }
        } catch(Throwable t){
            XposedBridge.log(t);
        }


        try{
            if(isCellActive(lpparam.packageName)){
                final int mnc = prefs.getInt(lpparam.packageName + Common.PREFS_MNC, 0);
                final int lac = prefs.getInt(lpparam.packageName + Common.PREFS_LAC, 0);
                final int cid = prefs.getInt(lpparam.packageName + Common.PREFS_CID, 0);
                final int netType = prefs.getInt(lpparam.packageName +Common.PREFS_CELL_TYPE, 0);

                findAndHookMethod("android.telephony.TelephonyManager", lpparam.classLoader, "getCellLocation", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Log.i("sai", "Hook getCellLocation");
                        GsmCellLocation gsmCellLocation = new GsmCellLocation();
                        gsmCellLocation.setLacAndCid(lac, cid);
                        param.setResult(gsmCellLocation);
                    }
                });

                findAndHookMethod("android.telephony.PhoneStateListener", lpparam.classLoader, "onCellLocationChanged",
                        CellLocation.class, new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                //此处加Log
                                Log.i("sai","Hook onCellLocationChanged");
                                GsmCellLocation gsmCellLocation = new GsmCellLocation();
                                gsmCellLocation.setLacAndCid(lac, cid);
                                param.setResult(gsmCellLocation);
                            }
                        });

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                    findAndHookMethod("android.telephony.TelephonyManager", lpparam.classLoader, "getPhoneCount", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Log.i("sai","Hook getPhoneCount");
                            param.setResult(1);
                        }
                    });
                }

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    findAndHookMethod("android.telephony.TelephonyManager", lpparam.classLoader, "getNeighboringCellInfo", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Log.i("sai","Hook getNeighbouringCellInfo");
                            param.setResult(new ArrayList<>());
                        }
                    });
                }

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                    findAndHookMethod("android.telephony.TelephonyManager", lpparam.classLoader, "getAllCellInfo", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Log.i("sai", "Hook getCellInfo");
                            param.setResult(getCell(460, mnc, lac, cid, 0, netType));
                        }
                    });

                    findAndHookMethod("android.telephony.PhoneStateListener", lpparam.classLoader, "onCellInfoChanged",
                            List.class, new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                    Log.i("sai", "Hook onCellInfoChanged");
                                    param.setResult(getCell(460, mnc, lac, cid, 0, netType));
                                }
                            });

                    //获取手机扫描到的Wifi信息
                    findAndHookMethod("android.net.wifi.WifiManager", lpparam.classLoader, "getScanResults", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Log.i("sai", "Hook getScanResults");
                            param.setResult(new ArrayList<>());
                        }
                    });

                    //查看Wifi状态，WIFI_STATE_DISABLED = 1代表Wifi不能使用
                    findAndHookMethod("android.net.wifi.WifiManager", lpparam.classLoader, "getWifiState", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Log.i("sai", "getWifiState");
                            param.setResult(1);
                        }
                    });

                    //Wifi功能是否开启
                    findAndHookMethod("android.net.wifi.WifiManager", lpparam.classLoader, "isWifiEnabled", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Log.i("sai", "Hook isWifiEnabled");
                            param.setResult(true);
                        }
                    });

                    //获取Mac地址
                    findAndHookMethod("android.net.wifi.WifiInfo", lpparam.classLoader, "getMacAddress", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Log.i("sai", "Hook getMacAddress");
                            param.setResult("00-00-00-00-00-00-00-00");
                        }
                    });

                    //获取Wifi名称信息
                    findAndHookMethod("android.net.wifi.WifiInfo", lpparam.classLoader, "getSSID", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Log.i("sai", "Hook getSSID");
                            param.setResult("null");
                        }
                    });

                    //获取接入点的地址，即路由器的Mac地址
                    findAndHookMethod("android.net.wifi.WifiInfo", lpparam.classLoader, "getBSSID", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Log.i("sai", "Hook getBSSID");
                            param.setResult("00-00-00-00-00-00-00-00");
                        }
                    });

                    //获取网络类型名称，一般取值"WIFI"或"MOBILE"
                    findAndHookMethod("android.net.NetworkInfo", lpparam.classLoader, "getTypeName", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Log.i("sai", "Hook getTypeName");
                            param.setResult("WIFI");
                        }
                    });

                    //判断是否已经连接或是正在连接
                    findAndHookMethod("android.net.NetworkInfo", lpparam.classLoader, "isConnectedOrConnecting", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Log.i("sai", "Hook isConnectedOrConnecting");
                            param.setResult(true);
                        }
                    });

                    findAndHookMethod("android.net.NetworkInfo", lpparam.classLoader, "isConnected", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Log.i("sai", "Hook isConnected");
                            param.setResult(true);
                        }
                    });

                    findAndHookMethod("android.net.NetworkInfo", lpparam.classLoader, "isAvailable", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Log.i("sai", "Hook isAvailable");
                            param.setResult(true);
                        }
                    });

                    findAndHookMethod("android.telephony.CellInfo", lpparam.classLoader, "isRegistered", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Log.i("sai", "Hook isRegistered");
                            param.setResult(true);
                        }
                    });
                }
            }
        }catch(Throwable t){
            XposedBridge.log(t);
        }

    }

    private static ArrayList getCell(int mcc, int mnc, int lac, int cid, int sid, int networkType) {
        ArrayList arrayList = new ArrayList();

        //The “unique IDs” are SID:NID:BID for CDMA , and MCC:MNC LAC:CID for GSM

        CellInfoGsm cellInfoGsm = (CellInfoGsm) XposedHelpers.newInstance(CellInfoGsm.class);
        XposedHelpers.callMethod(cellInfoGsm, "setCellIdentity", XposedHelpers.newInstance(CellIdentityGsm.class,
                new Object[]{Integer.valueOf(mcc), Integer.valueOf(mnc), Integer.valueOf(lac), Integer.valueOf(cid)}));

        CellInfoCdma cellInfoCdma = (CellInfoCdma) XposedHelpers.newInstance(CellInfoCdma.class);
        XposedHelpers.callMethod(cellInfoCdma, "setCellIdentity", XposedHelpers.newInstance(CellIdentityCdma.class,
                new Object[]{Integer.valueOf(lac), Integer.valueOf(sid), Integer.valueOf(cid), Integer.valueOf(0), Integer.valueOf(0)}));

        CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) XposedHelpers.newInstance(CellInfoWcdma.class);
        XposedHelpers.callMethod(cellInfoWcdma, "setCellIdentity", XposedHelpers.newInstance(CellIdentityWcdma.class,
                new Object[]{Integer.valueOf(mcc), Integer.valueOf(mnc), Integer.valueOf(lac), Integer.valueOf(cid), Integer.valueOf(300)}));

        CellInfoLte cellInfoLte = (CellInfoLte) XposedHelpers.newInstance(CellInfoLte.class);
        XposedHelpers.callMethod(cellInfoLte, "setCellIdentity", XposedHelpers.newInstance(CellIdentityLte.class,
                new Object[]{Integer.valueOf(mcc), Integer.valueOf(mnc), Integer.valueOf(cid), Integer.valueOf(300), Integer.valueOf(lac)}));
        if (networkType == 0) {
            arrayList.add(cellInfoGsm);
        } else if (networkType == 1) {
            arrayList.add(cellInfoLte);
        } else if (networkType == 2) {
            arrayList.add(cellInfoCdma);
        } else if (networkType == 3) {
            arrayList.add(cellInfoWcdma);
        }
        return arrayList;
    }

    public static void loadPrefs(){
        prefs = new XSharedPreferences(Common.MY_PACKAGE_NAME, Common.PREFS);
        prefs.makeWorldReadable();
    }

    public static boolean isGpsActive(String packageName){
        return prefs.getBoolean(packageName + Common.PREFS_GPS_ACTIVE, false);
    }

    public static boolean isCellActive(String packageName){
        return prefs.getBoolean(packageName + Common.PREFS_CELL_ACTIVE, false);
    }

}
