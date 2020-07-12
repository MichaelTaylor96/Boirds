package com.taylorbros

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import ktx.app.KtxScreen
import ktx.box2d.createWorld
import ktx.graphics.use

class ForestRun : KtxScreen {

    private val box2dWorld = createWorld()
    private val collisionManager = CollisionManager()
    private val batch = SpriteBatch()
    private val pixelsPerMeter = 50f
    private var stageWidth = Gdx.graphics.width / pixelsPerMeter
    private var stageHeight = Gdx.graphics.height / pixelsPerMeter
    private val camera = OrthographicCamera(stageWidth, stageHeight)
    private val debugRenderer = Box2DDebugRenderer()
    var timeStep = 1.0f / 60.0f // TODO figure out relationship between frame rate and physics simulation rate
    var velocityIterations = 8
    var positionIterations = 3

    private val boidCount = 50
    private val maxSpeed = 10f
    private val maxAcceleration = 10f
    private val localDistance = 1.5f
    private val flockingPower = 10f
    private val boids = mutableSetOf<Boid>()
    private val entities = mutableSetOf<Any>()

    val animatables = mutableListOf<Animatable>()
    val stillSprites = mutableListOf<HasStaticSprite>()
    val boidLord = BoidLord(
            box2dWorld,
            Vector2(0f, 0f),
            0.2f,
            10f,
            localDistance,
            flockingPower,
            pixelsPerMeter,
            stageWidth,
            stageHeight,
            3f
    )
    val tree = Tree(Vector2(5f, 1f), 1f, box2dWorld, pixelsPerMeter, 2.5f)
    val wolf = Wolf(Vector2(-5f, -1f), .5f, box2dWorld, pixelsPerMeter, 2f)
    val flame = Flame(Vector2(1f, -5f), 1f, box2dWorld, pixelsPerMeter, 1.2f)
    val lumberJack = LumberJack(Vector2(-1f, 5f), 1f, box2dWorld, pixelsPerMeter, 2.5f)
    val seedPile = SeedPile(Vector2(-5f, - 5f), 1f, box2dWorld, pixelsPerMeter, 2f)

    init {
        animatables.add(wolf)
        animatables.add(lumberJack)
        animatables.add(boidLord)
        animatables.add(flame)

        stillSprites.add(seedPile)
        stillSprites.add(tree)

        entities.add(boidLord)
        entities.add(wolf)
        entities.add(tree)
        entities.add(lumberJack)
        entities.add(flame)

        Gdx.app.input.inputProcessor = boidLord
        box2dWorld.setContactListener(collisionManager)

        repeat(boidCount) {
            val randomOffset = Vector2(((Math.random() * 5) - 2.5).toFloat(), ((Math.random() * 5) - 2.5).toFloat())
            val position = Vector2(0f, 0f).add(randomOffset)
            val variableFlockingPower = (MathUtils.random() * flockingPower * 2 + 0.5 * flockingPower).toFloat()
            val variableMaxSpeed = (MathUtils.random() * maxSpeed * 2 + 0.5 * maxSpeed).toFloat()
            val variableMaxAcceleration = (MathUtils.random() * maxAcceleration * 0.9 + 0.1 * maxAcceleration).toFloat()
            val initialImpulse = Vector2().setToRandomDirection().setLength(MathUtils.random() * variableMaxSpeed)
            val newBird = Bird(
                    0.1f,
                    box2dWorld,
                    position,
                    initialImpulse,
                    pixelsPerMeter,
                    3f,
                    localDistance,
                    variableFlockingPower,
                    variableMaxSpeed,
                    variableMaxAcceleration
            )

            animatables.add(newBird)
            entities.add(newBird)
        }
    }

    override fun render(delta: Float) {
        box2dWorld.step(timeStep, velocityIterations, positionIterations)
        entities.forEach { if (it is Updatable) it.update(entities) }

        batch.use {
            Gdx.gl.glClearColor(1f, 1f,1f, 1f)
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
            for (animatable in animatables) {
                animatable.elapsedTime += delta
                val img = animatable.getKeyFrame()
                batch.draw(img, animatable.pixelX, animatable.pixelY, animatable.pixelWidth, animatable.pixelHeight)
            }
            for (sprite in stillSprites) {
                batch.draw(sprite.sprite, sprite.pixelX, sprite.pixelY, sprite.pixelWidth, sprite.pixelHeight)
            }
        }
        debugRenderer!!.render(box2dWorld, camera.combined)
    }

    override fun dispose() {
        batch.dispose()
    }
}