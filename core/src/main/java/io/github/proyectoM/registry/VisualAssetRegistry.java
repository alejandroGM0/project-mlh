package io.github.proyectoM.registry;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import io.github.proyectoM.animation.AnimationKey;
import io.github.proyectoM.components.entity.animation.ActionStateComponent.ActionType;
import io.github.proyectoM.components.entity.animation.MovementDirectionStateComponent.MovementType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Collects visual atlases and builds directional animations for registered entities. */
public final class VisualAssetRegistry {
  private static final int DIRECTION_COUNT = 8;
  private static final int FRAMES_PER_DIRECTION = 15;
  private static final int TOTAL_DIRECTION_FRAMES = DIRECTION_COUNT * FRAMES_PER_DIRECTION;
  private static final int MAX_VARIANTS = 10;
  private static final int THREE_DIGIT_SUFFIX_LENGTH = 3;
  private static final int FOUR_DIGIT_SUFFIX_LENGTH = 4;
  private static final int DIRECTION_DIGITS_LENGTH = 2;
  private static final int FIRST_DIRECTION_INDEX = 0;
  private static final float DEFAULT_FRAME_DURATION = 0.1f;

  private static final String ANIMATION_IDLE = "Idle";
  private static final String ANIMATION_MOVE_FORWARD = "Run";
  private static final String ANIMATION_ATTACK = "Attack";
  private static final String ANIMATION_DIE = "Die";
  private static final String ANIMATION_MOVE_BACKWARDS = "RunBackwards";
  private static final String ANIMATION_STRAFE_LEFT = "StrafeLeft";
  private static final String ANIMATION_STRAFE_RIGHT = "StrafeRight";
  private static final String ANIMATION_HURT = "TakeDamage";

  private static final Set<String> ATLAS_PATHS = new HashSet<>();
  private static final Map<String, Map<AnimationKey, Animation<TextureRegion>>> ANIMATIONS =
      new HashMap<>();

  private VisualAssetRegistry() {}

  public static void collectAndRequestAtlases(AssetManager assetManager) {
    ATLAS_PATHS.clear();
    collectAtlasPaths();
    requestAtlasLoading(assetManager);
  }

  public static void loadAllAnimations(AssetManager assetManager) {
    for (String atlasPath : ATLAS_PATHS) {
      if (!assetManager.isLoaded(atlasPath, TextureAtlas.class)) {
        continue;
      }

      TextureAtlas atlas = assetManager.get(atlasPath, TextureAtlas.class);
      loadAnimationsFromAtlas(atlasPath, atlas);
    }
  }

  public static Animation<TextureRegion> getAnimation(String atlasPath, AnimationKey key) {
    Map<AnimationKey, Animation<TextureRegion>> atlasAnimations = ANIMATIONS.get(atlasPath);
    if (atlasAnimations == null) {
      return null;
    }
    return atlasAnimations.get(key);
  }

  public static Set<String> getAllAtlasPaths() {
    return new HashSet<>(ATLAS_PATHS);
  }

  public static void clear() {
    ANIMATIONS.clear();
    ATLAS_PATHS.clear();
  }

  private static void collectAtlasPaths() {
    ATLAS_PATHS.addAll(WeaponRegistry.getInstance().getAllAtlasPaths());
    ATLAS_PATHS.addAll(CompanionRegistry.getInstance().getAllAtlasPaths());
    ATLAS_PATHS.addAll(EnemyRegistry.getInstance().getAllAtlasPaths());
  }

  private static void requestAtlasLoading(AssetManager assetManager) {
    for (String atlasPath : ATLAS_PATHS) {
      assetManager.load(atlasPath, TextureAtlas.class);
    }
  }

  private static void loadAnimationsFromAtlas(String atlasPath, TextureAtlas atlas) {
    loadMovementAnimations(
        atlasPath, atlas, MovementType.FORWARD, ANIMATION_IDLE, ANIMATION_MOVE_FORWARD);
    loadMovementAnimations(
        atlasPath, atlas, MovementType.BACKWARDS, ANIMATION_IDLE, ANIMATION_MOVE_BACKWARDS);
    loadMovementAnimations(
        atlasPath, atlas, MovementType.STRAFE_LEFT, ANIMATION_IDLE, ANIMATION_STRAFE_LEFT);
    loadMovementAnimations(
        atlasPath, atlas, MovementType.STRAFE_RIGHT, ANIMATION_IDLE, ANIMATION_STRAFE_RIGHT);
  }

  private static void loadMovementAnimations(
      String atlasPath,
      TextureAtlas atlas,
      MovementType movementType,
      String idleAnimationName,
      String moveAnimationName) {
    loadAnimationWithVariants(atlasPath, ActionType.IDLE, movementType, atlas, idleAnimationName);
    loadAnimationWithVariants(atlasPath, ActionType.MOVE, movementType, atlas, moveAnimationName);
    loadAnimationWithVariants(atlasPath, ActionType.ATTACK, movementType, atlas, ANIMATION_ATTACK);
    loadAnimationWithVariants(atlasPath, ActionType.DIE, movementType, atlas, ANIMATION_DIE);
    loadAnimationWithVariants(atlasPath, ActionType.HURT, movementType, atlas, ANIMATION_HURT);
  }

