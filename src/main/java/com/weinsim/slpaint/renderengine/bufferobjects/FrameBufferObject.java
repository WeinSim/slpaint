package com.weinsim.slpaint.renderengine.bufferobjects;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

import com.weinsim.slpaint.renderengine.Cleanable;

public class FrameBufferObject implements Cleanable {

    public final int fboID;
    public final int textureID;
    public final int width;
    public final int height;

    public FrameBufferObject(int width, int height) {
        this.width = width;
        this.height = height;

        // https://learnopengl.com/Advanced-OpenGL/Framebuffers

        // create framebuffer
        fboID = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fboID);

        // create texture
        textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (int[]) null);
        // unbind texture
        glBindTexture(GL_TEXTURE_2D, 0);

        // attach texture to framebuffer
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureID, 0);
    }

    @Override
    public void cleanUp() {
        glDeleteFramebuffers(fboID);
        glDeleteTextures(textureID);
    }

}
