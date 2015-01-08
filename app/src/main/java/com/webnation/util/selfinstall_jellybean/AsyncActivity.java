package com.webnation.util.selfinstall_jellybean;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.TextView;

import com.webnation.util.selfinstall_jellybean.GlobalVars;
import com.webnation.util.selfinstall_jellybean.ServiceKeepInApp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class AsyncActivity extends Activity {

	private TextView lblUpdating;

	private String apkName = null;
	private String downloadPath = "";
	private String urlPath;
	private String thepackageName;
	private String ftpServerName = "";
	private String ftpUpdatePath = "";
	private String user = "techteam";
	private String pw = "Team1Tech";
	private String serverVersion = "";
	private int resumeCount = 0;

	public static int taskID;

	Intent keepInApp;

	private boolean messageShowing = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		lblUpdating = (TextView)findViewById(R.id.lblUpdating);

		taskID = getTaskId();

		keepInApp = new Intent(this.getApplicationContext(), ServiceKeepInApp.class);

		Bundle bundle = getIntent().getExtras();
		

		if (bundle != null) {
			thepackageName = bundle.getString(GlobalVars.keyPackageName);
			GlobalVars.KeyPackageName = thepackageName;

			urlPath = bundle.getString(GlobalVars.keyFTPPath);
			GlobalVars.KeyFTPPath = urlPath;

			downloadPath = bundle.getString(GlobalVars.keyDownloadLocation);
			GlobalVars.deviceDownloadPath = downloadPath;

			user = bundle.getString(GlobalVars.keyFTPUser);
			GlobalVars.FTPUser = user;

			pw = bundle.getString(GlobalVars.keyFTPPassword);
			GlobalVars.FTPPassword = pw;

			apkName = bundle.getString(GlobalVars.keyFileName);
			GlobalVars.APKName = apkName;

			serverVersion = bundle.getString(GlobalVars.keyServerVersion);
			GlobalVars.ServerVersion = serverVersion;

			if (bundle.getString(GlobalVars.keyScreenText) != null) {
				lblUpdating.setText(Html.fromHtml(bundle.getString(GlobalVars.keyScreenText)));
			}

			if (bundle.getString(GlobalVars.keyFont) != null) {
				if (!bundle.getString(GlobalVars.keyFont).equalsIgnoreCase("")) {
					Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/" + bundle.getString(GlobalVars.keyFont));
					lblUpdating.setTypeface(typeFace);
				}
			}

			if (StringUtils.isBlank(urlPath) || StringUtils.isBlank(downloadPath) || StringUtils.isBlank(user) || StringUtils.isBlank(pw)
					|| StringUtils.isBlank(apkName) || StringUtils.isBlank(thepackageName)) {
				stopService(keepInApp);
				finish();
				android.os.Process.killProcess(android.os.Process.myPid());

			} else {
				startService(keepInApp);
			}
		} else { 
			Log.d("Bundle","is null");
		}

		try {
			int position = urlPath.lastIndexOf(".");
			ftpServerName = urlPath.substring(0, position + 4); // +4 so we get .com
			ftpUpdatePath = urlPath.substring(position + 4); // +4 so we don't get .copm

			boolean downloadAPK = true;

			try {
				File apk = new File(downloadPath, apkName);

				if (apk != null) {
					try {
						PackageManager pm = getPackageManager();
						PackageInfo pi = pm.getPackageArchiveInfo(downloadPath + apkName, 0);
						pi.applicationInfo.sourceDir = downloadPath + apkName;
						pi.applicationInfo.publicSourceDir = downloadPath + apkName;

						if (Double.valueOf(pi.versionName).equals(Double.valueOf(serverVersion))) {
							downloadAPK = false;
							InstallApplication(thepackageName, apkName, downloadPath);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				downloadAPK = false;
				ProgressTask task = (ProgressTask)new ProgressTask(this);
				task.execute(user, pw, ftpServerName, ftpUpdatePath, downloadPath, apkName, thepackageName);
				e.printStackTrace();
			}

			if (downloadAPK) {
				ProgressTask task = (ProgressTask)new ProgressTask(this);
				task.execute(user, pw, ftpServerName, ftpUpdatePath, downloadPath, apkName, thepackageName);
			}

		} catch (Exception e) {
			stopService(keepInApp);
			finish();
			android.os.Process.killProcess(android.os.Process.myPid());
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		if (resumeCount == 1) {
			stopService(keepInApp);
			finish();
			StartPrimaryApp(thepackageName);
		} else {
			// resumeCount++;
		}

		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		resumeCount = 1;
		super.onStop();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus && messageShowing) {
			stopService(keepInApp);
			finish();
			StartPrimaryApp(thepackageName);
		}
	}

	public void StartPrimaryApp(String thepackageName) {
		Intent intent2 = getPackageManager().getLaunchIntentForPackage(thepackageName);
		intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent2.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		startActivity(intent2);

		android.os.Process.killProcess(android.os.Process.myPid());
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// return true to indicate we have handled the event, when in fact
			// we are ignoring it
			event.startTracking(); // start tracking for the long press on staff
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		// do nothing
		return;
	}

	@Override
	public void onAttachedToWindow() {
		getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		KeyguardManager manager = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);

		KeyguardManager.KeyguardLock keylock = manager.newKeyguardLock(KEYGUARD_SERVICE);
		keylock.disableKeyguard();

		super.onAttachedToWindow();
	}

	public void setIsMessageShowing(boolean messageShowing) {
		this.messageShowing = messageShowing;
	}

	public void InstallApplication(String packageName, String apkName, String installPath) {
		setIsMessageShowing(true);

		Uri packageURI = Uri.parse(packageName);
		// Intent intent = new Intent(android.content.Intent.ACTION_VIEW, packageURI);
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, packageURI);

		/*
		 * Right here, we should be able to change the relative file-pathing to
		 * wherever we choose to download the apk to.
		 */

		intent.setDataAndType(Uri.fromFile(new File(installPath.toString() + apkName.toString())), "application/vnd.android.package-archive");
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	/**
	 * @author Randall.Mutter
	 * @param index [0] = user name
	 * @param index [1] = password
	 * @param index [2] = ftpServerName
	 * @param index [3] = ftpUpdatePath
	 * @param index [4] = downloadPath
	 * @param index [5[ = apk name
	 * @param index [6] = package name
	 */

	class ProgressTask extends AsyncTask<String, Void, Boolean> {
		List<Message> titles;
		private FTPClient mFTPClient = null;

		ProgressTask(Context asyncActivity) {
			context = asyncActivity;
		}

		/** progress dialog to show user that the backup is processing. */

		/** application context. */
		private Context context;

		protected Boolean doInBackground(final String... args) {
			Boolean status = null;

			try {
				status = ftpConnect(args[2], args[0], args[1], 21);

				if (status) {
					File destinationPath = new File(args[4]);

					if (!destinationPath.exists()) {
						destinationPath.mkdirs();
					}

					File fromFile = new File(args[3] + args[5]);

					File toFile = new File(args[4] + "/" + args[5]);

					if (toFile.exists()) {
						toFile.delete();
					}

					status = ftpDownload(fromFile.toString(), toFile.toString());

					mFTPClient.logout();
					mFTPClient.disconnect();

					InstallApplication(args[6], args[5], args[4]);
				}
				return status;

			} catch (Exception e) {
				e.printStackTrace();
				return status;
			}
		}

		/**
		 * @param host
		 * @param username
		 * @param password
		 * @param port
		 * @return
		 */
		public boolean ftpConnect(String host, String username, String password, int port) {
			Boolean status = null;

			try {
				mFTPClient = new FTPClient();

				// make sure there is not an old connection open
				mFTPClient.disconnect();

				// connecting to the host
				mFTPClient.connect(host, port);

				// now check the reply code, if positive mean connection success
				if (FTPReply.isPositiveCompletion(mFTPClient.getReplyCode())) {
					// login using username & password
					status = mFTPClient.login(username, password);

					/* Set File Transfer Mode
					*
					* To avoid corruption issue you must specified a correct
					* transfer mode, such as ASCII_FILE_TYPE, BINARY_FILE_TYPE,
					* EBCDIC_FILE_TYPE .etc. Here, I use BINARY_FILE_TYPE
					* for transferring text, image, and compressed files.
					*/
					mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
					mFTPClient.enterLocalPassiveMode();

					return status;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return status;
		}

		/**
		 * mFTPClient: FTP client connection object srcFilePath: path to the source file in FTP server desFilePath: path to the destination file to be saved in
		 * sdcard
		 */
		public boolean ftpDownload(String srcFilePath, String desFilePath) {
			boolean status = false;
			try {
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(desFilePath), 8);

				status = mFTPClient.retrieveFile(srcFilePath, out);
				out.close();

				return status;

			} catch (Exception e) {
				e.printStackTrace();
			}

			return status;
		}

		public void unInstallApp(String packageName) {
			Uri packageURI = Uri.parse(packageName.toString());
			Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
			context.startActivity(uninstallIntent);
		}
		
		public void unInstallApp2(String packageName) { 
			Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
			intent.setData(Uri.parse("package:" + packageName));
			  startActivity(intent);
		}
	}
}
