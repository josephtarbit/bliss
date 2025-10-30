package sm0keysa1m0n.bliss.view.event;

// Cancellable
public record KeyEvent(int key, int scancode, int action, int mods) implements ViewEvent {}

