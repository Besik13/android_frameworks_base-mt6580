package android.content.res;

import android.os.*;
import android.content.Context;
import android.content.pm.*;
import android.util.Log;
import android.util.DisplayMetrics;
import android.R;
import android.app.ActivityManager;
import android.view.WindowManager;
import android.text.TextUtils;

/**
 * 
 * @author pankaj
 * @hide
 */
public final class CustomTheme implements Cloneable {

    private String mThemeId;
    private String mThemePackageName;
    private String mThemeResourcePath;  // Non null for delta themes ONLY! Points to the resource bundle of a delta theme.
    private boolean mHasParentTheme = false;
    private boolean mForceUpdate = false;

    private static final CustomTheme sDefaultTheme = new CustomTheme();

    private CustomTheme() {
        mThemeId = SystemProperties.get("default_theme.style_id");
        mThemePackageName = SystemProperties.get("default_theme.package_name");
    }

    public CustomTheme(String themeId, String packageName, boolean hasParent) {
        mThemeId = themeId;
        mThemePackageName = packageName;
        mHasParentTheme = hasParent;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof CustomTheme) {
            CustomTheme o = (CustomTheme) object;
            if (!mThemeId.equals(o.mThemeId)) {
                return false;
            }
            String currentPackageName = (mThemePackageName == null)? "" : mThemePackageName;
            String newPackageName = (o.mThemePackageName == null)? "" : o.mThemePackageName;
            String currentThemeId = (mThemeId == null)? "" : mThemeId;
            String newThemeId = (o.mThemeId == null)? "" : o.mThemeId;
            return (currentPackageName.trim().equalsIgnoreCase(newPackageName.trim())) &&
                    (currentThemeId.trim().equalsIgnoreCase(newThemeId.trim())) &&
                    hasParentTheme() == o.hasParentTheme() &&
                    !isForceUpdate();
        }
        return false;
    }

    @Override
    public final String toString() {
        StringBuilder result = new StringBuilder();
        result.append(mThemeId);
        result.append("_");
        if (mThemePackageName != null && mThemePackageName.length() > 0){
            result.append(mThemePackageName);
        } else {
            result.append("_");
        }
        result.append(":_");
        if (!TextUtils.isEmpty(mThemeResourcePath)) {
            result.append(mThemeResourcePath);
        } else {
            result.append("_");
        }
        result.append("_");
        result.append(mHasParentTheme);
        result.append("_");
        result.append(mForceUpdate);

        return result.toString();
    }

    @Override
    public synchronized int hashCode() {
        return mThemeId.hashCode() + mThemePackageName.hashCode();
    }

    public String getThemeId() {
        return mThemeId;
    }

    public String getThemePackageName() {
        return mThemePackageName;
    }

    public void setThemePackageName(String themePackageName) {
        mThemePackageName = themePackageName;
    }

    public String getThemeResourcePath() {
        return mThemeResourcePath;
    }

    public void setThemeResourcePath(String resourcePath) {
        mThemeResourcePath = resourcePath;
    }

    public boolean isForceUpdate() {
        return mForceUpdate;
    }

    public void setForceUpdate(boolean update) {
        mForceUpdate = update;
    }

    public boolean hasParentTheme() {
        return mHasParentTheme;
    }

    public static CustomTheme getDefault() {
        return sDefaultTheme;
    }

    public static int getStyleId(Context context, String packageName, String styleName) {
        if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(styleName)) {
            return R.style.Theme;
        }
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(packageName, 0);
            ThemeInfo[] infos = pi.themeInfos;
            if (infos != null) {
                for (ThemeInfo ti : infos) {
                    if (ti.themeId.equals(styleName)) {
                        return ti.styleResourceId;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("CustomTheme", "Unable to get style resource id", e);
        }
        return -1;
    }

    public static String getDeltaThemeStyleName(String themeId) {
        StringBuilder sb = new StringBuilder(themeId);
        sb.append("_generated");
        return sb.toString();
    }

    public static String getDeltaThemePackageName(String styleName) {
        StringBuilder sb = new StringBuilder("com.tmobile.theme.autogenerated.");
        sb.append(styleName);
        return sb.toString().replace(' ', '_');
    }

    public static int getDeltaThemeStyleId(Context context, String styleName, String packageName, String resourceBundlePath) {
        if (TextUtils.isEmpty(styleName) ||
            TextUtils.isEmpty(packageName) ||
            TextUtils.isEmpty(resourceBundlePath)) {
            return -1;
        }
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        Configuration config = am.getConfiguration();
        AssetManager assets = new AssetManager();
        assets.addAssetPath(resourceBundlePath);
        Resources res = new Resources(assets, metrics, config);
        return res.getIdentifier("style/" + styleName, "", packageName);
    }

}
