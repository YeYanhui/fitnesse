package fitnesse.testrunner;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class PageListSetUpTearDownSurrounder {
  private WikiPage root;
  private List<WikiTestPage> pageList;

  public PageListSetUpTearDownSurrounder(WikiPage root) {
    this.root = root;
  }

  public void surroundGroupsOfTestPagesWithRespectiveSetUpAndTearDowns(List<WikiTestPage> pageList) {
    this.pageList = pageList;
    Map<String, LinkedList<WikiTestPage>> pageSetUpTearDownGroups = new HashMap<String, LinkedList<WikiTestPage>>();
    createPageSetUpTearDownGroups(pageSetUpTearDownGroups);
    pageList.clear();
    reinsertPagesViaSetUpTearDownGroups(pageSetUpTearDownGroups);
  }

  private void createPageSetUpTearDownGroups(Map<String, LinkedList<WikiTestPage>> pageSetUpTearDownGroups) {
    for (WikiTestPage page : pageList) {
      makeSetUpTearDownPageGroupForPage(page, pageSetUpTearDownGroups);
    }
  }

  private void makeSetUpTearDownPageGroupForPage(WikiTestPage page, Map<String, LinkedList<WikiTestPage>> pageSetUpTearDownGroups) {
    String group = getSetUpTearDownGroup(page.getSourcePage());
    LinkedList<WikiTestPage> pageGroup;
    if (pageSetUpTearDownGroups.get(group) != null) {
      pageGroup = pageSetUpTearDownGroups.get(group);
      pageGroup.add(page);
    } else {
      pageGroup = new LinkedList<WikiTestPage>();
      pageGroup.add(page);
      pageSetUpTearDownGroups.put(group, pageGroup);
    }
  }

  private String getSetUpTearDownGroup(WikiPage page) {
    String setUpPath = getPathForSetUpTearDown(page, PageData.SUITE_SETUP_NAME);
    String tearDownPath = getPathForSetUpTearDown(page, PageData.SUITE_TEARDOWN_NAME);
    return setUpPath + "," + tearDownPath;
  }

  private String getPathForSetUpTearDown(WikiPage page, String setUpTearDownName) {
    String path = null;
    WikiPage suiteSetUpTearDown = page.getPageCrawler().getClosestInheritedPage(page, setUpTearDownName);
    if (suiteSetUpTearDown != null)
      path = suiteSetUpTearDown.getPageCrawler().getFullPath().toString();
    return path;
  }

  private void reinsertPagesViaSetUpTearDownGroups(Map<String, LinkedList<WikiTestPage>> pageSetUpTearDownGroups) {
    for (Map.Entry<String, LinkedList<WikiTestPage>> entry : pageSetUpTearDownGroups.entrySet()) {
      insertSetUpTearDownPageGroup(entry.getKey(), entry.getValue());
    }
  }

  private void insertSetUpTearDownPageGroup(String setUpAndTearDownGroupKey, LinkedList<WikiTestPage> pageGroup) {
    insertSetUpForThisGroup(setUpAndTearDownGroupKey);
    insertPagesOfThisGroup(pageGroup);
    insertTearDownForThisGroup(setUpAndTearDownGroupKey);
  }

  private void insertSetUpForThisGroup(String setUpAndTearDown) {
    String setUpPath = setUpAndTearDown.split(",")[0];
    WikiPage setUpPage = root.getPageCrawler().getPage(PathParser.parse(setUpPath));
    if (setUpPage != null)
      pageList.add(new WikiTestPage(setUpPage));
  }

  private void insertPagesOfThisGroup(LinkedList<WikiTestPage> pageGroup) {
      pageList.addAll(pageGroup);
  }

  private void insertTearDownForThisGroup(String setUpAndTearDownGroupKey) {
    String tearDownPath = setUpAndTearDownGroupKey.split(",")[1];
    WikiPage tearDownPage = root.getPageCrawler().getPage(PathParser.parse(tearDownPath));
    if (tearDownPage != null)
      pageList.add(new WikiTestPage(tearDownPage));
  }
}