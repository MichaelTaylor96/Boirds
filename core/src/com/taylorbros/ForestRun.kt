package com.taylorbros

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import ktx.app.KtxScreen
import ktx.box2d.createWorld
import ktx.graphics.use

class ForestRun : KtxScreen {

    private var yOffsetStep = 0.01f
    private var yOffsetCurrent = 0f
    private val box2dWorld = createWorld()
    private val collisionManager = CollisionManager()
    private val batch = SpriteBatch()
    private val pixelsPerMeter = 50f
    private var stageWidth = Gdx.graphics.width / pixelsPerMeter
    private var stageHeight = Gdx.graphics.height / pixelsPerMeter
    private val camera = OrthographicCamera(stageWidth, stageHeight)
    private val debugRenderer = Box2DDebugRenderer()
    private var timeStep = 1.0f / 60.0f
    private var velocityIterations = 8
    private var positionIterations = 3
    private var background = Texture("tiles/singleGrass.png")

    private val boidCount = 50
    private val maxSpeed = 10f
    private val maxAcceleration = 10f
    private val localDistance = 1.5f
    private val flockingPower = 10f

    private val entities = mutableSetOf<Any>()
    private val animatables = mutableListOf<Animatable>()
    private val stillSprites = mutableListOf<HasStaticSprite>()
    private val boidLord = BoidLord(
            box2dWorld,
            Vector2(0f, 0f),
            0.2f,
            10f,
            localDistance,
            flockingPower,
            pixelsPerMeter,
            stageWidth,
            stageHeight,
            3f,
            yOffsetCurrent
    )
    private val wolf = Wolf(Vector2(-5f, -1f), .5f, box2dWorld, pixelsPerMeter, 2f)
    private val lumberJack = LumberJack(Vector2(-1f, 5f), 1f, box2dWorld, pixelsPerMeter, 2.5f)
    private val seedPile = SeedPile(Vector2(-5f, 7f), 1f, box2dWorld, pixelsPerMeter, 2f)

    init {
        animatables.add(wolf)
        animatables.add(lumberJack)
        animatables.add(boidLord)

        stillSprites.add(seedPile)

        entities.add(boidLord)
        entities.add(wolf)
        entities.add(lumberJack)
        entities.add(seedPile)

        Gdx.app.input.inputProcessor = boidLord
        box2dWorld.setContactListener(collisionManager)
        background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)

        var treeY = -(stageHeight/2)
        while(treeY < (stageHeight/2) + 3) {
            val leftTree = Tree(Vector2(-(stageWidth/2), treeY), 1f, box2dWorld, pixelsPerMeter, 2.5f)
            val rightTree = Tree(Vector2(stageWidth/2, treeY), 1f, box2dWorld, pixelsPerMeter, 2.5f)
            entities.add(leftTree)
            entities.add(rightTree)
            stillSprites.add(leftTree)
            stillSprites.add(rightTree)
            treeY += 2f
        }

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

        val flameSize = 1f
        val flameCount =  (stageWidth/flameSize).toInt()
        for (i in 0..flameCount) {
            val flame = Flame(
                    Vector2(i * flameSize - stageWidth/2, flameSize/3 - stageHeight/2),
                    flameSize,
                    box2dWorld,
                    pixelsPerMeter,
                    1.2f,
                    yOffsetStep)
            animatables.add(flame)
            entities.add(flame)
        }
    }

    override fun render(delta: Float) {
        yOffsetCurrent += yOffsetStep
        boidLord.yOffsetCurrent = yOffsetCurrent
        if ((yOffsetCurrent + 1) % 2 < 0.01f) {
            addSideTrees()
        }
        val pixelOffset = yOffsetCurrent * pixelsPerMeter
        camera.translate(0f, yOffsetStep)
        camera.update()
        box2dWorld.step(timeStep, velocityIterations, positionIterations)
        entities.forEach { if (it is Updatable) it.update(entities) }

        batch.use {
            Gdx.gl.glClearColor(1f, 1f,1f, 1f)
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
            batch.draw(background, 0f, 0f)

            for (animatable in animatables) {
                animatable.elapsedTime += delta
                val img = animatable.getKeyFrame()
                batch.draw(img, animatable.pixelX, animatable.pixelY - pixelOffset, animatable.pixelWidth, animatable.pixelHeight)
            }
            for (sprite in stillSprites) {
                batch.draw(sprite.sprite, sprite.pixelX, sprite.pixelY - pixelOffset, sprite.pixelWidth, sprite.pixelHeight)
            }
        }
        debugRenderer.render(box2dWorld, camera.combined)
    }

    fun addSideTrees() {
        val leftTree = Tree(Vector2(-(stageWidth/2), stageHeight/2 + yOffsetCurrent + 1), 1f, box2dWorld, pixelsPerMeter, 2.5f)
        val rightTree = Tree(Vector2(stageWidth/2, stageHeight/2 + yOffsetCurrent + 1), 1f, box2dWorld, pixelsPerMeter, 2.5f)
        entities.add(leftTree)
        entities.add(rightTree)
        stillSprites.add(leftTree)
        stillSprites.add(rightTree)
    }

    override fun dispose() {
        batch.dispose()
    }
}