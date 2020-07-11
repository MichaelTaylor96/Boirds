package com.taylorbros

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import ktx.box2d.body
import ktx.box2d.circle
import ktx.box2d.polygon

class Tornado(position: Vector2, override val size: Float, world: World) : Obstacle {

    private val body = world.body {
        type = BodyDef.BodyType.KinematicBody
        this.position.set(position)
        circle(radius = size)
        polygon(Vector2(-size, 0f),
                Vector2(size, 0f),
                Vector2(0f, -size*3/2))
    }

    override val position: Vector2
        get() = this.body.position
}