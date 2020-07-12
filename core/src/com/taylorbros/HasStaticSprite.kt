package com.taylorbros

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture

interface HasStaticSprite : HasSprite, HasPosition, HasSize {
    val pixelsPerMeter: Float
    val scaleFactor: Float
    val sprite: Texture

    override val pixelHeight: Float
        get() = size * pixelsPerMeter * scaleFactor

    override val pixelWidth: Float
        get() = size * pixelsPerMeter * scaleFactor

    override val pixelX: Float
        get() = (position.x * pixelsPerMeter) + (Gdx.graphics.width / 2) - (pixelWidth / 2)

    override val pixelY: Float
        get() = (position.y * pixelsPerMeter) + (Gdx.graphics.height / 2) - (pixelHeight / 2)
}