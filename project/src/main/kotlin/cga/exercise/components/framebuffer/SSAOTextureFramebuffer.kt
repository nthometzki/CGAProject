package cga.exercise.components.framebuffer

import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.texture.Texture2D
import cga.framework.GLError
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import kotlin.random.Random

class SSAOTextureFramebuffer(_width : Int, _height : Int) : Framebuffer(_width, _height) {

    lateinit var ssaoColorTexture : Texture2D
        private set
    lateinit var ssaoNoiseTexture : Texture2D
        private set
    private lateinit var ssaoSamples : ArrayList<Vector3f>

    fun startRender(shader : ShaderProgram, gFramebuffer : GeometryFramebuffer)
    {
        startRender(shader)
        gFramebuffer.gPosition.bind(0)
        shader.setUniform("gPosition", 0)
        gFramebuffer.gNormal.bind(1)
        shader.setUniform("gNormal", 1)
        ssaoNoiseTexture.bind(2)
        shader.setUniform("texNoise", 2)

        shader.setUniform("screenSize", Vector2f(width.toFloat(), height.toFloat())); GLError.checkThrow()
        shader.setUniform("samples", ssaoSamples); GLError.checkThrow()
    }

    override fun configureFramebuffer() {
        ssaoColorTexture = createTextureAttachment(0, false, GL11.GL_RED, GL11.GL_RED, GL11.GL_FLOAT)
        //ssaoColorTexture = createTextureAttachment(0, false)
        ssaoColorTexture.setTexParams(GL11.GL_NEAREST, GL11.GL_NEAREST)

        val ssaoNoise = ArrayList<Float>()
        for (i in 0..16)
        {
            ssaoNoise.add(Random.nextFloat() * 2.0f - 1.0f)
            ssaoNoise.add(Random.nextFloat() * 2.0f - 1.0f)
            ssaoNoise.add(0.0f)
        }


        ssaoNoiseTexture = Texture2D(ssaoNoise.toFloatArray(), 4, 4, false, GL11.GL_RGB16, GL11.GL_RGB, GL11.GL_FLOAT)
        ssaoNoiseTexture.setTexParams(GL11.GL_REPEAT, GL11.GL_REPEAT, GL11.GL_NEAREST, GL11.GL_NEAREST)

        ssaoSamples = ArrayList<Vector3f>()
        var lerp = {a :Float, b : Float, f :Float -> a + f * (b - a)}

        for (i : Int in 0..64)
        {
            val sample = Vector3f(
                    Random.nextFloat() * 2.0f -1.0f,
                    Random.nextFloat() * 2.0f -1.0f,
                    Random.nextFloat() * 2.0f -1.0f)

            sample.normalize()
            sample.mul(Random.nextFloat())
            var scale = i.toFloat() / 64.0f
            scale = lerp(0.1f, 1.0f, scale * scale)
            sample.mul(scale)
            ssaoSamples.add(sample)
        }

    }

    override fun initRender(shader: ShaderProgram) {
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f); GLError.checkThrow()
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT); GLError.checkThrow()
        GL11.glDisable(GL11.GL_DEPTH_TEST)
    }
}