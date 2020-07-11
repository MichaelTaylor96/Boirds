package com.taylorbros

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import ktx.box2d.body
import ktx.box2d.circle

class SeedPile(position: Vector2, override val size: Float, world: World) : HasPosition, HasSize {

        private val body = world.body {
            type = BodyDef.BodyType.StaticBody
            this.position.set(position)
            circle(radius = size/6, position = Vector2(-size*2/6, -size/3))
            circle(radius = size/6, position = Vector2(0f, -size/3))
            circle(radius = size/6, position = Vector2(size*2/6, -size/3))
            circle(radius = size/6, position = Vector2(-size/6, 0f))
            circle(radius = size/6, position = Vector2(size/6, 0f))
            circle(radius = size/6, position = Vector2(0f, size/3))
        }

        override val position: Vector2
        get() = this.body.position
}