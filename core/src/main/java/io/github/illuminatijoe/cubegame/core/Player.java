package io.github.illuminatijoe.cubegame.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;

public class Player {
    private final PerspectiveCamera camera;
    private final float moveSpeed = 10f;
    private final float mouseSensitivity = 0.2f;

    private float yaw = -90f;
    private float pitch = 0f;

    private Vector3 direction = new Vector3();
    private Vector3 right = new Vector3();
    private Vector3 up = new Vector3(0, 1, 0);

    public Player() {
        camera = new PerspectiveCamera(80, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0, 10, 0);
        camera.far = 300f;
        camera.near = 0.1f;
        updateVectors();
        camera.update();

        Gdx.input.setCursorCatched(true);
    }

    public void update(float deltaTime) {
        handleMouseLook();
        handleKeyboardInput(deltaTime);
        camera.update();
    }

    private void handleMouseLook() {
        float deltaX = Gdx.input.getDeltaX() * mouseSensitivity;
        float deltaY = -Gdx.input.getDeltaY() * mouseSensitivity;

        yaw += deltaX;
        pitch += deltaY;

        pitch = Math.max(-89f, Math.min(89f, pitch));

        updateVectors();
    }

    private void updateVectors() {
        direction.set(
            (float) Math.cos(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch)),
            (float) Math.sin(Math.toRadians(pitch)),
            (float) Math.sin(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch))
        ).nor();

        right.set(direction).crs(up).nor();
        camera.direction.set(direction);
    }

    private void handleKeyboardInput(float deltaTime) {
        float velocity = moveSpeed * deltaTime;
        Vector3 movement = new Vector3();

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            movement.x += direction.x;
            movement.z += direction.z;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            movement.x -= direction.x;
            movement.z -= direction.z;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) movement.sub(right);
        if (Gdx.input.isKeyPressed(Input.Keys.D)) movement.add(right);
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) movement.add(up);
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) movement.sub(up);

        movement.nor().scl(velocity);
        camera.position.add(movement);
    }

    public PerspectiveCamera getCamera() {
        return camera;
    }

    public Vector3 getPosition() {
        return camera.position;
    }

    public Vector3 getChunkPosition() {
        Vector3 chunkPos = new Vector3();
        chunkPos.x = Math.round(getPosition().x / Constants.CHUNK_SIZE);
        chunkPos.y = Math.round(getPosition().y / Constants.CHUNK_SIZE);
        chunkPos.z = Math.round(getPosition().z / Constants.CHUNK_SIZE);

        return chunkPos;
    }
}
