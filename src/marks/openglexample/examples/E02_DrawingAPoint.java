package marks.openglexample.examples;

import javax.swing.JFrame;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import static com.jogamp.opengl.GL4.*;

@SuppressWarnings("serial")
public class E02_DrawingAPoint extends JFrame implements GLEventListener {
	
	private GLCanvas myCanvas;
	private int rendering_program;
	private int vao[] = new int[1];
	
	public E02_DrawingAPoint() {
		// The same constructor and notes as E01.
		this.setTitle("DrawingAPoint");
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
		// loads the program containing the two compiled shaders onto the OpenGL pipeline.
		gl.glUseProgram(rendering_program);
		// sets the vertex (point) size on rendering.
		gl.glPointSize(30.0f);
		/* The following method executes the GLSL code put into the pipeline (In this case, the two String arrays in createShaderSource)
		 * This method should be called after all of the vertexes have been prepared and loaded onto the pipeline.  Code that may change how
		 * an object is drawn (such as pointSize) should be executed BEFORE glDrawArrays.  This method starts the next step in the pipeline.
		 * (int) GL_POINTS: an integer reference to the type of primitive to be drawn.  These include GL_POINTS, GL_LINES, GL_TRIANGLES.
		 * (int) 0: the vertex to start with when rendering.  In this case, the first vertex should be rendered.
		 * (int) 1: the number of vertexes to be rendered in total.
		 */
		gl.glDrawArrays(GL_POINTS, 0, 1);
	}
	
	@Override
	public void dispose(GLAutoDrawable arg0) {
	}
	
	@Override
	public void init(GLAutoDrawable arg0) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		// calls createShaderProgram()
		rendering_program = createShaderProgram();
		/* vao: a "Vertex Array Object," containers fpr bufferes used when sets of data are sent down in the pipeline.  At least
		 * one vao is required when using shaders.  In this case, a vao isn't necessary as only one point is being rendered, and this
		 * code is only here to avoid an exception.  More information about vaos can be found in future examples.
		 */
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
	}
	
	@Override
	public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) {
	}
	
	private int createShaderProgram() {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		// The following string arrays contain GLSL code that will be used to render the shaders (points).
		
		/* The vShaderSource represents the GLSL code for a vertex shader, identifying where in space the vertex lies.
		 * Each line is separated by \n and is broken down as follows:
		 * version: Indicates the OpenGL Version
		 * void main(void): the header for the method to be run when called
		 * {}: the body of the method.  Here, gl_Position is set to a vec4, a 4-tuple capable of holding a vertex's XYZ coordinates and
		 * 		a fourth value that will be used later.  gl_Position does not need to be returned as it is already a predefines output variable.
		 */
		String vShaderSource[] = {
				"#version 430	\n",
				"void main(void) \n",
				"{ gl_Position = vec4(0.0, 0.0, 0.0, 1.0); } \n"
		};
		
		/* The fShaderSource represents the GLSL code for a fragment shader, setting the RGB color of a point.
		 * Each line is separated by \n and is broken down as follows:
		 * version: Indicates the OpenGL Version
		 * out: specifies that color is output by the method.
		 * void main(void): the header for the method to be run when called
		 * {}: the body of the method.  color is initialized here.
		 */
		String fShaderSource[] = {
				"#version 430	\n",
				"out vec4 color; \n",
				"void main(void) \n",
				"{ color = vec4(1.0, 1.0, 1.0, 1.0); } \n"
		};
		
		// createShader loads an empty shader of a specified type.  In this case, GL_VERTEX_SHADER is input.
		int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
		/* glShaderSource loads the specified shader onto the empty shader object.
		 * (int) vShader: the integer pointer to the shader object
		 * (int) 3: the number of lines of code inside the input GLSL code
		 * (String[]) vShaderSource: the GLSL code, contained in a String array.
		 * (int[]) and (int): Variables that will be used later.
		 */
		gl.glShaderSource(vShader, 3, vShaderSource, null, 0);
		// Compiles the shader
		gl.glCompileShader(vShader);
		
		// The same process as above, but now with the fragment shader.
		int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);
		gl.glShaderSource(fShader,  4, fShaderSource, null, 0);
		gl.glCompileShader(fShader);
		
		// glCreateProgram creates a program object, and outputs an integer pointer to it.  A program object contains a series of compiled shaders.
		int vfprogram = gl.glCreateProgram();
		// Loading the shaders onto the program.
		gl.glAttachShader(vfprogram, vShader);
		gl.glAttachShader(vfprogram, fShader);
		// requests that the GLSL compiler ensures that the shaders are compatable.
		gl.glLinkProgram(vfprogram);
		
		// deletes that shader pointers, as they have already been loaded onto a program and are no longer needed.
		gl.glDeleteShader(vShader);
		gl.glDeleteShader(fShader);
		
		return vfprogram;
	}

}
