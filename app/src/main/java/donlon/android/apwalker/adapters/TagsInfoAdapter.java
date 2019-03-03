package donlon.android.apwalker.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import donlon.android.apwalker.R;
import donlon.android.apwalker.objectmodel.TagsInfo;

public class TagsInfoAdapter extends ArrayAdapter<TagsInfo> {
  public TagsInfoAdapter(@NonNull Context context, @NonNull List<TagsInfo> objects) {
    super(context, android.R.layout.simple_list_item_1, objects);
  }

  @SuppressLint("DefaultLocale")
  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    View v;
    ViewsHolder holder;
    if (convertView != null) {
      return convertView;
    } else {
      v = LayoutInflater.from(getContext()).inflate(R.layout.tags_list_entry, parent, false);
      holder = new ViewsHolder((TextView) v.findViewById(R.id.tv_tagName), (TextView) v.findViewById(R.id.tv_recodesCount));
      v.setTag(holder);
      TagsInfo item = getItem(position);
      assert item != null;
      holder.tvTagName.setText(item.tagName);
      holder.tvRecordsCount.setText(
              String.format("APs count: %d, Records count: %d",
                      item.apsCount, item.recordsCount));
      return v;
    }
  }

  private class ViewsHolder {
    private TextView tvTagName;
    private TextView tvRecordsCount;
    ViewsHolder(TextView tvTagName, TextView tvRecordsCount) {
      this.tvTagName = tvTagName;
      this.tvRecordsCount = tvRecordsCount;
    }
  }
}
