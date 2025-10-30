package sm0keysa1m0n.bliss.view.event;

import org.lwjgl.glfw.GLFW;

public abstract class MouseEvent implements ViewEvent {

  private final double mouseX;
  private final double mouseY;

  public MouseEvent(double mouseX, double mouseY) {
    this.mouseX = mouseX;
    this.mouseY = mouseY;
  }

  public double mouseX() {
    return this.mouseX;
  }

  public double mouseY() {
    return this.mouseY;
  }

  public static class MoveEvent extends MouseEvent {

    public MoveEvent(double mouseX, double mouseY) {
      super(mouseX, mouseY);
    }
  }

  // Cancellable
  public static class ButtonEvent extends MouseEvent {

    private final int button;
    private final int action;

    public ButtonEvent(double mouseX, double mouseY, int button, int action) {
      super(mouseX, mouseY);
      this.button = button;
      this.action = action;
    }


    /**
     * The mouse button that triggered this event.
     * https://www.glfw.org/docs/latest/group__buttons.html
     *
     * @see GLFW mouse constants starting with "GLFW_MOUSE_BUTTON_"
     */
    public int button() {
      return this.button;
    }

    /**
     * Integer representing the mouse button's action.
     *
     * @see GLFW#GLFW_PRESS
     * @see GLFW#GLFW_RELEASE
     */
    public int action() {
      return this.action;
    }
  }

  public static class DragEvent extends MouseEvent {

    private final int button;
    private final double deltaX;
    private final double deltaY;

    public DragEvent(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      super(mouseX, mouseY);
      this.button = button;
      this.deltaX = deltaX;
      this.deltaY = deltaY;
    }

    public int button() {
      return this.button;
    }

    public double deltaX() {
      return this.deltaX;
    }

    public double deltaY() {
      return this.deltaY;
    }
  }

  public static class ScrollEvent extends MouseEvent {

    private final double scrollDelta;

    public ScrollEvent(double mouseX, double mouseY, double scrollDelta) {
      super(mouseX, mouseY);
      this.scrollDelta = scrollDelta;
    }

    public double scrollDelta() {
      return this.scrollDelta;
    }
  }
}
