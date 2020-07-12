package com.taylorbros

import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Array
import ktx.box2d.body
import ktx.box2d.circle
import ktx.box2d.mouseJointWith
import ktx.box2d.polygon
import kotlin.math.pow

class BoidLord(
        private val world: World,
        initialPosition: Vector2, // in meters
        override val size: Float,
        density: Float,
        override val localDistance: Float,
        override val flockingPower: Float,
        override val pixelsPerMeter: Float,
        private val stageWidth: Float,
        private val stageHeight: Float,
        override val scaleFactor: Float,
        override var yOffsetCurrent: Float
) : Boid, InputProcessor, Animatable, Updatable, Mortal, Offsettable {

    private var mouseX = 0
    private var mouseY = 0
    override val animations: MutableMap<String, Animation<TextureRegion>> = mutableMapOf()
    override var currentAnimation = ""
    override var loop = true
    override var elapsedTime = 0f
    private val torqueFactor = 0.5f
    private val rotationalDragFactor = 0.05f

    init {
        val animNames = listOf("up", "down", "left", "right", "eat")
        val fileNames = listOf("birdUp.png", "birdDown.png", "birdLeft.png", "birdRight.png", "birdEat.png")
        for ((index, name) in animNames.withIndex()) {
            val img = Texture("sprites/bird/${fileNames[index]}")
            val tmpFrames = TextureRegion.split(img, 16, 15)
            val animationFrames = Array<TextureRegion>(tmpFrames[0])
            val animation = Animation<TextureRegion>(0.125f, animationFrames)
            animations[name] = animation
        }
        currentAnimation = animNames[0]
    }

    private val body = world.body {
        type = BodyDef.BodyType.DynamicBody
        userData = this@BoidLord
        position.set(initialPosition.x, initialPosition.y)
        circle(radius = size) {
            restitution = 0.2f
            this.density = density
        }
        polygon(Vector2(0f, size),
                Vector2(size * 2, 0f),
                Vector2(0f, -size))
    }

    override val position: Vector2
        get() = this.body.position

    override val velocity: Vector2
        get() = this.body.linearVelocity

    private val ground = world.body {
        type = BodyDef.BodyType.StaticBody
    }

    private val mousejoint = ground.mouseJointWith(body) {
        maxForce = 1000f
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return true
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        this.mouseX = screenX
        this.mouseY = screenY
        return true
    }

    override fun keyTyped(character: Char): Boolean {
        return true
    }

    override fun scrolled(amount: Int): Boolean {
        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        return true
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return true
    }

    override fun keyDown(keycode: Int): Boolean {
        return true
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return true
    }

    override fun update(entities: Set<Any>) {
        mousejoint.target = Vector2((mouseX/pixelsPerMeter) - (stageWidth/2), ((mouseY/pixelsPerMeter) - (stageHeight/2))*-1 + yOffsetCurrent)
        val torque = rotateIntoVelocity()
        body.applyTorque(torque, true)
        val rotationalDrag = rotationalDrag()
        body.applyTorque(rotationalDrag, true)
    }

    private fun rotationalDrag(): Float {
        val dragMagnitude = (rotationalDragFactor * body.angularVelocity).pow(2)
        return if (body.angularVelocity < 0) dragMagnitude else -dragMagnitude
    }

    private fun rotateIntoVelocity(): Float {
        val currentOrientation = Vector2(1f, 0f).setAngleRad(body.angle)
        val desiredOrientation = this.body.linearVelocity
        val difference = currentOrientation.angleRad(desiredOrientation)
        return torqueFactor * difference
    }

    var dead = false

    override fun die() {
        if (!dead) {
            dead = true
            println("$this died")
            world.destroyJoint(mousejoint)
            world.destroyBody(this.body)
            world.destroyBody(ground)
        }
    }
}