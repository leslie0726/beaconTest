package org.altbeacon.beaconreference;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;

/**
 * 
 * @author dyoung
 * @author Matt Tyler
 */
public class MonitoringActivity extends Activity  {
	protected static final String TAG = "MonitoringActivity";
	private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "MonitoringActivity_onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitoring);

		//檢查藍芽是否啟動
		verifyBluetooth();

        logToDisplay("應用已啟動");

        //取得訂位權限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("APP需要存取定位權限");
                builder.setMessage("請點選允許已應後台使用");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @TargetApi(23)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                PERMISSION_REQUEST_COARSE_LOCATION);
                    }

                });
                builder.show();
            }
        }
	}

	//點擊權限後
	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		Log.d(TAG, "MonitoringActivity_onRequestPermissionsResult");
		switch (requestCode) {
			case PERMISSION_REQUEST_COARSE_LOCATION: {

				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					//允許權限
					Log.d(TAG, "位置權限已許可");
				} else {
					//拒絕權限
					final AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("功能受限");
					builder.setMessage("因您選擇了拒絕存取，後台無法存取");
					builder.setPositiveButton(android.R.string.ok, null);
					builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
						}
					});
					builder.show();
				}
				return;
			}
		}
	}

	//點選到測距畫面
	public void onRangingClicked(View view) {
		Log.d(TAG, "MonitoringActivity_onRangingClicked");
		Intent myIntent = new Intent(this, RangingActivity.class);
		this.startActivity(myIntent);
	}


    @Override
    public void onResume() {
		//當Activity出現手機上後，呼叫onResume方法
		Log.d(TAG, "MonitoringActivity_onResume");
        super.onResume();
        ((BeaconReferenceApplication) this.getApplicationContext()).setMonitoringActivity(this);
    }

    @Override
    public void onPause() {
		//當使用者按下返回鍵結束Activity時， 先呼叫onPause方法
		Log.d(TAG, "MonitoringActivity_onPause");
        super.onPause();
        ((BeaconReferenceApplication) this.getApplicationContext()).setMonitoringActivity(null);
    }

	//藍芽檢查
	private void verifyBluetooth() {
		Log.d(TAG, "MonitoringActivity_verifyBluetooth");
		try {
			if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("藍芽未啟用");
				builder.setMessage("請打開藍芽!");
				builder.setPositiveButton(android.R.string.ok, null);
				builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						finish();
			            System.exit(0);
					}
				});
				builder.show();
			}
		}
		catch (RuntimeException e) {
			//出現例外
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("藍芽無法使用");
			builder.setMessage("手機藍芽版本過低，無法使用");
			builder.setPositiveButton(android.R.string.ok, null);
			builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					finish();
		            System.exit(0);
				}

			});
			builder.show();

		}

	}

    public void logToDisplay(final String line) {
		//把所有值都放到editText
    	runOnUiThread(new Runnable() {
    	    public void run() {
    	    	EditText editText = (EditText)MonitoringActivity.this
    					.findViewById(R.id.monitoringText);
       	    	editText.setText(line);
    	    }
    	});
    }

}
