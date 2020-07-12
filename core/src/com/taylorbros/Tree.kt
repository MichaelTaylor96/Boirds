package com.taylorbros

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import ktx.box2d.body
import ktx.box2d.circle
import ktx.box2d.polygon

class Tree (
        position: Vector2,
        override val size: Float,
        private val world: World,
        override val pixelsPerMeter: Float,
        override val scaleFactor: Float
) : Obstacle, HasStaticSprite, Destroyable {
    override val sprite = Texture("sprites/bigTree.png")

    private val body = world.body {
        type = BodyDef.BodyType.StaticBody
        userData = this@Tree
        this.position.set(position)
        circle(radius = size)
    }

    override val position: Vector2
        get() = this.body.position

    var dead = false

    override fun die() {
        if (!dead) {
            dead = true
            world.destroyBody(this.body)
        }
    }

}