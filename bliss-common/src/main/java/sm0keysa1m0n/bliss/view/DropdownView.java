package sm0keysa1m0n.bliss.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import io.github.humbleui.skija.Font;
import io.github.humbleui.skija.FontMgr;
import io.github.humbleui.skija.FontStyleSet;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.PaintMode;
import io.github.humbleui.skija.PaintStrokeCap;
import io.github.humbleui.skija.TextLine;
import io.github.humbleui.skija.shaper.Shaper;
import io.github.humbleui.skija.shaper.ShapingOptions;
import io.github.humbleui.types.Rect;
import sm0keysa1m0n.bliss.Bliss;
import sm0keysa1m0n.bliss.Color;
import sm0keysa1m0n.bliss.Size;
import sm0keysa1m0n.bliss.StyledText;
import sm0keysa1m0n.bliss.layout.MeasureMode;
import sm0keysa1m0n.bliss.style.States;
import sm0keysa1m0n.bliss.util.MathUtil;
import sm0keysa1m0n.bliss.view.event.ActionEvent;
import sm0keysa1m0n.bliss.view.event.ViewEvent;

public class DropdownView extends View {

  public static final int DEFAULT_HEIGHT = 14;
  public static final int DEFAULT_ITEM_BACKGROUND_COLOR = 0xFF444444;
  public static final int DEFAULT_SELECTED_ITEM_BACKGROUND_COLOR = 0xFF222222;
  public static final int DEFAULT_HOVERED_ITEM_BACKGROUND_COLOR = 0xFF333333;

  public static final float DEFAULT_ARROW_WIDTH = 6.0F;
  public static final float DEFAULT_ARROW_HEIGHT = 2.5F;
  public static final float DEFAULT_ARROW_LINE_WIDTH = 1F;
  public static final float DEFAULT_X_ARROW_OFFSET = 0.15F;

  private final List<Item> items = new ArrayList<>();

  private int itemBackgroundColor;
  private int selectedItemBackgroundColor;
  private int hoveredItemBackgroundColor;

  private boolean expanded = false;
  private int focusedItemIndex;
  private Item selectedItem;

  private float arrowWidth;
  private float arrowHeight;
  private float arrowLineWidth;
  private float xArrowOffset;

  private long fadeStartTimeMs;

  @Nullable
  private Item lastHoveredListener;

  private FontMgr fontManager = FontMgr.getDefault();

  public DropdownView(Properties properties) {
    super(properties.focusable(true));
    this.eventBus().subscribe(ActionEvent.class, event -> {
      if (this.expanded) {
        this.getFocusedItem().select();
      }
      this.toggleExpanded();
    });

    this.itemBackgroundColor = DEFAULT_ITEM_BACKGROUND_COLOR;
    this.selectedItemBackgroundColor = DEFAULT_SELECTED_ITEM_BACKGROUND_COLOR;
    this.hoveredItemBackgroundColor = DEFAULT_HOVERED_ITEM_BACKGROUND_COLOR;
    this.arrowWidth = DEFAULT_ARROW_WIDTH;
    this.arrowHeight = DEFAULT_ARROW_HEIGHT;
    this.arrowLineWidth = DEFAULT_ARROW_LINE_WIDTH;
    this.xArrowOffset = DEFAULT_X_ARROW_OFFSET;

    this.getStyle().fontFamily.addListener(__ -> this.items.forEach(Item::refreshTextLine));
  }

  @Override
  public void styleRefreshed(FontMgr fontManager) {
    if (this.fontManager != FontMgr.getDefault()) {
      this.fontManager.close();
    }
    this.fontManager = fontManager;
    this.items.forEach(Item::refreshTextLine);
  }

  @Override
  public void close() {
    super.close();
    if (this.fontManager != FontMgr.getDefault()) {
      this.fontManager.close();
    }
  }

  protected Size measure(MeasureMode widthMode, float width, MeasureMode heightMode, float height) {
    return new Size(width, DEFAULT_HEIGHT);
  }

  public DropdownView setArrowWidth(float arrowWidth) {
    this.arrowWidth = arrowWidth;
    return this;
  }

  public DropdownView setArrowHeight(float arrowHeight) {
    this.arrowHeight = arrowHeight;
    return this;
  }

  public DropdownView setArrowLineWidth(float arrowLineWidth) {
    this.arrowLineWidth = arrowLineWidth;
    return this;
  }

  /**
   * @param xArrowOffset RTL offset from 0.0F to 1.0F (something like percent margin right)
   */
  public DropdownView setXArrowOffset(float xArrowOffset) {
    this.xArrowOffset = xArrowOffset;
    return this;
  }

  public DropdownView setItemBackgroundColour(int itemBackgroundColour) {
    this.itemBackgroundColor = itemBackgroundColour;
    return this;
  }

