package me.manuelp.todoist_backup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.json.Json;
import com.googlecode.totallylazy.parser.Result;
import me.manuelp.todoist_backup.data.Item;
import me.manuelp.todoist_backup.data.Project;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.googlecode.totallylazy.Sequences.sequence;

public class TodoistGateway {
  private static Logger log = LoggerFactory.getLogger(TodoistGateway.class);

  private final String email;
  private final String password;

  public TodoistGateway(String email, String password) {
    this.email = email;
    this.password = password;
  }

  public String getApiToken() {
    String response = sendGetRequest("/API/login", new HashMap<String, String>() {
      {
        put("email", email);
        put("password", password);
      }
    });
    log.debug("Requested API token");
    try {
      Map<String, Object> res = new ObjectMapper().readValue(response, Map.class);
      return Functions.getApiToken().call(res);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public Sequence<Project> getProjects(String apiToken) {
    String response = sendGetRequest("/API/getProjects", new HashMap<String, String>() {
      {
        put("token", apiToken);
      }
    });
    log.debug("Requested projects");
    return parseList(response, Functions.readProject()).sort(Project.itemOrderComparator());
  }

  public Sequence<Project> getArchivedProjects(String apiToken) {
    String response = sendGetRequest("/API/getArchived", new HashMap<String, String>() {
      {
        put("token", apiToken);
      }
    });
    log.debug("Requested archived projects");
    return parseList(response, Functions.readProject()).sort(Project.itemOrderComparator());
  }

  public Sequence<Item> getItems(String apiToken, Long projectId) {
    String response = sendGetRequest("/API/getUncompletedItems", new HashMap<String, String>() {
      {
        put("token", apiToken);
        put("project_id", String.valueOf(projectId));
      }
    });
    log.debug("Requested items for project " + projectId);
    return parseList(response, m -> {
      Long id = getLong(m, "id");
      String content = (String) m.get("content");
      Option<Long> dueDate = getOptionalTimestamp(m, "due_date");
      Integer priority = getInteger(m, "priority");
      Long itemOrder = getLong(m, "item_order");
      Integer indent = getInteger(m, "indent");
      boolean checked = ((Integer) m.get("checked")).intValue() == 0 ? false : true;
      return new Item(id, projectId, content, dueDate, priority, itemOrder, indent, checked);
    }).sort(Item.itemOrderComparator());
  }

  private Option<Long> getOptionalTimestamp(Map<String, Object> m, String name) throws ParseException {
    Option<Long> dueDate;
    if (m.containsKey(name) && m.get(name) != null)
      dueDate = Option.some(new SimpleDateFormat("EEE dd MMM yyyy HH:mm:ss").parse((String) m.get(name)).getTime());
    else dueDate = Option.none();
    return dueDate;
  }

  private static int getInteger(Map<String, Object> m, String name) {
    return ((Integer) m.get(name)).intValue();
  }

  private static long getLong(Map<String, Object> m, String name) {
    return ((Integer) m.get(name)).longValue();
  }

  public <T> Sequence<T> parseList(String json, Callable1<Map<String, Object>, T> transformer) {
    try {
      List<Map<String, Object>> lst = new ObjectMapper().readValue(json, List.class);
      return sequence(lst).map(transformer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String sendGetRequest(String path, Map<String, String> parameters) {
    try {
      HttpGet getAuth = new HttpGet(createUri(path, parameters));
      getAuth.addHeader(new BasicHeader("Content-Type", "application/json; charset=UTF-8"));
      HttpResponse response = HttpClients.createDefault().execute(getAuth);
      return extractContent(response);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private URI createUri(String path, Map<String, String> params) throws URISyntaxException {
    URIBuilder builder = createUriBuilder().setPath(path);
    for (Map.Entry<String, String> entry : params.entrySet())
      builder = builder.setParameter(entry.getKey(), entry.getValue());
    return builder.build();
  }

  private URIBuilder createUriBuilder() {
    return new URIBuilder().setScheme("https").setHost("api.todoist.com");
  }

  private String extractContent(HttpResponse response) throws IOException {
    HttpEntity entity = response.getEntity();
    InputStream content = entity.getContent();
    StringWriter writer = new StringWriter();
    IOUtils.copy(content, writer);
    return writer.toString();
  }

  public static class Functions {
    public static Callable1<Map<String, Object>, String> getApiToken() {
      return m -> (String) m.get("api_token");
    }

    public static Callable1<Map<String, Object>, Project> readProject() {
      return m -> {
        Long id = getLong(m, "id");
        String name = (String) m.get("name");
        int indent = getInteger(m, "indent");
        int itemOrder = getInteger(m, "item_order");
        return new Project(id, name, indent, itemOrder);
      };
    }
  }

}
