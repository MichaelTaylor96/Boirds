package com.taylorbros

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import ktx.box2d.body
import ktx.box2d.box
import ktx.box2d.polygon

class Flame(position: Vector2, override val size: Float, world: World) : Obstacle {

    private val body = world.body {
        type = BodyDef.BodyType.KinematicBody
        this.position.set(position)
        box(width = size, height = size/2, position = Vector2(0f, -size/4))
        polygon(Vector2(-size/2, 0f),
            Vector2(-size*2/6, size/2),
            Vector2(-size/6, 0f))
        polygon(Vector2(-size/6, 0f),
                Vector2(0f, size/2),
                Vector2(size/6, 0f))
        polygon(Vector2(size/2, 0f),
                Vector2(size*2/6, size/2),
                Vector2(size/6, 0f))
    }

    override val position: Vector2
        get() = this.body.position

}