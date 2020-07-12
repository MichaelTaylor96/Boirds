package com.taylorbros

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*


class CollisionManager(
        private val entities: MutableSet<Any>,
        private val world: World,
        private val pixelsPerMeter: Float,
        private val flockingPower: Float,
        private val maxSpeed: Float,
        private val maxAcceleration: Float,
        private val localDistance: Float
        ) : ContactListener {

    private val entitiesToDestroy = mutableListOf<Destroyable>()
    val entitiesToCreate = mutableListOf<Bird>()

    fun destroyEntities() {
        entitiesToDestroy.forEach {
            it.die()
            if (entities.contains(it)) {
                entities.remove(it)
            }
        }
    }

    fun createEntities() {
        entities.addAll(entitiesToCreate)
        entitiesToCreate.clear()
    }

    override fun endContact(contact: Contact?) {}

    override fun beginContact(contact: Contact?) {
        val bodyA = contact!!.fixtureA.body
        val bodyB = contact!!.fixtureB.body
        val bodies = listOf(bodyA, bodyB)

        if (bodies.any{it.userData is Bird} && bodies.any{it.userData is Wolf}) {
            val wolf = bodies.find{it.userData is Wolf}!!.userData as Wolf
            handleWolfEat(wolf)
        }

        val userDataA = bodyA.userData
        val userDataB = bodyB.userData

        if (userDataA is Mortal && userDataB is Lethal) {
            entitiesToDestroy.add(userDataA)
        }

        if (userDataB is Mortal && userDataA is Lethal) {
            entitiesToDestroy.add(userDataB)
        }

        if (bodies.any{it.userData is Bird} && bodies.any{it.userData is SeedPile}) {
            val seedPile = bodies.find{it.userData is SeedPile}!!.userData as SeedPile
            val bird = bodies.find{it.userData is Bird}!!.userData as Bird
            handleBirdEat(bird, seedPile)
        }
    }

    private fun handleWolfEat(wolf: Wolf) {
        if (wolf.birdsEaten < 5) {
            wolf.eating = true
            wolf.currentAnimation = "eat"
            wolf.timeStartedEating = wolf.elapsedTime
            wolf.birdsEaten++
        }
    }

    private fun handleBirdEat(bird: Bird, seed: SeedPile) {
        bird.eating = true
        bird.currentAnimation = "eat"
        bird.timeStartedEating = bird.elapsedTime
        bird.body.setLinearVelocity(0f, 0f)

        seed.timesBeenEaten++
        val spawnPos = seed.position
        if (seed.timesBeenEaten > 4) {
            repeat(5) {
                val randomOffset = Vector2(((Math.random() * 5) - 2.5).toFloat(), ((Math.random() * 5) - 2.5).toFloat())
                val position = spawnPos.add(randomOffset)
                val variableFlockingPower = (MathUtils.random() * flockingPower * 2 + 0.5 * flockingPower).toFloat()
                val variableMaxSpeed = (MathUtils.random() * maxSpeed * 2 + 0.5 * maxSpeed).toFloat()
                val variableMaxAcceleration = (MathUtils.random() * maxAcceleration * 0.9 + 0.1 * maxAcceleration).toFloat()
                val initialImpulse = Vector2(0f, 0f)
//                val newBird = Bird(
//                        0.1f,
//                        world,
//                        position,
//                        initialImpulse,
//                        pixelsPerMeter,
//                        3f,
//                        localDistance,
//                        variableFlockingPower,
//                        variableMaxSpeed,
//                        variableMaxAcceleration
//                )
//                entitiesToCreate.add(newBird)
            }
            entitiesToDestroy.add(seed)
        }
    }

    override fun preSolve(contact: Contact?, oldManifold: Manifold?) {
    }

    override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {
    }
}