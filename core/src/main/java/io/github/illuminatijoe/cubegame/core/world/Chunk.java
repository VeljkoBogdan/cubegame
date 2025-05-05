package io.github.illuminatijoe.cubegame.core.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import io.github.illuminatijoe.cubegame.Main;
import io.github.illuminatijoe.cubegame.core.Constants;
import io.github.illuminatijoe.cubegame.core.Direction;
import io.github.illuminatijoe.cubegame.core.world.block.Block;

import java.util.HashMap;
import java.util.Map;

public class Chunk {
    private Vector3 chunkPos;
    private Map<Vector3, Block> blockMap;
    private ModelInstance mesh;

    public Chunk(Vector3 chunkPos) {
        blockMap = new HashMap<>();
        this.chunkPos = chunkPos;

        // populate the map
        for (int i = 15; i < Constants.CHUNK_SIZE; i++) {
            for (int j = 15; j < Constants.CHUNK_SIZE; j++) {
                for (int k = 15; k < Constants.CHUNK_SIZE; k++) {
                    Block block = new Block(Main.blockModel);
                    blockMap.put(new Vector3(i,j,k), block);
                }
            }
        }

        // build mesh
        mesh = buildMesh();
    }

    public Map<Vector3, Block> getBlockMap() {
        return blockMap;
    }

    public ModelInstance buildMesh() {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("chunk",
            GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
            new Material(ColorAttribute.createDiffuse(Color.WHITE))
        );

        for (int x = 0; x < Constants.CHUNK_SIZE; x++) {
            for (int y = 0; y < Constants.CHUNK_SIZE; y++) {
                for (int z = 0; z < Constants.CHUNK_SIZE; z++) {
                    Vector3 pos = new Vector3(x, y, z);
                    Block block = blockMap.get(pos);
                    if (block == null) continue;

                    for (Direction dir : Direction.values()) {
                        Vector3 neighborPos = new Vector3(
                            x + dir.dx,
                            y + dir.dy,
                            z + dir.dz
                        );
                        Block neighbor = blockMap.get(neighborPos);
                        if (neighbor == null || neighbor.isTransparent()) {
                            addFace(builder, x, y, z, dir);
                        }
                    }
                }
            }
        }

        Model model = modelBuilder.end();
        return new ModelInstance(model);
    }

    private void addFace(MeshPartBuilder builder, int x, int y, int z, Direction dir) {
        float size = 1f;
        Vector3 p = new Vector3(x, y, z);

        Vector3[] face = getFaceVertices(p, dir, size);
        Vector3 normal = getNormal(dir);

        builder.rect(face[0], face[1], face[2], face[3], normal);
    }

    private Vector3 getNormal(Direction dir) {
        return new Vector3(dir.dx, dir.dy, dir.dz).nor(); // normalize just in case
    }

    private Vector3[] getFaceVertices(Vector3 pos, Direction dir, float size) {
        float x = pos.x;
        float y = pos.y;
        float z = pos.z;

        switch (dir) {
            case UP:
                return new Vector3[] {
                    new Vector3(x,     y + size, z),
                    new Vector3(x,     y + size, z + size),
                    new Vector3(x + size, y + size, z + size),
                    new Vector3(x + size, y + size, z)
                };
            case DOWN:
                return new Vector3[] {
                    new Vector3(x,     y, z),
                    new Vector3(x + size, y, z),
                    new Vector3(x + size, y, z + size),
                    new Vector3(x,     y, z + size)
                };
            case NORTH:
                return new Vector3[] {
                    new Vector3(x,     y, z),
                    new Vector3(x,     y + size, z),
                    new Vector3(x + size, y + size, z),
                    new Vector3(x + size, y, z)
                };
            case SOUTH:
                return new Vector3[] {
                    new Vector3(x,     y, z + size),
                    new Vector3(x + size, y, z + size),
                    new Vector3(x + size, y + size, z + size),
                    new Vector3(x,     y + size, z + size)
                };
            case EAST:
                return new Vector3[] {
                    new Vector3(x + size, y, z),
                    new Vector3(x + size, y + size, z),
                    new Vector3(x + size, y + size, z + size),
                    new Vector3(x + size, y, z + size)
                };
            case WEST:
                return new Vector3[] {
                    new Vector3(x, y, z),
                    new Vector3(x, y, z + size),
                    new Vector3(x, y + size, z + size),
                    new Vector3(x, y + size, z)
                };
            default:
                return new Vector3[0];
        }
    }

    public ModelInstance getMesh() {
        return mesh;
    }
}
