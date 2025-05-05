package io.github.illuminatijoe.cubegame.core.world.block;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;

public class Block {
    private ModelInstance modelInstance;

    public Block(Model model) {
        modelInstance = new ModelInstance(model);
    }

    public ModelInstance getModelInstance() {
        return modelInstance;
    }

    public boolean isTransparent() {
        return false;
    }
}
