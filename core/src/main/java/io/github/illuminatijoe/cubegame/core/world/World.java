package io.github.illuminatijoe.cubegame.core.world;

import com.badlogic.gdx.math.Vector3;

import java.util.HashMap;
import java.util.Map;

public class World {
    private Map<Vector3, Chunk> chunkMap;

    public World() {
        chunkMap = new HashMap<>();
        Vector3 chunkPos = new Vector3(0,0,0);
        chunkMap.put(chunkPos, new Chunk(chunkPos));
    }

    public Map<Vector3, Chunk> getChunkMap() {
        return chunkMap;
    }
}
