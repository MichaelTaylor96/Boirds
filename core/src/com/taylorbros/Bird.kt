package com.taylorbros

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Array

class Bird(spriteSize: Float) : Animatable {

    override val animations: MutableMap<String, Animation<TextureRegion>> = mutableMapOf()
    override var currentAnimation: String = ""
    override var elapsedTime: Float = 0f
    override val spriteWidth: Float = spriteSize
    override val spriteHeight: Float = spriteSize

    init {
        var animNames = listOf("alert")
        var fileNames = listOf("wolfEat.png")
        for ((index, name) in animNames.withIndex()) {
            var img = Texture("sprites/wolf/${fileNames[index]}")
            var tmpFrames = TextureRegion.split(img, 32, 32)
            var animationFrames = Array<TextureRegion>(tmpFrames[0])
            var animation = Animation<TextureRegion>(0.125f, animationFrames)
            animations[name] = animation
        }
        currentAnimation = animNames[0]
    }
}