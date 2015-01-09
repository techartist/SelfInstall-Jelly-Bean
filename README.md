# SelfInstall-Jelly-Bean
Self Installer for apps
This app basically downloads a new version of an app, then it installs it on the user's device.  

In order to use this app, you will need to send an intent to the launcher with the ftp information.


				Intent intent = new Intent(Intent.ACTION_MAIN);
				PackageManager manager = mycon.getApplicationContext().getPackageManager();
				intent = manager.getLaunchIntentForPackage(selfInstallerPackageName);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);

				intent.putExtra(keyFTPPath, ftpPath);
				intent.putExtra(keyDownloadLocation, deviceDownloadPath);
				intent.putExtra(keyFTPUser,ftpUser);
				intent.putExtra(keyFTPPassword, ftpPassword);
				intent.putExtra(keyFileName, apkName);
				intent.putExtra(keyPackageName, MyContext.getPackageName().toString());
				intent.putExtra(keyScreenText, messageText);
				intent.putExtra(keyFont, subjectLanguageFont);
				intent.putExtra(keyServerVersion, serverVersion);
				mycon.startActivity(intent);
