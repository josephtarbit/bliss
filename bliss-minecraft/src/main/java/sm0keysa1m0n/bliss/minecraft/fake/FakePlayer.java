package sm0keysa1m0n.bliss.minecraft.fake;

import java.util.EnumMap;
import java.util.Map;
import com.google.common.base.MoreObjects;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;

public class FakePlayer extends AbstractClientPlayer {

  private final Map<Type, ResourceLocation> textureLocations = new EnumMap<>(Type.class);
  private boolean pendingTextures;
  private String skinModel;

  public FakePlayer(GameProfile gameProfile) {
    super(FakeLevel.getInstance(), gameProfile);
  }

  @Override
  public String getModelName() {
    return this.skinModel == null
        ? DefaultPlayerSkin.getSkinModelName(this.getGameProfile().getId())
        : this.skinModel;
  }

  @Override
  public boolean isSkinLoaded() {
    return this.textureLocations.containsKey(Type.SKIN);
  }

  @Override
  public ResourceLocation getSkinTextureLocation() {
    this.registerTextures();
    return MoreObjects.firstNonNull(this.textureLocations.get(Type.SKIN),
        DefaultPlayerSkin.getDefaultSkin(this.getGameProfile().getId()));
  }

  @Override
  public boolean isCapeLoaded() {
    return this.textureLocations.containsKey(Type.CAPE);
  }

  @Override
  public ResourceLocation getCloakTextureLocation() {
    this.registerTextures();
    return this.textureLocations.get(Type.CAPE);
  }

  @Override
  public boolean isElytraLoaded() {
    return this.textureLocations.containsKey(Type.ELYTRA);
  }

  @Override
  public ResourceLocation getElytraTextureLocation() {
    this.registerTextures();
    return this.textureLocations.get(Type.ELYTRA);
  }

  private void registerTextures() {
    synchronized (this) {
      if (!this.pendingTextures) {
        this.pendingTextures = true;
        Minecraft.getInstance().getSkinManager().registerSkins(this.getGameProfile(),
            (type, textureLocation, texture) -> {
              this.textureLocations.put(type, textureLocation);
              if (type == Type.SKIN) {
                this.skinModel = texture.getMetadata("model");
                if (this.skinModel == null) {
                  this.skinModel = "default";
                }
              }
            }, true);
      }
    }
  }

  @Override
  public Vec3 position() {
    return new Vec3(99.0D, 99.0D, 99.0D);
  }

  @Override
  protected PlayerInfo getPlayerInfo() {
    return null;
  }

  @Override
  public boolean isSpectator() {
    return false;
  }

  @Override
  public boolean isInvisibleTo(Player playerEntity) {
    return false;
  }

  @Override
  public Team getTeam() {
    return null;
  }
}
