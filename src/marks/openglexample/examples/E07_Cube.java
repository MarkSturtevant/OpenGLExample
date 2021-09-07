package marks.openglexample.examples;

import javax.swing.JFrame;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;

import graphicslib3D.Matrix3D;

import static com.jogamp.opengl.GL4.*;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Scanner;
import java.util.Vector;

@SuppressWarnings("serial")
public class E07_Cube extends JFrame implements GLEventListener {
	
	private GLCanvas myCanvas;
	private int rendering_program;
	// Vertex Array Object (VAO) - This array holds pointers to VAOs, which organize VBOs (next comment).
	private int vao[] = new int[1];
	/* Vertex Buffer Object (VBO) - This array holds pointers to VBOs.  VBOs hold vertex attributes (location)
	 * and are used to transfer these attributes to the GPU and OpenGL whenever necessary.  VBOs can communicate with
	 * the shader files.  In this case, in vert.shader, the line of code
	 * 			layout(location = 0) in vec3 position;
	 * specifies how this happens; in this case, the "layout(location = 0)" portion tells the VBO how to associate
	 * the vertex attribute with itself, and the "in vec3 position" shows that the VBO will give the shader a vec3
	 * value, in this case, corresponding to the vertex's coordinates.
	 */
	private int vbo[] = new int[1];
	private float cameraX, cameraY, cameraZ;
	private float cubeX, cubeY, cubeZ;
	private Matrix3D pMat; // perspective matrix
	
	public E07_Cube() {
		// The same constructor and notes as E02.
		this.setTitle("Cube");
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
		// Resets the depth buffer, allowing hidden surface removal to function properly in the case that the buffer contains a bad value.
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		// Remember, useProgram only installs the GLSL code onto the GPU.  It doesn't run it yet!
		gl.glUseProgram(rendering_program);
		
		/* Creating the transformations for the cubes location.  The end result is mvMat, a collection of matrix transformations
		 * the transport the original cube coordinates through a translation to finally align the cube in front of the OpenGL
		 * camera at a different location.
		 */
		Matrix3D vMat = new Matrix3D();
		vMat.translate(-cameraX, -cameraY, -cameraZ);
		Matrix3D mMat = new Matrix3D();
		mMat.translate(cubeX, cubeY, cubeZ);
		Matrix3D mvMat = new Matrix3D();
		/* A few concepts to know about the graphicslib3D Matrix3D class:
		 *  - It is a 4x4 matrix.
		 *  - It initializes (when the default constructor is called) to an identity matrix.
		 *  - The concatenate method performs vector multiplication; in the next case, mvMat = mvMat * vMat.
		 */
		mvMat.concatenate(vMat);
		mvMat.concatenate(mMat);
		
		// Creating uniform variables to pass the matrixes used for transforming into the shader files where they can be used.
		int mv_loc = gl.glGetUniformLocation(rendering_program, "mv_matrix");
		int proj_loc = gl.glGetUniformLocation(rendering_program, "proj_matrix");
		gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
		gl.glUniformMatrix4fv(mv_loc, 1, false, mvMat.getFloatValues(), 0);
		
		// bindBuffer makes the specified buffer "active"; it involves itself in the necessary OpenGL operations.
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		/* The following command creates a pointer [index] to a vertex attribute array, and associates
		 * that index with the active VBO.  When drawArrays is called (in this program), the shaders will require 3 floats to
		 * be input through a vertex attribute array in index 0 in order to run properly.
		 * Parameters:
		 * (int) 0: the index of the vertex attribute.
		 * (int) 3: the number of components per vertex attribute.  In this program, the shaders want a vec3 object, which can be created
		 * 			by passing in 3 floats.
		 * (int) GL_FLOAT: the data type of the array.  Here, we don't specify vec3; instead, we use float.
		 * (boolean) false: specifies if fixed data points should be normalized [true] or converted directly [false] on access.
		 * (int) 0: "stride"; specifies the byte offset of index grabs.
		 * (int) 0: The first index to be accessed from the vertex attribute array.
		 */
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		// Enables the vertex attribute array
		gl.glEnableVertexAttribArray(0);
		
		/* In many cases, commands will occur right before drawArrays to specify how OpenGL should handle the rendering.
		 * In this case, the commands only refer to depth testing and hidden surface removal.
		 */
		
		// Enabling depth testing to allow for hidden surface removal
		gl.glEnable(GL_DEPTH_TEST);
		// Specifying which depth function OpenGL should use.
		gl.glDepthFunc(GL_LEQUAL);
		// Now we draw everything.  There are 36 vertexes this time, and it is important to specify this.
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
	}
	
	@Override
	public void dispose(GLAutoDrawable arg0) {
	}
	
	@Override
	public void init(GLAutoDrawable arg0) {
		// Loading the shaders into the program.  Same process as E02.
		rendering_program = createShaderProgram();
		setupVertexes();
		/* Initializing the locations of the cube and theoretical camera.  The actual camera for OpenGL
		 * is locked at 0,0,0 facing in the negative Z axis.  Matrix transformations will be used to maintain relativity 
		 * and relocate objects to the actual camera's view.
		 */
		cameraX = 0.0f;  cameraY = 0.0f;  cameraZ = 8.0f;
		cubeX = 0.0f;  cubeY = -2.0f;  cubeZ = 0.0f;
		
		// sets up the aspect ratio of the canvas.  WIDTH / HEIGHT
		float aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		/* Initializes pMat, the perspective matrix.  This matrix transforms the 3D scene into a 2D scene while maintaining perspective.
		 * This matrix will be used as the final step when the objects have been prepared.  See the perspective() method for information about
		 * the parameters and construction.
		 */
		pMat = perspective(60.0f, aspect, 0.1f, 1000.0f);
	}
	
