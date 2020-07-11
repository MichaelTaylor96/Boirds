package com.taylorbros

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import ktx.box2d.body
import ktx.box2d.box
import ktx.box2d.circle
import ktx.box2d.polygon

class Wolf(position: Vector2, override val size: Float, world: World) : Obstacle {

    private val body = world.body {
        type = BodyDef.BodyType.DynamicBody
        this.position.set(position)
        box(width = size, height = size/2)
        box(width = size/3, height = size/3, position = Vector2(-size/2, size/3))
        box(width = size/4, height = size/3, position = Vector2(-size/3, -size/3))
        box(width = size/4, height = size/3, position = Vector2(size/3, -size/3))
    }

    override val position: Vector2
        get() = this.body.position
}