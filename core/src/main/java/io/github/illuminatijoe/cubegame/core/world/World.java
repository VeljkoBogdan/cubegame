package io.github.illuminatijoe.cubegame.core.world;

import com.badlogic.gdx.math.Vector3;
import io.github.illuminatijoe.cubegame.core.utils.Vector3i;

import java.util.HashMap;
import java.util.Map;

public class World {
    private final Map<Vector3, Chunk> chunkMap;
    public static long seed = 1158L;

    public World() {
        chunkMap = new HashMap<>();
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 8; y++) {
                for (int z = 0; z < 16; z++) {
                    Vector3 chunkPos = new Vector3(x, y, z);
                    chunkMap.put(chunkPos, new Chunk(chunkPos));
                }
            }
        }
    }

    public Map<Vector3, Chunk> getChunkMap() {
        return chunkMap;
    }
}
