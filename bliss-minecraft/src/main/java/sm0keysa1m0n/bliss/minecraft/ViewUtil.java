package sm0keysa1m0n.bliss.minecraft;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import net.minecraftforge.common.ForgeHooks;
import sm0keysa1m0n.bliss.Bliss;
import sm0keysa1m0n.bliss.Length;
import sm0keysa1m0n.bliss.TextVisitor;
import sm0keysa1m0n.bliss.style.parser.ParserException;
import sm0keysa1m0n.bliss.view.DownloadUtil;
import sm0keysa1m0n.bliss.view.ImageAccess;
import sm0keysa1m0n.bliss.view.ImageView;
import sm0keysa1m0n.bliss.view.LoggingErrorHandler;
import sm0keysa1m0n.bliss.view.ParentView;
import sm0keysa1m0n.bliss.view.TextView;
import sm0keysa1m0n.bliss.view.View;

public class ViewUtil {

  private static final Logger logger = LoggerFactory.getLogger(ViewUtil.class);

  /**
   * Add all the {@link View}s specified in the passed {@link File}, spacing them evenly with
   * {@code flex: 1;}
   * 
   * @param file - the {@link File} to read {@link View}s from
   * @return ourself
   */
  public static void addAll(ParentView parentView, File file) {
    addAll(parentView, file, view -> {
      try {
        view.getStyle().getStyleManager().parseInline("flex: 1;");
      } catch (ParserException e) {
        throw new IllegalStateException(e);
      }
    });
  }

  /**
   * Add all the {@link View}s specified in the passed {@link File}.
   * 
   * @param file - the {@link File} to read {@link View}s from
   * @param configurer - a {@link Consumer} used to configure {@link View}s before they're added
   * @return ourself
   */
  public static void addAll(ParentView parentView, File file, Consumer<View> configurer) {
    DocumentBuilder builder;
    CompletableFuture<Document> documentFuture;
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setIgnoringElementContentWhitespace(true);
      builder = factory.newDocumentBuilder();
      builder.setErrorHandler(LoggingErrorHandler.INSTANCE);
      documentFuture = CompletableFuture.supplyAsync(() -> {
        try {
          return builder.parse(file);
        } catch (IOException | SAXException e) {
          logger.warn("Failed to parse xml {} {}", file.getAbsolutePath(), e);
          return null;
        }
      }, Bliss.instance().platform().backgroundExecutor());
    } catch (ParserConfigurationException e) {
      logger.warn("Failed to create document builder", e);
      return;
    }

    documentFuture.thenAcceptAsync(
        document -> parseDocument(document, file, parentView, configurer),
        Bliss.instance().platform().mainExecutor());
  }

  private static void parseDocument(Document document, File file, ParentView parentView,
      Consumer<View> configurer) {
    var nodes = document.getDocumentElement().getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      var node = nodes.item(i);
      switch (node.getNodeName()) {
        case "text":
          final var text = node.getTextContent();

          if (text != null) {
            var view = new TextView(new View.Properties())
                .setText(extractStyles(ForgeHooks.newChatWithLinks(text)));

            Node styleNode = node.getAttributes().getNamedItem("style");
            if (styleNode != null && styleNode.getNodeValue() != null) {
              try {
                view.getStyle().getStyleManager().parseInline(styleNode.getNodeValue());
              } catch (ParserException e) {
                logger.warn("Invalid style: {}", styleNode.getNodeValue(), e);
              }
            }

            view.getStyle().width.set(Length.percentage(100.0F));
            parentView.addChild(view);
          }
          break;
        case "image":
          String url = null;
          Node urlNode = node.getAttributes().getNamedItem("url");
          if (urlNode != null && urlNode.getNodeValue() != null) {
            url = urlNode.getNodeValue();
          } else {
            logger.warn("No URL declared for image in {}", file.getAbsolutePath());
            break;
          }

          var view = new ImageView(new View.Properties());

          Node styleNode = node.getAttributes().getNamedItem("style");
          if (styleNode != null && styleNode.getNodeValue() != null) {
            try {
              view.getStyle().getStyleManager().parseInline(styleNode.getNodeValue());
            } catch (ParserException e) {
              logger.warn("Invalid style: {}", styleNode.getNodeValue(), e);
            }
          }

          parentView.addChild(view);
          DownloadUtil.downloadImage(url)
              .thenAcceptAsync(result -> result.ifPresent(image -> {
                view.setImage(ImageAccess.forImage(image));
                if (parentView.isAdded()) {
                  parentView.layout();
                }
              }), Bliss.instance().platform().mainExecutor());
          break;
        default:
          break;
      }
    }

    if (parentView.isAdded()) {
      parentView.layout();
    }
  }

  public static TextVisitor extractStyles(Component component) {
    var builder = new ComponentBuilder();
    StringDecomposer.iterateFormatted(component, Style.EMPTY, builder);
    return AdapterUtil.createTextVisitor(builder.getComponent());
  }

  private static class ComponentBuilder implements FormattedCharSink {

    private MutableComponent component = TextComponent.EMPTY.copy();

    private StringBuilder builder;
    private Style style;

    @Override
    public boolean accept(int index, Style style, int codePoint) {
      if (this.style != style) {
        if (this.builder != null) {
          this.component.append(new TextComponent(this.builder.toString()).setStyle(this.style));
        }

        this.builder = new StringBuilder();
        this.style = style;
      }
      this.builder.append((char) codePoint);
      return true;
    }

    public Component getComponent() {
      return this.component;
    }
  }
}
