package com.inventrax.athome_multiwh.services.appupdate;

import android.app.DownloadManager;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.inventrax.athome_multiwh.R;
import com.inventrax.athome_multiwh.application.AbstractApplication;
import com.inventrax.athome_multiwh.common.constants.ServiceURL;
import com.inventrax.athome_multiwh.util.NotificationUtils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateServiceUtils {

    private Context context;
    private UpdateRequest.Builder builder;
    private ServiceURL serviceURL;
    private static final String JSON_VERSION_CODE = "versionCode";
    private static final String JSON_UPDATE_URL = "updateURL";
    protected String url = null;
    protected String updateURL = null;

    public UpdateServiceUtils(){
        this.context= AbstractApplication.get();
        builder = new UpdateRequest.Builder(context);
        serviceURL = new ServiceURL();
    }

    public void checkUpdate(){
        try
        {

            builder.setVersionCheckStrategy(buildVersionCheckStrategy())
                    .setPreDownloadConfirmationStrategy(buildPreDownloadConfirmationStrategy())
                    .setDownloadStrategy(buildDownloadStrategy())
                    .setPreInstallConfirmationStrategy(buildPreInstallConfirmationStrategy())
                    .execute();

        }catch (Exception ex){
            Log.d("test123",ex.toString());
        }
    }

    private VersionCheckStrategy buildVersionCheckStrategy() {
        return (new SimpleHttpVersionCheckStrategy(serviceURL.getServiceUrl()+"update.json"));
    }

    private ConfirmationStrategy buildPreDownloadConfirmationStrategy() {
        MyTsk myTsk=new MyTsk(context);
        myTsk.execute();
        Notification n = NotificationUtils.getUpdateNotification(context, "Update Available", "Click to download the update!");
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        return (new NotificationConfirmationStrategy(n));
    }

    private DownloadStrategy buildDownloadStrategy() {
        if (Build.VERSION.SDK_INT >= 11) {
            return (new InternalHttpDownloadStrategy());
        }
        return (new SimpleHttpDownloadStrategy());
    }

    private ConfirmationStrategy buildPreInstallConfirmationStrategy() {
        Notification n = NotificationUtils.getUpdateNotification(context, "Update Ready to Install", "Click to install the update!");
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        return (new NotificationConfirmationStrategy(n));
    }

    public  class MyTsk extends AsyncTask {

        Context context;

        public MyTsk(Context context) {
            this.context=context;
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            HttpURLConnection conn=null;
            try {
                conn = (HttpURLConnection) new URL(serviceURL.getServiceUrl()+"update.json").openConnection();
                int result = -1;
                conn.connect();

                int status = conn.getResponseCode();

                if (status == 200) {
                    InputStream is = conn.getInputStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(is));
                    StringBuilder buf = new StringBuilder();
                    String str;

                    while ((str = in.readLine()) != null) {
                        buf.append(str);
                        buf.append('\n');
                    }

                    in.close();

                    JSONObject json = new JSONObject(buf.toString());

                    result = json.getInt(JSON_VERSION_CODE);
                    updateURL = json.getString(JSON_UPDATE_URL);
                    try {
                        PackageManager manager = context.getPackageManager();
                        PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
                        int version = info.versionCode;
                        if(version < result){
                            updateTheApplication(context,updateURL);
                        }

                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                } else {
                    // throw new RuntimeException(String.format("Received %d from server", status));
                }
            }catch (Exception e){
                // e.printStackTrace();
            } finally {
                conn.disconnect();
            }

            return objects;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
        }

        IntentFilter intentFilter;
        private void updateTheApplication(Context ctx, String apkPath) {

            long enqueue;
            DownloadManager dm = null;
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {

                        try {
                            final PackageManager pm = context.getPackageManager();
                            String apkName = "Nilkamal_MWMS.apk";
                            String fullPath = Environment.getExternalStorageDirectory().getPath() + "/Nilkamal/" + apkName;
                            PackageInfo info = pm.getPackageArchiveInfo(fullPath, 0);
                            int versionNumber = info.versionCode;
                            String versionName = info.versionName;

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                                Uri contentUri = FileProvider.getUriForFile(context, "com.inventrax.athome_multiwh.fileprovider" ,
                                        new File(Environment.getExternalStorageDirectory().getPath() + "/Nilkamal/Nilkamal_MWMS.apk"));
                                Intent openFileIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                                openFileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                openFileIntent.setData(contentUri);
                                context.startActivity(openFileIntent);

                            } else {
                                Intent intent1 = new Intent(Intent.ACTION_VIEW);
                                intent1.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory().getPath() + "/Nilkamal/Nilkamal_MWMS.apk")),
                                        "application/vnd.android.package-archive");
                                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
                                context.startActivity(intent1);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            if (intentFilter == null) {
                intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
                ctx.registerReceiver(receiver, intentFilter);
            }
            if (dm == null) {
                dm = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
            }

            String apkName = "Nilkamal_MWMS.apk";
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkPath));
            request.setTitle(ctx.getString(R.string.app_name));
            //request.setDescription(ctx.getString(R.string.dont_cancel));
            request.setDescription(ctx.getString(R.string.dont_cancel));
            request.setDestinationInExternalPublicDir("/Nilkamal", apkName);
            dm.enqueue(request);
        }
    }
}
