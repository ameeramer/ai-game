package com.aigame.heartquest.ui.components

import android.content.Context
import android.opengl.GLSurfaceView
import com.aigame.heartquest.game.NpcAnimation
import com.aigame.heartquest.renderer.GameRenderer

/**
 * Custom GLSurfaceView that hosts the 3D game renderer.
 */
class GameGLSurfaceView(context: Context) : GLSurfaceView(context) {

    val renderer: GameRenderer

    init {
        setEGLContextClientVersion(2)
        renderer = GameRenderer()
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    fun setNpcAnimation(animation: NpcAnimation) {
        queueEvent {
            renderer.npcAnimation = animation
        }
    }

    fun setSceneAmbience(ambience: Float) {
        queueEvent {
            renderer.sceneAmbience = ambience
        }
    }
}