	@Override
	public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) {
		// adjusts the aspect ratio of the screen and perspective matrix accordingly on the event of a resize.
		float aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat = perspective(60.0f, aspect, 0.1f, 1000.0f);
	}
	
	/* The main purpose of this method is to create the VBOs and VBAs used in constructing the model and copying the wanted vertexes into
	 * said buffers. 
	 */
	private void setupVertexes() {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		// Creating a float array to store the cube vertex positions.  Since we can only build in triangles, each group of 9 is the coordinates
		// for one triangle.  12 triangles make up the cube.
		float[] vertex_positions = {
				-1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 
				1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 
				1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 
				-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 
				-1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 
				-1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f
		};
		
		/* The following command generates vertex arrays to use in modeling.  The parameters are very simple;
		 * (int) vao.length: the amount of VAOs to create
		 * (int[]) vao: a place for the addresses (references) of the created VAOs to be stored.
		 * (int) 0: the index to start on when filling the arrays.
		 */
		gl.glGenVertexArrays(vao.length, vao, 0);
		// bindVertexArray makes the specified VAO "active" to where it associates itself with successive commands
		gl.glBindVertexArray(vao[0]);
		/* The same process as glGenVertexArrays, but VBOs are generated instead.  An important thing to note here is
		 * that these VBOs will be associated with vao[0] as it is the "active" VAO at the time of this command.
		 */
		gl.glGenBuffers(vbo.length, vbo, 0);
		
		// Similar with the VAO, BindBuffer makes the specified VBO "active."  The parameter GL_ARRAY_BUFFER
		// specifies to what type of data the vbo will be bound to -- in this case, vertex attributes (GL_ARRAY_BUFFER)
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		// Turning the float array into a buffer which can be applied to the active buffer.
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(vertex_positions);
		/* BufferData assigns data to the currently active buffer.  Parameters:
		 * (int) GL_ARRAY_BUFFER: the integer id for the type of data associated with the buffer.  Should be same as VBO!
		 * (int) vertBuf.limit() * 4: the size in bytes of the objects new data store.  In this case, a buffer of floats is present.  Each float 
		 * 			requires 4 bytes, the the number of floats multiplied by four gives the total number of bytes needed.
		 * (Buffer) vertBuf: the buffer in which the VBO will recieve data
		 * (int) GL_STATIC_DRAW: defines how the data will be used.  STATIC_DRAW represents the VBO's constants being changed once [here] and used many times.
		 */
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL_STATIC_DRAW);
	}
	
	/**	Generates a perspective matrix according to a formula (page 54 in textbook) and given parameters.
	 * 	A visualization of the parameters can be found on page 53 of the textbook.
	 * 
	 * 	@param fovy the field of view angle in degrees
	 * 	@param aspect the aspect ratio used.  This should always be the aspect ratio of the window.
	 *  @param zNear the distance from the camera to the smaller face of the frustum
	 *  @param zFar the distance from the camera to the larger face [base] of the frustum
	 *  @return the perspective matrix
	 */
	private Matrix3D perspective(float fovy, float aspect, float zNear, float zFar) {
		// No important concepts here.
		float q = 1.0f / ((float) Math.tan(Math.toRadians(0.5f * fovy)));
		float a = q / aspect;
		float b = (zNear + zFar) / (zNear - zFar);
		float c = (2.0f * zNear * zFar) / (zNear - zFar);
		Matrix3D r = new Matrix3D();
		r.setElementAt(0, 0, a);
		r.setElementAt(1, 1, q);
		r.setElementAt(2, 2, b);
		r.setElementAt(3, 2, -1.0f);
		r.setElementAt(2, 3, c);
		r.setElementAt(3, 3, 0.0f);
		return r;
	}
	
	private int createShaderProgram() {
		// The same code as E04.
		GL4 gl = (GL4) GLContext.getCurrentGL();
		String[] vShaderData = readShaderSource("D:\\Mark\\JavaPrograms\\Resources\\OpenGLExampleResources\\E07\\vert.shader");
		int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
		gl.glShaderSource(vShader, vShaderData.length, vShaderData, null, 0);
		gl.glCompileShader(vShader);
		String[] fShaderData = readShaderSource("D:\\Mark\\JavaPrograms\\Resources\\OpenGLExampleResources\\E07\\frag.shader");
		int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);
		gl.glShaderSource(fShader, fShaderData.length, fShaderData, null, 0);
		gl.glCompileShader(fShader);
		int prog = gl.glCreateProgram();
		gl.glAttachShader(prog, vShader);
		gl.glAttachShader(prog, fShader);
		gl.glLinkProgram(prog);

		gl.glDeleteShader(vShader);
		gl.glDeleteShader(fShader);

		return prog;
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