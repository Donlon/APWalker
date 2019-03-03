package donlon.android.apwalker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import donlon.android.apwalker.ApStorage;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class DbManager {
  public static final String SHARED_PREFERENCE_STORAGE_DB_NAME = "databases";
  public static final String STORAGE_DB_KEY = "databases";
  public static final String STORAGE_DEFAULT_DB_KEY = "default_database";

  public static List<String> getKnownStorageDBs(Context context) {
    SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCE_STORAGE_DB_NAME, Context.MODE_PRIVATE);

    try {
      List<String> knownDbList = new ArrayList<>();

      JSONArray jsonDecoder = new JSONArray(sp.getString(STORAGE_DB_KEY, "[]"));

      for (int i = 0; i < jsonDecoder.length(); i++) {
        knownDbList.add(jsonDecoder.getString(i));
      }

      return knownDbList;
    } catch (JSONException e) {
      e.printStackTrace();
      return new ArrayList<>();
    }
  }

  public static ApStorage createStorageDB(Context context, String name) {
    addKnownDB(getSharedPreferences(context), name);
    return new ApStorage(context, name);
  }

  public static ApStorage getStorage(Context context, String name) {
    return new ApStorage(context, name);
  }

  public static String getDefaultDb(Context context) {
    SharedPreferences sp = getSharedPreferences(context);

    if (!sp.contains(STORAGE_DEFAULT_DB_KEY)) {
      addKnownDB(sp, "ApInfo");
    }
    return sp.getString(STORAGE_DEFAULT_DB_KEY, "ApInfo");
  }

  public static ApStorage getDefaultStorage(Context context) {
    return getStorage(context, getDefaultDb(context));
  }

  public static void addKnownDB(Context context, String name) {
    addKnownDB(getSharedPreferences(context), name);
  }

  private static void addKnownDB(SharedPreferences sp, String name) {
    SharedPreferences.Editor editor = sp.edit();
    try {
      JSONArray jsonDecoder = new JSONArray(sp.getString(STORAGE_DB_KEY, "[]"));
      jsonDecoder.put(0, name);

      editor.putString(STORAGE_DB_KEY, jsonDecoder.toString());
      editor.apply();

    } catch (JSONException e) {
      e.printStackTrace();//TODO:...
      return;
    }
  }

  private static SharedPreferences getSharedPreferences(Context context) {
    return context.getSharedPreferences(SHARED_PREFERENCE_STORAGE_DB_NAME, Context.MODE_PRIVATE);
  }
}
