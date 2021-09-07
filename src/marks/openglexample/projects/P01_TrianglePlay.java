package marks.openglexample.projects;

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
public class P01_TrianglePlay extends JFrame implements GLEventListener {

	private static final int FPS = 50;
	private GLCanvas myCanvas;
	private int rendering_program;
	private int vao[] = new int[1];
	
	private float rotation = 0.0f;
	
	public P01_TrianglePlay() {
		this.setTitle("Project 1 - Rotation and Color");
		this.setSize(600, 600);
		this.setLocation(200, 200);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		this.add(myCanvas);
		setVisible(true);
		// Creating and starting a new FPS animator, which calls the display method the input number of frames per second (50 in this case)
		FPSAnimator ani = new FPSAnimator(myCanvas, FPS);
		ani.start();
	}

	@Override
	public void display(GLAutoDrawable arg0) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glUseProgram(rendering_program);
		
		gl.glClearBufferfv(GL_COLOR, 0, Buffers.newDirectFloatBuffer(new float[] {0.0f, 0.0f, 0.0f, 1.0f}));
		
		rotation += (float) (2.0 * Math.PI / FPS / 2.0);
		if (rotation > 2 * Math.PI)
			rotation -= (float) (2 * Math.PI);
		
		
		int rot_loc = gl.glGetUniformLocation(rendering_program, "rotation");
		gl.glProgramUniform1f(rendering_program, rot_loc, rotation);
		int length_loc = gl.glGetUniformLocation(rendering_program, "length");
		gl.glProgramUniform1i(rendering_program, length_loc, myCanvas.getWidth());
		int height_loc = gl.glGetUniformLocation(rendering_program, "height");
		gl.glProgramUniform1i(rendering_program, height_loc, myCanvas.getHeight());
		
		gl.glDrawArrays(GL_TRIANGLES, 0, 3);
	}

	@Override
	public void dispose(GLAutoDrawable arg0) {
	}

	@Override
	public void init(GLAutoDrawable arg0) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		String[] vShaderData = readShaderSource("D:\\Mark\\JavaPrograms\\Resources\\OpenGLExampleResources\\P01\\vert.shader");
		int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
		gl.glShaderSource(vShader, vShaderData.length, vShaderData, null, 0);
		gl.glCompileShader(vShader);
		String[] fShaderData = readShaderSource("D:\\Mark\\JavaPrograms\\Resources\\OpenGLExampleResources\\P01\\frag.shader");
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
