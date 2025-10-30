package sm0keysa1m0n.bliss.util;

public class MathUtil {

  public static int ceil(float value) {
    int i = (int) value;
    return value > i ? i + 1 : i;
  }

  public static int clamp(int value, int min, int max) {
    if (value < min) {
       return min;
    } else {
       return value > max ? max : value;
    }
 }
  
  public static float clamp(float value, float min, float max) {
    if (value < min) {
      return min;
    } else {
      return value > max ? max : value;
    }
  }

  public static int multiplyAlpha(int color, float alpha) {
    var newAlpha = (int) (((color >> 24) & 0xFF) * alpha);
    var rgb = color & 0x00FFFFFF;
    return ((newAlpha << 24) | rgb);
  }

  public static float[] getColor4f(int[] colour4i) {
    return new float[] {
        colour4i[0] / 255.0F,
        colour4i[1] / 255.0F,
        colour4i[2] / 255.0F,
        colour4i[3] / 255.0F};
  }

  public static int[] getColor4i(int color) {
    var rgba = new int[4];
    rgba[0] = (color >> 16) & 0xFF;
    rgba[1] = (color >> 8) & 0xFF;
    rgba[2] = (color >> 0) & 0xFF;
    rgba[3] = (color >> 24) & 0xFF;
    return rgba;
  }

  public static int[] getColor4i(float[] color4f) {
    return new int[] {
        (int) (color4f[0] * 255),
        (int) (color4f[1] * 255),
        (int) (color4f[2] * 255),
        (int) (color4f[3] * 255)};
  }

  public static int getColorHex(int[] color4i) {
    return ((color4i[3] & 0xFF) << 24)
        | ((color4i[0] & 0xFF) << 16)
        | ((color4i[1] & 0xFF) << 8)
        | ((color4i[2] & 0xFF) << 0);
  }

  public static float lerp(float percent, float start, float end) {
    return start + percent * (end - start);
  }

  public static long lerp(float percent, int colour1, int colour2) {
    return getColorHex(lerp(percent, getColor4i(colour1), getColor4i(colour2)));
  }

  public static int[] lerp(float percent, int[] colour1, int[] colour2) {
    var rgba = new int[4];
    for (int i = 0; i < 4; i++) {
      rgba[i] = (int) lerp(percent, colour1[i], colour2[i]);
    }
    return rgba;
  }

  public static float[] lerp(float percent, float[] colour1, float[] colour2) {
    var rgba = new float[4];
    for (int i = 0; i < 4; i++) {
      rgba[i] = lerp(percent, colour1[i], colour2[i]);
    }
    return rgba;
  }
}
