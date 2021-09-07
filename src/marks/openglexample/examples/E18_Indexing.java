package marks.openglexample.examples;

import static com.jogamp.opengl.GL4.*;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Scanner;
import java.util.Vector;

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
import graphicslib3D.Vertex3D;

// Indexing serves the same purpose as E17, but is more efficient when rendering.
@SuppressWarnings("serial")
public class E18_Indexing extends JFrame implements GLEventListener {
	
	private GLCanvas myCanvas;
	private int rendering_program;
	private int iceTexture;
	private int vao[] = new int[1];
	private int vbo[] = new int[4];
	private float cameraX, cameraY, cameraZ;
	private float sphX, sphY, sphZ;
	private Matrix3D pMat;
	
	private Sphere s;
	
	public E18_Indexing() {
		this.setTitle("Sphere");
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
		vMat.translate(-cameraX, -cameraY, -cameraZ);
		int proj_loc = gl.glGetUniformLocation(rendering_program, "proj_matrix");
		gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
		
		Matrix3D mMat = new Matrix3D();
		Matrix3D mvMat = new Matrix3D();
		int mv_loc = gl.glGetUniformLocation(rendering_program, "mv_matrix");
		
		mMat = new Matrix3D();
		mMat.translate(sphX, sphY, sphZ);
		mvMat = new Matrix3D();
		mvMat.concatenate(vMat);
		mvMat.concatenate(mMat);
		gl.glUniformMatrix4fv(mv_loc, 1, false, mvMat.getFloatValues(), 0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, iceTexture);
		
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		
		// Binding the indices buffer to make it active and used when drawing.
		gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[3]);
		// Now we are using drawElements instead of drawArrays, which allows triangles to be drawn through indexing.
		gl.glDrawElements(GL_TRIANGLES, s.getIndices().length, GL_UNSIGNED_INT, 0);
	}
	
	@Override
	public void dispose(GLAutoDrawable arg0) {
	}
	
	@Override
	public void init(GLAutoDrawable arg0) {
		rendering_program = createShaderProgram();
		cameraX = 0.0f;  cameraY = 0.0f;  cameraZ = 5.0f;
		sphX = 0.0f; sphY = 0.0f; sphZ = 0.0f;
		
		s = new Sphere(48);
		iceTexture = (loadTexture("D:\\Mark\\JavaPrograms\\Resources\\OpenGLExampleResources\\E14\\icetexture.png")).getTextureObject();
		
		setupVertexes();
		
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
		
		Vertex3D[] vertices = s.getVertices();
		int[] indices = s.getIndices();
		
		float[] pvalues = new float[indices.length * 3];
		float[] tvalues = new float[indices.length * 2];
		float[] nvalues = new float[indices.length * 3]; 
		
		// Now, we are just storing the points in arrays.  The selection of which points [indexes] to
		// use for a specific point will be done by openGL.
		for (int i = 0; i < vertices.length; i++) {
			pvalues[i * 3 + 0] = (float) (vertices[i]).getX();
			pvalues[i * 3 + 1] = (float) (vertices[i]).getY();
			pvalues[i * 3 + 2] = (float) (vertices[i]).getZ();
							
			tvalues[i * 2 + 0] = (float) (vertices[i]).getS();
			tvalues[i * 2 + 1] = (float) (vertices[i]).getT();
											
			nvalues[i * 3 + 0] = (float) (vertices[i]).getNormalX();
			nvalues[i * 3 + 1] = (float) (vertices[i]).getNormalY();
			nvalues[i * 3 + 2] = (float) (vertices[i]).getNormalZ();
		}
		
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(vbo.length, vbo, 0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer textBuf = Buffers.newDirectFloatBuffer(tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, textBuf.limit() * 4, textBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		FloatBuffer normBuf = Buffers.newDirectFloatBuffer(nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, normBuf.limit() * 4, normBuf, GL_STATIC_DRAW);
		
		// Creating a buffer to store the indices for each point.
		gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[3]);
		IntBuffer idxBuf = Buffers.newDirectIntBuffer(indices);
		gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, idxBuf.limit() * 4, idxBuf, GL_STATIC_DRAW);
	}
	
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