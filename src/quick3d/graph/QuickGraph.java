/**
 * Copyright (C) 2015 Anderson de Oliveira Antunes <anderson.utf@gmail.com>
 *
 * This file is part of QuickP3D.
 *
 * QuickP3D is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * QuickP3D is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * QuickP3D. If not, see http://www.gnu.org/licenses/.
 */

package quick3d.graph;

/**
 * Interface para a implementação de um elemento gráfico 2D/3D.
 * 
 * Recomenda-se usar essa interface apenas na criação de classes anônimas, em
 * outro caso implente ambas as interfaces {@link quick3d.graph.Graph2D} e
 * {@link quick3d.graph.Graph3D}.
 * 
 * @author Anderson Antunes
 */

public interface QuickGraph extends Graph2D, Graph3D{
    
}
