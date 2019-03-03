package donlon.android.apwalker;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;

import java.util.ArrayList;
import java.util.List;

import donlon.android.apwalker.objectmodel.ApCollectionInfo;
import donlon.android.apwalker.objectmodel.TagsInfo;
import donlon.android.apwalker.utils.Logger;

public class ApStorage {
  private Context mContext;
  private ApInfoDBHelper mDbHelper;
  private String mDbFileName;

  private static final int DB_VERSION = 3;
  private static final String AP_INFO_TABLE = "Ap_Info_v" + DB_VERSION;

  private static final String ACTIVE_AP_INFO_TABLE = "Active_Ap_Info_v" + DB_VERSION;
  private SQLiteDatabase db;

  private boolean mOpen;

  public ApStorage(Context context, String dbFileName){
    mDbFileName = dbFileName;
    mContext = context;

    mOpen = false;
  }

  public boolean open() {//TODO: Open DB automatically
    mDbHelper = new ApInfoDBHelper(mContext);
    db = mDbHelper.getWritableDatabase();

    return mOpen = db.isOpen();
  }

  public boolean beginStorage() {
    if (!db.isOpen()) {
      Logger.i("Error: DB closed.");
      return false;
    }
    db.beginTransaction();
    return true;
  }

  @SuppressLint("DefaultLocale")
  public void insertApInfo(String mTag, int mScanSequence, ScanResult result) {
    Logger.i("Insert:: BSSID: " + result.BSSID +
    ", SSID: " + result.SSID +
    ", capabilities: " + result.capabilities +
    ", centerFreq0: " + result.centerFreq0 +
    ", centerFreq1: " + result.centerFreq1 +
    ", channelWidth: " + result.channelWidth +
    ", frequency: " + result.frequency +
    ", level: " + result.level +
    ", operatorFriendlyName: " + result.operatorFriendlyName +
    ", timestamp: " + result.timestamp +
    ", venueName: " + result.venueName);

    ContentValues contentValues = new ContentValues();
    contentValues.put("Tag", mTag.replace('\'', '_'));
    contentValues.put("ScanSequence", mScanSequence);
    contentValues.put("BSSID", result.BSSID);
    contentValues.put("SSID", result.SSID);
    contentValues.put("Capabilities", result.capabilities);
    contentValues.put("CenterFreq0", result.centerFreq0);
    contentValues.put("CenterFreq1", result.centerFreq1);
    contentValues.put("ChannelWidth", result.channelWidth);
    contentValues.put("Frequency", result.frequency);
    contentValues.put("Level", result.level);
    contentValues.put("OperatorFriendlyName", result.operatorFriendlyName.toString());
    contentValues.put("Timestamp", result.timestamp);
    contentValues.put("VenueName", result.venueName.toString());

    try {
      db.insertOrThrow(AP_INFO_TABLE, null, contentValues);
    } catch (SQLException ex){
      ex.printStackTrace();
    }
  }

  public void insertActiveApInfo(String mTag, int mScanSequence, WifiInfo wifiInfo) {

  }

  public void endStorage() {
    db.setTransactionSuccessful();
    db.endTransaction();
  }

  public void close() {
    db.close();
  }

  public List<TagsInfo> getUsedTags() {
    List<TagsInfo> tagsInfoList = new ArrayList<>();
    getUsedTags(tagsInfoList);
    return tagsInfoList;
  }

  public void getUsedTags(List<TagsInfo> list) {
//    db.query(AP_INFO_TABLE, )
    list.clear();
    if (!mOpen) {
      return;
    }

    Cursor tagsCursor = db.rawQuery("select distinct Tag from " + AP_INFO_TABLE, null);

    while (tagsCursor.moveToNext()) {
      String tag = tagsCursor.getString(0);

      Cursor recordsCountCursor = db.rawQuery("select count(*) from " + AP_INFO_TABLE +" where Tag='" + tag + "'", null);
      recordsCountCursor.moveToFirst();

      Cursor apsCountCursor = db.rawQuery(
              "select count(distinct BSSID) from " + AP_INFO_TABLE + " where Tag='" + tag +"'", null);
      apsCountCursor.moveToFirst();

      list.add(new TagsInfo(tag,
              recordsCountCursor.getInt(0),
              apsCountCursor.getInt(0)));
      recordsCountCursor.close();
      apsCountCursor.close();
    }
    tagsCursor.close();
  }

  public List<ApCollectionInfo> getApCollectionInfo(String tagName) {
    List<ApCollectionInfo> list = new ArrayList<>();
    Cursor aps = db.rawQuery("select distinct SSID from " + AP_INFO_TABLE + " where Tag='" + tagName + "'", null);
    while (aps.moveToNext()) {
      String apSSID = aps.getString(0);

      Cursor recordsCountCursor = db.rawQuery(
              "select count(*) from " + AP_INFO_TABLE +" where Tag='" + tagName + "' and SSID='" + apSSID + "'", null);
      recordsCountCursor.moveToFirst();

      list.add(new ApCollectionInfo(apSSID, recordsCountCursor.getInt(0)));
      recordsCountCursor.close();
    }
    aps.close();
    return list;
  }

  public List<ApCollectionInfo> getApCollectionInfo() {
    List<ApCollectionInfo> list = new ArrayList<>();
    Cursor aps = db.rawQuery("select distinct SSID from " + AP_INFO_TABLE , null);
    while (aps.moveToNext()) {
      String apSSID = aps.getString(0);

      Cursor recordsCountCursor = db.rawQuery(
              "select count(*) from " + AP_INFO_TABLE +" where SSID='" + apSSID + "'", null);
      recordsCountCursor.moveToFirst();

      list.add(new ApCollectionInfo(apSSID, recordsCountCursor.getInt(0)));
      recordsCountCursor.close();
    }
    aps.close();
    return list;
  }

  public String getDefaultTag() {
    return "Default";//TODO
  }

  private class ApInfoDBHelper extends SQLiteOpenHelper {

    public ApInfoDBHelper(Context context) {
      super(context, mDbFileName, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
      onUpgrade(sqLiteDatabase, -1, DB_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
      String sql1 =
              "create table if not exists " + AP_INFO_TABLE +
                      "(Id integer primary key," +
                      "Tag text," +
                      "ScanSequence integer," +
                      "BSSID integer," +
                      "SSID text," +
                      "Capabilities text," +
                      "CenterFreq0 integer," +
                      "CenterFreq1 integer," +
                      "ChannelWidth integer," +
                      "Frequency integer," +
                      "Level integer," +
                      "OperatorFriendlyName text," +
                      "Timestamp text," +
                      "VenueName text);";
      sqLiteDatabase.execSQL(sql1);
    }
  }
  private class ActiveApInfoDBHelper extends SQLiteOpenHelper {

    public ActiveApInfoDBHelper(Context context) {
      super(context, mDbFileName, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
      // create table Orders(Id integer primary key, CustomName text, OrderPrice integer, Country text);
      String sql1 =
              "create table if not exists " + AP_INFO_TABLE +
              "(Id integer primary key," +
              "Tag text," +
              "ScanSequence integer," +
              "BSSID integer," +
              "SSID text," +
              "Capabilities text," +
              "CenterFreq0 integer," +
              "CenterFreq1 integer," +
              "ChannelWidth integer," +
              "Frequency integer," +
              "Level integer," +
              "OperatorFriendlyName text," +
              "Timestamp text," +
              "VenueName text);";
      sqLiteDatabase.execSQL(sql1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
//      String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
//      sqLiteDatabase.execSQL(sql);
//      onCreate(sqLiteDatabase);
    }
  }
}
