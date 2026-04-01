package io.github.proyectoM.world;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import io.github.proyectoM.physics.PhysicsConstants;

/** Creates static Box2D collisions from Tiled map object layers. */
public class MapCollisionLoader {
  private static final float COLLISION_DENSITY = 0f;
  private static final float COLLISION_FRICTION = 0.5f;
  private static final float COLLISION_RESTITUTION = 0f;
  private static final float MIN_RECTANGLE_SIZE_PIXELS = 0.1f;
  private static final float HALF_EXTENT_DIVISOR = 2f;
  private static final float STATIC_BODY_POSITION = 0f;
  private static final int COORDINATE_STRIDE = 2;
  private static final int MIN_BOX2D_VERTEX_COORDINATES = 6;
  private static final int MAX_BOX2D_VERTEX_COORDINATES = 16;
  private static final int RECTANGLE_VERTEX_COORDINATES = 8;

  private final World world;
  private final Array<Body> mapBodies = new Array<>();
  private final Array<Rectangle> collisionRectangles = new Array<>();

  public MapCollisionLoader(World world) {
    this.world = world;
  }

  public void createMapCollisions(TiledMap tiledMap) {
    clearMapCollisions();
    if (tiledMap == null) {
      return;
    }

    for (int index = 0; index < tiledMap.getLayers().getCount(); index++) {
      MapLayer layer = tiledMap.getLayers().get(index);
      if (layer.getObjects().getCount() > 0) {
        processObjectsInLayer(layer, tiledMap);
      }
    }
  }

  public Array<Rectangle> getCollisionRectangles() {
    return collisionRectangles;
  }

  public void dispose() {
    clearMapCollisions();
  }

  private void processObjectsInLayer(MapLayer layer, TiledMap map) {
    for (MapObject object : layer.getObjects()) {
      createCollisionFromObject(object, map);
    }
  }

  private void createCollisionFromObject(MapObject object, TiledMap map) {
    if (object instanceof RectangleMapObject) {
      createRectangleCollision((RectangleMapObject) object, map);
      return;
    }

    if (object instanceof PolygonMapObject) {
      createPolygonCollision((PolygonMapObject) object, map);
    }
  }

  private void clearMapCollisions() {
    if (world != null && !world.isLocked()) {
      for (Body body : mapBodies) {
        world.destroyBody(body);
      }
    }

    mapBodies.clear();
    collisionRectangles.clear();
  }

  private void createRectangleCollision(RectangleMapObject rectangleObject, TiledMap map) {
    Rectangle rect = rectangleObject.getRectangle();
    if (rect.width <= MIN_RECTANGLE_SIZE_PIXELS || rect.height <= MIN_RECTANGLE_SIZE_PIXELS) {
      return;
    }

    float[] vertices = buildRectangleVertices(rect, map);
    addCollisionBody(vertices);
  }

  private float[] buildRectangleVertices(Rectangle rectangle, TiledMap map) {
    float[] vertices = new float[RECTANGLE_VERTEX_COORDINATES];
    Vector2 firstPoint = TiledCoordinates.toIsometric(rectangle.x, rectangle.y, map);
    Vector2 secondPoint =
        TiledCoordinates.toIsometric(rectangle.x + rectangle.width, rectangle.y, map);
    Vector2 thirdPoint =
        TiledCoordinates.toIsometric(
            rectangle.x + rectangle.width, rectangle.y + rectangle.height, map);
    Vector2 fourthPoint =
        TiledCoordinates.toIsometric(rectangle.x, rectangle.y + rectangle.height, map);

    setVertex(vertices, 0, firstPoint);
    setVertex(vertices, 1, secondPoint);
    setVertex(vertices, 2, thirdPoint);
    setVertex(vertices, 3, fourthPoint);
    return vertices;
  }

  private void createPolygonCollision(PolygonMapObject polygonObject, TiledMap map) {
    float[] transformedVertices =
        convertPolygonVerticesToIsometric(polygonObject.getPolygon(), map);
    if (!isValidPolygonForBox2D(transformedVertices)) {
      return;
    }

    addCollisionBody(transformedVertices);
  }

  private float[] convertPolygonVerticesToIsometric(Polygon polygon, TiledMap map) {
    float[] originalVertices = polygon.getTransformedVertices();
    float[] transformedVertices = new float[originalVertices.length];

    for (int index = 0; index < originalVertices.length; index += COORDINATE_STRIDE) {
      Vector2 isometricPoint =
          TiledCoordinates.toIsometric(originalVertices[index], originalVertices[index + 1], map);
      transformedVertices[index] = isometricPoint.x;
      transformedVertices[index + 1] = isometricPoint.y;
    }

    return transformedVertices;
  }

