/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - [376245] Revert action in StructuredTextEditor does not revert diagram nodes and connections in SapphireDiagramEditor
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.layout;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceEvent;
import org.eclipse.sapphire.ui.Point;
import org.eclipse.sapphire.ui.diagram.DiagramConnectionPart;

/**
 * <p>Responsible for persisting layout of the diagram, such a location and size of nodes, connection bend points, etc.</p>
 * 
 * <p>Unlike other services, DiagramLayoutPersistenceService is not defined by methods that must be
 * implemented, but rather by its expected behavior.</p>
 * 
 * <ol>
 *   <li>During service initialization, the implementation is expected to load layout information and transfer it to
 *     diagram parts using API such as DiagramNodePart.setNodeBounds().</li>
 *   <li>After initialization, the implementation is expected to listen for changes to diagram parts and persist
 *     layout information. Persistence can happen immediately or be deferred until the save event is received.</li>
 *   <li>If implementation defers layout persistence until save, it is expected to implement dirty() method and to
 *     issue DirtyStateEvent when this state changes.</li>
 * </ol>
 * 
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class DiagramLayoutPersistenceService extends Service
{
	public boolean dirty()
	{
	    return false;
	}
	
	/**
	 * Retrieves a connection's bendpoints and label position.
	 * @param connection The connection part
	 * @return The diagram connection info including bend points and label position. 
	 * Returns null if the connection is not known to the layout persistence service. 
	 */
	public abstract DiagramConnectionInfo read(DiagramConnectionPart connection);
		
	public static class DirtyStateEvent extends ServiceEvent
	{
	    private final boolean before;
	    private final boolean after;
	    
	    public DirtyStateEvent( final Service service, final boolean before, final boolean after )
	    {
	        super( service );
	        
	        this.before = before;
	        this.after = after;
	    }
	    
	    public boolean before()
	    {
	        return this.before;
	    }
	    
	    public boolean after()
	    {
	        return this.after;
	    }
	}
	
	public static class DiagramConnectionInfo
	{
		private List<Point> bendPoints;
		private Point labelPosition;
		
		public DiagramConnectionInfo(List<Point> bendPoints)
		{
			this.bendPoints = new ArrayList<Point>();
			this.bendPoints.addAll(bendPoints);
		}
		
		public DiagramConnectionInfo(List<Point> bendPoints, Point labelPosition)
		{
			this.bendPoints = new ArrayList<Point>();
			if (bendPoints != null)
			{
				this.bendPoints.addAll(bendPoints);
			}
			if (labelPosition != null)
			{
				this.labelPosition = new Point(labelPosition.getX(), labelPosition.getY());
			}
		}
		
		public List<Point> getBendPoints()
		{
			return this.bendPoints;
		}
		
		public Point getLabelPosition()
		{
			return this.labelPosition;
		}
	}
	
}
