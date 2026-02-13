package com.aigame.heartquest.renderer

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/**
 * A ground plane with a gradient effect for the scene floor.
 */
class GroundPlane {

    private val program: Int
    private val vertexBuffer: FloatBuffer
    private val indexBuffer: ShortBuffer

    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        attribute vec4 aPosition;
        varying vec2 vTexCoord;

        void main() {
            gl_Position = uMVPMatrix * aPosition;
            vTexCoord = aPosition.xz * 0.2;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        precision mediump float;
        varying vec2 vTexCoord;
        uniform float uAmbience;
        uniform float uTime;

        void main() {
            // Create a nice ground pattern
            float dist = length(vTexCoord);

            // Base ground color (warm stone/wood)
            vec3 groundColor = mix(
                vec3(0.25, 0.2, 0.18),
                vec3(0.4, 0.35, 0.3),
                uAmbience
            );

            // Circular gradient from center
            float gradient = smoothstep(3.0, 0.0, dist);
            groundColor *= (0.5 + gradient * 0.5);

            // Subtle tile pattern
            float tile = step(0.02, fract(vTexCoord.x * 2.0)) * step(0.02, fract(vTexCoord.y * 2.0));
            groundColor *= (0.95 + tile * 0.05);

            // Soft warm light pool in center
            float spotLight = smoothstep(1.5, 0.0, dist) * 0.15;
            groundColor += vec3(spotLight * 1.0, spotLight * 0.8, spotLight * 0.5);

            float alpha = smoothstep(4.0, 1.5, dist);
            gl_FragColor = vec4(groundColor, alpha);
        }
    """.trimIndent()

    init {
        program = GameRenderer.createProgram(vertexShaderCode, fragmentShaderCode)

        // Large ground quad
        val vertices = floatArrayOf(
            -5f, 0f,  5f,
             5f, 0f,  5f,
             5f, 0f, -5f,
            -5f, 0f, -5f
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

    fun draw(mvpMatrix: FloatArray, ambience: Float, time: Float) {
        GLES20.glUseProgram(program)

        val mvpHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        val ambienceHandle = GLES20.glGetUniformLocation(program, "uAmbience")
        val timeHandle = GLES20.glGetUniformLocation(program, "uTime")
        val posHandle = GLES20.glGetAttribLocation(program, "aPosition")

        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniform1f(ambienceHandle, ambience)
        GLES20.glUniform1f(timeHandle, time)

        GLES20.glEnableVertexAttribArray(posHandle)
        vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(posHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer)

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, indexBuffer)
        GLES20.glDisableVertexAttribArray(posHandle)
    }
}
