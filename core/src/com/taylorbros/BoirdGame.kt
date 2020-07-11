package com.taylorbros

import com.badlogic.gdx.Screen
import ktx.app.KtxGame

class BoirdGame : KtxGame<Screen>() {
    override fun create() {
        addScreen(ForestRun())
        setScreen<ForestRun>()
    }
}