package com.taylorbros

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import ktx.app.KtxScreen
import ktx.box2d.createWorld
import ktx.graphics.use

class ForestRun : KtxScreen {

    private val box2dWorld = createWorld()
    private val batch = SpriteBatch().apply {
//        color = Color.WHITE
    }
    private val pixelsPerMeter = 50f
    private var stageWidth = Gdx.graphics.width / pixelsPerMeter
    private var stageHeight = Gdx.graphics.height / pixelsPerMeter
    private val camera = OrthographicCamera(stageWidth, stageHeight)
    private val debugRenderer = Box2DDebugRenderer()
    var timeStep = 1.0f / 60.0f // TODO figure out relationship between frame rate and physics simulation rate
    var velocityIterations = 8
    var positionIterations = 3
    val boidLord = BoidLord(box2dWorld, Vector2(1f, 1f), 0.1f, 10f)
    val testBird = Bird(100f)

    override fun render(delta: Float) {
        box2dWorld.step(timeStep, velocityIterations, positionIterations)
        batch.use {
            testBird.elapsedTime += delta
            if (Math.random() > 0.99) {
                testBird.currentAnimation = testBird.animations.keys.toList().shuffled()[0]
            }
            val img = testBird.getKeyFrame()
            batch.draw(img, 800f, 450f, testBird.spriteWidth, testBird.spriteHeight)
            Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
        }
//        debugRenderer!!.render(box2dWorld, camera.combined)
    }

    override fun dispose() {
        batch.dispose()
    }
}