package com.taylorbros

import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Manifold
import kotlin.reflect.typeOf


class CollisionManager : ContactListener {
    override fun endContact(contact: Contact?) {}

    override fun beginContact(contact: Contact?) {
        val bodyA = contact!!.fixtureA.body
        val bodyB = contact!!.fixtureB.body
        val bodies = listOf(bodyA, bodyB)

        if (bodies.any{it.userData is Bird} && bodies.any{it.userData is Wolf}) {
            val wolf = bodies.find{it.userData is Wolf}!!.userData as Wolf
            val bird = bodies.find{it.userData is Bird}!!.userData as Bird
            handleWolfEat(wolf, bird)
        }
        else if (bodies.any{it.userData is Bird} && bodies.any{it.userData is SeedPile}) {
            val seedPile = bodies.find{it.userData is Wolf}!!.userData as SeedPile
            val bird = bodies.find{it.userData is Bird}!!.userData as Bird
            handleBirdEat(bird, seedPile)
        }
    }

    private fun handleWolfEat(wolf: Wolf, bird: Bird) {
        wolf.eating = true
        wolf.currentAnimation = "eat"
        wolf.timeStartedEating = wolf.elapsedTime
        wolf.body.setLinearVelocity(0f, 0f)
    }

    private fun handleBirdEat(bird: Bird, seed: SeedPile) {
        bird.eating = true
        bird.currentAnimation = "eat"
        bird.timeStartedEating = bird.elapsedTime
        bird.body.setLinearVelocity(0f, 0f)
    }

    override fun preSolve(contact: Contact?, oldManifold: Manifold?) {
//        TODO("Not yet implemented")
    }

    override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {
//        TODO("Not yet implemented")
    }
}