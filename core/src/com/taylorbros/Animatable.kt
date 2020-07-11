package com.taylorbros

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion

interface Animatable {
    val animations: MutableMap<String, Animation<TextureRegion>>
    var currentAnimation: String
    var loop: Boolean
    var elapsedTime: Float
    val spriteWidth: Float
    val spriteHeight: Float

    fun getKeyFrame(): TextureRegion {
        return (animations[currentAnimation] ?: error("No such animation: $currentAnimation")).getKeyFrame(elapsedTime, loop)
    }
}