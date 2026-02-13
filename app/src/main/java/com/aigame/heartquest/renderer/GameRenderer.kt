package com.aigame.heartquest.renderer

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.aigame.heartquest.game.NpcAnimation
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

/**
 * OpenGL ES 2.0 renderer for the 3D game scene.
 * Renders a stylized scene with the NPC character (Adrian) and environment.
 */
class GameRenderer : GLSurfaceView.Renderer {

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val tempMatrix = FloatArray(16)

    private var npcModel: NpcCharacterModel? = null
    private var ground: GroundPlane? = null
    private var skybox: SkyGradient? = null
    private var particles: ParticleSystem? = null

    private var time = 0f
    var npcAnimation: NpcAnimation = NpcAnimation.IDLE
    var sceneAmbience: Float = 0.7f // 0=dark, 1=bright

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.05f, 0.05f, 0.15f, 1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        npcModel = NpcCharacterModel()
        ground = GroundPlane()
        skybox = SkyGradient()
        particles = ParticleSystem()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, 45f, ratio, 0.1f, 100f)
    }

    override fun onDrawFrame(gl: GL10?) {
        time += 0.016f // ~60fps
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Camera setup - looking at the NPC
        Matrix.setLookAtM(
            viewMatrix, 0,
            0f, 1.5f, 4f,   // eye position
            0f, 1.0f, 0f,    // look at
            0f, 1f, 0f       // up vector
        )

        // Draw sky gradient background
        drawSky()

        // Draw ground plane
        drawGround()

        // Draw NPC character
        drawNpc()

        // Draw ambient particles (hearts, sparkles based on mood)
        drawParticles()
    }

    private fun drawSky() {
        skybox?.draw(projectionMatrix, viewMatrix, sceneAmbience, time)
    }

    private fun drawGround() {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0f, 0f, 0f)
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
        ground?.draw(mvpMatrix, sceneAmbience, time)
    }

    private fun drawNpc() {
        Matrix.setIdentityM(modelMatrix, 0)

        // Apply animation
        when (npcAnimation) {
            NpcAnimation.IDLE -> {
                // Gentle breathing sway
                val sway = sin(time * 1.5f) * 0.02f
                Matrix.translateM(modelMatrix, 0, 0f, sway, 0f)
            }
            NpcAnimation.HAPPY -> {
                // Bouncy movement
                val bounce = Math.abs(sin(time * 4f).toFloat()) * 0.08f
                Matrix.translateM(modelMatrix, 0, 0f, bounce, 0f)
            }
            NpcAnimation.FLIRTY -> {
                // Lean and sway
                val lean = sin(time * 2f) * 0.05f
                Matrix.translateM(modelMatrix, 0, lean, 0f, 0f)
                Matrix.rotateM(modelMatrix, 0, sin(time * 2f) * 5f, 0f, 0f, 1f)
            }
            NpcAnimation.ANNOYED -> {
                // Slight lean back
                Matrix.translateM(modelMatrix, 0, 0f, 0f, 0.2f)
                Matrix.rotateM(modelMatrix, 0, -5f, 1f, 0f, 0f)
            }
            NpcAnimation.SHY -> {
                // Look down slightly, small sway
                Matrix.rotateM(modelMatrix, 0, 10f, 1f, 0f, 0f)
                val sway = sin(time * 1.2f) * 0.03f
                Matrix.translateM(modelMatrix, 0, sway, 0f, 0f)
            }
            NpcAnimation.TALKING -> {
                // Subtle forward lean
                val sway = sin(time * 2f) * 0.01f
                Matrix.translateM(modelMatrix, 0, sway, 0f, -0.1f)
            }
        }

        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
        npcModel?.draw(mvpMatrix, npcAnimation, time, sceneAmbience)
    }

    private fun drawParticles() {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
        particles?.draw(mvpMatrix, npcAnimation, time)
    }

    companion object {
        fun loadShader(type: Int, shaderCode: String): Int {
            val shader = GLES20.glCreateShader(type)
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
            return shader
        }

        fun createProgram(vertexCode: String, fragmentCode: String): Int {
            val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexCode)
            val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentCode)
            val program = GLES20.glCreateProgram()
            GLES20.glAttachShader(program, vertexShader)
            GLES20.glAttachShader(program, fragmentShader)
            GLES20.glLinkProgram(program)
            return program
        }
    }
}
