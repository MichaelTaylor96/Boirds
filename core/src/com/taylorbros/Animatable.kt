package com.taylorbros

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion

interface Animatable : HasSprite, HasPosition, HasSize {
    val animations: MutableMap<String, Animation<TextureRegion>>
    var currentAnimation: String
    var loop: Boolean
    var elapsedTime: Float
    val pixelsPerMeter: Float
    val scaleFactor: Float

    fun getKeyFrame(): TextureRegion {
        return (animations[currentAnimation] ?: error("No such animation: $currentAnimation")).getKeyFrame(elapsedTime, loop)
    }

    override val pixelHeight: Float
        get() = size * pixelsPerMeter * scaleFactor

    override val pixelWidth: Float
        get() = size * pixelsPerMeter * scaleFactor

    override val pixelX: Float
        get() = (position.x * pixelsPerMeter) + (Gdx.graphics.width / 2) - (pixelWidth / 2)

    override val pixelY: Float
        get() = (position.y * pixelsPerMeter) + (Gdx.graphics.height / 2) - (pixelHeight / 2)
}