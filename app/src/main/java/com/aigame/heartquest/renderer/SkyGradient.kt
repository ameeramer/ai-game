package com.aigame.heartquest.renderer

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/**
 * A fullscreen sky gradient rendered as a background.
 */
class SkyGradient {

    private val program: Int
    private val vertexBuffer: FloatBuffer
    private val indexBuffer: ShortBuffer

    private val vertexShaderCode = """
        attribute vec4 aPosition;
        varying vec2 vUV;

        void main() {
            gl_Position = aPosition;
            vUV = aPosition.xy * 0.5 + 0.5;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        precision mediump float;
        varying vec2 vUV;
        uniform float uAmbience;
        uniform float uTime;

        void main() {
            // Sky gradient from dark blue (bottom) to deep purple/rose (top)
            vec3 bottomColor = mix(
                vec3(0.05, 0.05, 0.15),  // dark night
                vec3(0.15, 0.12, 0.25),  // twilight
                uAmbience
            );

            vec3 topColor = mix(
                vec3(0.1, 0.05, 0.2),    // night purple
                vec3(0.35, 0.15, 0.35),   // sunset rose
                uAmbience
            );

            vec3 color = mix(bottomColor, topColor, vUV.y);

            // Add subtle stars
            float starField = fract(sin(dot(floor(vUV * 200.0), vec2(12.9898, 78.233))) * 43758.5453);
            float stars = step(0.995, starField) * (1.0 - uAmbience) * 0.8;
            float twinkle = sin(uTime * 3.0 + starField * 100.0) * 0.5 + 0.5;
            color += stars * twinkle;

            // Warm glow from bottom center (city lights / scene lighting)
            float glow = smoothstep(0.8, 0.0, length(vec2(vUV.x - 0.5, vUV.y * 2.0)));
            color += vec3(0.1, 0.05, 0.08) * glow * uAmbience;

            gl_FragColor = vec4(color, 1.0);
        }
    """.trimIndent()

    init {
        program = GameRenderer.createProgram(vertexShaderCode, fragmentShaderCode)

        // Fullscreen quad in NDC
        val vertices = floatArrayOf(
            -1f, -1f, 0.999f,
             1f, -1f, 0.999f,
             1f,  1f, 0.999f,
            -1f,  1f, 0.999f
        )

        val indices = shortArrayOf(0, 1, 2, 0, 2, 3)

        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(vertices)
                position(0)
            }

        indexBuffer = ByteBuffer.allocateDirect(indices.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .apply {
                put(indices)
                position(0)
            }
    }

    fun draw(projectionMatrix: FloatArray, viewMatrix: FloatArray, ambience: Float, time: Float) {
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glUseProgram(program)

        val ambienceHandle = GLES20.glGetUniformLocation(program, "uAmbience")
        val timeHandle = GLES20.glGetUniformLocation(program, "uTime")
        val posHandle = GLES20.glGetAttribLocation(program, "aPosition")

        GLES20.glUniform1f(ambienceHandle, ambience)
        GLES20.glUniform1f(timeHandle, time)

        GLES20.glEnableVertexAttribArray(posHandle)
        vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(posHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer)

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, indexBuffer)
        GLES20.glDisableVertexAttribArray(posHandle)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    }
}
