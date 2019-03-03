package donlon.android.apwalker;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;

import donlon.android.apwalker.utils.DbManager;
import donlon.android.apwalker.utils.ViewUtils;

public class DbListPopupView extends FrameLayout {

  private View mContentView;
  private EditText editNewDbName;
  private Button btnSubmit;
  private ListView lvDbList;
  private ArrayAdapter<String> mAdapter;

  public DbListPopupView(Context context) {
    this(context, null);
  }

  public DbListPopupView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public DbListPopupView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, 0);
  }

  public DbListPopupView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);

    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    if (inflater != null) {
      mContentView = inflater.inflate(R.layout.db_list_popup, this, true);

      editNewDbName = ViewUtils.findViewById(mContentView, R.id.editDbName);
      btnSubmit = ViewUtils.findViewById(mContentView, R.id.btnSubmit);
      lvDbList = ViewUtils.findViewById(mContentView, R.id.lvKnownDBs);

      mAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_expandable_list_item_1);

      btnSubmit.setOnClickListener(v -> {
        DbManager.addKnownDB(context, editNewDbName.getText().toString());
        updateListView();
      });

      lvDbList.setAdapter(mAdapter);
      lvDbList.setOnItemClickListener((parent, view, position, id) -> {
        if (mItemSelectedListener != null) {
          mItemSelectedListener.selected(position, mAdapter.getItem(position));
        }
      });

//      addView(mContentView);
    }
  }

  public void show() {
    updateListView();

    AlertDialog dialog = new AlertDialog.Builder(getContext()).setTitle("Select Database")
            .setView(this)
            .create();
    dialog.show();
  }

  private void updateListView() {
    mAdapter.clear();
    mAdapter.addAll(DbManager.getKnownStorageDBs(getContext()));
  }

  public interface ItemSelectedListener {
    void selected(int index, String name);
  }

  private ItemSelectedListener mItemSelectedListener;

  public void setItemSelectedListener(ItemSelectedListener listener) {
    this.mItemSelectedListener = listener;
  }
}
