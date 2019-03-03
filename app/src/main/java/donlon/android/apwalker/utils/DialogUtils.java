package donlon.android.apwalker.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.EditText;

public class DialogUtils {
  public static AlertDialog.Builder getTipsDialogBuilder(Context context, String title, String content) {
    return new AlertDialog.Builder(context).setTitle(title).setMessage(content);
  }

  public static void showTips(Context context, String title, String content) {
    getTipsDialogBuilder(context, title, content).show();
  }

  public static void showTextRequireDlg(Context context, DialogTextCallback callback) {
    EditText editText = new EditText(context);

    AlertDialog.Builder editDialog = new AlertDialog.Builder(context);
    editDialog.setTitle("Title");
    //editDialog.setIcon(R.mipmap.ic_launcher_round);

    editDialog.setView(editText);

    editDialog.setPositiveButton("OK", (dialog, which) -> {
      callback.callback(editText.getText().toString().trim());
      dialog.dismiss();
    });
    editDialog.create().show();
  }

  public interface DialogTextCallback {
    void callback(String string);
  }
}
