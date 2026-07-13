package com.weinsim.slpaint.renderengine.bufferobjects;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.ByteBuffer;

import com.weinsim.slpaint.renderengine.Cleanable;

public class PingPongFBO implements Cleanable {

    private final int fboID;
    // private final int texture0, texture1;
    public final int[] textureIDs;

    private int activeTexture;

    public PingPongFBO() {
        glGetError();
        fboID = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fboID);
        // texture0 = glGenTextures();
        // texture1 = glGenTextures();
        textureIDs = new int[] { glGenTextures(), glGenTextures() };
        // bind both textures to make sure the texture objects are actually created
        // glBindTexture(GL_TEXTURE_2D, texture0);
        // glBindTexture(GL_TEXTURE_2D, texture1);
        // glBindTexture(GL_TEXTURE_2D, 0);
        // System.out.format("error: 0x%X (NO_ERROR = 0x%X)\n", glGetError(),
        // GL_NO_ERROR);
        activeTexture = 0;
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void initInactiveTexture(int width, int height) {
        glBindTexture(GL_TEXTURE_2D, getInactiveTextureID());
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_BGRA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
    }

    public void bind() {
        bind(true);
    }

    /**
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
