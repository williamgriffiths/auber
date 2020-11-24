package com.threecubed.auber.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer.Task;
import com.threecubed.auber.World;


public class Projectile extends GameEntity {
  CollisionActions collisionAction;
  GameEntity originEntity;

  private static final Texture texture = new Texture("projectile.png");

  public static enum CollisionActions {
    CONFUSE,
  }

  public Projectile(float x, float y, Vector2 velocity, GameEntity originEntity,
      CollisionActions action) {
    super(x, y, texture);
    collisionAction = action;
    this.originEntity = originEntity;
    this.velocity = velocity;
  }

  /**
   * Step the projectile in its target direction, execute the collision handler if it hits the
   * {@link Player}, destroy if it hits anything else.
   *
   * @param world The game world
   * */
  public void update(World world) {
    position.add(velocity);
    for (GameEntity entity : world.getEntities()) {
      if (Intersector.overlaps(entity.sprite.getBoundingRectangle(),
            sprite.getBoundingRectangle())
          && entity != originEntity && entity != this) {
        if (entity instanceof Player) {
          handleCollisionWithPlayer(world);
        } 
        world.queueEntityRemove(this);
        return;
      }
    }

    TiledMapTileLayer collisionLayer = (TiledMapTileLayer)
        World.map.getLayers().get("collision_layer");

    int[] cellCoordinates = world.navigationMesh.getTilemapCoordinates(getCenterX(), getCenterY());

    if (collisionLayer.getCell(cellCoordinates[0], cellCoordinates[1]) != null) {
      world.queueEntityRemove(this);
    }
  }

  private void handleCollisionWithPlayer(World world) {
    switch (collisionAction) {
      case CONFUSE:
        confusePlayer(world);
        break;
      default:
        break;
    }
    world.player.health -= 0.5;
  }

  private void confusePlayer(final World world) {
    world.player.confused = true;
    world.player.playerTimer.scheduleTask(new Task() {
      @Override
      public void run() {
        world.player.confused = false;
      }
    }, World.AUBER_DEBUFF_TIME);
  }
}