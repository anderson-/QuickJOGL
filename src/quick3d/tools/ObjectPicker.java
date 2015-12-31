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
package quick3d.tools;

import static com.jogamp.opengl.GL.*;
import com.jogamp.opengl.GL2;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Exemplo de um objeto gráfico 3D.
 *
 * Esta classe desenha um objeto tridmensional composto por três setas, uma para
 * cada eixo, em um ponto pivô predefinido e coloridas em RGB, respectivamente.
 *
 * @author Anderson Antunes
 */
public class ObjectPicker<T> implements Iterable<T> {

    public interface Selectable<T> {

        public T select(int index);
    }

    public static final String ADD = "add";
    public static final String REMOVE = "remove";

    private final PropertyChangeSupport support;
    private final ArrayList<T> selected;
    private final Selectable<T> source;

    public ObjectPicker(Selectable<T> source) {
        this.source = source;
        selected = new ArrayList<>();
        support = new PropertyChangeSupport(this);
    }

    public synchronized void add(T t) {
        if (!selected.contains(t)) {
            selected.add(t);
            support.firePropertyChange(ADD, null, t);
        }
    }

    public synchronized void remove(T t) {
        if (selected.remove(t)) {
            support.firePropertyChange(REMOVE, null, t);
        }
    }

    public synchronized void clear() {
        for (T t : new ArrayList<>(selected)) {
            support.firePropertyChange(REMOVE, null, t);
        }
        selected.clear();
    }

    public boolean isEmpty() {
        return selected.isEmpty();
    }

    public int getSize() {
        return selected.size();
    }

    public int getIndex(GL2 gl, int x, int y) {
        gl.glFlush();
        gl.glFinish();
        gl.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        ByteBuffer vpBuffer = ByteBuffer.allocateDirect(8);
        gl.glReadPixels(x, y, 1, 1, GL_RGBA, GL_UNSIGNED_BYTE, vpBuffer);
        byte[] data = new byte[4];
        vpBuffer.get(data);
        int id = data[0] + (data[1] << 8) + (data[2] << 16);
        if (id < 0) {
            id = 256 + id;
        }
        return id;
    }

    public float[] getColor(int index) {
        if (index < 0 || index > 16777216) {
            throw new RuntimeException("Invalid range index!");
        }
        int g = (index & 0x0000FF00) >> 8;
        int b = (index & 0x00FF0000) >> 16;
        return new float[]{(index & 0xFF) / 255f, g / 255f, b / 255f, 1f};
    }

    public final T pick(GL2 gl, int x, int y) {
        return source.select(getIndex(gl, x, y));
    }

    public final void select(GL2 gl, int x, int y) {
        T tmp = pick(gl, x, y);
        if (tmp != null) {
            synchronized (this) {
                if (selected.contains(tmp)) {
                    selected.remove(tmp);
                    support.firePropertyChange(REMOVE, null, tmp);
                } else {
                    selected.add(tmp);
                    support.firePropertyChange(ADD, null, tmp);
                }
            }
        }
    }

    @Override
    public synchronized Iterator<T> iterator() {
        return selected.iterator();
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        support.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        support.removePropertyChangeListener(l);
    }

}
