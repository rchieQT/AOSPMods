package sh.siava.AOSPMods.utils;

import static com.topjohnwu.superuser.Shell.cmd;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DownloadManager;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.VibratorManager;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.Contract;

import sh.siava.AOSPMods.BuildConfig;

public class SystemUtils {

	@SuppressLint("StaticFieldLeak")
	static SystemUtils instance;

	Context mContext;
	VibratorManager mVibrationManager;
	AudioManager mAudioManager;
	PowerManager mPowerManager;
	ConnectivityManager mConnectivityManager;
	TelephonyManager mTelephonyManager;
	AlarmManager mAlarmManager;
	NetworkStats mNetworkStats;
	DownloadManager mDownloadManager = null;
	boolean hasVibrator;

	public static void RestartSystemUI() {
		cmd("killall com.android.systemui").submit();
	}

	public static void Restart() {
		cmd("am start -a android.intent.action.REBOOT").submit();
	}

	public static boolean isFlashOn() {
		if (instance == null) return false;
		return TorchCallback.torchOn;
	}

	public static void ToggleFlash() {
		if (instance == null) return;
		instance.toggleFlashInternal();
	}

	public static NetworkStats NetworkStats() {
		if (instance == null) return null;
		instance.initiateNetworkStats();
		return instance.mNetworkStats;
	}

	private void initiateNetworkStats() {
		if (mNetworkStats == null) {
			mNetworkStats = new NetworkStats(mContext);
		}
	}

	public static void setFlash(boolean enabled, float pct) {
		if (instance == null) return;
		instance.setFlashInternal(enabled, pct);
	}

	public static void setFlash(boolean enabled) {
		if (instance == null) return;
		instance.setFlashInternal(enabled);
	}

	@Nullable
	@Contract(pure = true)
	public static AudioManager AudioManager() {
		if (instance == null) return null;
		return instance.getAudioManager();
	}

	private AudioManager getAudioManager() { //we don't init audio manager unless it's requested by someone
		if (mAudioManager == null) {
			//Audio
			try {
				mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
			} catch (Throwable t) {
				if (BuildConfig.DEBUG) {
					log("AOSPMods Error getting audio manager");
					t.printStackTrace();
				}
			}
		}
		return mAudioManager;
	}

	@Nullable
	@Contract(pure = true)
	public static ConnectivityManager ConnectivityManager() {
		if (instance == null) return null;
		return instance.mConnectivityManager;
	}

	@Nullable
	@Contract(pure = true)
	public static PowerManager PowerManager() {
		if (instance == null) return null;
		return instance.mPowerManager;
	}

	@Nullable
	@Contract(pure = true)
	public static AlarmManager AlarmManager() {
		if (instance == null) return null;
		return instance.mAlarmManager;
	}


	@Nullable
	@Contract(pure = true)
	public static TelephonyManager TelephonyManager() {
		if (instance == null) return null;
		return instance.mTelephonyManager;
	}

	public static DownloadManager DownloadManager() {
		if (instance == null) return null;
		return instance.getDownloadManager();
	}

	public static void vibrate(int effect) {
		vibrate(VibrationEffect.createPredefined(effect));
	}

	@SuppressLint("MissingPermission")
	public static void vibrate(VibrationEffect effect) {
		if (instance == null || !instance.hasVibrator) return;
		try {
			instance.mVibrationManager.getDefaultVibrator().vibrate(effect);
		} catch (Exception ignored) {
		}
	}

	public static void Sleep() {
		if (instance == null) return;

		try {
			callMethod(instance.mPowerManager, "goToSleep", SystemClock.uptimeMillis());
		} catch (Throwable ignored) {
		}
	}

	public SystemUtils(Context context) {
		mContext = context;

		instance = this;


		//Connectivity
		try {
			mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		} catch (Throwable t) {
			if (BuildConfig.DEBUG) {
				log("AOSPMods Error getting connection manager");
				t.printStackTrace();
			}
		}

		//Power
		try {
			mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		} catch (Throwable t) {
			if (BuildConfig.DEBUG) {
				log("AOSPMods Error getting power manager");
				t.printStackTrace();
			}
		}

		//Telephony
		try {
			mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
		} catch (Throwable t) {
			if (BuildConfig.DEBUG) {
				log("AOSPMods Error getting telephony manager");
				t.printStackTrace();
			}
		}

		//Alarm
		try {
			mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		} catch (Throwable t) {
			if (BuildConfig.DEBUG) {
				log("AOSPMods Error getting alarm manager");
				t.printStackTrace();
			}
		}

		//Vibrator
		try {
			mVibrationManager = (VibratorManager) mContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
			hasVibrator = mVibrationManager.getDefaultVibrator().hasVibrator();
		} catch (Throwable t) {
			if (BuildConfig.DEBUG) {
				log("AOSPMods Error getting vibrator");
				t.printStackTrace();
			}
		}
	}

	private void setFlashInternal(boolean enabled) {
		return;
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public static boolean supportsFlashLevels() {
		if (instance == null) return false;
		return instance.supportsFlashLevelsInternal();
	}

	private boolean supportsFlashLevelsInternal() {
		return false;
	}

	private void setFlashInternal(boolean enabled, float pct) {
		return;
	}

	private void toggleFlashInternal() {
		setFlashInternal(!TorchCallback.torchOn);
	}

	private String getFlashID(@NonNull CameraManager cameraManager) throws CameraAccessException {
		String[] ids = cameraManager.getCameraIdList();
		for (String id : ids) {
			if (cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) == CameraMetadata.LENS_FACING_BACK) {
				if (cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
					return id;
				}
			}
		}
		return "";
	}

	private DownloadManager getDownloadManager() {
		if (mDownloadManager == null) {
			mDownloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
		}
		return mDownloadManager;
	}

	static class TorchCallback extends CameraManager.TorchCallback {
		static boolean torchOn = false;

		@Override
		public void onTorchModeChanged(@NonNull String cameraId, boolean enabled) {
			super.onTorchModeChanged(cameraId, enabled);
			torchOn = enabled;
		}
	}

	public static boolean isDarkMode() {
		if (instance == null) return false;
		return instance.getIsDark();
	}

	private boolean getIsDark() {
		return (mContext.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_YES) == Configuration.UI_MODE_NIGHT_YES;
	}

	static boolean darkSwitching = false;

	public static void doubleToggleDarkMode() {
		boolean isDark = isDarkMode();
		new Thread(() -> {
			try {
				while (darkSwitching) {
					Thread.currentThread().wait(100);
				}
				darkSwitching = true;

				cmd("cmd uimode night " + (isDark ? "no" : "yes")).exec();
				Thread.sleep(1000);
				cmd("cmd uimode night " + (isDark ? "yes" : "no")).exec();

				Thread.sleep(500);
				darkSwitching = false;
			} catch (Exception ignored) {
			}
		}).start();
	}
}