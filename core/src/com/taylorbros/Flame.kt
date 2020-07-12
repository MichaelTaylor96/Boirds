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
import ktx.box2d.polygon

class Flame(
        position: Vector2,
        override val size: Float,
        world: World,
        override val pixelsPerMeter: Float,
        override val scaleFactor: Float
) : Obstacle, Animatable {

    override val animations: MutableMap<String, Animation<TextureRegion>> = mutableMapOf()
    override var currentAnimation = ""
    override var loop = true
    override var elapsedTime = 0f

    init {
        var animNames = listOf("flame")
        var fileNames = listOf("fire.png")
        for ((index, name) in animNames.withIndex()) {
            var img = Texture("sprites/${fileNames[index]}")
            var tmpFrames = TextureRegion.split(img, 32, 32)
            var animationFrames = Array<TextureRegion>(tmpFrames[0])
            var animation = Animation<TextureRegion>(0.125f, animationFrames)
            animations[name] = animation
        }
        currentAnimation = animNames[0]
    }

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