  private boolean isValidPolygonForBox2D(float[] vertices) {
    return vertices.length >= MIN_BOX2D_VERTEX_COORDINATES
        && vertices.length <= MAX_BOX2D_VERTEX_COORDINATES;
  }

  private void addCollisionBody(float[] vertices) {
    collisionRectangles.add(calculatePolygonBoundingBox(vertices));

    Body body = createPolygonBody(vertices);
    if (body != null) {
      mapBodies.add(body);
    }
  }

  private Rectangle calculatePolygonBoundingBox(float[] vertices) {
    float minX = Float.MAX_VALUE;
    float minY = Float.MAX_VALUE;
    float maxX = -Float.MAX_VALUE;
    float maxY = -Float.MAX_VALUE;

    for (int index = 0; index < vertices.length; index += COORDINATE_STRIDE) {
      minX = Math.min(minX, vertices[index]);
      minY = Math.min(minY, vertices[index + 1]);
      maxX = Math.max(maxX, vertices[index]);
      maxY = Math.max(maxY, vertices[index + 1]);
    }

    return new Rectangle(minX, minY, maxX - minX, maxY - minY);
  }

  private Body createPolygonBody(float[] vertices) {
    if (world == null) {
      return null;
    }

    float[] metersVertices = convertVerticesToMeters(vertices);
    ensureCounterClockwise(metersVertices);

    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.StaticBody;
    bodyDef.position.set(STATIC_BODY_POSITION, STATIC_BODY_POSITION);

    Body body = world.createBody(bodyDef);
    PolygonShape shape = new PolygonShape();
    try {
      shape.set(metersVertices);
    } catch (RuntimeException exception) {
      world.destroyBody(body);
      shape.dispose();
      return null;
    }

    body.createFixture(createFixtureDef(shape));
    shape.dispose();
    return body;
  }

  private FixtureDef createFixtureDef(Shape shape) {
    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = shape;
    fixtureDef.density = COLLISION_DENSITY;
    fixtureDef.friction = COLLISION_FRICTION;
    fixtureDef.restitution = COLLISION_RESTITUTION;
    return fixtureDef;
  }

  private void ensureCounterClockwise(float[] vertices) {
    if (vertices.length < MIN_BOX2D_VERTEX_COORDINATES) {
      return;
    }

    float signedArea = 0f;
    for (int index = 0; index < vertices.length; index += COORDINATE_STRIDE) {
      int nextIndex = (index + COORDINATE_STRIDE) % vertices.length;
      float currentX = vertices[index];
      float currentY = vertices[index + 1];
      float nextX = vertices[nextIndex];
      float nextY = vertices[nextIndex + 1];
      signedArea += (nextX - currentX) * (nextY + currentY);
    }

    if (signedArea > 0f) {
      reverseVertexOrder(vertices);
    }
  }

  private void reverseVertexOrder(float[] vertices) {
    int vertexCount = vertices.length / COORDINATE_STRIDE;
    for (int vertexIndex = 0; vertexIndex < vertexCount / HALF_EXTENT_DIVISOR; vertexIndex++) {
      int leftIndex = vertexIndex * COORDINATE_STRIDE;
      int rightIndex = vertices.length - COORDINATE_STRIDE - leftIndex;
      swapVertex(vertices, leftIndex, rightIndex);
    }
  }

  private void swapVertex(float[] vertices, int leftIndex, int rightIndex) {
    float leftX = vertices[leftIndex];
    float leftY = vertices[leftIndex + 1];
    vertices[leftIndex] = vertices[rightIndex];
    vertices[leftIndex + 1] = vertices[rightIndex + 1];
    vertices[rightIndex] = leftX;
    vertices[rightIndex + 1] = leftY;
  }

  private float[] convertVerticesToMeters(float[] vertices) {
    float[] metersVertices = new float[vertices.length];
    for (int index = 0; index < vertices.length; index += COORDINATE_STRIDE) {
      metersVertices[index] = vertices[index] * PhysicsConstants.METERS_PER_PIXEL;
      metersVertices[index + 1] = vertices[index + 1] * PhysicsConstants.METERS_PER_PIXEL;
    }
    return metersVertices;
  }

  private void setVertex(float[] vertices, int vertexIndex, Vector2 point) {
    int baseIndex = vertexIndex * COORDINATE_STRIDE;
    vertices[baseIndex] = point.x;
    vertices[baseIndex + 1] = point.y;
  }
}
