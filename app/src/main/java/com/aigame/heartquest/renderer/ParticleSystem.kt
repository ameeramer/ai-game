package com.aigame.heartquest.renderer

import android.opengl.GLES20
import com.aigame.heartquest.game.NpcAnimation
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin

/**
 * Particle system for mood-based ambient effects (hearts, sparkles, etc.).
 * Uses GL_POINTS for lightweight rendering.
 */
class ParticleSystem {

    private val program: Int
    private val maxParticles = 50

    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        uniform float uTime;
        uniform float uPointSize;
        attribute vec3 aPosition;
        attribute float aPhase;
        varying float vAlpha;
        varying float vPhase;

        void main() {
            float t = mod(uTime + aPhase * 10.0, 6.0);
            vec3 pos = aPosition;

            // Float upward
            pos.y += t * 0.3;

            // Gentle sway
            pos.x += sin(uTime * 1.5 + aPhase * 6.28) * 0.15;
            pos.z += cos(uTime * 1.2 + aPhase * 3.14) * 0.1;

            // Fade in and out
            vAlpha = smoothstep(0.0, 0.5, t) * smoothstep(6.0, 4.0, t);
            vPhase = aPhase;

            gl_Position = uMVPMatrix * vec4(pos, 1.0);
            gl_PointSize = uPointSize * (1.0 - t / 6.0) * vAlpha;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        precision mediump float;
        varying float vAlpha;
        varying float vPhase;
        uniform vec3 uParticleColor;

        void main() {
            // Circular soft particle
            vec2 center = gl_PointCoord - vec2(0.5);
            float dist = length(center);
            if (dist > 0.5) discard;

            float softEdge = smoothstep(0.5, 0.2, dist);

            // Heart shape for romantic particles
            vec2 p = (gl_PointCoord - vec2(0.5)) * 2.0;
            float heart = step(0.0, 1.0 - length(p));

            gl_FragColor = vec4(uParticleColor, softEdge * vAlpha * 0.7);
        }
    """.trimIndent()

    private val vertexBuffer: FloatBuffer

    init {
        program = GameRenderer.createProgram(vertexShaderCode, fragmentShaderCode)

        // Pre-generate particle positions and phases
        // 4 floats per particle: x, y, z, phase
        val data = FloatArray(maxParticles * 4)
        for (i in 0 until maxParticles) {
            val idx = i * 4
            data[idx] = (Math.random().toFloat() - 0.5f) * 2.0f      // x: -1 to 1
            data[idx + 1] = Math.random().toFloat() * 0.5f + 0.5f     // y: 0.5 to 1.0
            data[idx + 2] = (Math.random().toFloat() - 0.5f) * 1.0f   // z: -0.5 to 0.5
            data[idx + 3] = Math.random().toFloat()                     // phase: 0 to 1
        }

        vertexBuffer = ByteBuffer.allocateDirect(data.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(data)
                position(0)
            }
    }

    fun draw(mvpMatrix: FloatArray, animation: NpcAnimation, time: Float) {
        // Only show particles for positive moods
        val particleCount: Int
        val color: FloatArray
        val pointSize: Float

        when (animation) {
            NpcAnimation.HAPPY -> {
                particleCount = 30
                color = floatArrayOf(1.0f, 0.85f, 0.2f) // golden sparkles
                pointSize = 12f
            }
            NpcAnimation.FLIRTY -> {
                particleCount = 40
                color = floatArrayOf(1.0f, 0.3f, 0.5f) // pink hearts
                pointSize = 15f
            }
            NpcAnimation.SHY -> {
                particleCount = 20
                color = floatArrayOf(1.0f, 0.6f, 0.7f) // soft pink
                pointSize = 10f
            }
            else -> {
                particleCount = 8
                color = floatArrayOf(0.6f, 0.6f, 0.8f) // subtle blue ambient
                pointSize = 6f
            }
        }

        GLES20.glUseProgram(program)

        val mvpHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        val timeHandle = GLES20.glGetUniformLocation(program, "uTime")
        val sizeHandle = GLES20.glGetUniformLocation(program, "uPointSize")
        val colorHandle = GLES20.glGetUniformLocation(program, "uParticleColor")
        val posHandle = GLES20.glGetAttribLocation(program, "aPosition")
        val phaseHandle = GLES20.glGetAttribLocation(program, "aPhase")

        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniform1f(timeHandle, time)
        GLES20.glUniform1f(sizeHandle, pointSize)
        GLES20.glUniform3fv(colorHandle, 1, color, 0)

        GLES20.glEnableVertexAttribArray(posHandle)
        GLES20.glEnableVertexAttribArray(phaseHandle)

        // stride: 4 floats = 16 bytes (x, y, z, phase)
        vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(posHandle, 3, GLES20.GL_FLOAT, false, 16, vertexBuffer)

        vertexBuffer.position(3)
        GLES20.glVertexAttribPointer(phaseHandle, 1, GLES20.GL_FLOAT, false, 16, vertexBuffer)

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, particleCount)

        GLES20.glDisableVertexAttribArray(posHandle)
        GLES20.glDisableVertexAttribArray(phaseHandle)
    }
}
