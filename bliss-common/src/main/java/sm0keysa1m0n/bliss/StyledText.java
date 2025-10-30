package sm0keysa1m0n.bliss;

import org.jetbrains.annotations.Nullable;
import io.github.humbleui.skija.FontStyle;

public record StyledText(String text, FontStyle fontStyle, @Nullable Color color) {

  public static final StyledText EMPTY = of("");

  public StyledText(String text, FontStyle fontStyle, @Nullable Color color) {
    this.text = text == null ? "" : text;
    this.fontStyle = fontStyle;
    this.color = color;
  }

  public static StyledText of(@Nullable String text) {
    return of(text, null);
  }

  public static StyledText of(String text, @Nullable Color color) {
    return new StyledText(text, FontStyle.NORMAL, null);
  }
}
