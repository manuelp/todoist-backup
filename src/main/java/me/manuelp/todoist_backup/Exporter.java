package me.manuelp.todoist_backup;

import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import me.manuelp.todoist_backup.data.Item;
import me.manuelp.todoist_backup.data.Project;
import me.manuelp.todoist_backup.data.export.formats.JsonExporter;
import me.manuelp.todoist_backup.data.export.formats.MarkdownFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Exporter {
  private static Logger log = LoggerFactory.getLogger(Exporter.class);

  public static final String USERNAME = System.getenv("TODOIST_USERNAME");
  public static final String PASSWORD = System.getenv("TODOIST_PASSWORD");
  public static final String EXPORT_FILENAME_PREFIX = "todoist";

  public static void main(String[] args) throws IOException {
    TodoistGateway gw = new TodoistGateway(USERNAME, PASSWORD);
    final String apiToken = gw.getApiToken();
    Sequence<Pair<Project, Sequence<Item>>> projects =
        gw.getProjects(apiToken).mapConcurrently(p -> Pair.pair(p, gw.getItems(apiToken, p.getId())));
    log.info("Exporting {} projects...", projects.size());
    export(projects);

    System.out.println("Done!");
  }

  private static void export(Sequence<Pair<Project, Sequence<Item>>> projects) throws IOException {
    String exportFileName = buildExportFileNameWithoutExtension(EXPORT_FILENAME_PREFIX);
    exportToMarkdown(Paths.get(exportFileName + ".md"), projects);
    exportToJson(Paths.get(exportFileName + ".json"), projects);
  }

  private static String buildExportFileNameWithoutExtension(String prefix) {
    String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss").format(new Date());
    return prefix + "_" + timestamp;
  }

  private static void exportToMarkdown(Path exportPath, Sequence<Pair<Project, Sequence<Item>>> projects) throws
      IOException {
    StringBuffer markdownExport = new StringBuffer();
    int numProjects = projects.size();
    int cur = 1;
    markdownExport.append("# Todoist Export (" + new Date().toString() + ")");
    for (Pair<Project, Sequence<Item>> p : projects) {
      log.info(String.format("Exporting project %d/%d: %s", cur++, numProjects, p.first().toString()));
      markdownExport.append(MarkdownFormatter.formatProject(p.first(), p.second()));
    }
    Files.write(exportPath, markdownExport.toString().getBytes());
  }

  private static void exportToJson(Path exportPath, Sequence<Pair<Project, Sequence<Item>>> projects) {
    try {
      new JsonExporter(exportPath).export(projects);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
