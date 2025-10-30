package sm0keysa1m0n.bliss.style.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.humbleui.skija.Typeface;
import sm0keysa1m0n.bliss.style.StyleList;
import sm0keysa1m0n.bliss.style.StyleProperty;

public class StyleSheetParser {

  private static final Logger logger = LoggerFactory.getLogger(StyleSheetParser.class);

  private static final String IMPORT = "@import";
  private static final String FONT_FACE = "@font-face";

  private StyleSheetParser() {}

  @FunctionalInterface
  public interface FontLoader {

    Typeface load(String location) throws IOException;
  }

  public static ParseResult loadStyleSheet(InputStream inputStream, FontLoader fontLoader)
      throws IOException {
    try (var reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
      return loadStyleSheet(reader, fontLoader);
    }
  }

  public static ParseResult loadStyleSheet(Reader reader, FontLoader fontLoader)
      throws IOException {
    var list = new StyleList();
    var dependencies = new ArrayList<String>();
    var iterator = new NumberedLineIterator(reader);
    boolean insideComment = false;
    while (iterator.hasNext()) {
      var line = iterator.nextLine().trim();
      if (StringUtils.isEmpty(line)) {
        continue;
      }

      if (line.startsWith("/*")) {
        insideComment = !line.endsWith("*/");
        continue;
      }

      if (insideComment && line.startsWith("*/")) {
        insideComment = false;
        continue;
      }

      if (line.startsWith("@")) {
        if (line.startsWith(FONT_FACE)) {
          readFontFace(iterator, list, fontLoader);
        } else if (line.startsWith(IMPORT)) {
          dependencies.add(line.substring(IMPORT.length())
              .replace("'", "")
              .replace("\"", "")
              .replace(";", "")
              .trim());
        } else {
          logger.warn("Unknown at-rule: {}", line);
        }
        continue;
      }


      var selectors = StyleSelectorParser.readSelectors(line, iterator);
      var properties = readBlock(iterator);
      for (var selector : selectors) {
        list.addRule(selector, properties);
      }
    }

    return new ParseResult(list, dependencies);
  }

  public record ParseResult(StyleList styleList, Collection<String> dependencies) {}

  private static void readFontFace(NumberedLineIterator content, StyleList styleList,
      FontLoader fontLoader) throws IOException {
    int startLineNumber = content.getLineNumber();
    var currentLine = content.nextLine().trim();
    String fontFamily = null;
    String location = null;
    while (!StringUtils.contains(currentLine, "}")) {
      if (currentLine.startsWith("font-family")) {
        char quote;
        if (currentLine.contains("\"")) {
          quote = '"';
        } else {
          quote = '\'';
        }
        fontFamily =
            currentLine.substring(currentLine.indexOf(quote) + 1, currentLine.lastIndexOf(quote));
        currentLine = content.nextLine().trim();
        continue;
      }

      if (currentLine.startsWith("src")) {
        location = currentLine.substring(currentLine.indexOf(':') + 1)
            .replace("'", "")
            .replace("\"", "")
            .replace(";", "")
            .trim();
      }
      currentLine = content.nextLine().trim();
    }

    if (fontFamily == null) {
      logger.error("No font-family property specified for font-face at line: {}", startLineNumber);
      return;
    }

    if (location == null) {
      logger.error("No src property specified for font-face at line: {}", startLineNumber);
      return;
    }

    styleList.addFont(fontFamily, fontLoader.load(location));
  }

  private static Set<StyleProperty> readBlock(NumberedLineIterator content) {
    if (!content.hasNext()) {
      return Set.of();
    }
    var currentLine = content.nextLine().strip();
    var elements = new LinkedHashSet<StyleProperty>();
    while (!StringUtils.contains(currentLine, "}")) {
      if (StringUtils.contains(currentLine, "{")) {
        logger.error(
            "Found opening bracket at line " + content.getLineNumber() + " while inside a block");
        return Set.of();
      }
      if (currentLine.isBlank()) {
        currentLine = content.nextLine().strip();
        continue;
      }
      var propertyBuilder = new StringBuilder(currentLine);
      while (!currentLine.endsWith(";")) {
        currentLine = content.nextLine().strip();
        propertyBuilder.append(currentLine);
      }
      var propertyParts = propertyBuilder.toString().replace(';', ' ').strip().split(":", 2);
      var property = new StyleProperty(propertyParts[0].strip(), propertyParts[1].strip());
      // Replace existing.
      elements.remove(property);
      elements.add(property);
      if (!content.hasNext()) {
        return Set.of();
      }
      currentLine = content.nextLine().strip();
    }
    return elements;
  }
}
