package io.github.proyectoM.systems.companion.movement;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import io.github.proyectoM.components.companion.GroupControllerComponent.FormationType;
import io.github.proyectoM.components.companion.SquadMovementComponent;
import java.util.List;

/** Computes formation offsets for squad members based on the current formation type. */
final class FormationCalculator {
  private FormationCalculator() {}

  /**
   * Recalculates the {@link SquadMovementComponent#formationOffset} for every member in the list.
   *
   * @param formation the current formation type
   * @param spacing distance between members
   * @param members the list of squad member entities
   */
  static void recalculate(FormationType formation, float spacing, List<Entity> members) {
    ComponentMapper<SquadMovementComponent> mapper =
        ComponentMapper.getFor(SquadMovementComponent.class);

    for (int i = 0; i < members.size(); i++) {
      SquadMovementComponent movement = mapper.get(members.get(i));
      if (movement == null) {
        continue;
      }
      movement.memberIndex = i;
      computeOffset(formation, spacing, i, members.size(), movement);
    }
  }

  private static void computeOffset(
      FormationType formation,
      float spacing,
      int index,
      int total,
      SquadMovementComponent movement) {
    switch (formation) {
      case LINE:
        computeLineOffset(spacing, index, total, movement);
        break;
      case COLUMN:
        computeColumnOffset(spacing, index, movement);
        break;
      case CIRCLE:
        computeCircleOffset(spacing, index, total, movement);
        break;
      case V_FORMATION:
        computeVOffset(spacing, index, movement);
        break;
      case SQUARE:
        computeSquareOffset(spacing, index, total, movement);
        break;
      default:
        computeLineOffset(spacing, index, total, movement);
        break;
    }
  }

  private static void computeLineOffset(
      float spacing, int index, int total, SquadMovementComponent movement) {
    float totalWidth = (total - 1) * spacing;
    movement.formationOffset.set(index * spacing - totalWidth / 2f, 0f);
  }

  private static void computeColumnOffset(
      float spacing, int index, SquadMovementComponent movement) {
    movement.formationOffset.set(0f, -index * spacing);
  }

  private static void computeCircleOffset(
      float spacing, int index, int total, SquadMovementComponent movement) {
    float angle = (float) (2 * Math.PI * index / Math.max(1, total));
    movement.formationOffset.set(
        (float) Math.cos(angle) * spacing, (float) Math.sin(angle) * spacing);
  }

  private static void computeVOffset(float spacing, int index, SquadMovementComponent movement) {
    int side = (index % 2 == 0) ? 1 : -1;
    int row = (index + 1) / 2;
    movement.formationOffset.set(side * row * spacing, -row * spacing);
  }

  private static void computeSquareOffset(
      float spacing, int index, int total, SquadMovementComponent movement) {
    int side = (int) Math.ceil(Math.sqrt(total));
    int row = index / side;
    int col = index % side;
    float totalWidth = (side - 1) * spacing;
    movement.formationOffset.set(col * spacing - totalWidth / 2f, -row * spacing);
  }
}
