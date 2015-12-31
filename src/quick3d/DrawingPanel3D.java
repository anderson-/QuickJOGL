/**
 * Copyright (C) 2012 Anderson de Oliveira Antunes <anderson.utf@gmail.com>
 *
 * This file is part of QuickP3D.
 *
 * QuickP3D is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * QuickP3D is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * QuickP3D. If not, see http://www.gnu.org/licenses/.
 */
package quick3d;

import com.jogamp.graph.curve.Region;
import com.jogamp.graph.curve.opengl.RegionRenderer;
import com.jogamp.graph.curve.opengl.RenderState;
import com.jogamp.graph.curve.opengl.TextRegionUtil;
import com.jogamp.graph.curve.opengl.TextRegionUtil;
import com.jogamp.graph.font.FontFactory;
import com.jogamp.graph.geom.SVertex;
import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_LINE_STRIP;
import com.jogamp.opengl.GL2;
import java.awt.Canvas;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.JPanel;
import quick3d.graph.Graph;
import quick3d.graph.Graph2D;
import quick3d.graph.Graph3D;
import quick3d.simplegraphics.Axis;
import static com.jogamp.opengl.GL2.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import static com.jogamp.opengl.GLProfile.GL4;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.math.VectorUtil;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.PMVMatrix;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.glsl.ShaderState;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Exemplo de um objeto gráfico 3D.
 *
 * Esta classe desenha um objeto tridmensional composto por três setas, uma para
 * cada eixo, em um ponto pivô predefinido e coloridas em RGB, respectivamente.
 *
 * @author Anderson Antunes
 */
public class DrawingPanel3D implements Graph2D, Graph3D, GLEventListener {

    public static DrawingPanel3D asd = null;

    int width = 800, height = 600;
    final static int frameRate = 90;
    boolean[] keys = new boolean[256];
    Camera camera;
    private boolean envInit = false;
    long fps = 0;
    long lastFPS = 0;
    long tfps = 0;
    boolean wireframe = false;
    boolean transparency = false;
    boolean showFPS = true;
    private ArrayList<Graph> graphics = new ArrayList<>();

    public static TextRenderer renderer;
    GL2 gl;

    /*
     * Função de teste para a classe DrawingPanel3D.
     */
    public static void main(String[] args) {

        // instanciando um painel de desenho de 300px x 300px
        DrawingPanel3D drawingPanel = new DrawingPanel3D();
        // criando e exibindo a janela principal
//        drawingPanel.createFrame("Meu teste de desenho 3D");
        // incluindo um objeto 3D (no caso as setas RGB no ponto [0,0,0])
//        drawingPanel.append(new Axis());
        drawingPanel.createFrame("asdds", false);
        drawingPanel.append(new Axis());
    }

    public DrawingPanel3D() {
        camera = new Camera(this);
        append(this);
        asd = this;
    }

