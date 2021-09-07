package marks.openglexample.examples;

import javax.swing.JFrame;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;

import static com.jogamp.opengl.GL4.*;

// NOTE -- This code (except for checkOpenGLError) is the same of that of example E02.
// In this code, a typo has been places in the fragment shader GLSL code and will be logged by the openGL error checker method.
@SuppressWarnings("serial")
public class E03_ErrorDetection extends JFrame implements GLEventListener {
	
	private GLCanvas myCanvas;
	private int rendering_program;
	private int vao[] = new int[1];
	
	public E03_ErrorDetection() {
		this.setTitle("ErrorDetection");
		this.setSize(600, 400);
		this.setLocation(200, 200);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		this.add(myCanvas);
		setVisible(true);
	}
	
	@Override
	public void display(GLAutoDrawable arg0) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glUseProgram(rendering_program);
		gl.glPointSize(30.0f);
		gl.glDrawArrays(GL_POINTS, 0, 1);
		checkOpenGLError();
	}
	
	@Override
	public void dispose(GLAutoDrawable arg0) {
	}
	
	@Override
	public void init(GLAutoDrawable arg0) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		rendering_program = createShaderProgram();
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
	}
	
	@Override
	public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) {
	}
	
	private int createShaderProgram() {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		String vShaderSource[] = {
				"#version 430	\n",
				"void main(void) \n",
				"{ gl_Position = vec4(0.0, 0.0, 0.0, 1.0); } \n"
		};
		
		// This fragment shader contains an error: "===" instead of "=".
		String fShaderSource[] = {
				"#version 430	\n",
				"out vec4 color; \n",
				"void main(void) \n",
				"{ color === vec4(1.0, 1.0, 1.0, 1.0); } \n"
		};
		
		int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
		gl.glShaderSource(vShader, 3, vShaderSource, null, 0);
		gl.glCompileShader(vShader);
		printShaderLog(vShader);
		
		int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);
		gl.glShaderSource(fShader,  4, fShaderSource, null, 0);
		gl.glCompileShader(fShader);
		printShaderLog(fShader);
		
		int vfprogram = gl.glCreateProgram();
		gl.glAttachShader(vfprogram, vShader);
		gl.glAttachShader(vfprogram, fShader);
		gl.glLinkProgram(vfprogram);
		printProgramLog(vfprogram);
		
		gl.glDeleteShader(vShader);
		gl.glDeleteShader(fShader);
		
		return vfprogram;
	}
	
	// This method checks for error in the OpenGL Error log.  This method doesn't catch GLSL errors, though.
	private boolean checkOpenGLError() {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		boolean foundError = false;
		// Creating an OpenGL Utility Library to transform errors from integer (id) form to a readable form the programmer can understand.
		GLU glu = new GLU();
		/*	glGetError returns an integer id of a certain type of error logged in OpenGL.  If there are no errors, an integer value equal to that of
		 * 	GL_NO_ERROR is output.  If there is more than one error, the first error will be returned, then the next, etc. until no more errors are present.
		 */
		int glErr = gl.glGetError();
		while (glErr != GL_NO_ERROR) {
			// outputs the error in System.err
			System.err.println("glError: " + glu.gluErrorString(glErr));
			foundError = true;
			// finds the next error id.
			glErr = gl.glGetError();
		}
		return foundError;
	}
	
	// Displays the contents of OpenGL's log when GLSL compilation fails.
	private void printShaderLog(int shader) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] len = new int[1], chWrittn = new int[1];
		byte[] log = null;
		
		// determines the length of the shader compilation log and stores it in len[0].
		// Note - glGetShaderiv returns a parameter from a shader object.  There are other options besides GL_INFO_LOG_LENGTH.
		gl.glGetShaderiv(shader, GL_INFO_LOG_LENGTH, len, 0);
		if (len[0] > 0) {
			log = new byte[len[0]];
			/* 	This method puts the information log into a byte array that can be turned into characters.  The len[0] specifies the maximum size of the returned byte
			 * 	buffer. chWritten and 0 constitute an intBuffer (in this case, null), that specifies the length of the returned string -- not important.  The log (byte[]) is
			 * 	the output for the log, with 0 as the starting index in the array.
			 */
			gl.glGetShaderInfoLog(shader, len[0], chWrittn, 0, log, 0);
			System.out.println("Shader Info Log: ");
			for (int i = 0; i < log.length; i++)
				System.out.print((char) log[i]);
		}
	}
	
	// Displays the contents of OpenGL's log when GLSL linking fails.
	private void printProgramLog(int program) {
		// The following code is the same as printShaderLog, but a program is accessed instead of a shader.
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] len = new int[1], chWrittn = new int[1];
		byte[] log = null;
		
		gl.glGetProgramiv(program, GL_INFO_LOG_LENGTH, len, 0);
		if (len[0] > 0) {
			log = new byte[len[0]];
			gl.glGetProgramInfoLog(program, len[0], chWrittn, 0, log, 0);
			System.out.println("Program Info Log: ");
			for (int i = 0; i < log.length; i++)
				System.out.print((char) log[i]);
		}
	}

}
