package com.taylorbros

import com.badlogic.gdx.math.Vector2

interface Boid : HasPosition, HasVelocity, HasSize {
    val localDistance: Float
    val flockingPower: Float
}