package me.manuelp.todoist_backup;

import com.googlecode.totallylazy.json.Json;
import com.googlecode.totallylazy.parser.Result;
import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TotallyLazyTest {
  @Test
  public void canParseSimpleText() {
    String json = "[\"text\"]";

    Result<List<String>> listResult = parse(json);

    assertTrue(listResult.success());
  }

  private Result<List<String>> parse(String json) {
    String preprocessed = StringEscapeUtils.unescapeJava(json);
    System.out.println(String.format("Original: %s, Preprocessed: %s", json, preprocessed));
    return Json.<String>parseList(preprocessed);
  }

  @Test
  public void canParseEscapedUtf8Characters() {
    String json = "[\"text\\u00f2\"]";

    Result<List<String>> listResult = parse(json);

    assertTrue(listResult.success());
  }

  @Test
  public void canParseEscapedStrings() {
    String json = "[\"Some \\\"escaped\\\" text\"]";

    Result<List<String>> listResult = parse(json);

    assertTrue(listResult.success());
    assertEquals("Some \"escaped\" text", listResult.value().get(0));
  }

  @Test
  public void canRepaceUnicodeEscapeCodes() throws UnsupportedEncodingException {
    //    assertEquals("D*", "D\\u00f2".replaceAll("\\\\u[0-9]{4}", "*"));

    String source = "\\\"Pubblicazione\\\"";
    String destination = source.replaceAll("\\\"", "\\\\\"");
    String expected = "\\\\\"Pubblicazione\\\\\"";
    System.out.println("Source: " + source);
    System.out.println("Destination: " + destination);
    System.out.println("Expected: " + expected);
    assertEquals(expected, destination);

  }

}