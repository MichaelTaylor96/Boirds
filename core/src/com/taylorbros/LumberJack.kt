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
import ktx.box2d.circle
import ktx.box2d.polygon

class LumberJack(
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
        var animNames = listOf("whateveryouwant")
        var fileNames = listOf("lumberjack.png")
        for ((index, name) in animNames.withIndex()) {
            var img = Texture("sprites/lumberjack/${fileNames[index]}")
            var tmpFrames = TextureRegion.split(img, 32, 32)
            var animationFrames = Array<TextureRegion>(tmpFrames[0])
            var animation = Animation<TextureRegion>(0.125f, animationFrames)
            animations[name] = animation
        }
        currentAnimation = animNames[0]
    }

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