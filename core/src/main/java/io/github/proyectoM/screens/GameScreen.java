package io.github.proyectoM.screens;

import box2dLight.RayHandler;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.github.proyectoM.Main;
import io.github.proyectoM.audio.AudioManager;
import io.github.proyectoM.debug.DebugSystem;
import io.github.proyectoM.debug.RenderDebugSettings;
import io.github.proyectoM.factories.BulletFactory;
import io.github.proyectoM.factories.CompanionFactory;
import io.github.proyectoM.factories.EnemyFactory;
import io.github.proyectoM.factories.GroupControllerFactory;
import io.github.proyectoM.physics.PhysicsConstants;
import io.github.proyectoM.physics.PhysicsWorldProvider;
import io.github.proyectoM.registry.BulletRegistry;
import io.github.proyectoM.registry.CompanionRegistry;
import io.github.proyectoM.registry.EnemyRegistry;
import io.github.proyectoM.registry.MapRegistry;
import io.github.proyectoM.registry.WeaponRegistry;
import io.github.proyectoM.settings.GameSettings;
import io.github.proyectoM.systems.animation.ActionStateSystem;
import io.github.proyectoM.systems.animation.AnimationSelectionSystem;
import io.github.proyectoM.templates.MapTemplate;
import io.github.proyectoM.systems.animation.AnimationSystem;
import io.github.proyectoM.systems.animation.MuzzlePointUpdateSystem;
import io.github.proyectoM.systems.combat.BulletHomingSystem;
import io.github.proyectoM.systems.combat.BulletLifeSystem;
import io.github.proyectoM.systems.combat.DamageCleanupSystem;
import io.github.proyectoM.systems.combat.DamageSystem;
import io.github.proyectoM.systems.combat.DeathCleanupSystem;
import io.github.proyectoM.systems.combat.LookAtSystem;
import io.github.proyectoM.systems.combat.TargetSelectorSystem;
import io.github.proyectoM.systems.combat.weapons.MeleeWeaponSystem;
import io.github.proyectoM.systems.combat.weapons.MuzzleFlashSystem;
import io.github.proyectoM.systems.combat.weapons.RangedWeaponSystem;
import io.github.proyectoM.systems.companion.movement.SquadMovementSystem;
import io.github.proyectoM.systems.core.CameraSystem;
import io.github.proyectoM.systems.core.InputSystem;
import io.github.proyectoM.systems.core.SoundSystem;
import io.github.proyectoM.systems.enemy.EnemySpawnerSystem;
import io.github.proyectoM.systems.enemy.WaveSystem;
import io.github.proyectoM.systems.enemy.movement.EnemyMovementSystem;
import io.github.proyectoM.systems.movement.MovementDirectionStateSystem;
import io.github.proyectoM.systems.physics.GameContactListener;
import io.github.proyectoM.systems.physics.PhysicsSyncSystem;
import io.github.proyectoM.systems.physics.PhysicsSystem;
import io.github.proyectoM.systems.rendering.DustParticleSystem;
import io.github.proyectoM.systems.rendering.GameUISystem;
import io.github.proyectoM.systems.rendering.HealthBarRenderSystem;
import io.github.proyectoM.systems.rendering.LightFlickerSystem;
import io.github.proyectoM.systems.rendering.LightSystem;
import io.github.proyectoM.systems.rendering.MuzzleSmokeSystem;
import io.github.proyectoM.systems.rendering.RenderSpriteSystem;
import io.github.proyectoM.systems.rendering.RenderSystem;
import io.github.proyectoM.systems.sync.ParentSyncSystem;
import io.github.proyectoM.systems.sync.TargetSyncSystem;

/** Coordinates the main gameplay loop, ECS systems, physics, and rendering. */
public class GameScreen implements Screen {
  private static final int CAMERA_VIEWPORT_WIDTH = 1400;
  private static final int CAMERA_VIEWPORT_HEIGHT = 1000;
  private static final float PLAYER_START_X = 6400f;
  private static final float PLAYER_START_Y = 0f;
  private static final boolean DEBUG_PHYSICS = false;
  private static final float CAMERA_ZOOM_DEBUG = 2f;
  private static final float AMBIENT_LIGHT_INTENSITY = 0.02f;
  private static final int LIGHT_RAY_COUNT = 128;
  private static final int LIGHT_BLUR_PASSES = 8;
  private static final float FULL_ALPHA = 1f;

