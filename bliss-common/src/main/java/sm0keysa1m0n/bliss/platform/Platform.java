package sm0keysa1m0n.bliss.platform;

import java.net.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import org.lwjgl.glfw.GLFW;

public interface Platform {

  default boolean joinMainThread(Runnable task) {
    if (this.isMainThread()) {
      return true;
    }
    this.submitToMainThread(task).join();
    return false;
  }

  CompletableFuture<Void> submitToMainThread(Runnable task);

  <T> CompletableFuture<T> submitToMainThread(Supplier<T> task);

  boolean isMainThread();

  Executor mainExecutor();

  Executor backgroundExecutor();

  String getClipboard();

  void setClipboard(String value);

  long milliTime();

  int ticksPerSecond();

  default Proxy proxy() {
    return Proxy.NO_PROXY;
  }

  boolean hasControlDown();

  boolean hasShiftDown();

  boolean hasAltDown();

  default boolean isCut(int key) {
    return key == GLFW.GLFW_KEY_X
        && this.hasControlDown()
        && !this.hasShiftDown()
        && !this.hasAltDown();
  }

  default boolean isPaste(int key) {
    return key == GLFW.GLFW_KEY_V
        && this.hasControlDown()
        && !this.hasShiftDown()
        && !this.hasAltDown();
  }

  default boolean isCopy(int key) {
    return key == GLFW.GLFW_KEY_C
        && this.hasControlDown()
        && !this.hasShiftDown()
        && !this.hasAltDown();
  }

  default boolean isSelectAll(int key) {
    return key == GLFW.GLFW_KEY_A
        && this.hasControlDown()
        && !this.hasShiftDown()
        && !this.hasAltDown();
  }
}
