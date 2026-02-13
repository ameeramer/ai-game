package com.aigame.heartquest.renderer

import android.opengl.GLES20
import com.aigame.heartquest.game.NpcAnimation
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.cos
import kotlin.math.sin

/**
 * 3D stylized character model for Adrian (NPC).
 * Built from geometric primitives to create a low-poly stylized human figure.
 */
class NpcCharacterModel {

    private val program: Int

    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        uniform float uTime;
        uniform int uAnimation;
        attribute vec4 aPosition;
        attribute vec4 aColor;
        varying vec4 vColor;
        varying float vHeight;

        void main() {
            vec4 pos = aPosition;
            vHeight = pos.y;

            // Apply subtle vertex animation
            if (uAnimation == 1) { // HAPPY
                pos.y += sin(uTime * 4.0 + pos.x * 2.0) * 0.02;
            } else if (uAnimation == 2) { // FLIRTY
                pos.x += sin(uTime * 2.0 + pos.y * 3.0) * 0.01;
            }

            gl_Position = uMVPMatrix * pos;
            vColor = aColor;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        precision mediump float;
        varying vec4 vColor;
        varying float vHeight;
        uniform float uAmbience;
        uniform float uTime;

        void main() {
            // Apply lighting based on height and ambience
            float light = mix(0.6, 1.0, uAmbience);
            float rim = smoothstep(0.0, 2.0, vHeight) * 0.2;
            vec3 color = vColor.rgb * light + rim;

            // Subtle pulsing glow
            float glow = sin(uTime * 2.0) * 0.05 + 0.95;
            color *= glow;

            gl_FragColor = vec4(color, vColor.a);
        }
    """.trimIndent()

    // Character parts built from primitives
    private val headVertexBuffer: FloatBuffer
    private val headIndexBuffer: ShortBuffer
    private val headIndexCount: Int

    private val bodyVertexBuffer: FloatBuffer
    private val bodyIndexBuffer: ShortBuffer
    private val bodyIndexCount: Int

    private val hairVertexBuffer: FloatBuffer
    private val hairIndexBuffer: ShortBuffer
    private val hairIndexCount: Int

    init {
        program = GameRenderer.createProgram(vertexShaderCode, fragmentShaderCode)

        // Head - sphere approximation (skin tone)
        val headData = createSphere(0f, 1.8f, 0f, 0.18f, 12, 8,
            floatArrayOf(0.96f, 0.82f, 0.71f, 1.0f)) // skin color
        headVertexBuffer = headData.first
        headIndexBuffer = headData.second
        headIndexCount = headData.third

        // Body - torso + legs (navy blazer / clothing)
        val bodyData = createBody()
        bodyVertexBuffer = bodyData.first
        bodyIndexBuffer = bodyData.second
        bodyIndexCount = bodyData.third

        // Hair - dark swept style
        val hairData = createHair()
        hairVertexBuffer = hairData.first
        hairIndexBuffer = hairData.second
        hairIndexCount = hairData.third
    }

    fun draw(mvpMatrix: FloatArray, animation: NpcAnimation, time: Float, ambience: Float) {
        GLES20.glUseProgram(program)

        val mvpHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        val timeHandle = GLES20.glGetUniformLocation(program, "uTime")
        val animHandle = GLES20.glGetUniformLocation(program, "uAnimation")
        val ambienceHandle = GLES20.glGetUniformLocation(program, "uAmbience")

        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniform1f(timeHandle, time)
        GLES20.glUniform1i(animHandle, animation.ordinal)
        GLES20.glUniform1f(ambienceHandle, ambience)

        // Draw body first (behind head)
        drawPart(bodyVertexBuffer, bodyIndexBuffer, bodyIndexCount)

        // Draw head
        drawPart(headVertexBuffer, headIndexBuffer, headIndexCount)

        // Draw hair on top
        drawPart(hairVertexBuffer, hairIndexBuffer, hairIndexCount)
    }

    private fun drawPart(vertexBuffer: FloatBuffer, indexBuffer: ShortBuffer, indexCount: Int) {
        val posHandle = GLES20.glGetAttribLocation(program, "aPosition")
        val colorHandle = GLES20.glGetAttribLocation(program, "aColor")

        GLES20.glEnableVertexAttribArray(posHandle)
        GLES20.glEnableVertexAttribArray(colorHandle)

        // Stride: 7 floats per vertex (3 pos + 4 color) = 28 bytes
        vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(posHandle, 3, GLES20.GL_FLOAT, false, 28, vertexBuffer)

        vertexBuffer.position(3)
        GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 28, vertexBuffer)

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer)

        GLES20.glDisableVertexAttribArray(posHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
    }

    private fun createSphere(
        cx: Float, cy: Float, cz: Float, radius: Float,
        slices: Int, stacks: Int, color: FloatArray
    ): Triple<FloatBuffer, ShortBuffer, Int> {
        val vertices = mutableListOf<Float>()
        val indices = mutableListOf<Short>()

        for (i in 0..stacks) {
            val phi = Math.PI * i / stacks
            for (j in 0..slices) {
                val theta = 2.0 * Math.PI * j / slices
                val x = cx + radius * sin(phi).toFloat() * cos(theta).toFloat()
                val y = cy + radius * cos(phi).toFloat()
                val z = cz + radius * sin(phi).toFloat() * sin(theta).toFloat()

                vertices.addAll(listOf(x, y, z))
                vertices.addAll(color.toList())
            }
        }

        for (i in 0 until stacks) {
            for (j in 0 until slices) {
                val first = (i * (slices + 1) + j).toShort()
                val second = (first + slices + 1).toShort()

                indices.addAll(listOf(first, second, (first + 1).toShort()))
                indices.addAll(listOf((first + 1).toShort(), second, (second + 1).toShort()))
            }
        }

        return Triple(
            createFloatBuffer(vertices.toFloatArray()),
            createShortBuffer(indices.toShortArray()),
            indices.size
        )
    }

    private fun createBody(): Triple<FloatBuffer, ShortBuffer, Int> {
        val vertices = mutableListOf<Float>()
        val indices = mutableListOf<Short>()
        var indexOffset: Short = 0

        // Torso (tapered box) - Navy blazer
        val blazerColor = floatArrayOf(0.12f, 0.14f, 0.35f, 1.0f)
        indexOffset = addTaperedBox(vertices, indices, indexOffset,
            0f, 1.2f, 0f,          // center
            0.28f, 0.45f, 0.15f,    // top half-widths
            0.22f, 0.45f, 0.12f,    // bottom half-widths
            blazerColor)

        // Shirt collar peek
        val shirtColor = floatArrayOf(0.95f, 0.95f, 0.97f, 1.0f)
        indexOffset = addTaperedBox(vertices, indices, indexOffset,
            0f, 1.55f, 0.02f,
            0.10f, 0.05f, 0.08f,
            0.12f, 0.05f, 0.10f,
            shirtColor)

        // Left arm
        indexOffset = addTaperedBox(vertices, indices, indexOffset,
            -0.35f, 1.15f, 0f,
            0.07f, 0.35f, 0.07f,
            0.06f, 0.35f, 0.06f,
            blazerColor)

        // Right arm
        indexOffset = addTaperedBox(vertices, indices, indexOffset,
            0.35f, 1.15f, 0f,
            0.07f, 0.35f, 0.07f,
            0.06f, 0.35f, 0.06f,
            blazerColor)

        // Left hand (skin)
        val skinColor = floatArrayOf(0.96f, 0.82f, 0.71f, 1.0f)
        indexOffset = addTaperedBox(vertices, indices, indexOffset,
            -0.35f, 0.75f, 0f,
            0.05f, 0.06f, 0.04f,
            0.04f, 0.06f, 0.04f,
            skinColor)

        // Right hand (skin)
        indexOffset = addTaperedBox(vertices, indices, indexOffset,
            0.35f, 0.75f, 0f,
            0.05f, 0.06f, 0.04f,
            0.04f, 0.06f, 0.04f,
            skinColor)

        // Pants - dark grey
        val pantsColor = floatArrayOf(0.15f, 0.15f, 0.18f, 1.0f)

        // Left leg
        indexOffset = addTaperedBox(vertices, indices, indexOffset,
            -0.10f, 0.4f, 0f,
            0.10f, 0.38f, 0.10f,
            0.08f, 0.38f, 0.08f,
            pantsColor)

        // Right leg
        indexOffset = addTaperedBox(vertices, indices, indexOffset,
            0.10f, 0.4f, 0f,
            0.10f, 0.38f, 0.10f,
            0.08f, 0.38f, 0.08f,
            pantsColor)

        // Shoes - dark brown
        val shoeColor = floatArrayOf(0.2f, 0.12f, 0.08f, 1.0f)
        indexOffset = addTaperedBox(vertices, indices, indexOffset,
            -0.10f, 0.03f, 0.02f,
            0.08f, 0.04f, 0.12f,
            0.08f, 0.04f, 0.12f,
            shoeColor)

        addTaperedBox(vertices, indices, indexOffset,
            0.10f, 0.03f, 0.02f,
            0.08f, 0.04f, 0.12f,
            0.08f, 0.04f, 0.12f,
            shoeColor)

        return Triple(
            createFloatBuffer(vertices.toFloatArray()),
            createShortBuffer(indices.toShortArray()),
            indices.size
        )
    }

    private fun createHair(): Triple<FloatBuffer, ShortBuffer, Int> {
        val vertices = mutableListOf<Float>()
        val indices = mutableListOf<Short>()
        var indexOffset: Short = 0
        val hairColor = floatArrayOf(0.1f, 0.08f, 0.06f, 1.0f) // dark brown/black

        // Main hair volume on top of head
        indexOffset = addTaperedBox(vertices, indices, indexOffset,
            0f, 1.95f, -0.02f,
            0.19f, 0.06f, 0.18f,
            0.20f, 0.06f, 0.19f,
            hairColor)

        // Side hair left
        indexOffset = addTaperedBox(vertices, indices, indexOffset,
            -0.16f, 1.85f, 0f,
            0.05f, 0.10f, 0.14f,
            0.04f, 0.10f, 0.12f,
            hairColor)

        // Side hair right
        indexOffset = addTaperedBox(vertices, indices, indexOffset,
            0.16f, 1.85f, 0f,
            0.05f, 0.10f, 0.14f,
            0.04f, 0.10f, 0.12f,
            hairColor)

        // Back hair
        addTaperedBox(vertices, indices, indexOffset,
            0f, 1.82f, -0.14f,
            0.16f, 0.12f, 0.04f,
            0.14f, 0.12f, 0.04f,
            hairColor)

        return Triple(
            createFloatBuffer(vertices.toFloatArray()),
            createShortBuffer(indices.toShortArray()),
            indices.size
        )
    }

    private fun addTaperedBox(
        vertices: MutableList<Float>,
        indices: MutableList<Short>,
        offset: Short,
        cx: Float, cy: Float, cz: Float,
        topHw: Float, topHh: Float, topHd: Float,
        botHw: Float, botHh: Float, botHd: Float,
        color: FloatArray
    ): Short {
        // 8 vertices for a tapered box
        val verts = floatArrayOf(
            // Top face (y + hh)
            cx - topHw, cy + topHh, cz + topHd,  // 0: top-front-left
            cx + topHw, cy + topHh, cz + topHd,  // 1: top-front-right
            cx + topHw, cy + topHh, cz - topHd,  // 2: top-back-right
            cx - topHw, cy + topHh, cz - topHd,  // 3: top-back-left
            // Bottom face (y - hh)
            cx - botHw, cy - botHh, cz + botHd,  // 4: bot-front-left
            cx + botHw, cy - botHh, cz + botHd,  // 5: bot-front-right
            cx + botHw, cy - botHh, cz - botHd,  // 6: bot-back-right
            cx - botHw, cy - botHh, cz - botHd,  // 7: bot-back-left
        )

        for (i in verts.indices step 3) {
            vertices.add(verts[i])
            vertices.add(verts[i + 1])
            vertices.add(verts[i + 2])
            vertices.addAll(color.toList())
        }

        val o = offset.toInt()
        val boxIndices = shortArrayOf(
            // Front
            (o + 0).toShort(), (o + 1).toShort(), (o + 5).toShort(),
            (o + 0).toShort(), (o + 5).toShort(), (o + 4).toShort(),
            // Right
            (o + 1).toShort(), (o + 2).toShort(), (o + 6).toShort(),
            (o + 1).toShort(), (o + 6).toShort(), (o + 5).toShort(),
            // Back
            (o + 2).toShort(), (o + 3).toShort(), (o + 7).toShort(),
            (o + 2).toShort(), (o + 7).toShort(), (o + 6).toShort(),
            // Left
            (o + 3).toShort(), (o + 0).toShort(), (o + 4).toShort(),
            (o + 3).toShort(), (o + 4).toShort(), (o + 7).toShort(),
            // Top
            (o + 0).toShort(), (o + 3).toShort(), (o + 2).toShort(),
            (o + 0).toShort(), (o + 2).toShort(), (o + 1).toShort(),
            // Bottom
            (o + 4).toShort(), (o + 5).toShort(), (o + 6).toShort(),
            (o + 4).toShort(), (o + 6).toShort(), (o + 7).toShort(),
        )
        indices.addAll(boxIndices.toList())

        return (o + 8).toShort()
    }

    private fun createFloatBuffer(data: FloatArray): FloatBuffer {
        return ByteBuffer.allocateDirect(data.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(data)
                position(0)
            }
    }

    private fun createShortBuffer(data: ShortArray): ShortBuffer {
        return ByteBuffer.allocateDirect(data.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .apply {
                put(data)
                position(0)
            }
    }
}