    public JFrame createFrame(String title, boolean fullscreen) {
        final JFrame jframe = new JFrame(title);
        jframe.setFocusable(true);
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Canvas canvas = createCanvas();
        jframe.getContentPane().add(canvas, BorderLayout.CENTER);

        if (fullscreen) {
            jframe.setSize(1920, 1080);
            jframe.setUndecorated(true);
            jframe.setAlwaysOnTop(true);
        } else {
            jframe.setSize(1280, 720);
        }
        jframe.setVisible(true);

        Rectangle r = jframe.getBounds();
        camera.setMouseCenter(new Point(r.x + r.width / 2, r.y + r.height / 2));

        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        // Create a new blank cursor.
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                cursorImg, new Point(0, 0), "blank cursor");
        jframe.setCursor(blankCursor);
        canvas.requestFocus();
        return jframe;
    }

    public JPanel createPanel() {
        JPanel panel = new JPanel() {
            private long time = System.currentTimeMillis();

            @Override
            public void validate() {
                super.validate();
                //evita reconstruir os buffers com frequencia
                if (System.currentTimeMillis() - time > 50) {
//                        System.out.println("validate");
                    resizeFrame(this.getWidth(), this.getHeight());
                } else {
                    time = System.currentTimeMillis();
                }
            }
        };

        panel.add(createCanvas());
        panel.setSize(DrawingPanel3D.this.width, DrawingPanel3D.this.height);
        return panel;
    }

    public Canvas createCanvas() {
        GLProfile glprofile = GLProfile.getDefault();
        GLCapabilities glcapabilities = new GLCapabilities(glprofile);
        GLCanvas glcanvas = new GLCanvas(glcapabilities);

        glcanvas.addGLEventListener(this);
        glcanvas.addKeyListener(camera);
        glcanvas.addMouseListener(camera);
        glcanvas.addMouseMotionListener(camera);

        lastFPS = System.currentTimeMillis();
        tfps = 0;

        Animator animator = new Animator(glcanvas);
//        animator.setRunAsFastAsPossible(true);
        animator.start();
        return glcanvas;
    }

    public void resizeFrame(int width, int height) {
//            if (pGraphics3D != null) {
//                /*
//                 * Nota: Ignorar quaisquer warnings de sincronização de campos 
//                 * não finais.
//                 */
//                synchronized (pGraphics3D) {
//                    pGraphics3D.setSize(width, height);
//                    //pGraphics3D = (PGraphics3D) createGraphics(width, height, P3D);
//                }
//                synchronized (pGraphics2D) {
//                    pGraphics2D.setSize(width, height);
//                    //pGraphics2D = (PGraphics2D) createGraphics(width, height, P2D);
//                }
//            }
    }

    public void drawString(String s, int x, int y) {
        renderer.beginRendering(width, height);
        renderer.draw(s, x, height - y);
        renderer.endRendering();
    }

    public void render(GLAutoDrawable drawable) {
        gl = drawable.getGL().getGL2();

        width = drawable.getSurfaceWidth();
        height = drawable.getSurfaceHeight();
        
        initGL3();
        clearGL();
        camera.translatePostion(gl);
        GLUT glut = new GLUT();

        boolean selecting = false;// Mouse.isButtonDown(0);

        for (Graph g : graphics) {
            if (g instanceof Graph3D) {
                ((Graph3D) g).draw(gl, glut, selecting);
            }
        }

        if (selecting) {
            selecting = false;
            //select(400, 300);
        }

        initGL2();
        for (Graph g : graphics) {
            if (g instanceof Graph2D) {
                ((Graph2D) g).draw(gl, glut);
            }
        }

        if (showFPS) {
            gl.glColor3f(1, 1, 0);
            drawString("FPS: " + fps, width - 65, 25);
        }

        gl.glBegin(GL_POINTS);
        gl.glColor3f(0.4f, 0.4f, 1);
        gl.glVertex2f(width / 2, height / 2);

        gl.glColor3f(1, 1, 1);
        for (int i = 0; i < 360; i += 90) {
            gl.glVertex2d(width / 2 + Math.sin(i * Math.PI / 180) * 8, height / 2 + Math.cos(i * Math.PI / 180) * 8);
        }
        gl.glEnd();

        if (System.currentTimeMillis() - lastFPS > 1000) {
            fps = tfps;
            tfps = 0; //reset the FPS counter
            lastFPS += 1000; //add one second
        }
        tfps++;

        if (saveScreenshot) {
            saveScreenshot = false;
            saveScreenshotOnDrawingThread();
        }
    }

    public void renderObjs() {

        int a = 0, b = 0, c = 0;

        int max = 10;
        int min = -max;

        int ba = 0, bb = 0, bc = 0;
        int i = 0;
        for (a = min; a <= max; a++) {
            for (b = min; b <= max; b++) {
                for (c = min; c <= max; c++) {
                    i++;

                    drawCube(a, b, c, .1f, i);
                    gl.glColor3f(0.0f, 1.0f, 0.2f);
                    gl.glLineWidth(2f);
                    gl.glBegin(GL_LINE_STRIP);
                    gl.glVertex3f(a, b, c);
                    gl.glVertex3f(ba, bb, bc);
                    gl.glEnd();
                    ba = a;
                    bb = b;
                    bc = c;
                }
            }
        }
    }

    public static void rotateAndGoToMidPoint(GL2 gl, float[] v0, float[] v1) {
        gl.glTranslatef((v0[0] + v1[0]) / 2, (v0[1] + v1[1]) / 2, (v0[2] + v1[2]) / 2);
        float[] y = new float[]{0, -1, 0};
        float[] d = VectorUtil.normalizeVec3(v0, VectorUtil.subVec3(v0, v0, v1));
        float[] a = VectorUtil.crossVec3(v1, y, d);
        float omega = (float) Math.acos(VectorUtil.dotVec3(y, d));
        gl.glRotatef(omega * 57.2958f, a[0], a[1], a[2]);
//        gl.glTranslatef(0,  m, 0);
    }

    public static void drawR(GL2 gl, float scale, Color cd, Color c0, Color c1, Color c2, Color c3) {
        gl.glPushMatrix();
        gl.glScalef(scale, scale, scale);

        float b, e;
        float o, i;
        float s;

        int[] sec = new int[]{0, 1, 2, 4, 5, 6, 10, 12, 14, 16, 18, 19, 20, 22, 23, 24};
        int[] d = new int[]{4, 5, 5, 5, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 4};
        s = 2;
        Color color;
        for (int k = 0; k < sec.length - 1; k++) {
            gl.glBegin(GL_QUAD_STRIP);
            b = sec[k] - 12;
            e = sec[k + 1] - 12;
            o = d[k];
            i = d[k + 1];
            switch (k) {
                case 2:
                    color = c0;
                    break;
                case 6:
                    color = c1;
                    break;
                case 8:
                    color = c2;
                    break;
                case 12:
                    color = c3;
                    break;
                default:
                    color = cd;
            }
            gl.glColor3f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
            if (k == 2 || k == 6 || k == 8 || k == 12) {
            } else {
            }
            gl.glVertex3f(s, b, o);
            gl.glVertex3f(s, e, i);
            gl.glVertex3f(o, b, s);
            gl.glVertex3f(i, e, s);
            gl.glVertex3f(o, b, -s);
            gl.glVertex3f(i, e, -s);
            gl.glVertex3f(s, b, -o);
            gl.glVertex3f(s, e, -i);
            gl.glVertex3f(-s, b, -o);
            gl.glVertex3f(-s, e, -i);
            gl.glVertex3f(-o, b, -s);
            gl.glVertex3f(-i, e, -s);
            gl.glVertex3f(-o, b, s);
            gl.glVertex3f(-i, e, s);
            gl.glVertex3f(-s, b, o);
            gl.glVertex3f(-s, e, i);
            gl.glVertex3f(s, b, o);
            gl.glVertex3f(s, e, i);
            gl.glVertex3f(o, b, s);
            gl.glVertex3f(i, e, s);
            gl.glEnd();
        }
//
        gl.glBegin(GL_TRIANGLE_FAN);
        gl.glVertex3f(2, -12, 4);
        gl.glVertex3f(4, -12, 2);
        gl.glVertex3f(4, -12, -2);
        gl.glVertex3f(2, -12, -4);
        gl.glVertex3f(-2, -12, -4);
        gl.glVertex3f(-4, -12, -2);
        gl.glVertex3f(-4, -12, 2);
        gl.glVertex3f(-2, -12, 4);
        gl.glVertex3f(2, -12, 4);
        gl.glVertex3f(4, -12, 2);
        gl.glEnd();

        gl.glBegin(GL_TRIANGLE_FAN);
        gl.glVertex3f(2, 12, 4);
        gl.glVertex3f(4, 12, 2);
        gl.glVertex3f(4, 12, -2);
        gl.glVertex3f(2, 12, -4);
        gl.glVertex3f(-2, 12, -4);
        gl.glVertex3f(-4, 12, -2);
        gl.glVertex3f(-4, 12, 2);
        gl.glVertex3f(-2, 12, 4);
        gl.glVertex3f(2, 12, 4);
        gl.glVertex3f(4, 12, 2);
        gl.glEnd();

        gl.glPopMatrix();
    }

    public static void drawD(GL2 gl, float scale) {
        gl.glPushMatrix();
        gl.glScalef(scale, scale, scale);

        gl.glColor3f(1, .41f, .41f);

        float b, e;
        float o, i;
        float s;
        gl.glBegin(GL_QUAD_STRIP);
        b = 0;
        e = 1;
        o = 3.5f;
        i = 4.5f;
        s = 1.5f;
        gl.glVertex3f(s, b, o);
        gl.glVertex3f(s, e, i);
        gl.glVertex3f(o, b, s);
        gl.glVertex3f(i, e, s);
        gl.glVertex3f(o, b, -s);
        gl.glVertex3f(i, e, -s);
        gl.glVertex3f(s, b, -o);
        gl.glVertex3f(s, e, -i);
        gl.glVertex3f(-s, b, -o);
        gl.glVertex3f(-s, e, -i);
        gl.glVertex3f(-o, b, -s);
        gl.glVertex3f(-i, e, -s);
        gl.glVertex3f(-o, b, s);
        gl.glVertex3f(-i, e, s);
        gl.glVertex3f(-s, b, o);
        gl.glVertex3f(-s, e, i);
        gl.glVertex3f(s, b, o);
        gl.glVertex3f(s, e, i);
        gl.glVertex3f(o, b, s);
        gl.glVertex3f(i, e, s);
        gl.glEnd();

        gl.glBegin(GL_QUAD_STRIP);
        b = 1;
        e = 11;
        o = 4.5f;
        i = 4.5f;
        s = 1.5f;
        gl.glVertex3f(s, b, o);
        gl.glVertex3f(s, e, i);
        gl.glVertex3f(o, b, s);
        gl.glVertex3f(i, e, s);
        gl.glVertex3f(o, b, -s);
        gl.glVertex3f(i, e, -s);
        gl.glVertex3f(s, b, -o);
        gl.glVertex3f(s, e, -i);
        gl.glVertex3f(-s, b, -o);
        gl.glVertex3f(-s, e, -i);
        gl.glVertex3f(-o, b, -s);
        gl.glVertex3f(-i, e, -s);
        gl.glVertex3f(-o, b, s);
        gl.glVertex3f(-i, e, s);
        gl.glVertex3f(-s, b, o);
        gl.glVertex3f(-s, e, i);
        gl.glVertex3f(s, b, o);
        gl.glVertex3f(s, e, i);
        gl.glVertex3f(o, b, s);
        gl.glVertex3f(i, e, s);
        gl.glEnd();

        gl.glColor3f(.2f, .2f, .2f);
        gl.glBegin(GL_QUAD_STRIP);
        b = 11;
        e = 14;
        o = 4.5f;
        i = 4.5f;
        s = 1.5f;
        gl.glVertex3f(s, b, o);
        gl.glVertex3f(s, e, i);
        gl.glVertex3f(o, b, s);
        gl.glVertex3f(i, e, s);
        gl.glVertex3f(o, b, -s);
        gl.glVertex3f(i, e, -s);
        gl.glVertex3f(s, b, -o);
        gl.glVertex3f(s, e, -i);
        gl.glVertex3f(-s, b, -o);
        gl.glVertex3f(-s, e, -i);
        gl.glVertex3f(-o, b, -s);
        gl.glVertex3f(-i, e, -s);
        gl.glVertex3f(-o, b, s);
        gl.glVertex3f(-i, e, s);
        gl.glVertex3f(-s, b, o);
        gl.glVertex3f(-s, e, i);
        gl.glVertex3f(s, b, o);
        gl.glVertex3f(s, e, i);
        gl.glVertex3f(o, b, s);
        gl.glVertex3f(i, e, s);
        gl.glEnd();

        gl.glColor3f(1, .41f, .41f);
        gl.glBegin(GL_QUAD_STRIP);
        b = 14;
        e = 15;
        o = 4.5f;
        i = 4.5f;
        s = 1.5f;
        gl.glVertex3f(s, b, o);
        gl.glVertex3f(s, e, i);
        gl.glVertex3f(o, b, s);
        gl.glVertex3f(i, e, s);
        gl.glVertex3f(o, b, -s);
        gl.glVertex3f(i, e, -s);
        gl.glVertex3f(s, b, -o);
        gl.glVertex3f(s, e, -i);
        gl.glVertex3f(-s, b, -o);
        gl.glVertex3f(-s, e, -i);
        gl.glVertex3f(-o, b, -s);
        gl.glVertex3f(-i, e, -s);
        gl.glVertex3f(-o, b, s);
        gl.glVertex3f(-i, e, s);
        gl.glVertex3f(-s, b, o);
        gl.glVertex3f(-s, e, i);
        gl.glVertex3f(s, b, o);
        gl.glVertex3f(s, e, i);
        gl.glVertex3f(o, b, s);
        gl.glVertex3f(i, e, s);
        gl.glEnd();

        gl.glBegin(GL_QUAD_STRIP);
        b = 15;
        e = 16;
        o = 4.5f;
        i = 3.5f;
        s = 1.5f;
        gl.glVertex3f(s, b, o);
        gl.glVertex3f(s, e, i);
        gl.glVertex3f(o, b, s);
        gl.glVertex3f(i, e, s);
        gl.glVertex3f(o, b, -s);
        gl.glVertex3f(i, e, -s);
        gl.glVertex3f(s, b, -o);
        gl.glVertex3f(s, e, -i);
        gl.glVertex3f(-s, b, -o);
        gl.glVertex3f(-s, e, -i);
        gl.glVertex3f(-o, b, -s);
        gl.glVertex3f(-i, e, -s);
        gl.glVertex3f(-o, b, s);
        gl.glVertex3f(-i, e, s);
        gl.glVertex3f(-s, b, o);
        gl.glVertex3f(-s, e, i);
        gl.glVertex3f(s, b, o);
        gl.glVertex3f(s, e, i);
        gl.glVertex3f(o, b, s);
        gl.glVertex3f(i, e, s);
        gl.glEnd();

        gl.glBegin(GL_TRIANGLE_FAN);
        gl.glVertex3f(1.5f, 0, 3.5f);
        gl.glVertex3f(3.5f, 0, 1.5f);
        gl.glVertex3f(3.5f, 0, -1.5f);
        gl.glVertex3f(1.5f, 0, -3.5f);
        gl.glVertex3f(-1.5f, 0, -3.5f);
        gl.glVertex3f(-3.5f, 0, -1.5f);
        gl.glVertex3f(-3.5f, 0, 1.5f);
        gl.glVertex3f(-1.5f, 0, 3.5f);
        gl.glVertex3f(1.5f, 0, 3.5f);
        gl.glVertex3f(3.5f, 0, 1.5f);
        gl.glEnd();

        gl.glBegin(GL_TRIANGLE_FAN);
        gl.glVertex3f(1.5f, 16, 3.5f);
        gl.glVertex3f(3.5f, 16, 1.5f);
        gl.glVertex3f(3.5f, 16, -1.5f);
        gl.glVertex3f(1.5f, 16, -3.5f);
        gl.glVertex3f(-1.5f, 16, -3.5f);
        gl.glVertex3f(-3.5f, 16, -1.5f);
        gl.glVertex3f(-3.5f, 16, 1.5f);
        gl.glVertex3f(-1.5f, 16, 3.5f);
        gl.glVertex3f(1.5f, 16, 3.5f);
        gl.glVertex3f(3.5f, 16, 1.5f);
        gl.glEnd();

        gl.glPopMatrix();
    }

    public static void drawT(GL2 gl, float scale) {

        gl.glPushMatrix();
        gl.glScalef(scale, scale, scale);

        gl.glLineWidth(4f);
        gl.glColor3f(.4f, .4f, .4f);
        gl.glBegin(GL_LINES);
        gl.glVertex3f(-3, 0, -1);
        gl.glVertex3f(-3, 6, -1);
        gl.glVertex3f(3, 0, -1);
        gl.glVertex3f(3, 6, -1);
        gl.glVertex3f(3, 0, 0);
        gl.glVertex3f(3, 6, 0);
        gl.glVertex3f(2, 0, 2);
        gl.glVertex3f(2, 6, 2);
        gl.glVertex3f(-2, 0, 2);
        gl.glVertex3f(-2, 6, 2);
        gl.glVertex3f(-3, 0, 0);
        gl.glVertex3f(-3, 6, 0);
        gl.glVertex3f(-3, 0, -1);
        gl.glVertex3f(-3, 6, -1);
        gl.glEnd();

        gl.glBegin(GL_LINE_STRIP);
        gl.glVertex3f(-3, 0, -1);
        gl.glVertex3f(3, 0, -1);
        gl.glVertex3f(3, 0, 0);
        gl.glVertex3f(2, 0, 2);
        gl.glVertex3f(-2, 0, 2);
        gl.glVertex3f(-3, 0, 0);
        gl.glVertex3f(-3, 0, -1);
        gl.glEnd();

        gl.glBegin(GL_LINE_STRIP);
        gl.glVertex3f(-3, 6, -1);
        gl.glVertex3f(3, 6, -1);
        gl.glVertex3f(3, 6, 0);
        gl.glVertex3f(2, 6, 2);
        gl.glVertex3f(-2, 6, 2);
        gl.glVertex3f(-3, 6, 0);
        gl.glVertex3f(-3, 6, -1);
        gl.glEnd();

        gl.glColor3f(.2f, .2f, .2f);
        gl.glBegin(GL_QUAD_STRIP);
        gl.glVertex3f(-3, 0, -1);
        gl.glVertex3f(-3, 6, -1);
        gl.glVertex3f(3, 0, -1);
        gl.glVertex3f(3, 6, -1);
        gl.glVertex3f(3, 0, 0);
        gl.glVertex3f(3, 6, 0);
        gl.glVertex3f(2, 0, 2);
        gl.glVertex3f(2, 6, 2);
        gl.glVertex3f(-2, 0, 2);
        gl.glVertex3f(-2, 6, 2);
        gl.glVertex3f(-3, 0, 0);
        gl.glVertex3f(-3, 6, 0);
        gl.glVertex3f(-3, 0, -1);
        gl.glVertex3f(-3, 6, -1);
        gl.glEnd();

        gl.glBegin(GL_TRIANGLE_FAN);
        gl.glVertex3f(0, 6, -1);
        gl.glVertex3f(-3, 6, -1);
        gl.glVertex3f(-3, 6, 0);
        gl.glVertex3f(-2, 6, 2);
        gl.glVertex3f(2, 6, 2);
        gl.glVertex3f(3, 6, 0);
        gl.glVertex3f(3, 6, -1);
        gl.glEnd();

        gl.glBegin(GL_TRIANGLE_FAN);
        gl.glVertex3f(0, 0, -1);
        gl.glVertex3f(-3, 0, -1);
        gl.glVertex3f(-3, 0, 0);
        gl.glVertex3f(-2, 0, 2);
        gl.glVertex3f(2, 0, 2);
        gl.glVertex3f(3, 0, 0);
        gl.glVertex3f(3, 0, -1);
        gl.glEnd();

        gl.glPopMatrix();
    }

    public void drawCube(float x, float y, float z, float scale, int i) {

        int r = (i & 0x000000FF) >> 0;
        int g = (i & 0x0000FF00) >> 8;
        int b = (i & 0x00FF0000) >> 16;

//        byte array[] = ByteBuffer.allocate(4).putInt(color * 1000).array();
//        int a = (array[1] & 0xFF) << 16 | (array[2] & 0xFF) << 8 | (array[3] & 0xFF);
//        System.out.println(color + " " + (color * 1000) + " " + Arrays.toString(array) + " " + a);
        gl.glPushMatrix();
        gl.glTranslatef(x, y, z);
        gl.glScalef(scale, scale, scale);
        gl.glColor4f(r / 255.0f, g / 255.0f, b / 255.0f, 1.0f);
//        GL11.glColor3f(0.5f, 0.5f, 1.0f);                 // Set The Color To Blue One Time Only
        gl.glBegin(gl.GL_QUADS);                        // Draw A Quad
//        GL11.glColor3f(0.0f, 1.0f, 0.0f);             // Set The Color To Green
        gl.glVertex3f(1.0f, 1.0f, -1.0f);         // Top Right Of The Quad (Top)
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);         // Top Left Of The Quad (Top)
        gl.glVertex3f(-1.0f, 1.0f, 1.0f);         // Bottom Left Of The Quad (Top)
        gl.glVertex3f(1.0f, 1.0f, 1.0f);         // Bottom Right Of The Quad (Top)
