package com.taylorbros

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Array
import ktx.box2d.body
import ktx.box2d.circle
import ktx.box2d.polygon
import kotlin.math.absoluteValue
import kotlin.math.pow

class Bird(
        override val size: Float,
        world: World,
        initialPosition: Vector2,
        initialVelocity: Vector2,
        override val pixelsPerMeter: Float,
        override val scaleFactor: Float,
        override val localDistance: Float,
        override val flockingPower: Float,
        private val maxSpeed: Float,
        private val maxAcceleration: Float
) : Animatable, Boid, Updatable {

    override val animations: MutableMap<String, Animation<TextureRegion>> = mutableMapOf()
    override var currentAnimation = ""
    override var loop = true
    override var elapsedTime = 0f

    init {
        var animNames = listOf("up", "down", "left", "right", "eat")
        var fileNames = listOf("birdUp.png", "birdDown.png", "birdLeft.png", "birdRight.png", "birdEat.png")
        for ((index, name) in animNames.withIndex()) {
            var img = Texture("sprites/bird/${fileNames[index]}")
            var tmpFrames = TextureRegion.split(img, 16, 15)
            var animationFrames = Array<TextureRegion>(tmpFrames[0])
            var animation = Animation<TextureRegion>(0.125f, animationFrames)
            animations[name] = animation
        }
        currentAnimation = animNames[0]
    }

    // set up box2d physics body
    private val body = world.body {
        type = BodyDef.BodyType.DynamicBody
        position.set(initialPosition.x, initialPosition.y)
        angle = initialVelocity.angleRad()
        userData = this@Bird

        circle(radius = size) {
            restitution = 0.2f
            density = 10f
        }
        polygon(Vector2(0f, size),
                Vector2(size * 2, 0f),
                Vector2(0f, -size))
    }
    init {
        body.linearVelocity = initialVelocity
    }

    private var desiredMovement = Vector2()
    private var drag = Vector2()
    private val dragFactor = 0.04f
    private val avoidFactor = 10f
    private val torqueFactor = 0.1f
    private val rotationalDragFactor = 0.05f

    override fun update(entities: Set<Any>) {
        desiredMovement = Vector2()
        val localBoids = localBoidsFrom(entities)
        if (localBoids.isNotEmpty()) {
            desiredMovement.add(separationForce(localBoids))
            desiredMovement.add(alignmentForce(localBoids))
            desiredMovement.add(cohesionForce(localBoids))
        }
        val localObstacles = localObstaclesFrom(entities)
        if (localObstacles.isNotEmpty()) {
            desiredMovement.add(obstacleAvoidanceForce(localObstacles))
        }
        val targets = entities.filterIsInstance<Target>()
        if (targets.isNotEmpty()) {
//            desiredMovement.add(targetsSeekingForce(targets))
        }
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
        setAnimationDirection()
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

    private fun localObstaclesFrom(entities: Set<Any>): List<Obstacle> {
        return entities.filter {
            it is Obstacle
                    && this != it
                    && (this.position.dst(it.position) - this.size - it.size) < localDistance
        }.map { it as Obstacle }
    }

    private fun localBoidsFrom(entities: Set<Any>): List<Boid> {
        return entities.filter {
            it is Boid
                    && this != it
                    && (this.position.dst(it.position) - this.size - it.size) < localDistance
        }.map { it as Boid }
    }

    // steer to avoid crowding local flockmates
    private fun separationForce(otherLocalBoids: List<Boid>): Vector2 {
        val separationForce = Vector2()
        otherLocalBoids.forEach { other ->
            val vectorAwayFromOther = this.position.cpy().sub(other.position)

            // make separation force stronger for closer flockmates
            val distance = vectorAwayFromOther.len()
            val proportionOfLocalDistance = distance / localDistance
            val inverseProportionOfLocalDistance = 1 - proportionOfLocalDistance
            vectorAwayFromOther.setLength(inverseProportionOfLocalDistance)

            separationForce.add(vectorAwayFromOther)
        }

        // separation force should never exceed 1
        if (separationForce.len() > 1f) {
            separationForce.setLength(1f)
        }

        // scale separation force by the flockingPower
        return separationForce.scl(flockingPower)
    }

    // steer towards the average heading of local flockmates
    private fun alignmentForce(otherLocalBoids: List<Boid>): Vector2 {
        val sumOfOtherVelocities = Vector2()
        otherLocalBoids.forEach { other ->
            sumOfOtherVelocities.add(other.velocity)
        }
        val averageOfOtherVelocities = sumOfOtherVelocities.scl( 1f / otherLocalBoids.count() )

        // make alignment force stronger when this boids heading is more different than the average heading
        val velocityDifference = averageOfOtherVelocities.sub(this.velocity)
        val velocityDifferenceMagnitude = velocityDifference.len()
        val averageOtherVelocityMagnitude = averageOfOtherVelocities.len()
        val thisVelocityMagnitude = this.velocity.len()

        // alignment force should never be greater than 1
        val normalizedMagnitude = velocityDifferenceMagnitude / (averageOtherVelocityMagnitude + thisVelocityMagnitude)
        val normalizedVelocityDifference = velocityDifference.setLength(normalizedMagnitude)

        // scale alignment force by the flockingPower
        var scalar = flockingPower
        if (otherLocalBoids.any{it is BoidLord}) scalar *= 2
        return normalizedVelocityDifference.scl(scalar)
    }

    // steer to move towards the average position of local flockmates
    private fun cohesionForce(otherLocalBoids: List<Boid>): Vector2 {
        val sumOfVectorsToOthers = Vector2()
        otherLocalBoids.forEach { other ->
            val vectorToOther = other.position.cpy().sub(this.position)
            sumOfVectorsToOthers.add(vectorToOther)
        }
        val vectorToAverageOtherCenters = sumOfVectorsToOthers.scl(1f / otherLocalBoids.count())

        // cohesion force should never be greater than 1
        val distance = vectorToAverageOtherCenters.len()
        val proportionOfLocalDistance = distance / localDistance
        val cohesionForce = vectorToAverageOtherCenters.setLength(proportionOfLocalDistance)

        // scale cohesion force by the flockingPower
        return cohesionForce.scl(flockingPower)
    }

    private fun obstacleAvoidanceForce(obstacles: List<Obstacle>): Vector2 {
        val avoidForce = Vector2()
        obstacles.forEach {
            val distance = this.position.dst(it.position)
            if (distance < (this.size + it.size + localDistance)) {
                val avoidMagnitude = 1 - (localDistance / (distance - this.size - it.size))
                val avoidVector = this.position.cpy().sub(it.position).setLength(avoidMagnitude * avoidFactor)
                avoidForce.add(avoidVector)
            }
        }
        return avoidForce
    }

    private fun setAnimationDirection() {
        val eighthPi = Math.PI / 8
        val absAngle = (body.linearVelocity.angleRad() + Math.PI) % Math.PI
        if (absAngle < eighthPi || absAngle > 7 * eighthPi) {
            currentAnimation = "right"
        }
        if (absAngle < 3 * eighthPi && absAngle > eighthPi) {
            currentAnimation = "up"
        }
        if (absAngle < 5 * eighthPi && absAngle > 3 * eighthPi) {
            currentAnimation = "down"
        }
        if (absAngle < 7 * eighthPi && absAngle > 5 * eighthPi) {
            currentAnimation = "left"
        }
    }

    override val position: Vector2
        get() = this.body.position

    override val velocity: Vector2
        get() = this.body.linearVelocity
}