package sm0keysa1m0n.bliss;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface TextVisitor {

  void visit(BiConsumer<StyledText, TextDecoration> consumer);

  static TextVisitor of(StyledText styledText) {
    return of(styledText, TextDecoration.NONE);
  }

  static TextVisitor of(StyledText styledText, TextDecoration decoration) {
    return consumer -> consumer.accept(styledText, decoration);
  }
}
