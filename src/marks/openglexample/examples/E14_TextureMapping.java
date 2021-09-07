package marks.openglexample.examples;

import javax.swing.JFrame;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import graphicslib3D.Matrix3D;

import static com.jogamp.opengl.GL4.*;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Scanner;
import java.util.Vector;

@SuppressWarnings("serial")
public class E14_TextureMapping extends JFrame implements GLEventListener {
	
	private GLCanvas myCanvas;
	private int rendering_program;
	private int iceTexture;
	private int vao[] = new int[1];
	/*	In this case, vbo[0] will be used for vertex positions in 3D space while
	 *  vbo[1] will be used for the texture positions for the vertex.
	 */
	private int vbo[] = new int[2];
	private float cameraX, cameraY, cameraZ;
	private float pyrX, pyrY, pyrZ;
	private Matrix3D pMat;
	
	public E14_TextureMapping() {
		// The same constructor and notes as E02.
		this.setTitle("Basic Texture Mapping");
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
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glUseProgram(rendering_program);
		
		Matrix3D vMat = new Matrix3D();
		vMat.rotate(-20, 20, -10);
		vMat.translate(-cameraX, -cameraY, -cameraZ);
		int proj_loc = gl.glGetUniformLocation(rendering_program, "proj_matrix");
		gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
		
		// setting up the pyramid model
		
		Matrix3D mMat = new Matrix3D();
		Matrix3D mvMat = new Matrix3D();
		int mv_loc = gl.glGetUniformLocation(rendering_program, "mv_matrix");
		
		mMat = new Matrix3D();
		mMat.translate(pyrX, pyrY, pyrZ);
		mvMat = new Matrix3D();
		mvMat.concatenate(vMat);
		mvMat.concatenate(mMat);
		gl.glUniformMatrix4fv(mv_loc, 1, false, mvMat.getFloatValues(), 0);
		
		// Sending the vertex attribute array for vertex points in location 0.
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		/* Sending the vertex attribute array for vertex texture points in location 1.
		 * Note that here, the points on the texture file are 2D (not 3D) so only 2 points
		 * will be sent in at a time.
		 */
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		// Setting the current active texture unit on the computer's GPU to be the 0th one
		gl.glActiveTexture(GL_TEXTURE0);
		// Assigning the ice texture to the active texture unit to be used in texture mapping.
		gl.glBindTexture(GL_TEXTURE_2D, iceTexture);
		
		// Drawing the triangles.
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glDrawArrays(GL_TRIANGLES, 0, 18);
	}
	
	@Override
	public void dispose(GLAutoDrawable arg0) {
	}
	
	@Override
	public void init(GLAutoDrawable arg0) {
		rendering_program = createShaderProgram();
		setupVertexes();
		cameraX = -3.0f;  cameraY = -2.0f;  cameraZ = 4.0f;
		pyrX = 0.0f;  pyrY = 0.0f;  pyrZ = 0.0f;
		
		// Getting the texture wrapper for a texture image file (.png, other file formats are acceptable) and converting
		// it to its usable integer ID pointer.
		iceTexture = (loadTexture("D:\\Mark\\JavaPrograms\\Resources\\OpenGLExampleResources\\E14\\icetexture.png")).getTextureObject();
		
		float aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat = perspective(60.0f, aspect, 0.1f, 1000.0f);
	}
	
	@Override
	public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) {
		float aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat = perspective(60.0f, aspect, 0.1f, 1000.0f);
	}
	
	private void setupVertexes() {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		float[] pyramid_vertex_positions = {
				-1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,
				1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 0.0f,1.0f, 0.0f, 
				1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f, 
				-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 
				-1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 
				1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 
		};
		
		/*	The pyramid texture positions.  Each vertex gets a 2D coordinate set relating to the current
		 *  image file, with (0.0f, 0.0f) representing the lower left corner and (1.0f, 1.0f) representing
		 *  the upper right corner.
		 */
		float[] pyramid_texture_positions = {
				0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,		0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
				0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,		0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
				0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,		1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f
		};
		
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(vbo.length, vbo, 0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pyramid_vertex_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer vertBuf2 = Buffers.newDirectFloatBuffer(pyramid_texture_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf2.limit() * 4, vertBuf2, GL_STATIC_DRAW);
	}
	
	/**
	 * 	loadTexture creates a JOGL texture file that is a wrapper for a OpenGL texture object
	 * 	(represented by an integer ID)
	 * 
	 * @param fileName the path to the file
	 * @return the JOGL texture wrapper
	 */
	public Texture loadTexture(String fileName) {
		Texture text = null;
		try {
			text = TextureIO.newTexture(new File(fileName), false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	private Matrix3D perspective(float fovy, float aspect, float zNear, float zFar) {
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
		// Loading the shader files.  Nothing new here.
		GL4 gl = (GL4) GLContext.getCurrentGL();
		String[] vShaderData = readShaderSource("D:\\Mark\\JavaPrograms\\Resources\\OpenGLExampleResources\\E14\\vert.shader");
		int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
		gl.glShaderSource(vShader, vShaderData.length, vShaderData, null, 0);
		gl.glCompileShader(vShader);
		String[] fShaderData = readShaderSource("D:\\Mark\\JavaPrograms\\Resources\\OpenGLExampleResources\\E14\\frag.shader");
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