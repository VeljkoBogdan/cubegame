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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Chunk {
    private final Map<Vector3, Block> blockMap;
    private final Vector3 chunkPos;
    private BoundingBox boundingBox;

    private Model model;
    private final ModelInstance mesh;

    public Chunk(Vector3 chunkPos) {
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
                        blockMap.put(new Vector3(x, y, z), new Block());
                    }
                }
            }
        }

        // build mesh
        ModelInstance translatedMesh = buildMesh();
        Vector3 vecFloat = chunkPos.scl(Constants.CHUNK_SIZE);
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

    public ModelInstance buildMesh() {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
            MeshPartBuilder builder = modelBuilder.part("chunk",
                GL20.GL_TRIANGLES,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
                new Material(TextureAttribute.createDiffuse(Main.dirtTexture))
            );

            for (Direction dir : Direction.values()) {
                greedyMeshDirection(builder, dir);
            }
        model = modelBuilder.end();
        return new ModelInstance(model);
    }

    private void greedyMeshDirection(MeshPartBuilder builder, Direction dir) {
        int u = (dir.axis + 1) % 3;
        int v = (dir.axis + 2) % 3;
        int[] x = new int[3];
        int[] q = new int[3];
        q[dir.axis] = 1;

        int size = Constants.CHUNK_SIZE;

        boolean[] mask = new boolean[size * size];

        for (x[dir.axis] = -1; x[dir.axis] < size; ) {
            for (x[v] = 0; x[v] < size; ++x[v]) {
                for (x[u] = 0; x[u] < size; ++x[u]) {
                    Vector3 current = new Vector3(x[0], x[1], x[2]);
                    Vector3 neighbor = new Vector3(x[0] + q[0], x[1] + q[1], x[2] + q[2]);

                    boolean currentSolid = blockMap.containsKey(current); // x[dir.axis] >= 0 &&
                    boolean neighborSolid = blockMap.containsKey(neighbor); // x[dir.axis] < size - 1 &&

                    mask[x[u] + x[v] * size] = currentSolid != neighborSolid;
                }
            }

            x[dir.axis]++;

            for (int j = 0; j < size; ++j) {
                for (int i = 0; i < size;) {
                    int index = i + j * size;
                    if (mask[index]) {
                        // Determine width (w)
                        int w;
                        for (w = 0; i + w < size && mask[index + w]; ++w);

                        // Determine height (h)
                        int h;
                        boolean done = false;
                        for (h = 1; j + h < size; ++h) {
                            for (int k = 0; k < w; ++k) {
                                if (!mask[(i + k) + (j + h) * size]) {
                                    done = true;
                                    break;
                                }
                            }
                            if (done) break;
                        }

                        // x = corner of the quad
                        x[u] = i;
                        x[v] = j;

                        int[] du = new int[3];
                        int[] dv = new int[3];
                        du[u] = w;
                        dv[v] = h;

                        // Build quad face from corner point, extending by du and dv
                        Vector3 p = new Vector3(x[0], x[1], x[2]);

                        // Construct faces in CCW order
                        Vector3[] face;
                        if (!dir.negative) {
                            // Pos direction
                            face = getVector3s(du, dv, p, du[0], du[1], du[2], dv[0], dv[1], dv[2]);
                        } else {
                            // Neg direction
                            face = getVector3s(dv, dv, p, du[0], du[1], du[2], du[0], du[1], du[2]);
                        }

                        // Build the actual face
                        Vector3 normal = new Vector3(dir.dx, dir.dy, dir.dz);
                        normal.nor();
                        builder.rect(face[0], face[1], face[2], face[3], normal);

                        // invalidate mask
                        for (int l = 0; l < h; ++l) {
                            for (int k = 0; k < w; ++k) {
                                mask[(i + k) + (j + l) * size] = false;
                            }
                        }

                        i += w;
                    } else {
                        ++i;
                    }
                }
            }
        }
    }

    private Vector3[] getVector3s(int[] du, int[] dv, Vector3 p, int i2, int i3, int i4, int i5, int i6, int i7) {
        Vector3[] face;
        face = new Vector3[] {
            new Vector3(p),
            new Vector3(p).add(du[0], du[1], du[2]),
            new Vector3(p).add(i2 + dv[0], i3 + dv[1], i4 + dv[2]),
            new Vector3(p).add(i5, i6, i7)
        };
        return face;
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
