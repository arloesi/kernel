package kernel.content;

import org.eclipse.persistence.queries.FetchGroup;

public class Common {
  public static void addAttribute(FetchGroup parent, String name, FetchGroup child) {
    parent.addAttribute(name,child);
  }
}