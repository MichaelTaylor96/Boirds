package com.taylorbros

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import ktx.app.KtxScreen
import ktx.box2d.createWorld
import ktx.graphics.use
import java.lang.Integer.max
import java.lang.Integer.min

class ForestRun : KtxScreen {

    private var yOffsetStep = 0.01f
    private var yOffsetCurrent = 0f
    private val box2dWorld = createWorld()
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
    private var bgRegion = TextureRegion()

    private val boidCount = 50
    private val maxSpeed = 10f
    private val maxAcceleration = 10f
    private val localDistance = 1.5f
    private val flockingPower = 10f

    private val entities = mutableSetOf<Any>()
    private val collisionManager = CollisionManager(entities, box2dWorld, pixelsPerMeter, flockingPower, maxSpeed, maxAcceleration, localDistance)

    init {
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
                3f,
                yOffsetCurrent
        )
        entities.add(
                boidLord
        )

        Gdx.app.input.inputProcessor = boidLord
        box2dWorld.setContactListener(collisionManager)
        background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
        bgRegion.texture = background
        bgRegion.setRegion(0f, 0f, (stageWidth * pixelsPerMeter) + 16, (stageHeight * pixelsPerMeter) + 16)

        var treeY = -(stageHeight/2)
        while(treeY < (stageHeight/2 + 3)) {
            val leftTree = Tree(Vector2(-(stageWidth/2), treeY), 1f, box2dWorld, pixelsPerMeter, 2.5f)
            val rightTree = Tree(Vector2(stageWidth/2, treeY), 1f, box2dWorld, pixelsPerMeter, 2.5f)
            entities.add(leftTree)
            entities.add(rightTree)
            treeY += 2f
        }

        repeat(boidCount) {
            val randomOffset = Vector2(((Math.random() * 5) - 2.5).toFloat(), ((Math.random() * 5) - 2.5).toFloat())
            val position = Vector2(0f, 0f).add(randomOffset)
            val variableFlockingPower = (MathUtils.random() * flockingPower * 2 + 0.5 * flockingPower).toFloat()
            val variableMaxSpeed = (MathUtils.random() * maxSpeed * 2 + 0.5 * maxSpeed).toFloat()
            val variableMaxAcceleration = (MathUtils.random() * maxAcceleration * 0.9 + 0.1 * maxAcceleration).toFloat()
            val initialImpulse = Vector2(0f, 0f)
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
            entities.add(flame)
        }
    }

    override fun render(delta: Float) {
        yOffsetCurrent += yOffsetStep
        conditionallyAddEntities()
        entities.filterIsInstance<Offsettable>().forEach { it.yOffsetCurrent = yOffsetCurrent }
        if ((yOffsetCurrent) % 2 < 0.01f) {
            addSideTrees()
        }
        val pixelOffset = yOffsetCurrent * pixelsPerMeter
        camera.translate(0f, yOffsetStep)
        camera.update()
        box2dWorld.step(timeStep, velocityIterations, positionIterations)
        collisionManager.destroyEntities()
        collisionManager.createEntities()
        entities.forEach { if (it is Updatable) it.update(entities) }
        batch.use {
            Gdx.gl.glClearColor(1f, 1f,1f, 1f)
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
            val bgOffset = (yOffsetCurrent*pixelsPerMeter)%16
            batch.draw(bgRegion, 0f, 0f - bgOffset)

            for (animatable in entities.filterIsInstance<Animatable>()) {
                animatable.elapsedTime += delta
                val img = animatable.getKeyFrame()
                batch.draw(img, animatable.pixelX, animatable.pixelY - pixelOffset, animatable.pixelWidth, animatable.pixelHeight)
            }
            for (sprite in entities.filterIsInstance<HasStaticSprite>()) {
                batch.draw(sprite.sprite, sprite.pixelX, sprite.pixelY - pixelOffset, sprite.pixelWidth, sprite.pixelHeight)
            }
        }
        removeEntitiesBelowFloor()
//        debugRenderer.render(box2dWorld, camera.combined)
    }

    private fun conditionallyAddEntities() {
        val targetWolves = min((yOffsetCurrent/10).toInt(), 5)
        if (entities.count { it is Wolf } < targetWolves) {
            entities.add(
                    Wolf(
                            Vector2((Math.random()).toFloat()* stageWidth - stageWidth/2, stageHeight*2/3 + yOffsetCurrent),
                            .5f,
                            box2dWorld,
                            pixelsPerMeter,
                            2f))
        }
        val targetTrees = min((yOffsetCurrent).toInt() + 20, 30)
        if (entities.count { it is Tree } < targetTrees) {
            entities.add(
                    Tree(
                            Vector2((Math.random()).toFloat()* stageWidth - stageWidth/2, stageHeight*2/3 + yOffsetCurrent),
                            Math.random().toFloat() + 0.5f,
                            box2dWorld,
                            pixelsPerMeter,
                            2f))
        }
        val targetSeedPile = min((yOffsetCurrent/4).toInt(), 3)
        if (entities.count { it is SeedPile } < targetSeedPile) {
            entities.add(
                    SeedPile(
                            Vector2((Math.random()).toFloat()* stageWidth - stageWidth/2, stageHeight*2/3 + yOffsetCurrent),
                            0.5f,
                            box2dWorld,
                            pixelsPerMeter,
                            2f))
        }
        val targetLumberJacks = min((yOffsetCurrent/10).toInt(), 5)
        if (entities.count { it is LumberJack } < targetLumberJacks) {
            entities.add(
                    LumberJack(
                            Vector2((Math.random()).toFloat()* stageWidth - stageWidth/2, stageHeight*2/3 + yOffsetCurrent),
                            1f,
                            box2dWorld,
                            pixelsPerMeter,
                            2.5f))
        }
    }
    private fun removeEntitiesBelowFloor() {
        entities.filterIsInstance<HasPosition>().forEach {
            if (it.position.y < yOffsetCurrent - stageHeight / 2) {
                if (it is Destroyable) {
                    it.die()
                    entities.remove(it)
                }
            }
        }
    }

    fun addSideTrees() {
        val leftTree = Tree(Vector2(-(stageWidth/2), stageHeight/2 + yOffsetCurrent + 1), 1f, box2dWorld, pixelsPerMeter, 2.5f)
        val rightTree = Tree(Vector2(stageWidth/2, stageHeight/2 + yOffsetCurrent + 1), 1f, box2dWorld, pixelsPerMeter, 2.5f)
        entities.add(leftTree)
        entities.add(rightTree)
    }

    override fun dispose() {
        batch.dispose()
    }
}