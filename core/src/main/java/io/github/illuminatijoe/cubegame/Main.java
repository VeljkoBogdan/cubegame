package io.github.illuminatijoe.cubegame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.illuminatijoe.cubegame.core.Constants;
import io.github.illuminatijoe.cubegame.core.Player;
import io.github.illuminatijoe.cubegame.core.world.Chunk;
import io.github.illuminatijoe.cubegame.core.world.World;
import io.github.illuminatijoe.cubegame.core.world.block.Block;

import java.util.ArrayList;
import java.util.List;

public class Main extends ApplicationAdapter {
    private ModelBatch batch;
    public static Model blockModel;
    private Environment ambientLight;
    private ModelCache modelCache;

    private World world;

    private Player player;

    private BitmapFont font;
    private SpriteBatch spriteBatch;

    @Override
    public void create() {
        modelCache = new ModelCache();

        font = new BitmapFont();
        spriteBatch = new SpriteBatch();
        player = new Player();

        ambientLight = new Environment();
        ambientLight.set(ColorAttribute.createAmbientLight(0.4f, 0.4f, 0.4f, 1f));
        ambientLight.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        batch = new ModelBatch();

        ModelBuilder modelBuilder = new ModelBuilder();
        blockModel = modelBuilder.createBox(Constants.BLOCK_SIZE, Constants.BLOCK_SIZE, Constants.BLOCK_SIZE,
            new Material(ColorAttribute.createDiffuse(Color.WHITE)),
            VertexAttributes.Usage.Normal | VertexAttributes.Usage.Position);

        world = new World();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0f, 0f, 0f, 1f, true);

        float delta = Gdx.graphics.getDeltaTime();

        player.update(delta);

        modelCache.begin();
        for (Chunk chunk : world.getChunkMap().values()) {
            modelCache.add(chunk.getMesh());
        }
        modelCache.end();

        batch.begin(player.getCamera());
//            Gdx.gl.glEnable(GL20.GL_CULL_FACE);
//            Gdx.gl.glCullFace(GL20.GL_BACK);
//            Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
//            Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);

            batch.render(modelCache, ambientLight);
        batch.end();

        spriteBatch.begin();
            font.draw(spriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, Gdx.graphics.getHeight() - 20);
            font.draw(spriteBatch, "Heap: " + Gdx.app.getJavaHeap() / 1_000_000 + " MB", 10, Gdx.graphics.getHeight() - 40);
        spriteBatch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        blockModel.dispose();
        font.dispose();
        spriteBatch.dispose();
        modelCache.dispose();
    }
}
