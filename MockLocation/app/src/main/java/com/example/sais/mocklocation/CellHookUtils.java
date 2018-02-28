package com.example.sais.mocklocation;

import android.os.Build;
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

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

/**
 * Created by ywq on 2017-09-07.
 */
public class CellHookUtils {
    static void HookAndChange(ClassLoader classLoader, final int lac, final int cid) {
        XposedHelpers.findAndHookMethod("android.net.wifi.WifiManager", classLoader, "getScanResults", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(new ArrayList<>());
            }
        });

        XposedHelpers.findAndHookMethod("android.net.wifi.WifiManager", classLoader, "getWifiState", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(1);
            }
        });

        XposedHelpers.findAndHookMethod("android.net.wifi.WifiManager", classLoader, "isWifiEnabled", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(true);
            }
        });

        XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", classLoader, "getMacAddress", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult("00-00-00-00-00-00-00-00");
            }
        });

        XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", classLoader, "getSSID", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult("null");
            }
        });

        XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", classLoader, "getBSSID", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult("00-00-00-00-00-00-00-00");
            }
        });


        XposedHelpers.findAndHookMethod("android.net.NetworkInfo", classLoader,
                "getTypeName", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult("WIFI");
                    }
                });
        XposedHelpers.findAndHookMethod("android.net.NetworkInfo", classLoader,
                "isConnectedOrConnecting", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(true);
                    }
                });

        XposedHelpers.findAndHookMethod("android.net.NetworkInfo", classLoader,
                "isConnected", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(true);
                    }
                });

        XposedHelpers.findAndHookMethod("android.net.NetworkInfo", classLoader,
                "isAvailable", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(true);
                    }
                });

        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", classLoader,
                "getCellLocation", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        GsmCellLocation gsmCellLocation = new GsmCellLocation();
                        gsmCellLocation.setLacAndCid(lac, cid);
                        param.setResult(gsmCellLocation);
                    }
                });

        XposedHelpers.findAndHookMethod("android.telephony.PhoneStateListener", classLoader,
                "onCellLocationChanged", CellLocation.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        GsmCellLocation gsmCellLocation = new GsmCellLocation();
                        gsmCellLocation.setLacAndCid(lac, cid);
                        param.setResult(gsmCellLocation);
                    }
                });

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", classLoader,
                    "getPhoneCount", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(1);
                        }
                    });
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", classLoader,
                    "getNeighboringCellInfo", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(new ArrayList<>());
                        }
                    });
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", classLoader,
                    "getAllCellInfo", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            //Gsm(其他Network Type则参数值不同)
                            param.setResult(getCell(460, 0, lac, cid, 0, 1));
                        }
                    });
            XposedHelpers.findAndHookMethod("android.telephony.PhoneStateListener", classLoader,
                    "onCellInfoChanged", List.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(getCell(460, 0, lac, cid, 0, 1));
                        }
                    });
        }


        XposedHelpers.findAndHookMethod("android.telephony.CellInfo", classLoader,
                "isRegistered", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(true);
                    }
                });
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
        if (networkType == 1 || networkType == 2) {
            arrayList.add(cellInfoGsm);
        } else if (networkType == 13) {
            arrayList.add(cellInfoLte);
        } else if (networkType == 4 || networkType == 5 || networkType == 6 || networkType == 7 || networkType == 12 || networkType == 14) {
            arrayList.add(cellInfoCdma);
        } else if (networkType == 3 || networkType == 8 || networkType == 9 || networkType == 10 || networkType == 15) {
            arrayList.add(cellInfoWcdma);
        }
        return arrayList;
    }


}