  // System priorities — lower values execute first. Groups spaced by 100 for easy insertion.
  private static final int PRIORITY_CORE = 0;
  private static final int PRIORITY_INPUT = 100;
  private static final int PRIORITY_TARGETING = 200;
  private static final int PRIORITY_PHYSICS = 300;
  private static final int PRIORITY_MOVEMENT = 400;
  private static final int PRIORITY_COMBAT = 500;
  private static final int PRIORITY_ANIMATION = 600;
  private static final int PRIORITY_DAMAGE = 700;
  private static final int PRIORITY_LIGHTING = 800;
  private static final int PRIORITY_RENDERING = 900;
  private static final int PRIORITY_UI = 1000;

  private final Main game;
  private final PooledEngine engine;
  private final SpriteBatch batch;
  private final Stage uiStage;
  private final OrthographicCamera camera;
  private final CameraSystem cameraSystem;
  private final String mapId;
  private final GameMode gameMode;

  private boolean initialized = false;

  private World world;
  private RayHandler rayHandler;
  private Box2DDebugRenderer debugRenderer;
  private ShapeRenderer shapeRenderer;
  private RenderDebugSettings renderDebugSettings;

  private GameScreenMapCoordinator mapCoordinator;
  private GameScreenDebugCoordinator debugCoordinator;
  private GameScreenRenderCoordinator renderCoordinator;
  private GameScreenStateCoordinator stateCoordinator;

  private WaveSystem waveSystem;
  private GameUISystem uiSystem;
  private InputSystem playerInputSystem;
  private CompanionFactory companionFactory;
  private EnemyFactory enemyFactory;
  private BulletFactory bulletFactory;

  public GameScreen(Main game) {
    this(game, null, null);
  }

  /**
   * Creates a gameplay screen that loads the specified map and game mode.
   *
   * @param game the main game instance
   * @param mapId registry identifier of the map to load, or null for the default map
   * @param gameMode the selected game mode, or null for default
   */
  public GameScreen(Main game, String mapId, GameMode gameMode) {
    this.game = game;
    this.engine = game.getEngine();
    this.batch = game.getBatch();
    this.uiStage = game.getGameStage();
    this.camera = new OrthographicCamera(CAMERA_VIEWPORT_WIDTH, CAMERA_VIEWPORT_HEIGHT);
    this.cameraSystem = new CameraSystem(camera);
    this.mapId = mapId;
    this.gameMode = gameMode;
  }

  @Override
  public void show() {
    if (initialized) {
      return;
    }
    initialized = true;

    uiStage.clear();
    initializePhysics();
    initializeGraphicsResources();
    initializeFactories();
    initializeCoordinators();

    resetEngineState();
    initializeGameplayState();
    initializeGameEntities();
    addSystems();
    debugCoordinator.configurePanels();
    DebugSystem.getInstance().hide();
    Gdx.input.setInputProcessor(uiStage);
  }

  private void initializePhysics() {
    this.world = PhysicsWorldProvider.getWorld();
    this.world.setContactListener(new GameContactListener(engine));
  }

  private void initializeGraphicsResources() {
    RayHandler.setGammaCorrection(true);
    RayHandler.useDiffuseLight(true);
    this.rayHandler = createRayHandler(world);
    this.shapeRenderer = new ShapeRenderer();
    this.debugRenderer = new Box2DDebugRenderer();
    this.renderDebugSettings = new RenderDebugSettings(DEBUG_PHYSICS);
  }

  private void initializeFactories() {
    WeaponRegistry weaponRegistry = WeaponRegistry.getInstance();
    this.bulletFactory = new BulletFactory(engine);
    this.companionFactory =
        new CompanionFactory(engine, world, weaponRegistry, CompanionRegistry.getInstance());
    this.enemyFactory = new EnemyFactory(engine, world, weaponRegistry);
  }

