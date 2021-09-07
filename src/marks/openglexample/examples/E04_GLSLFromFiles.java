package marks.openglexample.examples;

import javax.swing.JFrame;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;

import static com.jogamp.opengl.GL4.*;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;


@SuppressWarnings("serial")
public class E04_GLSLFromFiles extends JFrame implements GLEventListener {
	
	private GLCanvas myCanvas;
	private int rendering_program;
	private int vao[] = new int[1];
	
	public E04_GLSLFromFiles() {
		// The same constructor and notes as E02.
		this.setTitle("FileReading");
		this.setSize(600, 400);
		this.setLocation(200, 200);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		this.add(myCanvas);
		setVisible(true);
	}
	
	@Override
	public void display(GLAutoDrawable arg0) {
		// The same code and notes as E02.
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glUseProgram(rendering_program);
		gl.glPointSize(30.0f);
		gl.glDrawArrays(GL_POINTS, 0, 1);
	}
	
	@Override
	public void dispose(GLAutoDrawable arg0) {
	}
	
	@Override
	public void init(GLAutoDrawable arg0) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		// Using the new method to get a String array for the vertex shader from a text file.
		String[] vShaderData = readShaderSource("D:\\Mark\\JavaPrograms\\Resources\\OpenGLExampleResources\\E04\\vert.shader");
		int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
		gl.glShaderSource(vShader, vShaderData.length, vShaderData, null, 0);
		gl.glCompileShader(vShader);
		
		// Using the new method to get a String array for the fragment shader from a text file.
		String[] fShaderData = readShaderSource("D:\\Mark\\JavaPrograms\\Resources\\OpenGLExampleResources\\E04\\frag.shader");
		int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);
		gl.glShaderSource(fShader, fShaderData.length, fShaderData, null, 0);
		gl.glCompileShader(fShader);
		
		// The rest of this code is identical of that of E02 -- just relocated.
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
	
	// Very simple text file reader.  In this example, the text files have an extension of .shader
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
