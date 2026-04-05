"""
generate_map.py

Generates a new isometric TMX map file for ProyectoM.
Uses the existing IsometricRural tileset with varied terrain,
decorations, lights, and collision objects.

Proyecto: ProyectoM
Autor: AlejandroGM0
Fecha: 2026-03-28
"""

import random
import math
import os

random.seed(42)

MAP_WIDTH = 100
MAP_HEIGHT = 100
TILE_WIDTH = 128
TILE_HEIGHT = 64

GROUND_A2_N = 558
GROUND_A1_E = 553
GROUND_B3_W = 628
GROUND_B5_W = 636
GROUND_C3_E = 681

FLORA_B8_N = 250
FLORA_B8_S = 251
FLORA_B8_W = 252
FLORA_B9_S = 255
FLORA_B11_W = 264
FLORA_B17_W = 288
OBJECT23_W = 900
TREE_A3_N = 1240
WINDMILL_N = 1835
WINDMILL_W = 1838
BOAT1_N = 2
CAR9_N = 58

ALL_FLORA = [FLORA_B8_N, FLORA_B8_S, FLORA_B8_W, FLORA_B9_S, FLORA_B11_W, FLORA_B17_W]
ALL_GROUND = [GROUND_A2_N, GROUND_A1_E, GROUND_B3_W, GROUND_B5_W, GROUND_C3_E]


def distance(x1, y1, x2, y2):
    return math.sqrt((x1 - x2) ** 2 + (y1 - y2) ** 2)


def generate_base_layer():
    layer = [[GROUND_A2_N] * MAP_WIDTH for _ in range(MAP_HEIGHT)]

    for y in range(MAP_HEIGHT):
        for x in range(MAP_WIDTH):
            if 45 <= x <= 55:
                layer[y][x] = GROUND_A1_E
            if 45 <= y <= 55:
                layer[y][x] = GROUND_A1_E

    for y in range(MAP_HEIGHT):
        for x in range(MAP_WIDTH):
            d = distance(x, y, 50, 50)
            if d < 8:
                layer[y][x] = GROUND_C3_E

    for y in range(MAP_HEIGHT):
        for x in range(MAP_WIDTH):
            d = distance(x, y, 20, 20)
            if d < 12:
                layer[y][x] = GROUND_B3_W

    for y in range(MAP_HEIGHT):
        for x in range(MAP_WIDTH):
            d = distance(x, y, 80, 25)
            if d < 10:
                layer[y][x] = GROUND_B5_W

    for y in range(MAP_HEIGHT):
        for x in range(MAP_WIDTH):
            d = distance(x, y, 75, 75)
            if d < 15:
                layer[y][x] = GROUND_B3_W

    for y in range(MAP_HEIGHT):
        for x in range(MAP_WIDTH):
            d = distance(x, y, 25, 80)
            if d < 8:
                layer[y][x] = GROUND_B5_W

    for y in range(MAP_HEIGHT):
        for x in range(MAP_WIDTH):
            if layer[y][x] == GROUND_A2_N and random.random() < 0.03:
                layer[y][x] = random.choice([GROUND_A1_E, GROUND_B3_W])

    return layer