  private void initializeCoordinators() {
    this.mapCoordinator = new GameScreenMapCoordinator(world, engine, rayHandler);
    this.stateCoordinator =
        new GameScreenStateCoordinator(
            engine,
            () -> ScreenManager.getInstance().showScreen(ScreenManager.ScreenType.PAUSE),
            stats -> GameSessionManager.getInstance().showGameOver(stats));
    this.debugCoordinator =
        new GameScreenDebugCoordinator(
            engine,
            camera,
            world,
            companionFactory,
            enemyFactory,
            mapCoordinator,
            renderDebugSettings,
            DebugSystem.getInstance());
    this.renderCoordinator =
        new GameScreenRenderCoordinator(
            batch,
            uiStage,
            camera,
            engine,
            world,
            shapeRenderer,
            debugRenderer,
            rayHandler,
            renderDebugSettings,
            mapCoordinator,
            debugCoordinator);
  }

  private void resetEngineState() {
    engine.removeAllSystems();
    engine.removeAllEntities();
  }

  private void initializeGameplayState() {
    stateCoordinator.initializeGlobalStateEntity();
    mapCoordinator.initialize(mapId);
  }

  private void initializeGameEntities() {
    Vector2 spawnPoint = resolveSpawnPoint();
    GroupControllerFactory.createGroupController(engine, world, spawnPoint.x, spawnPoint.y);
    float startXMeters = spawnPoint.x * PhysicsConstants.METERS_PER_PIXEL;
    float startYMeters = spawnPoint.y * PhysicsConstants.METERS_PER_PIXEL;
    companionFactory.createCompanion("soldier", new Vector2(startXMeters, startYMeters));
  }

  /**
   * Resolves the player spawn position: reads from the map's spawn layer when available, falling
   * back to the hardcoded default center position.
   *
   * @return spawn position in world pixels
   */
  private Vector2 resolveSpawnPoint() {
    Vector2 spawnPoint = mapCoordinator.getSpawnPoint();
    if (spawnPoint != null) {
      return spawnPoint;
    }
    return new Vector2(PLAYER_START_X, PLAYER_START_Y);
  }

  private void addSystems() {
    engine.removeAllSystems();

    waveSystem = new WaveSystem();
    uiSystem = new GameUISystem(uiStage, engine);
    playerInputSystem = new InputSystem();

    addCoreSystems();
    addTargetingAndPhysicsSystems();
    addCombatAndAnimationSystems();
    addLightingAndCleanupSystems();
    addRenderingSystems();
  }

  private void addCoreSystems() {
    addSystemWithPriority(new SoundSystem(AudioManager.getInstance()), PRIORITY_CORE);
    addSystemWithPriority(waveSystem, PRIORITY_CORE + 1);
    addSystemWithPriority(
        new EnemySpawnerSystem(enemyFactory, EnemyRegistry.getInstance()), PRIORITY_CORE + 2);
    addSystemWithPriority(cameraSystem, PRIORITY_CORE + 3);
    addSystemWithPriority(playerInputSystem, PRIORITY_INPUT);
  }

  private void addTargetingAndPhysicsSystems() {
    addSystemWithPriority(new TargetSelectorSystem(), PRIORITY_TARGETING);
    addSystemWithPriority(new TargetSyncSystem(), PRIORITY_TARGETING + 1);
    addSystemWithPriority(new PhysicsSystem(world), PRIORITY_PHYSICS);
    addSystemWithPriority(new PhysicsSyncSystem(), PRIORITY_PHYSICS + 1);
    addSystemWithPriority(new LookAtSystem(), PRIORITY_PHYSICS + 2);
    addSystemWithPriority(new MovementDirectionStateSystem(), PRIORITY_MOVEMENT);
    addSystemWithPriority(new ParentSyncSystem(), PRIORITY_MOVEMENT + 1);
    addSystemWithPriority(new EnemyMovementSystem(), PRIORITY_MOVEMENT + 2);
    addSystemWithPriority(new SquadMovementSystem(), PRIORITY_MOVEMENT + 3);
  }

