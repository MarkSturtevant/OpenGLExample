package marks.openglexample.examples;

import javax.swing.JFrame;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

import graphicslib3D.Matrix3D;
import graphicslib3D.MatrixStack;
import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;

import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL4.*;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Scanner;
import java.util.Vector;

@SuppressWarnings("serial")
public class E13_LookAtMatrix extends JFrame implements GLEventListener {
	
	private GLCanvas myCanvas;
	private int rendering_program;
	private int vao[] = new int[1];
	private int vbo[] = new int[2];
	private float pyrX, pyrY, pyrZ;
	private Matrix3D pMat;
	
	public E13_LookAtMatrix() {
		this.setTitle("Cube And Pyramid");
		this.setSize(600, 400);
		this.setLocation(200, 200);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		this.add(myCanvas);
		setVisible(true);
		FPSAnimator ani = new FPSAnimator(myCanvas, 50);
		ani.start();
	}
	
	@Override
	public void display(GLAutoDrawable arg0) {
		// The majority of this code is from E12.  The only difference is the view matrix [vMat], loaded before the models.
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glClearBufferfv(GL_COLOR, 0, Buffers.newDirectFloatBuffer(new float[] {0.0f, 0.0f, 0.0f, 1.0f}));
		gl.glUseProgram(rendering_program);
		
		int proj_loc = gl.glGetUniformLocation(rendering_program, "proj_matrix");
		gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
		int mv_loc = gl.glGetUniformLocation(rendering_program, "mv_matrix");
		double t = (double) (System.currentTimeMillis()) / 1000.0;
		
		MatrixStack mvStack = new MatrixStack(20);
		
		mvStack.pushMatrix();
		// In this version of E12, the camera will constantly rotate to focus on the planets.
		// The camera will be locked in place at the specified point.
		Point3D cameraLoc = new Point3D(0.0, 0.0, 10.0f);
		// The camera will look at the earth
		Point3D targetLoc = new Point3D(Math.sin(t) * 4.0f, 0.0f, Math.cos(t) * 4.0f);
		// Creating the look-at matrix
		Matrix3D vMat = lookat(cameraLoc, targetLoc, new Vector3D(0.0, 1.0, 0.0));
		// Setting the newly pushed matrix on the stack to vMat
		mvStack.loadMatrix(vMat);
		
		// PYRAMID MODEL (SUN)
		mvStack.pushMatrix();
		mvStack.translate(pyrX, pyrY, pyrZ);
		mvStack.pushMatrix();
		mvStack.rotate(t * 100.0, 1.0, 0.0, 0.0);
		gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glDrawArrays(GL_TRIANGLES, 0, 18);
		
		mvStack.popMatrix();
		
		// CUBE MODEL (EARTH)
		mvStack.pushMatrix();
		mvStack.translate(Math.sin(t) * 4.0f, 0.0f, Math.cos(t) * 4.0f);
		mvStack.pushMatrix();
		mvStack.rotate(t * 100.0, 0.0, 1.0, 0.0);
		gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
		
		mvStack.popMatrix();
		
		// SCALED CUBE MODEL (MOON)
		mvStack.pushMatrix();
		mvStack.translate(0.0f, Math.sin(t) * 2.0f, Math.cos(t) * 2.0f);
		mvStack.pushMatrix();
		mvStack.rotate(t * 100.0, 0.0, 0.0, 1.0);
		mvStack.scale(0.25, 0.25, 0.25);
		gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
		
		mvStack.popMatrix(); mvStack.popMatrix(); mvStack.popMatrix(); mvStack.popMatrix();
	}
	
	@Override
	public void dispose(GLAutoDrawable arg0) {
	}
	
	@Override
	public void init(GLAutoDrawable arg0) {
		// Loading the shaders into the program.  Same process as E02.
		rendering_program = createShaderProgram();
		setupVertexes();
		pyrX = 0.0f;  pyrY = 0.0f;  pyrZ = 0.0f;
		
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
		float[] cube_vertex_positions = {
				-1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 
				1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 
				1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 
				-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 
				-1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 
				-1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f
		};
		
		float[] pyramid_vertex_positions = {
				-1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,
				1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 0.0f,1.0f, 0.0f, 
				1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f, 
				-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 
				-1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 
				1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 
		};
		
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		// Note that this command now generates two buffers, since the vbo array has two indexes!
		gl.glGenBuffers(vbo.length, vbo, 0);
		
		// Loading the cube vertex data onto the first buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(cube_vertex_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL_STATIC_DRAW);
		
		// Loading the pyramid vertex data onto the second buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer vertBuf2 = Buffers.newDirectFloatBuffer(pyramid_vertex_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf2.limit() * 4, vertBuf2, GL_STATIC_DRAW);
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
	
	/** Similar method to perspective, but generates a Look-At matrix to justify camera movement.
	 * 
	 * @param eyeLoc the eye location [point].  This is the point where the camera is.
	 * @param targetLoc the target location [point].  This is the point the camera should look at.
	 * @return the look-at matrix
	 */
	private Matrix3D lookat(Point3D eye, Point3D target, Vector3D y) {
		Vector3D eyeV = new Vector3D(eye);
		Vector3D targetV = new Vector3D(target);
		Vector3D fwd = (targetV.minus(eyeV)).normalize();
		Vector3D side = (fwd.cross(y)).normalize();
		Vector3D up = (side.cross(fwd)).normalize();
		
		Matrix3D look = new Matrix3D();
		look.setElementAt(0, 0, side.getX());
		look.setElementAt(1, 0, up.getX());
		look.setElementAt(2, 0, -fwd.getX());
		look.setElementAt(3, 0, 0.0f);
		look.setElementAt(0, 1, side.getY());
		look.setElementAt(1, 1, up.getY());
		look.setElementAt(2, 1, -fwd.getY());
		look.setElementAt(3, 1, 0.0f);
		look.setElementAt(0, 2, side.getZ());
		look.setElementAt(1, 2, up.getZ());
		look.setElementAt(2, 2, fwd.getZ());
		look.setElementAt(3, 2, 0.0f);
		look.setElementAt(0, 3, side.dot(eyeV.mult(-1)));
		look.setElementAt(1, 3, up.dot(eyeV.mult(-1)));
		look.setElementAt(2, 3, (fwd.mult(-1)).dot(eyeV.mult(-1)));
		look.setElementAt(3, 3, 1.0f);
		return look;
	}
	
	private int createShaderProgram() {
		// The same shaders as E08 can be used.  Even though the shape is different, the differences are specified
		// inside the program and NOT the shaders.
		GL4 gl = (GL4) GLContext.getCurrentGL();
		String[] vShaderData = readShaderSource("D:\\Mark\\JavaPrograms\\Resources\\OpenGLExampleResources\\E08\\vert.shader");
		int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
		gl.glShaderSource(vShader, vShaderData.length, vShaderData, null, 0);
		gl.glCompileShader(vShader);
		String[] fShaderData = readShaderSource("D:\\Mark\\JavaPrograms\\Resources\\OpenGLExampleResources\\E08\\frag.shader");
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