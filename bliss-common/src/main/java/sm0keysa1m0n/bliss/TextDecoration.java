package sm0keysa1m0n.bliss;

public record TextDecoration(boolean underline, boolean lineThrough) {

  public static final TextDecoration NONE = new TextDecoration(false, false);
}
