package me.manuelp.todoist_backup;

import com.googlecode.totallylazy.Sequence;
import me.manuelp.todoist_backup.data.Item;
import me.manuelp.todoist_backup.data.Project;
import me.manuelp.todoist_backup.data.export.formats.MarkdownFormatter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class Exporter {
  public static final Path EXPORT_PATH = Paths.get("export.md");
  public static final String USERNAME = System.getenv("TODOIST_USERNAME");
  public static final String PASSWORD = System.getenv("TODOIST_PASSWORD");

  public static void main(String[] args) throws IOException {
    TodoistGateway gw = new TodoistGateway(USERNAME, PASSWORD);
    final String apiToken = gw.getApiToken();

    StringBuffer markdownExport = new StringBuffer();
    Sequence<Project> projects = gw.getProjects(apiToken);
    int numProjects = projects.size();
    int cur = 1;
    markdownExport.append("# Todoist Export (" + new Date().toString() + ")");
    for (Project p : projects) {
      System.out.println(String.format("Exporting project %d/%d: %s", cur++, numProjects, p.toString()));
      Sequence<Item> items = gw.getItems(apiToken, p.getId());
      markdownExport.append(MarkdownFormatter.formatProject(p, items));
    }
    Files.write(EXPORT_PATH, markdownExport.toString().getBytes());
  }
}
