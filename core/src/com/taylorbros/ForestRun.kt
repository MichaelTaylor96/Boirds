package com.taylorbros

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.Color
import ktx.app.KtxScreen
import ktx.graphics.use

class ForestRun : KtxScreen {

    private val batch = SpriteBatch().apply {
        color = Color.BLACK
    }

    override fun render(delta: Float) {
        batch.use {
        }
    }

    override fun dispose() {
        batch.dispose()
    }
}