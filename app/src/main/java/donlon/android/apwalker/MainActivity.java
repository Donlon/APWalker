package donlon.android.apwalker;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import com.google.android.material.snackbar.Snackbar;
import donlon.android.apwalker.adapters.TagsInfoAdapter;
import donlon.android.apwalker.objectmodel.ApCollectionInfo;
import donlon.android.apwalker.objectmodel.TagsInfo;
import donlon.android.apwalker.utils.DbManager;
import donlon.android.apwalker.utils.DialogUtils;
import donlon.android.apwalker.utils.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class MainActivity extends AppCompatActivity {
  private static final int WIFI_SCAN_PERMISSION_CODE = 23333;
  private WalkerManager mWalkerManager;
  private String mCurrentTag;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
      throwable.printStackTrace();
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      throwable.printStackTrace(pw);
      pw.flush();
      Logger.i(sw.toString());
    });
    Logger.i("AP Walker started.");
    setContentView(R.layout.activity_main);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    initUi();
    checkPermission();
  }

  private Button btn_scan;
  private Button btn_setTag;
  private EditText edit_tagName;
  private Switch sw_walkerOn;
  private ListView lv_tags;
  private ConstraintLayout mConstraintLayout;
  private DbListPopupView mDbListPopupView;

  private ArrayAdapter<?> mTagsListAdapter;
  private List<TagsInfo> mTagsList;

  private ApStorage mApStorage;

  private void initUi() {
    btn_scan = findViewById(R.id.btn_scan);
    btn_setTag = findViewById(R.id.btn_setTag);
    edit_tagName = findViewById(R.id.tx_tagName);
    sw_walkerOn = findViewById(R.id.sw_walkerOn);
    lv_tags = findViewById(R.id.lv_tags);
    mConstraintLayout = findViewById(R.id.rootLayout_main);

    btn_setTag.setOnClickListener(e -> {
      mCurrentTag = edit_tagName.getText().toString();
      mCurrentTag = mCurrentTag.replace('\'', '_');
      edit_tagName.setText(mCurrentTag);
      edit_tagName.setEnabled(false);
      mWalkerManager.setTag(mCurrentTag);
    });
    btn_scan.setOnClickListener(e -> mWalkerManager.requestScanning());

    edit_tagName.setOnClickListener((v) -> {
      if (!v.isEnabled()) {
        edit_tagName.setEnabled(true);
      }
    });

    sw_walkerOn.setOnCheckedChangeListener((v, isChecked) -> {
      if (isChecked) {
        mWalkerManager.startContinuousScanning(1000);
      } else {
        mWalkerManager.stopScanningInterval();
      }
    });

    lv_tags.setOnItemClickListener((parent, view, position, id) -> {
      List<ApCollectionInfo> collectionInfo = mWalkerManager.getApCollectionInfo(mTagsList.get(position).tagName);
    });

    mDbListPopupView = new DbListPopupView(this);
    mDbListPopupView.setItemSelectedListener((index, name) -> {

    });
  }

  private void checkPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && ContextCompat.checkSelfPermission(this, Manifest.permission_group.LOCATION)
            != PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE)
            != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(new String[]{
              Manifest.permission.ACCESS_FINE_LOCATION,
              Manifest.permission.ACCESS_COARSE_LOCATION,
              Manifest.permission.ACCESS_WIFI_STATE,
      }, WIFI_SCAN_PERMISSION_CODE);
    } else {
      permissionsGranted();
    }
  }

  private void permissionsGranted() {
    init();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    switch (requestCode) {
      case WIFI_SCAN_PERMISSION_CODE:
        if (grantResults.length == 3
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
                && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
          permissionsGranted();
        } else {
          Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show();
        }
        break;
    }
  }

  private void init() {
    mApStorage = DbManager.getDefaultStorage(this);
    mApStorage.open();

    mTagsList = mApStorage.getUsedTags();
    mTagsListAdapter = new TagsInfoAdapter(this, mTagsList);
    lv_tags.setAdapter(mTagsListAdapter);
    //    mTagsListAdapter.notifyDataSetChanged();

    mWalkerManager = new WalkerManager(this);
    mWalkerManager.setSnackPresenter(this::showToast);
    mWalkerManager.setStorage(mApStorage);
    mWalkerManager.setTag(mApStorage.getDefaultTag());
    if (!mWalkerManager.init()) {
      DialogUtils.showTips(this, "Error.", "WalkerManager init failed.");
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.mwnu_createTag:
        break;
      case R.id.menu_closeDb://for debugging
        mWalkerManager.stop();
        break;
      case R.id.menu_selectDb:
        if (mWalkerManager.isRunning()) {
          Toast.makeText(this, "AP Walker should be paused before alternating storage", Toast.LENGTH_SHORT).show();
        } else {
          mDbListPopupView.show();
        }
        break;
      default:
        return super.onOptionsItemSelected(item);
    }
    return true;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main_menu, menu);
    return true;
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mWalkerManager.stop();
  }

  private void showToast(String str, int lengthShort) {
    Snackbar snackbar = Snackbar.make(mConstraintLayout, str, lengthShort);
    snackbar.show();
  }
}
