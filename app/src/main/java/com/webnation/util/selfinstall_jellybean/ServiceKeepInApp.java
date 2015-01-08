package com.webnation.util.selfinstall_jellybean;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class ServiceKeepInApp extends Service {

	private boolean sendHandler = false;

	Handler taskHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			ActivityManager activityManager = (ActivityManager)getSystemService(Service.ACTIVITY_SERVICE);

			if (activityManager.getRecentTasks(2, 0).get(0).id != AsyncActivity.taskID) {
				Intent intent = new Intent(Intent.ACTION_MAIN);
				Context mycon = getApplicationContext();
				PackageManager manager = mycon.getApplicationContext().getPackageManager();
				intent = manager.getLaunchIntentForPackage(mycon.getPackageName());

				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

				intent.putExtra("keyFTPPath", GlobalVars.FTPPath);
				intent.putExtra("keyDownloadLocation", GlobalVars.deviceDownloadPath);
				intent.putExtra("keyFTPUser", GlobalVars.FTPUser);
				intent.putExtra("keyFTPPassword", GlobalVars.FTPPassword);
				intent.putExtra("keyFileName", GlobalVars.APKName);
				intent.putExtra("keyPackageName", GlobalVars.KeyPackageName);
				intent.putExtra(GlobalVars.keyServerVersion, GlobalVars.ServerVersion);

				mycon.startActivity(intent);
			}

			if (sendHandler) {
				taskHandler.sendEmptyMessageDelayed(0, 1000);
			}
		}
	};

	@Override
	public void onCreate() {
		Log.v("Service", "created");
		super.onCreate();
		sendHandler = true;
		taskHandler.sendEmptyMessage(0);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("", "Service Stopped");
		sendHandler = false;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