  public DropdownView setSelectedItemBackgroundColour(int selectedItemBackgroundColour) {
    this.selectedItemBackgroundColor = selectedItemBackgroundColour;
    return this;
  }

  public DropdownView setHoveredItemBackgroundColour(int hoveredItemBackgroundColour) {
    this.hoveredItemBackgroundColor = hoveredItemBackgroundColour;
    return this;
  }

  public DropdownView addItem(StyledText text, Runnable actionCallback) {
    this.items.add(new Item(this.items.size(), text, actionCallback));
    return this;
  }

  public DropdownView setDisabled(int itemId, boolean disabled) {
    this.items.get(itemId).setDisabled(disabled);
    return this;
  }

  private Item getFocusedItem() {
    return this.items.get(this.focusedItemIndex);
  }

  @Override
  public void mouseMoved(double mouseX, double mouseY) {
    super.mouseMoved(mouseX, mouseY);
    var hoveredListener = this.items.stream()
        .filter(item -> item.isMouseOver(mouseX, mouseY))
        .findFirst()
        .orElse(null);
    if (hoveredListener != this.lastHoveredListener) {
      this.eventBus().publish(new ItemHoverEvent(hoveredListener));
    }
    this.lastHoveredListener = hoveredListener;
  }

  @Override
  public boolean mousePressed(double mouseX, double mouseY, int button) {
    if (this.expanded) {
      for (var item : this.items) {
        if (item.isMouseOver(mouseX, mouseY) && !item.disabled) {
          this.focusedItemIndex = item.index;
          break;
        }
      }
    }
    return super.mousePressed(mouseX, mouseY, button);
  }

  @Override
  protected void focusChanged() {
    if (!this.isFocused() && this.expanded) {
      this.toggleExpanded();
    }
  }

  @Override
  public Optional<View> changeFocus(boolean forward) {
    if (this.isFocused() && this.expanded) {
      this.getStyleManager().addState(States.FOCUS_VISIBLE);
      this.getStyleManager().notifyListeners();
      this.toggleExpanded();
      return Optional.of(this);
    }
    return super.changeFocus(forward);
  }

  @Override
  protected void layout() {
    super.layout();
    this.items.forEach(Item::refreshTextLine);
  }

  @Override
  protected void added() {
    super.added();
    if (this.items.size() > 0) {
      this.items.get(0).select();
    }
  }

  @Override
  public void keyPressed(int keyCode, int scanCode, int modifiers) {
    if (keyCode == GLFW.GLFW_KEY_DOWN) {
      this.focusedItemIndex = Math.min(this.focusedItemIndex + 1, this.items.size() - 1);
    } else if (keyCode == GLFW.GLFW_KEY_UP) {
      this.focusedItemIndex = Math.max(this.focusedItemIndex - 1, 0);
    }

    if (!this.expanded) {
      this.getFocusedItem().select();
    }

    super.keyPressed(keyCode, scanCode, modifiers);
  }

  protected void toggleExpanded() {
    this.fadeStartTimeMs = Bliss.instance().platform().milliTime();
    this.expanded = !this.expanded;
    this.eventBus().publish(new ToggleExpandedEvent(this.expanded));
  }

  @Override
  public boolean isMouseOver(double mouseX, double mouseY) {
    return mouseX > this.getScaledX() && mouseX < this.getScaledX() + this.getScaledWidth() &&
        mouseY > this.getScaledY() && mouseY < this.getScaledY() + this.getScaledHeight()
            + (this.expanded ? this.items.size() * this.getItemHeight() : 0);
  }

  protected int getItemHeight() {
    return (int) this.getScaledContentHeight();
  }

  @Override
  public void renderContent(int mouseX, int mouseY) {
    super.renderContent(mouseX, mouseY);

    this.selectedItem.render(Type.SELECTED, this.getAlpha());
    this.renderArrow();

    float alpha = MathUtil.clamp(
        (Bliss.instance().platform().milliTime() - this.fadeStartTimeMs) / 100.0F, 0.0F, 1.0F);
    if (!this.expanded) {
      alpha = 1.0F - alpha;
    }

    if (alpha == 0.0F) {
      return;
    }

    for (var item : this.items) {
      Type type;
      if (item.disabled) {
        type = Type.DISABLED;
      } else if (item == this.selectedItem) {
        type = Type.HIGHLIGHTED;
      } else if (item.isMouseOver(mouseX, mouseY) || this.focusedItemIndex == item.index) {
        type = Type.HOVERED;
      } else {
        type = Type.NONE;
      }

      item.render(type, alpha * this.getAlpha());
    }
  }