def generate_decoration_layer(base_layer):
    layer = [[0] * MAP_WIDTH for _ in range(MAP_HEIGHT)]

    tree_clusters = [
        (10, 10, 6, 15),
        (85, 10, 5, 10),
        (10, 85, 5, 8),
        (90, 90, 4, 7),
        (35, 15, 4, 6),
        (65, 85, 5, 8),
        (15, 55, 3, 5),
        (85, 55, 4, 6),
    ]

    for cx, cy, radius, count in tree_clusters:
        placed = 0
        attempts = 0
        while placed < count and attempts < count * 5:
            tx = cx + random.randint(-radius, radius)
            ty = cy + random.randint(-radius, radius)
            if 1 <= tx < MAP_WIDTH - 1 and 1 <= ty < MAP_HEIGHT - 1:
                if layer[ty][tx] == 0 and distance(tx, ty, cx, cy) <= radius:
                    layer[ty][tx] = TREE_A3_N
                    placed += 1
            attempts += 1

    for y in range(MAP_HEIGHT):
        for x in range(MAP_WIDTH):
            if layer[y][x] == 0 and random.random() < 0.025:
                if base_layer[y][x] == GROUND_A2_N:
                    if distance(x, y, 50, 50) > 12:
                        layer[y][x] = random.choice(ALL_FLORA)

    for y in range(MAP_HEIGHT):
        for x in range(MAP_WIDTH):
            if layer[y][x] == 0 and base_layer[y][x] in [GROUND_B3_W, GROUND_B5_W]:
                if random.random() < 0.06:
                    layer[y][x] = random.choice(ALL_FLORA)

    windmill_positions = [(30, 30), (70, 30), (50, 70), (20, 60)]
    for wx, wy in windmill_positions:
        if layer[wy][wx] == 0:
            layer[wy][wx] = random.choice([WINDMILL_N, WINDMILL_W])

    object_positions = [
        (40, 40), (60, 40), (40, 60), (60, 60),
        (50, 35), (50, 65), (35, 50), (65, 50),
        (25, 25), (75, 25), (25, 75), (75, 75),
        (15, 45), (85, 45), (45, 15), (45, 85),
    ]
    for ox, oy in object_positions:
        if layer[oy][ox] == 0:
            layer[oy][ox] = OBJECT23_W

    car_positions = [(48, 50), (52, 50), (50, 48), (50, 52), (46, 46), (54, 54)]
    for cx, cy in car_positions:
        if layer[cy][cx] == 0 and base_layer[cy][cx] == GROUND_A1_E:
            layer[cy][cx] = CAR9_N

    boat_positions = [(18, 22), (22, 18), (82, 27)]
    for bx, by in boat_positions:
        if layer[by][bx] == 0:
            layer[by][bx] = BOAT1_N

    for y in range(MAP_HEIGHT):
        for x in range(MAP_WIDTH):
            if (x <= 2 or x >= MAP_WIDTH - 3 or y <= 2 or y >= MAP_HEIGHT - 3):
                if layer[y][x] == 0 and random.random() < 0.15:
                    layer[y][x] = random.choice([TREE_A3_N] + ALL_FLORA)

    return layer


def layer_to_csv(layer):
    rows = []
    for y in range(MAP_HEIGHT):
        row = ",".join(str(layer[y][x]) for x in range(MAP_WIDTH))
        rows.append(row)
    return ",\n".join(rows)


def generate_lights():
    lights = []
    obj_id = 1

    center_lights = [
        (50, 50), (48, 48), (52, 48), (48, 52), (52, 52),
    ]
    for lx, ly in center_lights:
        iso_x = (lx - ly) * (TILE_WIDTH / 2) + (MAP_WIDTH * TILE_WIDTH / 2)
        iso_y = (lx + ly) * (TILE_HEIGHT / 2)
        lights.append(f'  <object id="{obj_id}" x="{iso_x:.0f}" y="{iso_y:.0f}"/>')
        obj_id += 1

    path_lights = [
        (50, 30), (50, 40), (50, 60), (50, 70),
        (30, 50), (40, 50), (60, 50), (70, 50),
    ]
    for lx, ly in path_lights:
        iso_x = (lx - ly) * (TILE_WIDTH / 2) + (MAP_WIDTH * TILE_WIDTH / 2)
        iso_y = (lx + ly) * (TILE_HEIGHT / 2)
        lights.append(f'  <object id="{obj_id}" x="{iso_x:.0f}" y="{iso_y:.0f}"/>')
        obj_id += 1

    windmill_light_positions = [(30, 30), (70, 30), (50, 70), (20, 60)]
    for lx, ly in windmill_light_positions:
        iso_x = (lx - ly) * (TILE_WIDTH / 2) + (MAP_WIDTH * TILE_WIDTH / 2)
        iso_y = (lx + ly) * (TILE_HEIGHT / 2)
        lights.append(f'  <object id="{obj_id}" x="{iso_x:.0f}" y="{iso_y:.0f}"/>')
        obj_id += 1

    return lights, obj_id


def generate_collisions():
    collisions = []
    obj_id = 100

    collision_rects = [
        (0, 0, 200, 100),
        (MAP_WIDTH * TILE_WIDTH - 200, 0, 200, 100),
        (0, MAP_HEIGHT * TILE_HEIGHT - 100, 200, 100),
        (MAP_WIDTH * TILE_WIDTH - 200, MAP_HEIGHT * TILE_HEIGHT - 100, 200, 100),
    ]

    for cx, cy, cw, ch in collision_rects:
        collisions.append(
            f'  <object id="{obj_id}" x="{cx}" y="{cy}" width="{cw}" height="{ch}"/>'
        )
        obj_id += 1

    border_walls = [
        (0, 0, MAP_WIDTH * TILE_WIDTH, 50),
        (0, MAP_HEIGHT * TILE_HEIGHT - 50, MAP_WIDTH * TILE_WIDTH, 50),
        (0, 0, 50, MAP_HEIGHT * TILE_HEIGHT),
        (MAP_WIDTH * TILE_WIDTH - 50, 0, 50, MAP_HEIGHT * TILE_HEIGHT),
    ]

    for bx, by, bw, bh in border_walls:
        collisions.append(
            f'  <object id="{obj_id}" x="{bx}" y="{by}" width="{bw}" height="{bh}"/>'
        )
        obj_id += 1

    return collisions, obj_id


