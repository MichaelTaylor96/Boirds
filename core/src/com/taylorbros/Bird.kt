package com.taylorbros

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Array
import ktx.box2d.body
import ktx.box2d.circle
import ktx.box2d.polygon

class Bird(
        override val size: Float,
        world: World,
        initialPosition: Vector2,
        initialVelocity: Vector2,
        pixelsPerMeter: Float
    ) : Animatable, Boid {

    override val animations: MutableMap<String, Animation<TextureRegion>> = mutableMapOf()
    override var currentAnimation = ""
    override var elapsedTime = 0f
    override val spriteWidth = size * pixelsPerMeter
    override val spriteHeight = size * pixelsPerMeter

    init {
        var animNames = listOf("sleep")
        var fileNames = listOf("wolfSleep.png")
        for ((index, name) in animNames.withIndex()) {
            var img = Texture("sprites/wolf/${fileNames[index]}")
            var tmpFrames = TextureRegion.split(img, 32, 32)
            var animationFrames = Array<TextureRegion>(tmpFrames[0])
            var animation = Animation<TextureRegion>(0.1f, animationFrames)
            animations[name] = animation
        }
        currentAnimation = animNames[0]
    }

    // set up box2d physics body
    private val body = world.body {
        type = BodyDef.BodyType.DynamicBody
        position.set(initialPosition.x, initialPosition.y)
        angle = initialVelocity.angleRad()
        circle(radius = size) {
            restitution = 0.2f
            density = 10f
        }
        polygon(Vector2(0f, size),
                Vector2(size * 2, 0f),
                Vector2(0f, -size))
    }
    init {
        body.linearVelocity = initialVelocity
    }

    override val position: Vector2
        get() = this.body.position

    override val velocity: Vector2
        get() = this.body.linearVelocity
}