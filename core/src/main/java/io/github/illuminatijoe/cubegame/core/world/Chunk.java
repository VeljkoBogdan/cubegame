package io.github.illuminatijoe.cubegame.core.world;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import io.github.illuminatijoe.cubegame.Main;
import io.github.illuminatijoe.cubegame.core.Constants;
import io.github.illuminatijoe.cubegame.core.Direction;
import io.github.illuminatijoe.cubegame.core.utils.OpenSimplex2S;
import io.github.illuminatijoe.cubegame.core.utils.Vector3i;
import io.github.illuminatijoe.cubegame.core.world.block.Block;

import java.util.HashMap;
import java.util.Map;

public class Chunk {
    private final Map<Vector3i, Block> blockMap;
    private final Vector3 chunkPos;
    private BoundingBox boundingBox;

    private Model model;
    private ModelInstance mesh;

    public Chunk(Vector3i chunkPos) {
        this.chunkPos = new Vector3(chunkPos.x, chunkPos.y, chunkPos.z);
        blockMap = new HashMap<>();

        float frequency = 0.01f;
        int maxHeight = Constants.CHUNK_SIZE * 2;

        for (int x = 0; x < Constants.CHUNK_SIZE; x++) {
            for (int z = 0; z < Constants.CHUNK_SIZE; z++) {
                // Convert world position
                float worldX = x + chunkPos.x * Constants.CHUNK_SIZE;
                float worldZ = z + chunkPos.z * Constants.CHUNK_SIZE;

                // Use 2D noise to generate terrain height
                float rawNoise = OpenSimplex2S.noise2(World.seed, worldX * frequency, worldZ * frequency);
                rawNoise = (rawNoise + 1f) / 2f; // normalize to [0, 1]
                int height = (int)(rawNoise * maxHeight);

                for (int y = 0; y < Constants.CHUNK_SIZE; y++) {
                    float worldY = y + chunkPos.y * Constants.CHUNK_SIZE;
                    if (worldY <= height) {
                        blockMap.put(new Vector3i(x, y, z), new Block());
                    }
                }
            }
        }

        // build mesh
        ModelInstance translatedMesh = buildMesh();
        Vector3i vecInt = chunkPos.scl(Constants.CHUNK_SIZE);
        Vector3 vecFloat = new Vector3(vecInt.x, vecInt.y, vecInt.z);
        translatedMesh.transform.translate(vecFloat);
        mesh = translatedMesh;

        boundingBox = buildBoundingBox();
    }

    private BoundingBox buildBoundingBox() {
        Vector3 min = new Vector3(
            chunkPos.x * Constants.CHUNK_SIZE,
            chunkPos.y * Constants.CHUNK_SIZE,
            chunkPos.z * Constants.CHUNK_SIZE
        );
        Vector3 max = new Vector3(
            min.x + Constants.CHUNK_SIZE,
            min.y + Constants.CHUNK_SIZE,
            min.z + Constants.CHUNK_SIZE
        );
        return new BoundingBox(min, max);
    }

    public Map<Vector3i, Block> getBlockMap() {
        return blockMap;
    }

    public ModelInstance buildMesh() {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("chunk",
            GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            new Material(TextureAttribute.createDiffuse(Main.dirtTexture))
        );

        for (int x = 0; x < Constants.CHUNK_SIZE; x++) {
            for (int y = 0; y < Constants.CHUNK_SIZE; y++) {
                for (int z = 0; z < Constants.CHUNK_SIZE; z++) {
                    Vector3i pos = new Vector3i(x, y, z);
                    Block block = blockMap.get(pos);
                    if (block == null) continue;

                    for (Direction dir : Direction.values()) {
                        Vector3i neighborPos = new Vector3i(
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

        model = modelBuilder.end();
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

    public void dispose() {
        model.dispose();
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public Vector3 getChunkPos() {
        return chunkPos;
    }
}