  private void addCombatAndAnimationSystems() {
    addSystemWithPriority(
        new RangedWeaponSystem(world, bulletFactory, BulletRegistry.getInstance()), PRIORITY_COMBAT);
    addSystemWithPriority(new MeleeWeaponSystem(), PRIORITY_COMBAT + 1);
    addSystemWithPriority(new ActionStateSystem(), PRIORITY_ANIMATION);
    addSystemWithPriority(new AnimationSelectionSystem(), PRIORITY_ANIMATION + 1);
    addSystemWithPriority(new AnimationSystem(), PRIORITY_ANIMATION + 2);
    addSystemWithPriority(new MuzzlePointUpdateSystem(WeaponRegistry.getInstance()), PRIORITY_ANIMATION + 3);
    addSystemWithPriority(new MuzzleFlashSystem(), PRIORITY_ANIMATION + 4);
    addSystemWithPriority(new BulletHomingSystem(), PRIORITY_COMBAT + 2);
    addSystemWithPriority(new BulletLifeSystem(), PRIORITY_COMBAT + 3);
  }

  private void addLightingAndCleanupSystems() {
    addSystemWithPriority(new LightFlickerSystem(), PRIORITY_LIGHTING);
    addSystemWithPriority(new LightSystem(rayHandler, LIGHT_RAY_COUNT), PRIORITY_LIGHTING + 1);
    addSystemWithPriority(new DamageSystem(), PRIORITY_DAMAGE);
    addSystemWithPriority(new DamageCleanupSystem(), PRIORITY_DAMAGE + 1);
    addSystemWithPriority(new DeathCleanupSystem(), PRIORITY_DAMAGE + 2);
  }

  private void addRenderingSystems() {
    addSystemWithPriority(new RenderSystem(batch), PRIORITY_RENDERING);
    addSystemWithPriority(new RenderSpriteSystem(batch), PRIORITY_RENDERING + 1);
    addSystemWithPriority(new HealthBarRenderSystem(batch), PRIORITY_RENDERING + 2);
    addSystemWithPriority(new DustParticleSystem(shapeRenderer, camera), PRIORITY_RENDERING + 3);
    addSystemWithPriority(new MuzzleSmokeSystem(shapeRenderer, camera), PRIORITY_RENDERING + 4);
    addSystemWithPriority(uiSystem, PRIORITY_UI);
  }

  private void addSystemWithPriority(
      com.badlogic.ashley.core.EntitySystem system, int systemPriority) {
    system.priority = systemPriority;
    engine.addSystem(system);
  }

  @Override
  public void render(float delta) {
    if (Gdx.input.isKeyJustPressed(GameSettings.getInstance().getPauseKey())) {
      stateCoordinator.showPauseScreen();
      return;
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
      mapCoordinator.reload();
    }

    if (stateCoordinator.updateGameOverTransition()) {
      return;
    }
    renderCoordinator.renderFrame(delta);
  }

  @Override
  public void resize(int width, int height) {
    camera.viewportWidth = width;
    camera.viewportHeight = height;
    camera.update();

    debugCoordinator.resize(width, height);
  }

  /** Not used in the game screen. */
  @Override
  public void pause() {}

  /** Not used in the game screen. */
  @Override
  public void resume() {}

  /** Not used in the game screen. */
  @Override
  public void hide() {}

  public PooledEngine getEngine() {
    return engine;
  }

  @Override
  public void dispose() {
    cleanupResources();
  }

  private void cleanupResources() {
    engine.removeAllSystems();
    engine.removeAllEntities();

    if (bulletFactory != null) {
      bulletFactory.dispose();
      bulletFactory = null;
    }
    if (rayHandler != null) {
      rayHandler.dispose();
      rayHandler = null;
    }
    if (mapCoordinator != null) {
      mapCoordinator.dispose();
      mapCoordinator = null;
    }
    if (debugRenderer != null) {
      debugRenderer.dispose();
      debugRenderer = null;
    }
    if (shapeRenderer != null) {
      shapeRenderer.dispose();
      shapeRenderer = null;
    }
  }

  private RayHandler createRayHandler(World physicsWorld) {
    RayHandler handler = new RayHandler(physicsWorld);
    handler.setAmbientLight(
        AMBIENT_LIGHT_INTENSITY, AMBIENT_LIGHT_INTENSITY, AMBIENT_LIGHT_INTENSITY, FULL_ALPHA);
    handler.setShadows(true);
    handler.setBlurNum(LIGHT_BLUR_PASSES);
    return handler;
  }
}