//        GL11.glColor3f(1.0f, 0.5f, 0.0f);             // Set The Color To Orange
        gl.glVertex3f(1.0f, -1.0f, 1.0f);         // Top Right Of The Quad (Bottom)
        gl.glVertex3f(-1.0f, -1.0f, 1.0f);         // Top Left Of The Quad (Bottom)
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);         // Bottom Left Of The Quad (Bottom)
        gl.glVertex3f(1.0f, -1.0f, -1.0f);         // Bottom Right Of The Quad (Bottom)
//        GL11.glColor3f(1.0f, 0.0f, 0.0f);             // Set The Color To Red
        gl.glVertex3f(1.0f, 1.0f, 1.0f);         // Top Right Of The Quad (Front)
        gl.glVertex3f(-1.0f, 1.0f, 1.0f);         // Top Left Of The Quad (Front)
        gl.glVertex3f(-1.0f, -1.0f, 1.0f);         // Bottom Left Of The Quad (Front)
        gl.glVertex3f(1.0f, -1.0f, 1.0f);         // Bottom Right Of The Quad (Front)
//        GL11.glColor3f(1.0f, 1.0f, 0.0f);             // Set The Color To Yellow
        gl.glVertex3f(1.0f, -1.0f, -1.0f);         // Bottom Left Of The Quad (Back)
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);         // Bottom Right Of The Quad (Back)
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);         // Top Right Of The Quad (Back)
        gl.glVertex3f(1.0f, 1.0f, -1.0f);         // Top Left Of The Quad (Back)
