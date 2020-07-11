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
        var animNames = listOf("up", "down", "left", "right", "eat")
        var fileNames = listOf("birdUp.png", "birdDown.png", "birdLeft.png", "birdRight.png", "birdEat.png")
        for ((index, name) in animNames.withIndex()) {
            var img = Texture("sprites/bird/${fileNames[index]}")
            var tmpFrames = TextureRegion.split(img, 16, 15)
            var animationFrames = Array<TextureRegion>(tmpFrames[0])
            var animation = Animation<TextureRegion>(0.1f, animationFrames)
            animations[name] = animation
        }
        currentAnimation = animNames[0]
    }
}