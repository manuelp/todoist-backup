package me.manuelp.todoist_backup.data.export.formats;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.callables.JoinString;
import me.manuelp.todoist_backup.data.Item;
import me.manuelp.todoist_backup.data.Project;

import java.util.Date;

import static com.googlecode.totallylazy.Sequences.repeat;

public class MarkdownFormatter {
  public static String formatProject(Project p, Sequence<Item> items) {
    /*
     * +1 since we want to start by header level 2.
     */
    String header = repeat("#").take(p.getIndent() + 1).reduce(Strings.join);
    return String.format("\n\n%s %s\n\n%s", header, p.getName(), formatItems(items));
  }

  private static String formatItems(Sequence<Item> items) {
    return items.map(formatItem()).intersperse("\n").reduce(JoinString.instance);
  }

  private static Callable1<Item, String> formatItem() {
    return i -> {
      Object dueDate = i.getDueDate().isDefined() ? new Date(i.getDueDate().get()) : "";
      String checked = i.isChecked() ? "x" : " ";
      return String
          .format("%s* [%s] (P:%d) %s <%s>", spacesIndentation(i.getIndent()), checked, i.getPriority(), i.getContent(),
                  dueDate);
    };
  }

  private static String spacesIndentation(Integer n) {
    return repeat(" ").take((n - 1) * 4).reduce(JoinString.instance);
  }
}
