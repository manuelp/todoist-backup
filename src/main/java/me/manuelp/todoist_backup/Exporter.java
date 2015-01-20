package me.manuelp.todoist_backup;

import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import me.manuelp.todoist_backup.data.Item;
import me.manuelp.todoist_backup.data.Project;
import me.manuelp.todoist_backup.data.export.formats.JsonExporter;
import me.manuelp.todoist_backup.data.export.formats.MarkdownFormatter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class Exporter {
  public static final String USERNAME = System.getenv("TODOIST_USERNAME");
  public static final String PASSWORD = System.getenv("TODOIST_PASSWORD");
  public static final Path EXPORT_MD_PATH = Paths.get("export.md");
  private static final Path EXPORT_JSON_PATH = Paths.get("export.json");

  public static void main(String[] args) throws IOException {
    TodoistGateway gw = new TodoistGateway(USERNAME, PASSWORD);
    final String apiToken = gw.getApiToken();
    Sequence<Pair<Project, Sequence<Item>>> projects =
        gw.getProjects(apiToken).mapConcurrently(p -> Pair.pair(p, gw.getItems(apiToken, p.getId())));

    exportToMarkdown(projects);
    exportToJson(projects);
  }

  private static void exportToJson(Sequence<Pair<Project, Sequence<Item>>> projects) {
    try {
      new JsonExporter(EXPORT_JSON_PATH).export(projects);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void exportToMarkdown(Sequence<Pair<Project, Sequence<Item>>> projects) throws IOException {
    StringBuffer markdownExport = new StringBuffer();
    int numProjects = projects.size();
    int cur = 1;
    markdownExport.append("# Todoist Export (" + new Date().toString() + ")");
    for (Pair<Project, Sequence<Item>> p : projects) {
      System.out.println(String.format("Exporting project %d/%d: %s", cur++, numProjects, p.first().toString()));
      markdownExport.append(MarkdownFormatter.formatProject(p.first(), p.second()));
    }
    Files.write(EXPORT_MD_PATH, markdownExport.toString().getBytes());
  }
}