  @SuppressWarnings("resource")
  private void renderArrow() {
    var xOffset =
        this.getScaledContentX() + this.getScaledContentWidth() * (1 - this.xArrowOffset);
    var yOffset =
        (this.getScaledContentY() + (this.getScaledContentHeight() - this.arrowHeight) / 2.0F);

    var canvas = this.graphicsContext.canvas();
    var scale = this.graphicsContext.scale();
    try (var paint = new Paint().setStrokeCap(PaintStrokeCap.ROUND)
        .setStrokeWidth(this.arrowLineWidth * this.graphicsContext.scale())
        .setColor(0xFFFFFFFF)
        .setMode(PaintMode.FILL)) {
      canvas.drawLine(
          xOffset * scale,
          yOffset * scale,
          (xOffset + this.arrowWidth / 2.0F) * scale,
          (yOffset + this.arrowHeight) * scale,
          paint);
      canvas.drawLine(
          (xOffset + this.arrowWidth / 2.0F) * scale,
          (yOffset + this.arrowHeight) * scale,
          (xOffset + this.arrowWidth) * scale,
          yOffset * scale,
          paint);
    }
  }

  private enum Type {

    HIGHLIGHTED, SELECTED, DISABLED, HOVERED, NONE;
  }

  public record ToggleExpandedEvent(boolean expanded) implements ViewEvent {}

  public record ItemHoverEvent(Item item) implements ViewEvent {}

  public class Item {

    private final int index;
    private final StyledText text;
    private final Runnable actionCallback;

    private boolean disabled = false;

    private TextLine textLine;

    public Item(int index, StyledText text, Runnable actionCallback) {
      this.index = index;
      this.text = text;
      this.actionCallback = actionCallback;
      this.refreshTextLine();
    }

    private void refreshTextLine() {
      var fontSet = FontStyleSet.makeEmpty();
      for (var family : DropdownView.this.getStyle().fontFamily.get()) {
        fontSet = DropdownView.this.fontManager.matchFamily(family);
        if (fontSet.count() > 0) {
          break;
        }
      }

      this.textLine = Shaper.make(DropdownView.this.fontManager).shapeLine(this.text.text(),
          new Font(fontSet.matchStyle(this.text.fontStyle()),
              DropdownView.this.getStyle().fontSize.get()
                  * DropdownView.this.graphicsContext.scale()),
          ShapingOptions.DEFAULT);
    }

    private void render(Type type, float alpha) {
      float y = this.getY();

      int backgroundColor = DropdownView.this.itemBackgroundColor;
      var textColor = Color.GRAY;

      switch (type) {
        case SELECTED:
          y = DropdownView.this.getScaledContentY();
          backgroundColor ^= 0x000000;
          backgroundColor += 128 << 24;
          textColor = Color.WHITE;
          break;
        case HIGHLIGHTED:
          backgroundColor = DropdownView.this.selectedItemBackgroundColor;
          break;
        case DISABLED:
          textColor = Color.DARK_GRAY;
          break;
        case HOVERED:
          backgroundColor = DropdownView.this.hoveredItemBackgroundColor;
          break;
        default:
          break;
      }

      this.render(DropdownView.this.getScaledContentX(), y,
          DropdownView.this.getScaledContentWidth(), DropdownView.this.getItemHeight(),
          backgroundColor, textColor.hex(), alpha);
    }

    private void render(float x, float y, float width, float height,
        int backgroundColor, int textColor, float alpha) {

      var canvas = DropdownView.this.graphicsContext.canvas();

      var scale = DropdownView.this.graphicsContext.scale();

      canvas.translate(x * scale, y * scale);
      canvas.scale(DropdownView.this.getXScale(), DropdownView.this.getYScale());

      try (var paint = new Paint()) {
        paint.setMode(PaintMode.FILL);
        paint.setColor(MathUtil.multiplyAlpha(backgroundColor, alpha));
        canvas.drawRect(Rect.makeWH(width * scale, height * scale), paint);
      }

      try (var paint = new Paint()) {
        paint.setColor(MathUtil.multiplyAlpha(this.text.color() == null
            ? DropdownView.this.getStyle().color.get().hex()
            : this.text.color().hex(), alpha));
        canvas.translate(4 * scale,
            this.textLine.getHeight() + (height * scale) / 2.0F
                - this.textLine.getXHeight() * 2.0F);
        canvas.drawTextLine(this.textLine, 0, 0, paint);
      }

      canvas.resetMatrix();
    }

    private void select() {
      if (DropdownView.this.selectedItem != this) {
        this.actionCallback.run();
        DropdownView.this.selectedItem = this;
      }
    }

    public void setDisabled(boolean disabled) {
      this.disabled = disabled;
    }

    private float getY() {
      return DropdownView.this.getScaledContentY()
          + DropdownView.this.getScaledContentHeight()
          + DropdownView.this.getItemHeight() * this.index;
    }

    private boolean isMouseOver(double mouseX, double mouseY) {
      final float y = this.getY();
      return DropdownView.this.isMouseOver(mouseX, mouseY) && mouseY >= y
          && mouseY <= y + DropdownView.this.getItemHeight();
    }
  }
}
