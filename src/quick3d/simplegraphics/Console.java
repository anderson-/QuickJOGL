/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quick3d.simplegraphics;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.gl2.GLUT;
import java.awt.Color;
import java.util.LinkedList;
import quick3d.DrawingPanel3D;
import quick3d.graph.Graph2D;

/**
 *
 * @author andy
 */
public class Console implements Graph2D {

    private final int x;
    private final int y;
    private final int limit;
    private final float[] color;
    private final LinkedList<String> data;

    public Console(int x, int y, int limit, Color color) {
        this.x = x;
        this.y = y;
        this.limit = limit;
        this.color = color.getColorComponents(null);
        data = new LinkedList<>();
    }

    public synchronized void put(String str) {
        if (str.startsWith("{clear}")) {
            data.clear();
            return;
        }
        String[] sub = str.split("\n");
        for (int i = sub.length - 1; i >= 0; i--) {
            if (!sub[i].isEmpty()) {
                data.addFirst(sub[i]);
            }
        }
        while (data.size() > limit) {
            data.removeLast();
        }
    }

    @Override
    public synchronized void draw(GL2 gl, GLUT glut) {
        int ty = y;
        gl.glColor3f(color[0], color[1], color[2]);
        for (String str : data) {
//            gl.glRasterPos2f(x, ty);
//            glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, str);

            ty += 18;
            DrawingPanel3D.asd.drawString(str, x, ty);

        }
    }

}
