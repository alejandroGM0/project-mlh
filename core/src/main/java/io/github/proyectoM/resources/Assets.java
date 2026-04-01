package io.github.proyectoM.resources;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import java.util.HashMap;
import java.util.Map;

/** Centralizes shared asset loading and cached texture access. */
public final class Assets {
  private static final AssetManager MANAGER = new AssetManager();
  private static final Map<String, Texture> TEXTURE_CACHE = new HashMap<>();

  static {
    MANAGER.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
  }

  private Assets() {}

  public static void loadTexture(String path) {
    if (!MANAGER.isLoaded(path, Texture.class)) {
      MANAGER.load(path, Texture.class);
    }
  }

  public static void finishLoading() {
    MANAGER.finishLoading();
  }

  public static Texture getTexture(String path) {
    Texture cachedTexture = TEXTURE_CACHE.get(path);
    if (cachedTexture != null) {
      return cachedTexture;
    }

    ensureTextureLoaded(path);
    Texture texture = MANAGER.get(path, Texture.class);
    TEXTURE_CACHE.put(path, texture);
    return texture;
  }

  public static AssetManager getManager() {
    return MANAGER;
  }

  public static void dispose() {
    MANAGER.dispose();
    TEXTURE_CACHE.clear();
  }

  private static void ensureTextureLoaded(String path) {
    if (MANAGER.isLoaded(path, Texture.class)) {
      return;
    }

    MANAGER.load(path, Texture.class);
    MANAGER.finishLoadingAsset(path);
  }
}
