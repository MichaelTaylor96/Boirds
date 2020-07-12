package com.taylorbros

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Array
import ktx.box2d.body
import ktx.box2d.box
import kotlin.math.pow

class Wolf(
        position: Vector2,
        override val size: Float,
        private val world: World, override val
        pixelsPerMeter: Float,
        override val scaleFactor: Float
) : Obstacle, Animatable, Updatable, Lethal, Mortal {

    override val animations: MutableMap<String, Animation<TextureRegion>> = mutableMapOf()
    override var currentAnimation = ""
    override var loop = true
    override var elapsedTime = 0f
    var timeStartedEating = 0f
    var sleeping = true
    var birdsEaten = 0
    var eating = false
    var awareness = .5f

    init {
        var animNames = listOf("sleep", "alert", "run", "eat")
        var fileNames = listOf("wolfSleep.png", "wolfAlert.png", "wolfRun.png", "wolfEat.png")
        for ((index, name) in animNames.withIndex()) {
            var img = Texture("sprites/wolf/${fileNames[index]}")
            var tmpFrames = TextureRegion.split(img, 32, 32)
            var animationFrames = Array<TextureRegion>(tmpFrames[0])
            var animation = Animation<TextureRegion>(0.125f, animationFrames)
            animations[name] = animation
        }
        currentAnimation = animNames[0]
    }

    val body = world.body {
        type = BodyDef.BodyType.DynamicBody
        userData = this@Wolf
        this.position.set(position)
        box(width = size, height = size*2)
    }

    private var desiredMovement = Vector2()
    private var drag = Vector2()
    private val dragFactor = 0.04f
    private val torqueFactor = 0.1f
    private val rotationalDragFactor = 0.005f
    private val maxAcceleration = 10f

    override fun update(entities: Set<Any>) {
        desiredMovement = Vector2()
        val localBoids = localBoidsFrom(entities)
        if (localBoids.isNotEmpty()) {
            if (sleeping) {
                sleeping = false
                currentAnimation = "run"
                awareness = 10f
            }
            val target = localBoids.sortedBy{ position.dst(it.position) - size - it.size }[0]
            desiredMovement.add(seekingForce(target))

            if (eating) {
                body.setLinearVelocity(0f, 0f)
                if (elapsedTime - timeStartedEating > 3) {
                    eating = false
                    currentAnimation = "run"
                }
            }
        }

        if (!sleeping && !eating && birdsEaten < 5) {
            if (desiredMovement.len() > maxAcceleration) {
                desiredMovement.setLength(maxAcceleration)
            }
            body.applyForceToCenter(desiredMovement, true)
            drag = dragForce()
            body.applyForceToCenter(drag, true)
            val torque = rotateIntoVelocity()
            body.applyTorque(torque, true)
            val rotationalDrag = rotationalDrag()
            body.applyTorque(rotationalDrag, true)
        }

        else if (birdsEaten > 4) {
            body.applyForceToCenter(Vector2(0f, 10f), true)
        }
    }

    private fun localBoidsFrom(entities: Set<Any>): List<Boid> {
        return entities.filter {
            it is Boid
                    && this != it
                    && (this.position.dst(it.position) - this.size - it.size) < awareness
        }.map { it as Boid }
    }

    private fun seekingForce(target: Boid): Vector2 {
        val vectorToTarget = target.position.cpy().sub(position)
        val distance = vectorToTarget.len()
        val proximity = awareness / distance
        return vectorToTarget.setLength(proximity + 2)
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

    private fun dragForce(): Vector2 {
        val magnitude = this.body.linearVelocity.len().pow(2) * dragFactor
        val dragVector = this.body.linearVelocity.cpy().rotate(180f)
        return dragVector.setLength(magnitude)
    }

    override val position: Vector2
        get() = this.body.position

    var dead = false

    override fun die() {
        if (!dead) {
            dead = true
            world.destroyBody(this.body)
        }
    }
}