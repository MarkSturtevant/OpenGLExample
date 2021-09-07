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
public class E15_TextureTechniques extends JFrame implements GLEventListener {
	
	private GLCanvas myCanvas;
	private int rendering_program;
	private int iceTexture;
	private int vao[] = new int[1];
	private int vbo[] = new int[2];
	private float cameraX, cameraY, cameraZ;
	private float pyrX, pyrY, pyrZ;
	private Matrix3D pMat;
	
	public E15_TextureTechniques() {
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
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, iceTexture);
		/* The following code is used to handle wrapping and tiling; when texture positions
		 * are outside the bound [0.0f, 1.0f].  The following code will specify how OpenGL will
		 * handle this.
		 * GL_REPEAT: simply repeats the texture.  This is the default handling.
		 * 			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		 *			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		 * GL_MIRRORED_REPEAT: Same as repeat, but reverses the coordinates when the integer [position] portion of the texture is odd.
		 * 			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
		 *			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);
		 * GL_CLAMP_TO_EDGE: Coordinates outside the bound are set to new locations on the border relative to their position.
		 *  		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		 *			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		 * GL_CLAMP_TO_BORDER: Coordinates outside the bound are assigned a border color.
		 * 			float[] borderColor = new float[] {r, g, b, a};
		 * 			gl.glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, borderColor, 0);
		 */
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);
		
		
		/* The following code fixes a problem known as aliasing, causing problems when rendering textures
		 * on deep surfaces.  For this program, it will not have much of effect.  However, it may prove
		 * useful in other programs where aliasing is a known issue.
		 */
		
		/* Transforming the iceTexture into a mipmap.  This changes the texture into a collection of said texture
		 * of different sizes.  The glTexParameter sets a property of the current binded texture; in this case, setting
		 * the minification technique (filter).  The GL_LINEAR_MIPMAP_LINEAR interpolates between two mipmap levels to
		 * find the color of the pixel, producing the cleanest result.  The tradeoff is a reduction on image quality.
		 */
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		gl.glGenerateMipmap(GL_TEXTURE_2D);
		/* An approach to bring the quality of the image back up is to use anisotropic filtering, which is related to mipmap
		 * filtering but uses rectangles instead of squares to better account for viewing at various angles.  The tradeoff
		 * to this method is that it's computationally expensive!
		 * This method doesn't HAVE to be paired with mipmapping.
		 */
		// Testing if the GPU supports anisotropic filtering
		if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic")) {
			float max[] = new float[1];
			// Finding the maximum degree of sampling
			gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, max, 0);
			// Activating anisotropic filtering
			gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, max[0]);
		}
		
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
		
		iceTexture = (loadTexture("D:\\Mark\\JavaPrograms\\Resources\\OpenGLExampleResources\\E15\\texture.png")).getTextureObject();
		
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
		
		// Note how the texture positions go beyond 1.0f.  This will be handled in the wrapping and
		// tiling methods in the display() method.
		float[] pyramid_texture_positions = {
				0.0f, 0.0f, 3.0f, 0.0f, 1.5f, 3.0f,		0.0f, 0.0f, 3.0f, 0.0f, 1.5f, 3.0f,
				0.0f, 0.0f, 3.0f, 0.0f, 1.5f, 3.0f,		0.0f, 0.0f, 3.0f, 0.0f, 1.5f, 3.0f,
				0.0f, 0.0f, 3.0f, 3.0f, 0.0f, 3.0f,		3.0f, 3.0f, 0.0f, 0.0f, 3.0f, 0.0f
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