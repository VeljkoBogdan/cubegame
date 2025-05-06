package io.github.illuminatijoe.cubegame.core.world;

import io.github.illuminatijoe.cubegame.core.utils.Vector3i;

import java.util.HashMap;
import java.util.Map;

public class World {
    private final Map<Vector3i, Chunk> chunkMap;
    public static long seed = 1158L;

    public World() {
        chunkMap = new HashMap<>();
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                for (int z = 0; z < 8; z++) {
                    Vector3i chunkPos = new Vector3i(x, y, z);
                    chunkMap.put(chunkPos, new Chunk(chunkPos));
                }
            }
        }
    }

    public Map<Vector3i, Chunk> getChunkMap() {
        return chunkMap;
    }
}
