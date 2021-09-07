package marks.openglexample.examples;

import javax.swing.JFrame;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

import static com.jogamp.opengl.GL4.*;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;


@SuppressWarnings("serial")
public class E06_BasicAnimation extends JFrame implements GLEventListener {
	
	private GLCanvas myCanvas;
	private int rendering_program;
	private int vao[] = new int[1];
	
	private float x = 0.0f;
	private float inc = 0.01f;
	
	public E06_BasicAnimation() {
		this.setTitle("BasicAnimation");
		this.setSize(600, 400);
		this.setLocation(200, 200);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		this.add(myCanvas);
		setVisible(true);
		// Creating and starting a new FPS animator, which calls the display method the input number of frames per second (50 in this case)
		FPSAnimator ani = new FPSAnimator(myCanvas, 50);
		ani.start();
	}
	
	@Override
	public void display(GLAutoDrawable arg0) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glUseProgram(rendering_program);
		
		// clearing the background to black to reset the canvas.  Without doing this, the triangle would leave a trail.  Same command as E01, but simplified.
		gl.glClearBufferfv(GL_COLOR, 0, Buffers.newDirectFloatBuffer(new float[] {0.0f, 0.0f, 0.0f, 1.0f}));
		
		// moving the triangle.
		x += inc;
		if (Math.abs(x) > 0.9f)
			inc *= -1;
		
		/* In order to communicate with the vertex shader where in space it will be at a given moment, a uniform variable is created that is passed
		 * in inside the vertex shader (see GLSL code for vert.shader).  The first command glGetUniformLocation creates a pointer to a uniform variable
		 * "offset", and glProgramUniform1f passes in the value 'x' as the float value for "offset".  From this point, when glDrawArrays is called, a
		 * value for offSet is known and can be calculated.
		 * One final note:  ProgramUniform1f ends with "1f".  If an object type other than float is wanted, there are other options to pass in other data types.
		 */
		int offset_loc = gl.glGetUniformLocation(rendering_program, "offset");
		gl.glProgramUniform1f(rendering_program, offset_loc, x);
		
		gl.glDrawArrays(GL_TRIANGLES, 0, 3);
	}
	
	@Override
	public void dispose(GLAutoDrawable arg0) {
	}
	
	@Override
	public void init(GLAutoDrawable arg0) {
		// The same code as E04.
		GL4 gl = (GL4) GLContext.getCurrentGL();
		String[] vShaderData = readShaderSource("D:\\Mark\\JavaPrograms\\Resources\\OpenGLExampleResources\\E06\\vert.shader");
		int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
		gl.glShaderSource(vShader, vShaderData.length, vShaderData, null, 0);
		gl.glCompileShader(vShader);
		String[] fShaderData = readShaderSource("D:\\Mark\\JavaPrograms\\Resources\\OpenGLExampleResources\\E06\\frag.shader");
		int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);
		gl.glShaderSource(fShader, fShaderData.length, fShaderData, null, 0);
		gl.glCompileShader(fShader);
		rendering_program = gl.glCreateProgram();
		gl.glAttachShader(rendering_program, vShader);
		gl.glAttachShader(rendering_program, fShader);
		gl.glLinkProgram(rendering_program);
		
		gl.glDeleteShader(vShader);
		gl.glDeleteShader(fShader);
		
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
	}
	
	@Override
	public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) {
	}
	
	private String[] readShaderSource(String filename) {
		// The same code as E04.
		Vector<String> lines = new Vector<>();
		try(Scanner sc = new Scanner(new File(filename))) {
			while(sc.hasNext())
				lines.addElement(sc.nextLine());
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
		String[] result = new String[lines.size()];
		for (int i = 0; i < lines.size(); i++)
			result[i] = lines.get(i) + "\n";
		return result;
	}
	
}
