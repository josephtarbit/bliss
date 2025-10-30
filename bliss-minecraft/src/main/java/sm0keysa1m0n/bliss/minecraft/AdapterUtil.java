package sm0keysa1m0n.bliss.minecraft;

import java.io.IOException;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.humbleui.skija.Data;
import io.github.humbleui.skija.FontStyle;
import io.github.humbleui.skija.Image;
import io.github.humbleui.skija.svg.SVGDOM;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import sm0keysa1m0n.bliss.Color;
import sm0keysa1m0n.bliss.StyledText;
import sm0keysa1m0n.bliss.TextDecoration;
import sm0keysa1m0n.bliss.TextVisitor;
import sm0keysa1m0n.bliss.view.ImageAccess;
import sm0keysa1m0n.bliss.view.SimpleImageAccess;
import sm0keysa1m0n.bliss.view.SvgImageAccess;

public class AdapterUtil {

  private static final Logger logger = LoggerFactory.getLogger(AdapterUtil.class);

  public static TextVisitor createTextVisitor(Component text) {
    return consumer -> text.visit((style, content) -> {
      consumer.accept(createStyledText(text),
          new TextDecoration(
              text.getStyle().isUnderlined(),
              text.getStyle().isStrikethrough()));
      return Optional.empty();
    }, Style.EMPTY);
  }

  public static StyledText createStyledText(Component component) {
    return new StyledText(component.getString(), createFontStyle(component.getStyle()),
        createColor(component.getStyle()).orElse(null));
  }

  public static Optional<Color> createColor(Style style) {
    return style.getColor() == null
        ? Optional.empty()
        : Optional.of(Color.create(style.getColor().getValue() + (255 << 24)));
  }

  public static FontStyle createFontStyle(Style style) {
    return style.isBold() && style.isItalic()
        ? FontStyle.BOLD_ITALIC
        : style.isBold()
            ? FontStyle.BOLD
            : style.isItalic()
                ? FontStyle.ITALIC
                : FontStyle.NORMAL;
  }

  @SuppressWarnings("resource")
  @Nullable
  public static ImageAccess createImageAccess(ResourceLocation imageLocation) {
    try (var inputStream =
        Minecraft.getInstance().getResourceManager().getResource(imageLocation).getInputStream()) {
      var bytes = inputStream.readAllBytes();
      if (imageLocation.getPath().endsWith(".svg")) {
        return new SvgImageAccess(new SVGDOM(Data.makeFromBytes(bytes)));
      } else {
        return new SimpleImageAccess(Image.makeFromEncoded(bytes));
      }
    } catch (IOException e) {
      logger.warn("Failed to load image: {}", imageLocation);
      return null;
    }
  }
}
