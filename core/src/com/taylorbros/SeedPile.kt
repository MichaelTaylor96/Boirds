package com.taylorbros

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import ktx.box2d.body
import ktx.box2d.circle

class SeedPile(
        position: Vector2,
        override val size: Float,
        val world: World,
        override val pixelsPerMeter: Float,
        override val scaleFactor: Float
) : HasPosition, HasSize, HasStaticSprite {
    override val sprite = Texture("sprites/seed.png")
    var timesBeenEaten = 0

    private val body = world.body {
        type = BodyDef.BodyType.StaticBody
        this.position.set(position)
        userData = this@SeedPile
        circle(radius = size/6, position = Vector2(-size*2/6, -size/3))
        circle(radius = size/6, position = Vector2(0f, -size/3))
        circle(radius = size/6, position = Vector2(size*2/6, -size/3))
        circle(radius = size/6, position = Vector2(-size/6, 0f))
        circle(radius = size/6, position = Vector2(size/6, 0f))
    }

    override val position: Vector2
    get() = this.body.position

    var dead = false
    fun die() {
        if (!dead) {
            dead = true
            world.destroyBody(this.body)
        }
    }
}