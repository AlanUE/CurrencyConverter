package sg.com.kaplan.pdma.currencyconverter;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.InputStream;
import java.net.URL;

public class internet_connection {
    // This variable is used for debug log (LogCat)
    private static final String TAG = "CC:internet_connection";
    private TelephonyManager telephonyManager;
    private WifiManager wifiManager;

    // flags
    private boolean roaming_network = false;

    public internet_connection(Context context) {
        // get telephony service
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        // get WIFI service
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public void EnableNetworkRoaming(boolean flag) {
        roaming_network = flag;
    }

    public boolean IsWIFIAvailabe() {
        try {
            if (wifiManager.isWifiEnabled()) {
                if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {

                    WifiInfo info = wifiManager.getConnectionInfo();

                    if (info.getNetworkId() != -1) {
                        return true;
                    } else {
                        Log.w(TAG, "No network is connected by WIFI");
                    }
                } else {
                    Log.w(TAG, "WIFI state is not enabled");
                }
            } else {
                Log.w(TAG, "WIFI is not enabled");
            }
        } catch (Exception e) {
            Log.e(TAG, "IsWIFIAvailabe:" + e.toString());
        }

        return false;
    }

    public boolean IsPhoneAvaiable() {
        int result;

        result = telephonyManager.getDataActivity();
        Log.d(TAG, "Phone data activity = " + Integer.toString(result));
        //if(result != TelephonyManager.DATA_ACTIVITY_INOUT ) {
        //	Log.w(TAG, "Phone data activity is not IN and OUT");
        //	return false;
        //}

        result = telephonyManager.getDataState();
        Log.d(TAG, "Phone data state = " + Integer.toString(result));
        if (result != TelephonyManager.DATA_CONNECTED) {
            Log.w(TAG, "IP traffic might not be available");
            return false;
        }

        result = telephonyManager.getCallState();
        Log.d(TAG, "Phone call state = " + Integer.toString(result));
        if (result != TelephonyManager.CALL_STATE_IDLE) {
            Log.w(TAG, "Phone call state is not idle");
            return false;
        }

        if (telephonyManager.isNetworkRoaming()) {
            if (roaming_network == false) {
                Log.w(TAG, "Do not connect to Internet during network roaming");
                return false;
            }
        }

        return true;
    }

    public boolean TestConnection(String szURL) {
        try {
            URL url = new URL(szURL);

            if (IsPhoneAvaiable() == false) {
                if (IsWIFIAvailabe() == false) {
                    return false;
                }
            }

            InputStream in = url.openStream();
            in.close();

            return true;
        } catch (Exception e) {
            Log.e(TAG, "CreateConnection: " + e.toString());
            return false;
        }
    }
}
