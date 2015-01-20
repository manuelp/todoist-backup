package me.manuelp.todoist_backup.data.export.formats;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import me.manuelp.todoist_backup.data.Item;
import me.manuelp.todoist_backup.data.Project;

import java.io.IOException;
import java.nio.file.Path;

public class JsonExporter {
  private final JsonGenerator g;

  public JsonExporter(Path exportPath) throws IOException {
    JsonFactory f = new JsonFactory();
    g = f.createGenerator(exportPath.toFile(), JsonEncoding.UTF8);
  }

  public void export(Sequence<Pair<Project, Sequence<Item>>> projects) {
    try {
      g.writeStartArray();
      for (Pair<Project, Sequence<Item>> project : projects)
        exportProject(project.first(), project.second());
      g.writeEndArray();
      g.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void exportProject(Project p, Sequence<Item> items) throws IOException {
    g.writeStartObject();
    g.writeNumberField("id", p.getId());
    g.writeStringField("name", p.getName());
    g.writeNumberField("indent", p.getIndent());
    g.writeNumberField("itemOrder", p.getItemOrder());

    g.writeArrayFieldStart("items");
    for (Item item : items)
      exportItem(item);
    g.writeEndArray();

    g.writeEndObject();
  }

  private void exportItem(Item item) throws IOException {
    g.writeStartObject();
    g.writeNumberField("id", item.getId());
    g.writeBooleanField("checked", item.isChecked());
    g.writeStringField("text", item.getContent());
    if (item.getDueDate().isDefined()) g.writeNumberField("dueDate", item.getDueDate().get());
    g.writeNumberField("priority", item.getPriority());
    g.writeNumberField("order", item.getItemOrder());
    g.writeNumberField("indent", item.getIndent());
    g.writeEndObject();
  }
}
