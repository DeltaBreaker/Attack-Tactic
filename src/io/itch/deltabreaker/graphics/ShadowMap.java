package io.itch.deltabreaker.graphics;

import org.lwjgl.opengl.GL40;

import io.itch.deltabreaker.core.SettingsManager;

public class ShadowMap {
	
	public final int depthMapFBO;
	public final Texture depthMap;
	
	public ShadowMap() throws Exception {
		depthMapFBO = GL40.glGenFramebuffers();
		depthMap = new Texture(SettingsManager.shadowMapSize, SettingsManager.shadowMapSize, GL40.GL_DEPTH_COMPONENT);
		
		GL40.glBindFramebuffer(GL40.GL_FRAMEBUFFER, depthMapFBO);
		GL40.glFramebufferTexture2D(GL40.GL_FRAMEBUFFER, GL40.GL_DEPTH_ATTACHMENT, GL40.GL_TEXTURE_2D, depthMap.getID(), 0);
		
		GL40.glDrawBuffer(GL40.GL_NONE);
		GL40.glReadBuffer(GL40.GL_NONE);
		
		if (GL40.glCheckFramebufferStatus(GL40.GL_FRAMEBUFFER) != GL40.GL_FRAMEBUFFER_COMPLETE) {
            throw new Exception("[ShadowMap]: Could not create FrameBuffer");
        }
		
		GL40.glBindFramebuffer(GL40.GL_FRAMEBUFFER, 0);
	}
	
}
