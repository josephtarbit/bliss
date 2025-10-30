package sm0keysa1m0n.bliss;

import java.util.Arrays;
import java.util.Objects;
import org.jdesktop.core.animation.timing.Evaluator;
import org.jdesktop.core.animation.timing.evaluators.KnownEvaluators;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import com.google.common.base.Preconditions;
import sm0keysa1m0n.bliss.util.MathUtil;

public record Color(float[] components4f, int[] components4i, int hex) {

  static {
    KnownEvaluators.getInstance().register(new Evaluator<Color>() {

      @Override
      public Color evaluate(Color v0, Color v1, double fraction) {
        return Color.create(MathUtil.lerp((float) fraction, v0.components4f(), v1.components4f()));
      }

      @Override
      public Class<Color> getEvaluatorClass() {
        return Color.class;
      }
    });
  }

  public static final int FULL_ALPHA = 255 << 24;

  public static final Color TRANSPARENT = create(0, 0, 0, 0);

  public static final Color BLACK = create(0, 0, 0);
  public static final Color DARK_BLUE = create(0, 0, 170);
  public static final Color DARK_GREEN = create(0, 170, 0);
  public static final Color DARK_AQUA = create(0, 170, 170);
  public static final Color DARK_RED = create(170, 0, 0);
  public static final Color DARK_PURPLE = create(170, 0, 170);
  public static final Color GOLD = create(255, 170, 0);
  public static final Color GRAY = create(170, 170, 170);
  public static final Color DARK_GRAY = create(85, 85, 85);
  public static final Color BLUE = create(85, 85, 255);
  public static final Color GREEN = create(85, 255, 85);
  public static final Color AQUA = create(85, 255, 255);
  public static final Color RED = create(255, 85, 85);
  public static final Color LIGHT_PURPLE = create(255, 85, 255);
  public static final Color YELLOW = create(255, 255, 85);
  public static final Color WHITE = create(255, 255, 255);
  public static final Color BLUE_C = create(170, 220, 240);
  public static final Color GRAY_224 = create(224, 224, 224);

  private static final Color[] VANILLA_COLORS =
      new Color[] {BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY,
          DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE};

  @ApiStatus.Internal
  public Color(float[] components4f, int[] components4i, int hex) {
    this.components4f = components4f;
    this.components4i = components4i;
    this.hex = hex;
  }

  public static Color parseWithFullAlpha(String hex) {
    return parseWithAlpha(hex, 1.0F);
  }

  public static Color parseWithAlpha(String hex, float alpha) {
    int padding = hex.startsWith("#") ? 1 : 0;
    var red = Integer.parseInt(hex.substring(padding, 2 + padding), 16) / 255.0F;
    var green = Integer.parseInt(hex.substring(2 + padding, 4 + padding), 16) / 255.0F;
    var blue = Integer.parseInt(hex.substring(4 + padding, 6 + padding), 16) / 255.0F;
    return create(red, green, blue, alpha);
  }

  public static Color createWithFullAlpha(int hex) {
    return createWithAlpha(hex, 255);
  }

  public static Color createWithAlpha(int hex, int alpha) {
    return create(hex + (alpha << 24));
  }

  public static Color create(int hex) {
    var components4i = MathUtil.getColor4i(hex);
    return new Color(MathUtil.getColor4f(components4i), components4i, hex);
  }

  public static Color create(int red, int green, int blue) {
    return create(red, green, blue, 255);
  }

  public static Color create(int red, int green, int blue, int alpha) {
    return create(new int[] {red, green, blue, alpha});
  }

  public static Color create(int[] components) {
    Preconditions.checkArgument(components.length == 4, "components must have length of 4");
    return new Color(MathUtil.getColor4f(components), components, MathUtil.getColorHex(components));
  }

  public static Color create(float red, float green, float blue) {
    return create(red, green, blue, 1.0F);
  }

  public static Color create(float red, float green, float blue, float alpha) {
    return create(new float[] {red, green, blue, alpha});
  }

  public static Color create(float[] components) {
    Preconditions.checkArgument(components.length == 4, "components must have length of 4");
    var color4i = MathUtil.getColor4i(components);
    return new Color(components, color4i, MathUtil.getColorHex(color4i));
  }

  public int red() {
    return this.components4i[0];
  }

  public int green() {
    return this.components4i[1];
  }

  public int blue() {
    return this.components4i[2];
  }

  public int alpha() {
    return this.components4i[3];
  }

  @Override
  public float[] components4f() {
    var result = new float[4];
    System.arraycopy(this.components4f, 0, result, 0, 4);
    return result;
  }

  @Override
  public int[] components4i() {
    var result = new int[4];
    System.arraycopy(this.components4i, 0, result, 0, 4);
    return result;
  }

  @Override
  public int hex() {
    return this.hex;
  }

  public int multiplied(float alpha) {
    return MathUtil.multiplyAlpha(this.hex, alpha);
  }

  public boolean transparent() {
    return this.alpha() == 0;
  }

  @Override
  public String toString() {
    return "Color[rgba" + Arrays.toString(this.components4i)
        + " / #" + Integer.toHexString(this.hex) + "]";
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.hex);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Color other && other.hex == this.hex;
  }

  @Nullable
  public static Color getFormattingColor(int code) {
    if (code >= 0 && code <= 15) {
      return VANILLA_COLORS[code];
    }
    return null;
  }
}
