package com.taylorbros

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import ktx.box2d.body
import ktx.box2d.box
import ktx.box2d.circle
import ktx.box2d.polygon

class LumberJack(position: Vector2, override val size: Float, world: World) : Obstacle {

    private val body = world.body {
        type = BodyDef.BodyType.StaticBody
        this.position.set(position)
        circle(radius = size/4, position = Vector2(-size/2, -size/2))
        circle(radius = size/4, position = Vector2(0f, -size/2))
        circle(radius = size*2/3, position = Vector2(size, 0f))
        polygon(Vector2(-size*2/3, -size/3),
                Vector2(-size*2/3, 0f),
                Vector2(-size*2/6, size/3),
                Vector2(size/6, size/3),
                Vector2(size/3, 0f),
                Vector2(size/3, -size/3)
                )
        box(width = size, height = size/6, position = Vector2(size/3, 0f))
    }

    override val position: Vector2
        get() = this.body.position
}