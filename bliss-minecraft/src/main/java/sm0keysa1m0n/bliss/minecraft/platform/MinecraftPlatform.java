package sm0keysa1m0n.bliss.minecraft.platform;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import sm0keysa1m0n.bliss.platform.Platform;
import sm0keysa1m0n.bliss.style.parser.value.ValueParserRegistry;

public class MinecraftPlatform implements Platform {

  static {
    ValueParserRegistry.instance().registerParser(ResourceLocation.class,
        ResourceLocationParser::parse);
  }

  private final Minecraft minecraft;

  public MinecraftPlatform(Minecraft minecraft) {
    this.minecraft = minecraft;
  }

  @Override
  public CompletableFuture<Void> submitToMainThread(Runnable task) {
    return this.minecraft.submit(task);
  }

  @Override
  public <T> CompletableFuture<T> submitToMainThread(Supplier<T> task) {
    return this.minecraft.submit(task);
  }

  @Override
  public boolean isMainThread() {
    return this.minecraft.isSameThread();
  }

  @Override
  public Executor mainExecutor() {
    return this.minecraft;
  }

  @Override
  public Executor backgroundExecutor() {
    return Util.backgroundExecutor();
  }

  @Override
  public String getClipboard() {
    return this.minecraft.keyboardHandler.getClipboard();
  }

  @Override
  public void setClipboard(String value) {
    this.minecraft.keyboardHandler.setClipboard(value);
  }

  @Override
  public long milliTime() {
    return Util.getMillis();
  }

  @Override
  public int ticksPerSecond() {
    return SharedConstants.TICKS_PER_SECOND;
  }

  @Override
  public boolean hasControlDown() {
    return Screen.hasControlDown();
  }

  @Override
  public boolean hasShiftDown() {
    return Screen.hasShiftDown();
  }

  @Override
  public boolean hasAltDown() {
    return Screen.hasAltDown();
  }
}
