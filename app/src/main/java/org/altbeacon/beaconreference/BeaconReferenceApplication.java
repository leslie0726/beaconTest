package org.altbeacon.beaconreference;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.altbeacon.beacon.startup.BootstrapNotifier;

/**
 * Created by dyoung on 12/13/13.
 */
public class BeaconReferenceApplication extends Application implements BootstrapNotifier {
    private static final String TAG = "BeaconReferenceApp";
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    private boolean haveDetectedBeaconsSinceBoot = false;
    private MonitoringActivity monitoringActivity = null;


    public void onCreate() {
        super.onCreate();
        BeaconManager beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
        // 預設抓altbeacon 要抓ibeacon就用下面兩行
        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser(). setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        // wake up the app when a beacon is seen
//        搜尋到beacon時喚醒 (未清楚)
//        設置beacon區域
        Region region = new Region("backgroundRegion", null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);

        // simply constructing this class and holding a reference to it in your custom Application
        // class will automatically cause the BeaconLibrary to save battery whenever the application
        // is not visible.  This reduces bluetooth power usage by about 60%
        //省電
        backgroundPowerSaver = new BackgroundPowerSaver(this);

        Log.d(TAG, "BeaconReferenceApplication_onCreate");
    }

//    BootstrapNotifier實作 進入區域時
    @Override
    public void didEnterRegion(Region arg0) {
        // In this example, this class sends a notification to the user whenever a Beacon
        // matching a Region (defined above) are first seen.
        Log.d(TAG, "BeaconReferenceApplication_didEnterRegion");
        if (!haveDetectedBeaconsSinceBoot) {
            Log.d(TAG, "自動帶入MonitoringActivity");
            // The very first time since boot that we detect an beacon, we launch the
            // MainActivity
            //進入monitoringActivity
            Intent intent = new Intent(this, MonitoringActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // Important:  make sure to add android:launchMode="singleInstance" in the manifest
            // to keep multiple copies of this activity from getting created if the user has
            // already manually launched the app.
            this.startActivity(intent);
            haveDetectedBeaconsSinceBoot = true;
        } else {
            if (monitoringActivity != null) {
                //顯示APP時第二次發現
                // If the Monitoring Activity is visible, we log info about the beacons we have
                // seen on its display
                monitoringActivity.logToDisplay("我又看到Beacon了!" );
            } else {
//              第二次發現Beacon時，沒有APP，推送通知
                // If we have already seen beacons before, but the monitoring activity is not in
                // the foreground, we send a notification to the user on subsequent detections.

                Log.d(TAG, "發送訊息");
                sendNotification();
            }
        }


    }

    @Override
    public void didExitRegion(Region region) {
        Log.d(TAG, "BeaconReferenceApplication_didExitRegion");
        if (monitoringActivity != null) {//APP使用中時
            monitoringActivity.logToDisplay("我離開Beacon範圍了");
        }
    }

    //偵測Beacon
    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        Log.d(TAG, "BeaconReferenceApplication_didDetermineStateForRegion");

        if (monitoringActivity != null) {//APP使用中時
            monitoringActivity.logToDisplay("Beacon搜尋狀態(0/無 1/有): " + state);
        }
    }

    private void sendNotification() {
        Log.d(TAG, "BeaconReferenceApplication_sendNotification");
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setContentTitle("我是推撥")
                        .setContentText("這是測試推撥訊息喔")
                        .setSmallIcon(R.drawable.ic_launcher);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, MonitoringActivity.class));
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

//    (顯示)onResume 設值
//    (跳出)onPause  設空
    public void setMonitoringActivity(MonitoringActivity activity) {
        Log.d(TAG, "BeaconReferenceApplication_setMonitoringActivity");
        this.monitoringActivity = activity;
    }

}