package me.manuelp.todoist_backup.data;

import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Objects;
import com.googlecode.totallylazy.json.Json;

import java.util.Comparator;
import java.util.HashMap;

import static com.googlecode.totallylazy.Sequences.sequence;

public class Project {
  private final Long id;
  private final String name;
  private final Integer indent;
  private Integer itemOrder;

  public Project(Long id, String name, Integer indent, Integer itemOrder) {
    this.id = id;
    this.name = name;
    this.indent = indent;
    this.itemOrder = itemOrder;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Integer getItemOrder() {
    return itemOrder;
  }

  public Integer getIndent() {
    return indent;
  }

  public static Comparator<Project> itemOrderComparator() {
    return (o1, o2) -> o1.getItemOrder().compareTo(o2.getItemOrder());
  }

  @Override
  public String toString() {
    return Json.json(new HashMap<String, Object>() {
      {
        put("id", id);
        put("name", name);
        put("itemOrder", itemOrder);
        put("indent", indent);
      }
    });
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null | !(obj instanceof Project)) return false;
    Project o = (Project) obj;
    return Objects.equalTo(id, o.id) && Objects.equalTo(name, o.name) && Objects.equalTo(itemOrder, o.itemOrder) &&
           Objects.equalTo(indent, o.indent);
  }

  @Override
  public int hashCode() {
    return sequence(id, name, itemOrder, indent).fold(1, Callables.hashCode);
  }
}
