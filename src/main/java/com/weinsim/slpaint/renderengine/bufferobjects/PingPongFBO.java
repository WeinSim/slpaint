package com.weinsim.slpaint.renderengine.bufferobjects;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.ByteBuffer;

import com.weinsim.slpaint.renderengine.Cleanable;

public class PingPongFBO implements Cleanable {

    private final int fboID;
    public final int[] textureIDs;

    private int activeTexture;

    public PingPongFBO() {
        glGetError();
        fboID = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fboID);
        textureIDs = new int[] { glGenTextures(), glGenTextures() };
        // System.out.format("error: 0x%X (NO_ERROR = 0x%X)\n", glGetError(),
        // GL_NO_ERROR);
        activeTexture = 0;
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    /**
     * Wrapper around {@code glTexImage2D}. It is only called if the texture is not
     * initialized or the texture size changes.
     * 
     * @param active Which of the two textures to work on. {@code true} refers to
     *               the active texture, {@code false} to the inactive one.
     * @param width  the desired width of the texture
     * @param height the desired height of the texture
     */
    public void setTextureSize(boolean active, int width, int height) {
        int textureID = textureIDs[active ? activeTexture : 1 - activeTexture];
        glBindTexture(GL_TEXTURE_2D, textureID);
        int currentWidth = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH),
                currentHeight = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH);
        if (currentWidth != width || currentHeight != height)
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_BGRA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
    }

    /**
     * Binds this framebuffer and attaches the active for rendering.
     */
    public void bind() {
        bind(true);
    }

    /**
     * Binds this framebuffer and attaches the specified texture (active / inactive)
     * for rendering.
     * 
     * @param drawOnActiveTexture Whether to use the currently active texture as the
     *                            rendering target. Passing {@code false} will
     *                            render to the inactive texture.
     */
    public void bind(boolean drawOnActiveTexture) {
        glBindFramebuffer(GL_FRAMEBUFFER, fboID);
        int textureID = textureIDs[drawOnActiveTexture ? activeTexture : 1 - activeTexture];
        // detach the other texture to prevent render outputs being clipped to the
        // smaller of the 2 textures
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureID, 0);
        glDrawBuffers(GL_COLOR_ATTACHMENT0);
    }

    /**
     * Unbinds the FBO and detaches the texture.
     */
    public void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, 0, 0);
    }

    @Override
    public void cleanUp() {
        glDeleteFramebuffers(fboID);
        glDeleteTextures(textureIDs[0]);
        glDeleteTextures(textureIDs[1]);
    }

    public int getTextureID() {
        return textureIDs[activeTexture];
    }

    public int getInactiveTextureID() {
        return textureIDs[1 - activeTexture];
    }

    /**
     * Swaps which texture is considered active and which one is considered
     * inactive.
     */
    public void swapTextures() {
        activeTexture = 1 - activeTexture;
    }

    public int getActiveTexture() {
        return activeTexture;
    }

    private void checkFramebuffer() {
        glBindFramebuffer(GL_FRAMEBUFFER, fboID);
        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        System.out.format("status = 0x%X, complete = 0x%X\n", status, GL_FRAMEBUFFER_COMPLETE);
    }

}
