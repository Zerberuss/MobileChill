package net.sytes.schneider.mobilechill;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by andi on 08/11/2017.
 */

public class MobileWifiManager {






    /**OLD VERSION -> loading from shared preferences */

    /** Key separator for Wifi and WifiCell preferences */
    public static final String KEY_SEPARATOR = "_";

    public static Set<String> getWifisByCell(Context context, int cellId, int lac) {

        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> keys = p.getAll().keySet();
        TreeSet<String> result = new TreeSet<String>();

        for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
            String key = iterator.next();
            //if (key.startsWith(PREFERENCE_WIFICELL) && key.endsWith(KEY_SEPARATOR + cellId + KEY_SEPARATOR + lac)) {
            //    result.add(getWifiOfWifiCellPreference(key));
            //}

            result.add(getWifiOfWifiCellPreference(key));
        }

        return result;
    }

    public static String getWifiOfWifiCellPreference(String wifiCellPreferenceKey) {

        String wifi = null;
        //if (wifiCellPreferenceKey != null && wifiCellPreferenceKey.startsWith(PREFERENCE_WIFICELL)) {
            String parts[] = wifiCellPreferenceKey.split(KEY_SEPARATOR);
            if (parts.length > 2) {
                String base64 = parts[2];
                try {
                    wifi = new String(Base64.decode(base64, Base64.NO_WRAP));
                }
                catch (IllegalArgumentException e) {
                    Log.e("getWifi fail", Log.getStackTraceString(e));
                }
            }
        return wifi;
    }
}
