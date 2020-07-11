package com.taylorbros

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion

class BoirdMain : ApplicationAdapter() {
    var batch: SpriteBatch? = null
    var img: Texture? = null
    var animationFrames: Array<TextureRegion>? = null
    var animation: Animation<TextureRegion>? = null
    var elapsedTime: Float = 0f

    override fun create() {
        batch = SpriteBatch()
        img = Texture("birdUp.png")
        var tmpFrames = TextureRegion.split(img, 8, 8)
        animationFrames = tmpFrames[0]
        animation = Animation(.25f, animationFrames!!)
    }

    override fun render() {
        elapsedTime += Gdx.graphics.deltaTime
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch!!.begin()
        batch!!.draw(animation!!.getKeyFrame(elapsedTime, true), 0f, 0f)
        batch!!.end()
    }

    override fun dispose() {
        batch!!.dispose()
        img!!.dispose()
    }
}