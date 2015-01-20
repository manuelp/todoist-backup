package me.manuelp.todoist_backup.data;

import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Objects;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.json.Json;

import java.util.Comparator;
import java.util.HashMap;

import static com.googlecode.totallylazy.Sequences.sequence;

public class Item {
  private final Long id;
  private final Long projectId;
  private final String content;
  private final Option<Long> dueDate;
  private final Integer priority;
  private final Long itemOrder;
  private final Integer indent;
  private final boolean checked;

  public Item(Long id, Long projectId, String content, Option<Long> dueDate, Integer priority, Long itemOrder,
              Integer indent, boolean checked) {
    this.id = id;
    this.projectId = projectId;
    this.content = content;
    this.dueDate = dueDate;
    this.priority = priority;
    this.itemOrder = itemOrder;
    this.indent = indent;
    this.checked = checked;
  }

  public static Comparator<Item> itemOrderComparator() {
    return (i1, i2) -> i1.getItemOrder().compareTo(i2.getItemOrder());
  }

  @Override
  public String toString() {
    return Json.json(new HashMap<String, Object>() {
      {
        put("id", id);
        put("projectId", projectId);
        put("content", content);
        put("dueDate", dueDate);
        put("priority", priority);
        put("itemOrder", itemOrder);
        put("indent", indent);
        put("checked", checked);
      }
    });
  }

  @Override
  public int hashCode() {
    return sequence(id, projectId, content, dueDate, priority, itemOrder, indent, checked).fold(1, Callables.hashCode);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof Item)) return false;
    Item o = (Item) obj;
    return Objects.equalTo(id, o.id) && Objects.equalTo(projectId, o.projectId) &&
           Objects.equalTo(content, o.content) &&
           Objects.equalTo(dueDate, o.dueDate) &&
           Objects.equalTo(priority, o.priority) && Objects.equalTo(itemOrder, o.itemOrder) &&
           Objects.equalTo(indent, o.indent) &&
           Objects.equalTo(checked, o.checked);
  }

  public Long getItemOrder() {
    return itemOrder;
  }

  public Integer getPriority() {
    return priority;
  }

  public Option<Long> getDueDate() {
    return dueDate;
  }

  public Long getProjectId() {
    return projectId;
  }

  public Long getId() {
    return id;
  }

  public String getContent() {
    return content;
  }

  public boolean isChecked() {
    return checked;
  }

  public Integer getIndent() {
    return indent;
  }
}
