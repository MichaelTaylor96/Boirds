package com.taylorbros

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import ktx.app.KtxScreen
import ktx.box2d.createWorld
import ktx.graphics.use

class ForestRun : KtxScreen {

    private val box2dWorld = createWorld()
    private val batch = SpriteBatch()
    private val pixelsPerMeter = 50f
    private var stageWidth = Gdx.graphics.width / pixelsPerMeter
    private var stageHeight = Gdx.graphics.height / pixelsPerMeter
    private val camera = OrthographicCamera(stageWidth, stageHeight)
    private val debugRenderer = Box2DDebugRenderer()
    var timeStep = 1.0f / 60.0f // TODO figure out relationship between frame rate and physics simulation rate
    var velocityIterations = 8
    var positionIterations = 3
    val animatables = mutableListOf<Animatable>()
    val boidLord = BoidLord(box2dWorld, Vector2(1f, 1f), 0.1f, 10f)
    val testBird = Bird(0.1f, box2dWorld, Vector2(1.5f, 1.5f), Vector2(0.1f, 0.1f), pixelsPerMeter, 3f)
    val tree = Tree(Vector2(5f, 1f), 1f, box2dWorld)
    val wolf = Wolf(Vector2(-5f, -1f), 1f, box2dWorld, pixelsPerMeter, 2f)
    val flame = Flame(Vector2(1f, -5f), 1f, box2dWorld)
    val lumberJack = LumberJack(Vector2(-1f, 5f), 1f, box2dWorld, pixelsPerMeter, 3f)
    val seedPile = SeedPile(Vector2(-5f, - 5f), 1f, box2dWorld)

    init {
        animatables.add(testBird)
        animatables.add(wolf)
        animatables.add(lumberJack)
    }

    override fun render(delta: Float) {
        box2dWorld.step(timeStep, velocityIterations, positionIterations)
        batch.use {
            Gdx.gl.glClearColor(1f, 1f,1f, 1f)
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
            for (animatable in animatables) {
                animatable.elapsedTime += delta
                if (Math.random() > 0.99) {
                    animatable.currentAnimation = animatable.animations.keys.toList().shuffled()[0]
                }
                val img = animatable.getKeyFrame()
                batch.draw(img, animatable.pixelX, animatable.pixelY, animatable.pixelWidth, animatable.pixelHeight)
            }
        }
        debugRenderer!!.render(box2dWorld, camera.combined)
    }

    override fun dispose() {
        batch.dispose()
    }
}