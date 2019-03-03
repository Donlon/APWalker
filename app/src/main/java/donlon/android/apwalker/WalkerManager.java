package donlon.android.apwalker;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import java.util.List;

import donlon.android.apwalker.objectmodel.ApCollectionInfo;

public class WalkerManager {
  private Context mContext;
  private WifiManager mWifiManager;
  private ApStorage mStorage;
  private int mScanSequence;

  private boolean mInitialized;
//  private boolean mRunning;

  private String mTag;

  private Handler mIntervalHandler;
  private Runnable mIntervalRunnable = () -> mWifiManager.startScan();

//  public WalkerManager(Context context) {
//    this(context, "Default");
//  }

  public WalkerManager(Context context) {
    mContext = context;
    mScanSequence = 0;

    mInitialized = false;
//    mRunning = false;

//    mTag = tag;
//    mStorage = DbManager.getDefaultStorage(context);

    mWifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

    mIntervalHandler = new Handler();
  }

  private BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (!mInitialized) {
        return;
      }
      boolean succeed = intent.getBooleanExtra(
              WifiManager.EXTRA_RESULTS_UPDATED, false);
      if (succeed) {
        scanSucceed();
      } else {
        scanFailed();
      }

      if (mRunning) {
        mIntervalHandler.postDelayed(mIntervalRunnable, mScanningDelay);
      }
    }
  };

  public boolean init() {//startImmediately: TODO
    if (mInitialized) {
      return false;
    }

    if (mWifiManager == null ||mStorage == null) {
      return false;
    }

    if (mTag == null || mTag.isEmpty()) {
      mTag = "Default";
    }

    if (!mWifiManager.isWifiEnabled()) {
      Toast.makeText(mContext, "Wi-Fi is disabled, and making it enabled.", Toast.LENGTH_LONG).show();
      mWifiManager.setWifiEnabled(true);
    }

    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
    mContext.registerReceiver(mWifiScanReceiver, intentFilter);

    mInitialized = true;
//    mRunning = startImmediately;

    return true;
  }

  @SuppressLint("DefaultLocale")
  private void scanSucceed() {
    List<ScanResult> results = mWifiManager.getScanResults();
    mSnackPresenter.show(String.format("Scanned %d AP(s), seq=%d", results.size(), mScanSequence) ,
            Toast.LENGTH_SHORT);
    saveRecordingSet(results);
  }

  private void scanFailed() {
    if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
      Toast.makeText(mContext,"当前区域没有无线网络", Toast.LENGTH_SHORT).show();
    } else if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING){
      Toast.makeText(mContext,"WiFi正在开启，请稍后重新点击扫描", Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText(mContext,"WiFi没有开启，无法扫描", Toast.LENGTH_SHORT).show();
    }
  }

  private boolean mRunning = false;

  private int mScanningDelay;

  public void startContinuousScanning(int delay) {
    mRunning = true;
    mScanningDelay = delay;
    mIntervalHandler.post(mIntervalRunnable);
  }

  public void stopScanningInterval() {
    if (!mRunning) {
      return;
    }
    mRunning = false;
    mIntervalHandler.removeCallbacks(mIntervalRunnable);
  }

  public void requestScanning() {
    if (!mInitialized) {
      mSnackPresenter.show("WalkerManager hasn't been started yet.", Snackbar.LENGTH_SHORT);
    }
    mWifiManager.startScan();
  }

  private void saveRecordingSet(List<ScanResult> results) {
    if (!mStorage.beginStorage()) {
      new AlertDialog.Builder(mContext).setTitle("Error").setMessage("Can't begin storage.").show();
      return;
    }

    for(ScanResult result : results) {
      mStorage.insertApInfo(mTag, mScanSequence, result);
    }
    mStorage.insertActiveApInfo(mTag, mScanSequence, mWifiManager.getConnectionInfo());
    mStorage.endStorage();
    mScanSequence++;
  }

  public String getCurrentTag() {
    return mTag;
  }

  public void setTag(String tag) {
    mTag = tag;
  }

  public ApStorage getStorage() {
    return mStorage;
  }

  public void setStorage(ApStorage storage) {
    if (mRunning) {
      mSnackPresenter.show("Can't set ApStorage when running.", Toast.LENGTH_SHORT);
      return;
    }
    mStorage = storage;
  }

  public void stop() {
    mStorage.close();
//    mInitialized = false;
    mRunning = false;
  }

  public boolean isRunning() {
    return mRunning;
  }

  public boolean initialized() {
    return mInitialized;
  }

  public List<ApCollectionInfo> getApCollectionInfo(String tagName) {
    return mStorage.getApCollectionInfo(tagName);
  }

  private SnackPresenter mSnackPresenter = (str, lengthShort) -> {};

  public void setSnackPresenter(SnackPresenter presenter) {
    mSnackPresenter = presenter;
  }

  public interface SnackPresenter {
    void show(String str, int lengthShort);
  }
}
