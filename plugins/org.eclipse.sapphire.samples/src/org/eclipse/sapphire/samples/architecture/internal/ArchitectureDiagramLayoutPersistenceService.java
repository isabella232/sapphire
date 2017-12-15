/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - [378756] Convert ModelElementListener and ModelPropertyListener to common listener infrastructure
 *    Konstantin Komissarchik - [376245] Revert action in StructuredTextEditor does not revert diagram nodes and connections in SapphireDiagramEditor
 *    Konstantin Komissarchik - [381233] IllegalStateException in ServiceContext when modifying xml in source editor in Architecture sample
 ******************************************************************************/

package org.eclipse.sapphire.samples.architecture.internal;

import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.PropertyEvent;
import org.eclipse.sapphire.samples.architecture.ConnectionBendpoint;
import org.eclipse.sapphire.samples.architecture.ArchitectureSketch;
import org.eclipse.sapphire.samples.architecture.Component;
import org.eclipse.sapphire.samples.architecture.ComponentDependency;
import org.eclipse.sapphire.ui.Bounds;
import org.eclipse.sapphire.ui.Point;
import org.eclipse.sapphire.ui.diagram.editor.DiagramConnectionBendPoints;
import org.eclipse.sapphire.ui.diagram.editor.DiagramConnectionEvent;
import org.eclipse.sapphire.ui.diagram.editor.DiagramConnectionPart;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodeBounds;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodeEvent;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodePart;
import org.eclipse.sapphire.ui.diagram.editor.DiagramPageEvent;
import org.eclipse.sapphire.ui.diagram.editor.IdUtil;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramPartListener;
import org.eclipse.sapphire.ui.diagram.layout.ConnectionHashKey;
import org.eclipse.sapphire.ui.diagram.layout.DiagramLayoutPersistenceService;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class ArchitectureDiagramLayoutPersistenceService extends DiagramLayoutPersistenceService 
{
	private ArchitectureSketch architecture;
	private SapphireDiagramPartListener diagramPartListener;
	private Listener componentListener;	
	private Listener componentDependencyListener;
	private Map<String, DiagramNodeBounds> nodeBounds;
	private Map<ConnectionHashKey, DiagramConnectionBendPoints> connectionBendPoints;
	private boolean dirty;
	
	@Override
    protected void init()
    {
    	super.init();    	
    	this.architecture = (ArchitectureSketch)context( SapphireDiagramEditorPagePart.class ).getLocalModelElement();
    	this.nodeBounds = new HashMap<String, DiagramNodeBounds>();
    	this.connectionBendPoints = new HashMap<ConnectionHashKey, DiagramConnectionBendPoints>();
    	this.dirty = false;
    	load();
    	refreshPersistedPartsCache();
    	addDiagramPartListener();
    	addModelListeners();
    }

	private void setGridVisible(boolean visible)
	{
		this.architecture.setShowGrid(visible);
	}
	
	private void setGuidesVisible(boolean visible)
	{
		this.architecture.setShowGuides(visible);
	}

	private void read(DiagramNodePart nodePart)
	{
		
		Component component = (Component)nodePart.getLocalModelElement();
		if (!component.disposed())
		{
			String nodeId = IdUtil.computeNodeId(nodePart);
	    	if (this.nodeBounds.containsKey(nodeId) && this.nodeBounds.get(nodeId) != null)
	    	{
	    		nodePart.setNodeBounds(this.nodeBounds.get(nodeId)); 		
	    	}
		}
	}
	
	private void write(DiagramNodePart nodePart) 
	{
		Component component = (Component)nodePart.getLocalModelElement();
		if (!component.disposed())
		{
			if (isNodeLayoutChanged(nodePart))
			{				
				this.architecture.detach(this.componentListener, "/Components/Position/*");
				writeComponentBounds(component, nodePart);
				this.architecture.attach(this.componentListener, "/Components/Position/*");
			}
			
			refreshDirtyState();
		}		
	}

	private void read(DiagramConnectionPart connPart)
	{
    	ConnectionHashKey key = ConnectionHashKey.createKey(connPart);
    	if (this.connectionBendPoints.containsKey(key) && this.connectionBendPoints.get(key) != null)
    	{    		
    		connPart.resetBendpoints(this.connectionBendPoints.get(key));
    	}		
	}
	
	private void write(DiagramConnectionPart connPart) 
	{
		ComponentDependency dependency = (ComponentDependency)connPart.getLocalModelElement();
		if (!dependency.disposed())
		{
			if (isConnectionLayoutChanged(connPart))
			{
				this.architecture.detach(this.componentDependencyListener, "/Components/Dependencies/ConnectionBendpoints/*");
				writeDependencyBendPoints(dependency, connPart);
				this.architecture.attach(this.componentDependencyListener, "/Components/Dependencies/ConnectionBendpoints/*");
			}
			
			refreshDirtyState();
		}			
	}
	
	@Override
	public void dispose()
	{
		if (this.diagramPartListener != null)
		{
		    context( SapphireDiagramEditorPagePart.class ).removeListener(this.diagramPartListener);
		}
		if (this.componentListener != null)
		{
			this.architecture.detach(this.componentListener, "/Components/Position/*");
		}
		if (this.componentDependencyListener != null)
		{
			this.architecture.detach(this.componentDependencyListener, "/Components/Dependencies/ConnectionBendpoints/*");
		}
	}

	private void load()
	{
	    context( SapphireDiagramEditorPagePart.class ).setGridVisible(this.architecture.isShowGrid().getContent());
	    context( SapphireDiagramEditorPagePart.class ).setShowGuides(this.architecture.isShowGuides().getContent());
		
		ModelElementList<Component> components = this.architecture.getComponents();
		for (Component component : components)
		{
			DiagramNodePart nodePart = context( SapphireDiagramEditorPagePart.class ).getDiagramNodePart(component);
			if (nodePart != null)
			{
				DiagramNodeBounds bounds = null;
				bounds = new DiagramNodeBounds(component.getPosition().getX().getContent(), 
						component.getPosition().getY().getContent(), -1, -1,
						false, false);
				nodePart.setNodeBounds(bounds);					
				
				// load the embedded connection layout
				ModelElementList<ComponentDependency> dependencies = component.getDependencies();
				for (ComponentDependency dependency : dependencies)
				{
					DiagramConnectionPart connPart = context( SapphireDiagramEditorPagePart.class ).getDiagramConnectionPart(dependency);
					if (connPart != null)
					{						
						ModelElementList<ConnectionBendpoint> bendpoints = dependency.getConnectionBendpoints();
						if (bendpoints.size() > 0)
						{
							int index = 0;
							for (ConnectionBendpoint bendpoint : bendpoints)
							{
								connPart.addBendpoint(index++, bendpoint.getX().getContent(), 
										bendpoint.getY().getContent());
							}							
						}
					}
				}				
			}
		}		
	}
	
	private void handleNodeLayoutChange(Component component)
	{
		DiagramNodePart nodePart = context( SapphireDiagramEditorPagePart.class ).getDiagramNodePart(component);
		DiagramNodeBounds nodeBounds = new DiagramNodeBounds(component.getPosition().getX().getContent(),
				component.getPosition().getY().getContent());
		nodePart.setNodeBounds(nodeBounds);
	}
	
	private void handleConnectionBendpointChange(ComponentDependency componentDependency)
	{
		DiagramConnectionPart connPart = context( SapphireDiagramEditorPagePart.class ).getDiagramConnectionPart(componentDependency);
		if (connPart != null)
		{
			List<Point> bendpoints = new ArrayList<Point>();
			for (ConnectionBendpoint bendpoint : componentDependency.getConnectionBendpoints())
			{
				bendpoints.add(new Point(bendpoint.getX().getContent(), bendpoint.getY().getContent()));
			}
			connPart.resetBendpoints(bendpoints, false, false);
		}
	}
	
	private void addNodeToPersistenceCache(DiagramNodePart nodePart)
	{
		String nodeId = IdUtil.computeNodeId(nodePart);
		this.nodeBounds.put(nodeId, nodePart.getNodeBounds());
	}
	
	private void addConnectionToPersistenceCache(DiagramConnectionPart connPart)
	{
		ConnectionHashKey connKey = ConnectionHashKey.createKey(connPart);
		this.connectionBendPoints.put(connKey, connPart.getConnectionBendpoints());
	}

	private void addDiagramPartListener()
	{
		this.diagramPartListener = new SapphireDiagramPartListener() 
		{
			@Override
            public void handleNodeAddEvent(final DiagramNodeEvent event)
            {
				read((DiagramNodePart)event.getPart());
            }
			
			@Override
		    public void handleNodeMoveEvent(final DiagramNodeEvent event)
		    {
				DiagramNodePart nodePart = (DiagramNodePart)event.getPart();
				DiagramNodeBounds nodeBounds = nodePart.getNodeBounds();
				
				if (nodeBounds.isAutoLayout())
				{
					// need to add the node bounds to the persistence cache so that "revert" could work
					addNodeToPersistenceCache(nodePart);
					refreshDirtyState();
				}
				else if (!nodeBounds.isDefaultPosition())
				{
					write((DiagramNodePart)event.getPart());
				}
		    }
			
			@Override
		    public void handleNodeDeleteEvent(final DiagramNodeEvent event)
			{
			    refreshDirtyState();
			}
			
			@Override
	        public void handleConnectionAddEvent(final DiagramConnectionEvent event)
			{
				DiagramConnectionPart connPart = (DiagramConnectionPart)event.getPart();
				read(connPart);
			}
			
			@Override
			public void handleConnectionDeleteEvent(final DiagramConnectionEvent event)
			{
				refreshDirtyState();
			}
			
			@Override
			public void handleConnectionAddBendpointEvent(final DiagramConnectionEvent event)
			{
				write((DiagramConnectionPart)event.getPart());
			}
			
			@Override
		    public void handleConnectionRemoveBendpointEvent(final DiagramConnectionEvent event)
		    {
				write((DiagramConnectionPart)event.getPart());
			}

		    public void handleConnectionMoveBendpointEvent(final DiagramConnectionEvent event)
		    {
		    	write((DiagramConnectionPart)event.getPart());
		    }

		    @Override
		    public void handleConnectionResetBendpointsEvent(final DiagramConnectionEvent event)
		    {
		    	DiagramConnectionPart connPart = (DiagramConnectionPart)event.getPart();
		    	DiagramConnectionBendPoints bendPoints = connPart.getConnectionBendpoints();
		    	if (bendPoints.isAutoLayout())
		    	{
		    		addConnectionToPersistenceCache(connPart);
		    		refreshDirtyState();
		    	}
		    	else
		    	{
		    		if (bendPoints.isDefault())
		    		{
		    			// Both the SapphireDiagramEditor and this class listen on connection
		    			// events and we don't control who receives the events first.
						// During "revert", t=if the default bend point is added after the connection 
						// was read, we need to re-read the connection to ensure "revert" works.		    			
		    			read(connPart);
		    		}
		    		else
		    		{
		    			write((DiagramConnectionPart)event.getPart());
		    		}
		    	}
		    }
		    
		    @Override
			public void handleGridStateChangeEvent(final DiagramPageEvent event)
			{
		    	SapphireDiagramEditorPagePart diagramPart = (SapphireDiagramEditorPagePart)event.getPart();
		    	setGridVisible(diagramPart.isGridVisible());
			}
			
			@Override
			public void handleGuideStateChangeEvent(final DiagramPageEvent event)
			{
		    	SapphireDiagramEditorPagePart diagramPart = (SapphireDiagramEditorPagePart)event.getPart();
		    	setGuidesVisible(diagramPart.isShowGuides());
			}
			
			@Override
			public void handleDiagramSaveEvent(final DiagramPageEvent event)
			{
				doSave();
			}
						
		};
		context( SapphireDiagramEditorPagePart.class ).addListener(this.diagramPartListener);
	}
	
	private void addModelListeners()
	{
		this.componentListener = new FilteredListener<PropertyEvent>() 
		{
		    @Override
		    protected void handleTypedEvent( final PropertyEvent event )
		    {
		    	if (event != null && event.element() != null)
		    	{
		    		Component component = event.element().nearest(Component.class);
		    		if (component != null)
		    		{
		    			handleNodeLayoutChange(component);
		    		}
		    	}
		    }
		};
		
		this.componentDependencyListener = new FilteredListener<PropertyEvent>()
		{
			@Override
			protected void handleTypedEvent( final PropertyEvent event )
			{
				if (event != null && event.element() != null)
				{
					ComponentDependency componentDependency = event.element().nearest(ComponentDependency.class);
					if (componentDependency != null)
					{
						handleConnectionBendpointChange(componentDependency);
					}
				}
			}
		};
		
		this.architecture.attach(this.componentListener, "/Components/Position/*");
		this.architecture.attach(this.componentDependencyListener, "/Components/Dependencies/ConnectionBendpoints/*");
		
	}
	
	private void writeComponentBounds(Component component, DiagramNodePart node)
	{
		Bounds bounds = node.getNodeBounds();
		if (bounds.getX() != component.getPosition().getX().getContent())
		{
			component.getPosition().setX(bounds.getX());
		}
		if (bounds.getY() != component.getPosition().getY().getContent())
		{
			component.getPosition().setY(bounds.getY());
		}
	}
	
	private void writeDependencyBendPoints(ComponentDependency dependency, DiagramConnectionPart connPart)
	{
	    final ModelElementList<ConnectionBendpoint> bpInModelList = dependency.getConnectionBendpoints();
	    final int bpInModelSize = bpInModelList.size();
	    final List<Point> bpInPartList = connPart.getConnectionBendpoints().getBendPoints();
	    final int bpInPartSize = bpInPartList.size();
	    
	    for( int i = 0, n = min( bpInModelSize, bpInPartSize ); i < n; i++ )
	    {
	        final ConnectionBendpoint bpInModel = bpInModelList.get( i );
	        final Point bpInPart = bpInPartList.get( i );
	        
	        if (bpInModel.getX().getContent() != bpInPart.getX())
	        {
	        	bpInModel.setX( bpInPart.getX() );
	        }
	        if (bpInModel.getY().getContent() != bpInPart.getY())
	        {
	        	bpInModel.setY( bpInPart.getY() );
	        }
	    }
	    
	    if( bpInModelSize < bpInPartSize )
	    {
	        for( int i = bpInModelSize; i < bpInPartSize; i++ )
	        {
	            final ConnectionBendpoint bpInModel = bpInModelList.insert();
	            final Point bpInPart = bpInPartList.get( i );
	            
	            bpInModel.setX( bpInPart.getX() );
	            bpInModel.setY( bpInPart.getY() );
	        }
	    }
	    else if( bpInModelSize > bpInPartSize )
	    {
	        for( int i = bpInModelSize - 1; i >= bpInPartSize; i-- )
	        {
	            bpInModelList.remove( i );
	        }
	    }
	}
		
	private void doSave()
	{
		refreshPersistedPartsCache();
		// For nodes that are placed using default node positions and connection bend points that
		// are calculated using connection router, we don't modify the corresponding model properties
		// in order to allow "revert" in source editor to work correctly.
		// So we need to do an explicit save of the node bounds and connection bend points here.
		this.architecture.detach(this.componentListener, "/Components/Position/*");
		this.architecture.detach(this.componentDependencyListener, "/Components/Dependencies/ConnectionBendpoints/*");
		
		for (DiagramNodePart nodePart : context( SapphireDiagramEditorPagePart.class ).getNodes())
		{
			Component component = (Component)nodePart.getLocalModelElement();
			if (!component.disposed())
			{
				writeComponentBounds(component, nodePart);
			}
		}
		
		for (DiagramConnectionPart connPart : context( SapphireDiagramEditorPagePart.class ).getConnections())
		{
			ComponentDependency dependency = (ComponentDependency)connPart.getLocalModelElement();
			if (!dependency.disposed())
			{
				writeDependencyBendPoints(dependency, connPart);
			}
		}
		
		this.architecture.attach(this.componentListener, "/Components/Position/*");
		this.architecture.attach(this.componentDependencyListener, "/Components/Dependencies/ConnectionBendpoints/*");
	}
	
    private boolean isNodeLayoutChanged(DiagramNodePart nodePart)
    {
		DiagramNodeBounds newBounds = nodePart.getNodeBounds();
		boolean changed = false;
		String nodeId = IdUtil.computeNodeId(nodePart);
		if (this.nodeBounds.containsKey(nodeId))
		{
			DiagramNodeBounds oldBounds = this.nodeBounds.get(nodeId);
			if (!newBounds.equals(oldBounds))
			{
				changed = true;
			}
		}
		else
		{
			changed = true;
		}
    	return changed;
    }
	
    private boolean isConnectionLayoutChanged(DiagramConnectionPart connPart)
    {
		// Detect whether the connection bendpoints have been changed.
    	DiagramConnectionBendPoints bendpoints = connPart.getConnectionBendpoints();
		ConnectionHashKey key = ConnectionHashKey.createKey(connPart);
		boolean changed = false;
		if (this.connectionBendPoints.containsKey(key))
		{		
			DiagramConnectionBendPoints oldBendpoints = this.connectionBendPoints.get(key);
			if (!bendpoints.equals(oldBendpoints))
			{
				changed = true;
			}
		}
		else
		{
			changed = true;
		}
    	return changed;
    }
    
    private boolean isDiagramLayoutChanged()
    {
    	boolean changed = false;
		for (DiagramNodePart nodePart : context( SapphireDiagramEditorPagePart.class ).getNodes())
		{
			if (!nodePart.getLocalModelElement().disposed() && isNodeLayoutChanged(nodePart))
			{
				changed = true;
				break;
			}
		}
		for (DiagramConnectionPart connPart : context( SapphireDiagramEditorPagePart.class ).getConnections())
		{
			if (!connPart.getLocalModelElement().disposed() && isConnectionLayoutChanged(connPart))
			{
				changed = true;
				break;
			}
		}
		
    	return changed;
    }
    
    @Override
    public boolean dirty()
    {
        return this.dirty;
    }

    private void refreshDirtyState()
    {
        final boolean after = isDiagramLayoutChanged();
        
        if( this.dirty != after )
        {
            final boolean before = this.dirty;
            this.dirty = after;
            
            broadcast( new DirtyStateEvent( this, before, after ) );
        }
    }
	
	private void refreshPersistedPartsCache()
	{
		this.nodeBounds.clear();
		this.connectionBendPoints.clear();
		for (DiagramConnectionPart connPart : context( SapphireDiagramEditorPagePart.class ).getConnections())
		{
			addConnectionToPersistenceCache(connPart);
		}
		for (DiagramNodePart nodePart : context( SapphireDiagramEditorPagePart.class ).getNodes())
		{
			addNodeToPersistenceCache(nodePart);
		}		
	}
	
}