def generate_tmx():
    base_layer = generate_base_layer()
    decoration_layer = generate_decoration_layer(base_layer)

    base_csv = layer_to_csv(base_layer)
    deco_csv = layer_to_csv(decoration_layer)

    lights, next_id = generate_lights()
    collisions, next_id = generate_collisions()

    lights_str = "\n".join(lights)
    collisions_str = "\n".join(collisions)

    tmx = f'''<?xml version="1.0" encoding="UTF-8"?>
<map version="1.10" tiledversion="1.11.2" orientation="isometric" renderorder="right-down" width="{MAP_WIDTH}" height="{MAP_HEIGHT}" tilewidth="{TILE_WIDTH}" tileheight="{TILE_HEIGHT}" infinite="0" nextlayerid="7" nextobjectid="{next_id + 1}">
 <properties>
  <property name="ambient" type="float" value="0.7"/>
 </properties>
 <tileset firstgid="1" name="IsometricRural" tilewidth="546" tileheight="512" tilecount="17" columns="0">
  <grid orientation="orthogonal" width="1" height="1"/>
  <tile id="1">
   <image source="Isometric Tiles/Boat1_N.png" width="256" height="512"/>
  </tile>
  <tile id="57">
   <image source="Isometric Tiles/Car9_N.png" width="256" height="512"/>
   <objectgroup draworder="index" id="2">
    <object id="1" x="45.4058" y="303.035" width="156.305" height="144.805"/>
   </objectgroup>
  </tile>
  <tile id="249">
   <image source="Isometric Tiles/Flora B8_N.png" width="128" height="256"/>
  </tile>
  <tile id="250">
   <image source="Isometric Tiles/Flora B8_S.png" width="128" height="256"/>
  </tile>
  <tile id="251">
   <image source="Isometric Tiles/Flora B8_W.png" width="128" height="256"/>
  </tile>
  <tile id="254">
   <image source="Isometric Tiles/Flora B9_S.png" width="128" height="256"/>
  </tile>
  <tile id="263">
   <image source="Isometric Tiles/Flora B11_W.png" width="128" height="256"/>
  </tile>
  <tile id="287">
   <image source="Isometric Tiles/Flora B17_W.png" width="128" height="256"/>
  </tile>
  <tile id="552">
   <image source="Isometric Tiles/Ground A1_E.png" width="128" height="256"/>
  </tile>
  <tile id="557">
   <image source="Isometric Tiles/Ground A2_N.png" width="128" height="256"/>
  </tile>
  <tile id="627">
   <image source="Isometric Tiles/Ground B3_W.png" width="128" height="256"/>
  </tile>
  <tile id="635">
   <image source="Isometric Tiles/Ground B5_W.png" width="128" height="256"/>
  </tile>
  <tile id="680">
   <image source="Isometric Tiles/Ground C3_E.png" width="128" height="256"/>
  </tile>
  <tile id="899">
   <image source="Isometric Tiles/Object23_W.png" width="128" height="256"/>
  </tile>
  <tile id="1239">
   <image source="Isometric Tiles/Tree A3_N.png" width="256" height="512"/>
  </tile>
  <tile id="1834">
   <image source="Isometric Tiles/Windmill_N.png" width="256" height="512"/>
  </tile>
  <tile id="1837">
   <image source="Isometric Tiles/Windmill_W.png" width="256" height="512"/>
  </tile>
 </tileset>
 <layer id="1" name="Capa de patrones 1" width="{MAP_WIDTH}" height="{MAP_HEIGHT}" offsetx="-36.6667" offsety="19.3333">
  <data encoding="csv">
{base_csv}
</data>
 </layer>
 <layer id="2" name="Capa de patrones 2" width="{MAP_WIDTH}" height="{MAP_HEIGHT}">
  <data encoding="csv">
{deco_csv}
</data>
 </layer>
 <objectgroup id="4" name="lights">
{lights_str}
 </objectgroup>
 <objectgroup id="5" name="collisions">
{collisions_str}
 </objectgroup>
 <objectgroup id="6" name="spawn">
  <object id="{next_id}" x="3200" y="3200"/>
 </objectgroup>
</map>'''

    return tmx


if __name__ == "__main__":
    tmx_content = generate_tmx()
    output_path = os.path.join(
        os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
        "proyectoM", "assets", "maps", "newIsometricMap.tmx"
    )
    with open(output_path, "w", encoding="utf-8") as f:
        f.write(tmx_content)
    print(f"Map generated: {output_path}")
