package app.solution.barcode.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by toukir on 9/5/18.
 */

public class PrefRewardedAd {

    private static final String KEY = "rewarded_ad_count";
    private static Context mContext;
    private static int v = 0;

    public PrefRewardedAd(Context context){
        this.mContext = context;
    }

    public static void setCount(int value) {
        // when v=2 then show rewarded ad
        v =+ value;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY, v);
        editor.commit();
    }

    public static int getCount() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return preferences.getInt(KEY, 0);
    }

}