  private static void loadAnimationWithVariants(
      String atlasPath,
      ActionType actionType,
      MovementType movementType,
      TextureAtlas atlas,
      String baseName) {
    for (int variant = 0; variant < MAX_VARIANTS; variant++) {
      String animationName = variant == 0 ? baseName : baseName + variant;
      Array<TextureAtlas.AtlasRegion> regions = findRegionsByPrefix(atlas, animationName);
      if (regions.size < TOTAL_DIRECTION_FRAMES) {
        continue;
      }

      createAndRegisterAnimations(atlasPath, actionType, movementType, regions, variant);
    }
  }

  private static Array<TextureAtlas.AtlasRegion> findRegionsByPrefix(
      TextureAtlas atlas, String prefix) {
    Array<TextureAtlas.AtlasRegion> matchingRegions = new Array<>();
    for (TextureAtlas.AtlasRegion region : atlas.getRegions()) {
      if (isValidAnimationRegion(region, prefix)) {
        matchingRegions.add(region);
      }
    }

    matchingRegions.sort((first, second) -> first.name.compareTo(second.name));
    return matchingRegions;
  }

  private static boolean isValidAnimationRegion(TextureAtlas.AtlasRegion region, String prefix) {
    if (!region.name.startsWith(prefix)) {
      return false;
    }

    String suffix = region.name.substring(prefix.length());
    if (suffix.length() == THREE_DIGIT_SUFFIX_LENGTH) {
      return isValidThreeDigitSuffix(suffix);
    }
    if (suffix.length() == FOUR_DIGIT_SUFFIX_LENGTH) {
      return isValidFourDigitSuffix(suffix);
    }
    return false;
  }

  private static boolean isValidThreeDigitSuffix(String suffix) {
    if (!isNumericSuffix(suffix)) {
      return false;
    }

    int direction = suffix.charAt(FIRST_DIRECTION_INDEX) - '0';
    return direction >= 0 && direction < DIRECTION_COUNT;
  }

  private static boolean isValidFourDigitSuffix(String suffix) {
    if (!isNumericSuffix(suffix)) {
      return false;
    }

    int direction = Integer.parseInt(suffix.substring(0, DIRECTION_DIGITS_LENGTH));
    return direction >= 0 && direction < DIRECTION_COUNT;
  }

  private static boolean isNumericSuffix(String suffix) {
    for (int index = 0; index < suffix.length(); index++) {
      if (!Character.isDigit(suffix.charAt(index))) {
        return false;
      }
    }
    return true;
  }

  private static void createAndRegisterAnimations(
      String atlasPath,
      ActionType actionType,
      MovementType movementType,
      Array<TextureAtlas.AtlasRegion> regions,
      int variant) {
    TextureRegion[] allFrames = extractFrames(regions);
    for (int direction = 0; direction < DIRECTION_COUNT; direction++) {
      TextureRegion[] directionFrames = extractDirectionFrames(allFrames, direction);
      Animation<TextureRegion> animation = createAnimation(directionFrames, actionType);
      AnimationKey key = AnimationKey.get(actionType, movementType, direction, variant);
      registerAnimation(atlasPath, key, animation);
    }
  }

  private static TextureRegion[] extractFrames(Array<TextureAtlas.AtlasRegion> regions) {
    TextureRegion[] frames = new TextureRegion[regions.size];
    for (int index = 0; index < regions.size; index++) {
      frames[index] = regions.get(index);
    }
    return frames;
  }

  private static TextureRegion[] extractDirectionFrames(
      TextureRegion[] allFrames, int directionIndex) {
    int startFrame = directionIndex * FRAMES_PER_DIRECTION;
    TextureRegion[] directionFrames = new TextureRegion[FRAMES_PER_DIRECTION];
    System.arraycopy(allFrames, startFrame, directionFrames, 0, FRAMES_PER_DIRECTION);
    return directionFrames;
  }

  private static Animation<TextureRegion> createAnimation(
      TextureRegion[] frames, ActionType actionType) {
    Animation<TextureRegion> animation = new Animation<>(DEFAULT_FRAME_DURATION, frames);
    animation.setPlayMode(getPlayMode(actionType));
    return animation;
  }

  private static Animation.PlayMode getPlayMode(ActionType actionType) {
    if (actionType == ActionType.DIE
        || actionType == ActionType.ATTACK
        || actionType == ActionType.HURT) {
      return Animation.PlayMode.NORMAL;
    }
    return Animation.PlayMode.LOOP;
  }

  private static void registerAnimation(
      String atlasPath, AnimationKey key, Animation<TextureRegion> animation) {
    ANIMATIONS.computeIfAbsent(atlasPath, ignored -> new HashMap<>()).put(key, animation);
  }
}