//        GL11.glColor3f(0.0f, 0.0f, 1.0f);             // Set The Color To Blue
        gl.glVertex3f(-1.0f, 1.0f, 1.0f);         // Top Right Of The Quad (Left)
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);         // Top Left Of The Quad (Left)
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);         // Bottom Left Of The Quad (Left)
        gl.glVertex3f(-1.0f, -1.0f, 1.0f);         // Bottom Right Of The Quad (Left)
//        GL11.glColor3f(1.0f, 0.0f, 1.0f);             // Set The Color To Violet
        gl.glVertex3f(1.0f, 1.0f, -1.0f);         // Top Right Of The Quad (Right)
        gl.glVertex3f(1.0f, 1.0f, 1.0f);         // Top Left Of The Quad (Right)
        gl.glVertex3f(1.0f, -1.0f, 1.0f);         // Bottom Left Of The Quad (Right)
        gl.glVertex3f(1.0f, -1.0f, -1.0f);         // Bottom Right Of The Quad (Right)
        gl.glEnd();                                       // Done Drawing The Quad
        gl.glPopMatrix();
    }

    public void update() {
        mapKeys();
        camera.update();
    }

    private void mapKeys() {
        //Update keys

    }

    private void initGL3() {
        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();

        new GLU().gluPerspective((float) 100, width / height * 1.5, 0.001f, 1000);
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glEnable(GL_TEXTURE_2D);
        gl.glShadeModel(GL_SMOOTH);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
        gl.glClearDepth(1.0f);

        if (transparency) {
            gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            gl.glEnable(GL_BLEND);
        } else {
            gl.glEnable(GL_DEPTH_TEST);
        }

        gl.glDepthFunc(GL_LEQUAL);
    }

    public void initGL2() {
        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(0, width, 0, height, -1, 1);
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();

        gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL_BLEND);

    }

    private void clearGL() {
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        if (wireframe) {
            gl.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        }
        gl.glLoadIdentity();
    }
    boolean saveScreenshot = false;
    File file;
    String format;

    public void saveScreenshot() {
        saveScreenshot("");
    }

    public void saveScreenshot(String str) {
        file = new File(new SimpleDateFormat("yyyy-MM-dd HH-mm-ss'" + str + ".png'").format(new Date()));
        format = "PNG";
        saveScreenshot = true;
    }

    private void saveScreenshotOnDrawingThread() {
//        if (file == null || format == null) {
//            return;
//        }
//        gl.glReadBuffer(GL_FRONT);
//        int width = Display.getDisplayMode().getWidth();
//        int height = Display.getDisplayMode().getHeight();
//        int bpp = 4; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha.
//        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bpp);
//        gl.glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
//
//        new Thread("Save Screenshot Thread" + Math.random()) {
//            @Override
//            public void run() {
//                if (file == null) {
//                    return;
//                }
//                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//                for (int x = 0; x < width; x++) {
//                    for (int y = 0; y < height; y++) {
//                        int i = (x + (width * y)) * bpp;
//                        int r = buffer.get(i) & 0xFF;
//                        int g = buffer.get(i + 1) & 0xFF;
//                        int b = buffer.get(i + 2) & 0xFF;
//                        image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
//                    }
//                }
//                try {
//                    System.out.println(file + "" + this);
//                    ImageIO.write(image, format, file);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } finally {
//                    file = null;
//                    format = null;
//                }
//            }
//        }.start();
    }

    public void append(Graph g) {
        graphics.add(g);
    }

    @Override
    public void draw(GL2 gl, GLUT glut, boolean colorPicking) {
        gl.glColor3f(0.4f, 0.5f, 0.9f);
        gl.glBegin(GL_QUADS);
        gl.glVertex3f(-5, -10, -5);
        gl.glVertex3f(5, -10, -5);
        gl.glVertex3f(5, -10, 5);
        gl.glVertex3f(-5, -10, 5);
        gl.glEnd();
    }

    @Override
    public void draw(GL2 gl, GLUT glut) {
    }

    @Override
    public void init(GLAutoDrawable glad) {
        GL gl = glad.getGL();

        // Don't artificially slow us down, at least on platforms where we
        // have control over this (note: on X11 platforms this may not
        // have the effect of overriding the setSwapInterval(1) in the
        // Gears demo)
        gl.setSwapInterval(0);

        renderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 16));
    }

    @Override
    public void dispose(GLAutoDrawable glad) {
        renderer = null;
    }

    @Override
    public void display(GLAutoDrawable glad) {
        render(glad);
        update();
    }

    @Override
    public void reshape(GLAutoDrawable glad, int i, int i1, int i2, int i3) {
    }

}
