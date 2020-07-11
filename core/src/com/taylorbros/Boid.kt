package com.taylorbros

import com.badlogic.gdx.math.Vector2

interface Boid {
    val position: Vector2 // in meters
    val velocity: Vector2 // in meters
    val size: Float // in meters
}