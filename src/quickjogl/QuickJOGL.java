/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickjogl;

import com.jogamp.newt.event.WindowEvent;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.GL2.*;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.awt.GLCanvas;
import javax.swing.JFrame;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;

class OneTriangle {

    protected static void setup(GL2 gl2, int width, int height) {
        GLU glu = new GLU();

        //----
        gl2.glMatrixMode(GL2.GL_PROJECTION);
        gl2.glLoadIdentity();

        glu.gluPerspective((float) 100, width / height, 0.001f, 1000);
        gl2.glMatrixMode(GL2.GL_MODELVIEW);
        gl2.glEnable(GL2.GL_TEXTURE_2D);
        gl2.glShadeModel(GL2.GL_SMOOTH);
        gl2.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
        gl2.glClearDepth(1.0f);

        if (false) {
            gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
            gl2.glEnable(GL2.GL_BLEND);
        } else {
            gl2.glEnable(GL2.GL_DEPTH_TEST);
        }

        gl2.glDepthFunc(GL2.GL_LEQUAL);
    }

    private static void clearGL(GL2 gl2) {
        gl2.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        if (false) {
            gl2.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        }
        gl2.glLoadIdentity();
    }

    protected static void render(GL2 gl2, int width, int height) {
        clearGL(gl2);
        gl2.glColor3f(0.4f, 0.5f, 0.9f);
        gl2.glBegin(GL_QUADS);
        gl2.glVertex3f(-5, -10, -5);
        gl2.glVertex3f(5, -10, -5);
        gl2.glVertex3f(5, -10, 5);
        gl2.glVertex3f(-5, -10, 5);
        gl2.glEnd();
    }
}

public class QuickJOGL {

    public static void main(String[] args) {
        GLProfile glprofile = GLProfile.getDefault();
        GLCapabilities glcapabilities = new GLCapabilities(glprofile);
        final GLCanvas glcanvas = new GLCanvas(glcapabilities);

        glcanvas.addGLEventListener(new GLEventListener() {

            @Override
            public void reshape(GLAutoDrawable glautodrawable, int x, int y, int width, int height) {
                OneTriangle.setup(glautodrawable.getGL().getGL2(), width, height);
            }

            @Override
            public void init(GLAutoDrawable glautodrawable) {
            }

            @Override
            public void dispose(GLAutoDrawable glautodrawable) {
            }

            @Override
            public void display(GLAutoDrawable glautodrawable) {
                OneTriangle.render(glautodrawable.getGL().getGL2(), glautodrawable.getSurfaceWidth(), glautodrawable.getSurfaceHeight());
            }
        });

        final JFrame jframe = new JFrame("One Triangle Swing GLCanvas");
        jframe.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowevent) {
                jframe.dispose();
                System.exit(0);
            }
        });

        jframe.getContentPane().add(glcanvas, BorderLayout.CENTER);
        jframe.setSize(640, 480);
        jframe.setVisible(true);
    }

}
