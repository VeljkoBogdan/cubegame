package io.github.illuminatijoe.cubegame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL31;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.illuminatijoe.cubegame.core.Constants;
import io.github.illuminatijoe.cubegame.core.Player;
import io.github.illuminatijoe.cubegame.core.world.Chunk;
import io.github.illuminatijoe.cubegame.core.world.World;

public class Main extends ApplicationAdapter {
    private ModelBatch batch;
    public static Model blockModel;
    private Environment ambientLight;
    private ModelCache modelCache;

    private World world;

    private Player player;
    public static int RENDER_DISTANCE = 12;

    private BitmapFont font;
    private SpriteBatch spriteBatch;

    public static Texture dirtTexture; // temp

    @Override
    public void create() {
        dirtTexture = new Texture(Gdx.files.internal("textures/blocks/dirt.png"));

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
            new Material(TextureAttribute.createDiffuse(dirtTexture)),
            VertexAttributes.Usage.Normal | VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates);

        world = new World();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0f, 0f, 0f, 1f, true);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glCullFace(GL20.GL_FRONT_AND_BACK);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);

        float delta = Gdx.graphics.getDeltaTime();

        player.update(delta);

        batch.begin(player.getCamera());
            for (Chunk chunk : world.getChunkMap().values()) {
                //if (chunk.getChunkPos().dst(player.getChunkPosition()) > RENDER_DISTANCE) continue;

                //BoundingBox chunkBounds = chunk.getBoundingBox();
                //if (player.getCamera().frustum.boundsInFrustum(chunkBounds)) {
                    batch.render(chunk.getMesh(), ambientLight);
                //}
            }
        batch.end();

//        Gdx.gl.glDisable(GL20.GL_CULL_FACE);
//        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
//        spriteBatch.begin();
//            font.draw(spriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, Gdx.graphics.getHeight() - 20);
//            font.draw(spriteBatch, "Heap: " + Gdx.app.getJavaHeap() / 1_000_000 + " MB", 10, Gdx.graphics.getHeight() - 40);
//        spriteBatch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        int error = Gdx.gl.glGetError();
        if (error != GL20.GL_NO_ERROR) {
            System.out.println("OpenGL error: " + error);
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        blockModel.dispose();
        font.dispose();
        spriteBatch.dispose();
        modelCache.dispose();
        dirtTexture.dispose();

        for (Chunk chunk : world.getChunkMap().values()) {
            chunk.dispose();
        }
    }
}
