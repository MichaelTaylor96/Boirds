package com.taylorbros

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Array
import ktx.box2d.body
import ktx.box2d.box

class Wolf(
        position: Vector2,
        override val size: Float,
        world: World, override val
        pixelsPerMeter: Float,
        override val scaleFactor: Float
) : Obstacle, Animatable {

    override val animations: MutableMap<String, Animation<TextureRegion>> = mutableMapOf()
    override var currentAnimation = ""
    override var loop = true
    override var elapsedTime = 0f

    init {
        var animNames = listOf("sleep", "alert", "run", "eat")
        var fileNames = listOf("wolfSleep.png", "wolfAlert.png", "wolfRun.png", "wolfEat.png")
        for ((index, name) in animNames.withIndex()) {
            var img = Texture("sprites/wolf/${fileNames[index]}")
            var tmpFrames = TextureRegion.split(img, 32, 32)
            var animationFrames = Array<TextureRegion>(tmpFrames[0])
            var animation = Animation<TextureRegion>(0.125f, animationFrames)
            animations[name] = animation
        }
        currentAnimation = animNames[0]
    }

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