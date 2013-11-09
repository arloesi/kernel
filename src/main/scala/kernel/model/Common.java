package kernel.model;

import org.eclipse.persistence.queries.FetchGroup;

public class Common {
  public static void addFetchGroupAttribute(FetchGroup parent, String name, FetchGroup child) {
    parent.addAttribute(name,child);
  }
}
