package marks.openglexample.examples;

import java.nio.FloatBuffer;

import javax.swing.JFrame;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import static com.jogamp.opengl.GL4.GL_COLOR;

@SuppressWarnings("serial")
public class E01_BackgroundColor extends JFrame implements GLEventListener {

	private GLCanvas myCanvas;
	
	public E01_BackgroundColor() {
		// Setting up JFrame Properties
		this.setTitle("Background Color");
		this.setSize(600, 400);
		this.setLocation(200, 200);
		// Initializing the GLCanvas to use and connecting it with the JFrame.
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		this.add(myCanvas);
		/* Making the JFrame window visible.  When this occurs with a GLCanvas object added to it, it automatically initizalizes
		 * OpenGL, which creates a GL4 object that will be retrieved later for method calls.
		 */
		setVisible(true);
	}

	// Called after init.  When animation is used, display will be called multiple times.
	@Override
	public void display(GLAutoDrawable arg0) {
		// Creating gl, a GL4 object.  GL4 is an interface containing the OpenGL functions.
		GL4 gl = (GL4) GLContext.getCurrentGL();
		// creating a rgba float to represent the color of the background.
		float bkg[] = {1.0f, 0.56f, 0.0f, 1.0f};
		// turning the float array into a buffer that can be used in the gl method used after this one.
		FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
		/* This method clears the current buffer to a new specified one.  The glClearBufferfv does this.  Each parameter:
		 * (int) GL_COLOR: an integer reference to a certain buffer.  In this case, GL_COLOR references the color buffer.
		 * (int) 0: Specifies which buffer to use;  there are more than one color buffer.  This example uses the first one.
		 * (FloatBuffer) bkgBuffer: the new buffer to set the input buffer to.
		 */
		gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);
	}

	// Called when the window is closed
	@Override
	public void dispose(GLAutoDrawable arg0) {
	}

	// Called directly after the window is shown.  init is usually used to read GLSL code and load 3D models (and more).
	@Override
	public void init(GLAutoDrawable arg0) {
	}

	// Called when the window is resized
	@Override
	public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) {
	}

}
