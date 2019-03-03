package donlon.android.apwalker.objectmodel;

public class TagsInfo {
  public String tagName;
  public int recordsCount;
  public int apsCount;

  public TagsInfo(String tagName, int recordsCount, int apsCount) {
    this.tagName = tagName;
    this.recordsCount = recordsCount;
    this.apsCount = apsCount;
  }
}
