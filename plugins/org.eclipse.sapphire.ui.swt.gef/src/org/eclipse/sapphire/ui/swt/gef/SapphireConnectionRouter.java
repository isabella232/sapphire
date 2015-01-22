/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.geometry.Vector;
import org.eclipse.sapphire.ui.Bounds;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramConnectionModel;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramNodeModel;
import org.eclipse.sapphire.ui.swt.gef.utils.MultiValueMap;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class SapphireConnectionRouter 
{
	private MultiValueMap<HashKey,DiagramConnectionModel> connections = new MultiValueMap<HashKey,DiagramConnectionModel>();
	private MultiValueMap<HashKey,Integer> connectionIndices = new MultiValueMap<HashKey,Integer>();
	private HashMap<DiagramConnectionModel, Integer> connectionIndexMap = 
					new HashMap<DiagramConnectionModel, Integer>();
	private int separation = 30;
	
	private static final class HashKey 
	{
		private DiagramNodeModel sourceNode, targetNode;

		HashKey(DiagramConnectionModel conn) 
		{
			sourceNode = conn.getSourceNode();
			targetNode = conn.getTargetNode();
		}

		public boolean equals(Object object) 
		{
			boolean isEqual = false;
			HashKey hashKey;

			if (object instanceof HashKey) 
			{
				hashKey = (HashKey) object;
				DiagramNodeModel hkA1 = hashKey.getFirstNode();
				DiagramNodeModel hkA2 = hashKey.getSecondNode();

				isEqual = (hkA1.equals(sourceNode) && hkA2.equals(targetNode))
						|| (hkA1.equals(targetNode) && hkA2.equals(sourceNode));
			}
			return isEqual;
		}

		public DiagramNodeModel getFirstNode() 
		{
			return sourceNode;
		}

		public DiagramNodeModel getSecondNode() 
		{
			return targetNode;
		}

		public int hashCode() 
		{
			return sourceNode.hashCode() ^ targetNode.hashCode();
		}
	}
	

	public SapphireConnectionRouter() {}
		
	public int getSeparation() 
	{
		return separation;
	}
	
	public void setSeparation(int value) 
	{
		separation = value;
	}
	
	public void removeConnectionFromCache(DiagramConnectionModel conn)
	{
		HashKey connectionKey = new HashKey(conn);
		connections.remove(connectionKey, conn);
		if (connectionIndexMap.containsKey(conn))
		{
			int index = connectionIndexMap.get(conn);
			connectionIndices.remove(connectionKey, index);
		}
	}
	
	public void clear()
	{
		this.connectionIndexMap.clear();
		this.connections.clear();
		this.connectionIndices.clear();
	}
	
	private Point getNodeLocation(DiagramNodeModel node) 
	{
		Bounds bounds = node.getNodeBounds();
		if (node.getShapePresentation().getFigure() != null)
		{
			Rectangle rect = node.getShapePresentation().getFigure().getBounds();
			int x = rect.x != -1 ? rect.x : bounds.getX();
			int y = rect.y != -1 ? rect.y : bounds.getY();
			return new Point(x + rect.width / 2, y + rect.height / 2);
		}
		else
		{					
			return new Point(bounds.getX() + bounds.getWidth() /2, bounds.getY() + bounds.getWidth() / 2);
		}
	}
	
	public Point route(DiagramConnectionModel conn) 
	{		
		final HashKey connectionKey = new HashKey(conn);

		if( this.connections.containsKey( connectionKey ) ) 
		{
			PointList points = new PointList();
			points.addPoint(getNodeLocation(conn.getSourceNode()));
			points.addPoint(getNodeLocation(conn.getTargetNode()));

			int index  = getNextConnectionIndex(connectionKey);
			addConnectionKey(conn, connectionKey, index);

			Point bendpoint = handleCollision(points, index);
			return bendpoint;
		}
		else 
		{
			addConnectionKey(conn, connectionKey, -1);
		}
		
		return null;
	}
	
	public void addConnection(DiagramConnectionModel conn)
	{
		HashKey connectionKey = new HashKey(conn);
		addConnectionKey(conn, connectionKey, -1);
	}
	
	private void addConnectionKey(DiagramConnectionModel conn, HashKey connectionKey, int index)
	{
		connections.put(connectionKey, conn);
		connectionIndices.put(connectionKey, index);
		connectionIndexMap.put(conn, index);		
	}
	
	private Point handleCollision(PointList points, int index) 
	{
		PrecisionPoint start = new PrecisionPoint( points.getFirstPoint() );
		PrecisionPoint end = new PrecisionPoint( points.getLastPoint() );

		if (start.equals(end))
			return null;

		Point midPoint = new Point((end.x + start.x) / 2, (end.y + start.y) / 2);
		int position = end.getPosition(start);
		Vector vector;
		if (position == PositionConstants.SOUTH
				|| position == PositionConstants.EAST)
			vector = new Vector(start, end);
		else
			vector = new Vector(end, start);
		double length = vector.getLength();

		double xSeparation = getSeparation() * vector.x / length;
		double ySeparation = getSeparation() * vector.y / length;

		Point bendPoint;

		if (index % 2 == 0)
		{
			bendPoint = new Point
			(
				(int) ( midPoint.x + ( (double) index / 2 ) * ( -1 * ySeparation ) ),
				(int) ( midPoint.y + ( (double) index / 2 ) * xSeparation )
			);
		}
		else
		{
			bendPoint = new Point
			(
			    (int) ( midPoint.x + ( (double) index / 2 ) * ySeparation ),
			    (int) ( midPoint.y + ( (double) index / 2 ) * ( -1 * xSeparation ) )
			);
		}
		if (!bendPoint.equals(midPoint)) {
			return bendPoint;
		}
		
		return null;
	}
	
	private int getNextConnectionIndex(HashKey connectionKey)
	{
		final Set<Integer> indices = connectionIndices.get(connectionKey);
		int[] intArr = new int[indices.size()];
		int i = 0;
		for (Object obj : indices)
		{
			Integer intObj = (Integer)obj;
			intArr[i++] = intObj;
		}
		Arrays.sort(intArr, 0, indices.size());
		
		int nextInt = -1;
		for (int j = 0; j < intArr.length; j++)
		{
			int temp = intArr[j];
			if (temp > 1 && temp != j + 1)
			{
				nextInt = j + 1;
				break;
			}
		}
		if (nextInt == -1)
		{
			nextInt = indices.size() + 1;
		}
		return nextInt;
	}

}
