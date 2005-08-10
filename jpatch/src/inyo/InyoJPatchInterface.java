package inyo;

/**
 * This interface is used by Inyo to communicate with JPatch.
 * JPatch will pass an object implementing this interface to
 * Inyo when invoking the renderer.
 */

public interface InyoJPatchInterface {
	
	/**
	 * Tell JPatch about the rendering progress
	 * @param progress 0.0 means rendering just started, 0.5 means half way done, 1.0 means rendering finished.
	 */
	public void progress(double progress);
	
	/**
	 * Pass the rendered image back to JPatch
	 * @param image the final image
	 */
	public void renderingDone(java.awt.Image image);
	
}
