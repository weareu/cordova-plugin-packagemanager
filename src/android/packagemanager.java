package fc.fcstudio;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;

import org.apache.cordova.PluginResult;

import java.util.ArrayList;
import java.util.List;

public class packagemanager extends CordovaPlugin {

    public Context context = null;
    private static final boolean IS_AT_LEAST_LOLLIPOP = Build.VERSION.SDK_INT >= 21;
    public boolean instApp = false;

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {

        context = IS_AT_LEAST_LOLLIPOP ? cordova.getActivity().getWindow().getContext() : cordova.getActivity().getApplicationContext();

        ArrayList<JSONObject> list = new ArrayList<JSONObject>();

        String filter = args.getString("filter");
        String[] filterArr = new String[0];
        if(filter != null && filter != "") {
            filterArr = filter.split("\\|"); 
        }

        if (action.equals("all")) {
            final PackageManager pm = cordova.getActivity().getPackageManager();
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> apps = pm.queryIntentActivities(intent, PackageManager.GET_META_DATA);
            for (ResolveInfo packageInfo : apps) {
                if(filterArr.length == 0 || containsCaseInsensitive(packageInfo.activityInfo.applicationInfo.packageName, filterArr)) {
                    JSONObject obj = new JSONObject();
                    obj.put("uid", packageInfo.activityInfo.applicationInfo.uid);
                    obj.put("dataDir", packageInfo.activityInfo.applicationInfo.dataDir);
                    obj.put("packageName", packageInfo.activityInfo.applicationInfo.packageName);
                    obj.put("icon", getBitmapOfAnAppAsBase64(packageInfo.activityInfo.applicationInfo.packageName));
                    list.add(obj);
                }
            }
        } else if(action.equals("none")) {
            List<ApplicationInfo> listInstalledApps = getInstalledApps(context);
            for (ApplicationInfo packageInfo : listInstalledApps) {
                if(filterArr.length == 0 || containsCaseInsensitive(packageInfo.packageName, filterArr)) {
                    JSONObject obj = new JSONObject();
                    obj.put("uid", packageInfo.uid);
                    obj.put("dataDir", packageInfo.dataDir);
                    obj.put("packageName", packageInfo.packageName);
                    obj.put("icon", getBitmapOfAnAppAsBase64(packageInfo.packageName));
                    list.add(obj);
                }
            }
        }

        if (action.equals("all") || action.equals("none")) {
            JSONArray jResult = new JSONArray(list);
            PluginResult pr = new PluginResult(PluginResult.Status.OK, jResult);
            callbackContext.sendPluginResult(pr);
            // callbackContext.success(jResult.toString());
            return true;
        } else {
            callbackContext.error("PackageManager " + action + " is not a supported function.");
            return false;
        }
    }

    public boolean containsCaseInsensitive(String s, List<String> l) {
        for (String string : l){
            if (string.equalsIgnoreCase(s)){
                return true;
            }
        }
        return false;
    }

    public boolean containsCaseInsensitive(String s, String[] l) {
        return containsCaseInsensitive(s, Arrays.asList(l));
    }

    private String getBitmapOfAnAppAsBase64(String packageName) {
        if(packageName.isEmpty() ) return new String("");

        String base64Encoded = "";
        Bitmap bitmap;

        try {
            Drawable appIcon = this.reactContext.getPackageManager().getApplicationIcon(packageName);
            if(appIcon instanceof BitmapDrawable) {
                bitmap= ((BitmapDrawable)appIcon).getBitmap();
            } else {
                bitmap = Bitmap.createBitmap(appIcon.getIntrinsicWidth(), appIcon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream .toByteArray();
            base64Encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

        } catch(Exception e) {
            Log.d(TAG,"An error was encounted while getting the package information. The error follows : " + e.toString());
        }

        return  base64Encoded;
    }

    public static List<ApplicationInfo> getInstalledApps(Context ctx) {
        final PackageManager pm = ctx.getPackageManager();
        //get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        return packages;
    }
}