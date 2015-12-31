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
package quick3d.simplegraphics;

import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.GL2.*;
import com.jogamp.opengl.util.gl2.GLUT;
import quick3d.graph.Graph3D;

/**
 * Exemplo de um objeto gráfico 3D.
 *
 * Esta classe desenha um objeto tridmensional composto por três setas, uma para
 * cada eixo, em um ponto pivô predefinido e coloridas em RGB, respectivamente.
 *
 * @author Anderson Antunes
 */
public class Axis implements Graph3D {

    @Override
    public void draw(GL2 gl, GLUT glut, boolean colorPicking) {
        gl.glPushMatrix();
        gl.glScalef(.1f, .1f, .1f);
        gl.glLineWidth(3);

        gl.glColor3f(0, 1, 0);
        gl.glBegin(GL_TRIANGLE_FAN);
        gl.glVertex3f(0, 13, 0);
        gl.glVertex3f(1, 10, 1);
        gl.glVertex3f(1, 10, -1);
        gl.glVertex3f(-1, 10, -1);
        gl.glVertex3f(-1, 10, 1);
        gl.glVertex3f(1, 10, 1);
        gl.glEnd();
        gl.glBegin(GL_LINE_STRIP);
        gl.glVertex3f(0, 0, 0);
        gl.glVertex3f(0, 13, 0);
        gl.glEnd();

        gl.glColor3f(0, 0, 1);
        gl.glBegin(GL_TRIANGLE_FAN);
        gl.glVertex3f(0, 0, 13);
        gl.glVertex3f(1, 1, 10);
        gl.glVertex3f(1, -1, 10);
        gl.glVertex3f(-1, -1, 10);
        gl.glVertex3f(-1, 1, 10);
        gl.glVertex3f(1, 1, 10);
        gl.glEnd();
        gl.glBegin(GL_LINE_STRIP);
        gl.glVertex3f(0, 0, 0);
        gl.glVertex3f(0, 0, 13);
        gl.glEnd();

        gl.glColor3f(1, 0, 0);
        gl.glBegin(GL_TRIANGLE_FAN);
        gl.glVertex3f(13, 0, 0);
        gl.glVertex3f(10, 1, 1);
        gl.glVertex3f(10, 1, -1);
        gl.glVertex3f(10, -1, -1);
        gl.glVertex3f(10, -1, 1);
        gl.glVertex3f(10, 1, 1);
        gl.glEnd();
        gl.glBegin(GL_LINE_STRIP);
        gl.glVertex3f(0, 0, 0);
        gl.glVertex3f(13, 0, 0);
        gl.glEnd();

        gl.glPopMatrix();
    }
}
