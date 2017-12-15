/******************************************************************************
 * Copyright (c) 2013 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - [342897] Integrate with properties view
 *    Ling Hao - [344319] Image specification for diagram parts inconsistent with the rest of sdef
 *    Konstantin Komissarchik - [375770] Support context menu actions when multiple diagram parts are selected
 *    Konstantin Komissarchik - [378756] Convert ModelElementListener and ModelPropertyListener to common listener infrastructure
 *    Gregory Amerson - [346172] Support zoom, print and save as image actions in the diagram editor
 *    Konstantin Komissarchik - [346172] Support zoom, print and save as image actions in the diagram editor
 *    Konstantin Komissarchik - [381794] Cleanup needed in presentation code for diagram context menu
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImageData;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.ui.IPropertiesViewContributorPart;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.PartVisibilityEvent;
import org.eclipse.sapphire.ui.Point;
import org.eclipse.sapphire.ui.PropertiesViewContributionManager;
import org.eclipse.sapphire.ui.PropertiesViewContributionPart;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.SapphireEditor;
import org.eclipse.sapphire.ui.SapphireEditorPagePart;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.SapphirePartListener;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.diagram.def.IDiagramConnectionDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramEditorPageDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramExplicitConnectionBindingDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramImplicitConnectionBindingDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramNodeDef;
import org.eclipse.sapphire.ui.diagram.editor.DiagramConnectionTemplate.DiagramConnectionTemplateListener;
import org.eclipse.sapphire.ui.diagram.editor.DiagramImplicitConnectionTemplate.DiagramImplicitConnectionTemplateListener;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodeTemplate.DiagramNodeTemplateListener;
import org.eclipse.sapphire.ui.diagram.state.DiagramEditorPageState;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.sapphire.util.ListFactory;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public final class SapphireDiagramEditorPagePart extends SapphireEditorPagePart
{
    private IModelElement modelElement;
    private IDiagramEditorPageDef diagramPageDef = null;
    private List<IDiagramNodeDef> nodeDefs;
    private List<IDiagramConnectionDef> connectionDefs;
    private List<DiagramNodeTemplate> nodeTemplates;
    private List<DiagramConnectionTemplate> connectionTemplates;
    private List<DiagramImplicitConnectionTemplate> implicitConnectionTemplates;
    private NodeTemplateListener nodeTemplateListener;
    private ConnectionTemplateListener connTemplateListener;
    private PropertiesViewContributionManager propertiesViewContributionManager;
    private List<ISapphirePart> selections;
    private ImplicitConnectionTemplateListener implicitConnTemplateListener;
    private boolean showGrid;
    private boolean showGuides;
    private int gridUnit;
    private int verticalGridUnit;
	private List<FunctionResult> connectionImageDataFunctionResults;
	private Point mouseLocation;
    private DiagramEditorPageState state;

    @Override
    protected void init()
    {
        super.init();
        
        try
        {
            final File stateFile = adapt( SapphireEditor.class ).getDefaultStateStorageFile( this );
            this.state = DiagramEditorPageState.TYPE.instantiate( new RootXmlResource( new XmlResourceStore( stateFile ) ) );
        }
        catch( ResourceStoreException e )
        {
            this.state = DiagramEditorPageState.TYPE.instantiate();
        }
            
        this.diagramPageDef = (IDiagramEditorPageDef)super.definition;
        ImpliedElementProperty modelElementProperty = (ImpliedElementProperty)resolve(this.diagramPageDef.getProperty().getContent());
        if (modelElementProperty != null)
        {
            this.modelElement = getModelElement().read( modelElementProperty ).element();            
        }
        else
        {
            this.modelElement = getModelElement();
        }
        
        this.showGrid = this.diagramPageDef.getGridDefinition().isVisible().getContent();
        this.showGuides = this.diagramPageDef.getGuidesDefinition().isVisible().getContent();
        this.gridUnit = this.diagramPageDef.getGridDefinition().getGridUnit().getContent();
        this.verticalGridUnit = this.diagramPageDef.getGridDefinition().getVerticalGridUnit().getContent();
        this.mouseLocation = new Point(0, 0);
        
        this.nodeTemplateListener = new NodeTemplateListener();
        this.connTemplateListener = new ConnectionTemplateListener();
        this.implicitConnTemplateListener = new ImplicitConnectionTemplateListener();
        
        this.nodeTemplates = new ArrayList<DiagramNodeTemplate>();
        this.nodeDefs = this.diagramPageDef.getDiagramNodeDefs();
        this.connectionDefs = this.diagramPageDef.getDiagramConnectionDefs();
        
        for (final IDiagramNodeDef nodeDef : this.nodeDefs)
        {
            final DiagramNodeTemplate nodeTemplate = new DiagramNodeTemplate();
            nodeTemplate.init(this, this.modelElement, nodeDef, Collections.<String,String>emptyMap());
            this.nodeTemplates.add(nodeTemplate);
            nodeTemplate.addTemplateListener(this.nodeTemplateListener);
            
            nodeTemplate.attach
            (
                 new FilteredListener<PartVisibilityEvent>()
                 {
                    @Override
                    protected void handleTypedEvent( final PartVisibilityEvent event )
                    {
                        refreshDiagramPalette( (DiagramNodeTemplate) event.part() );
                    }
                 }
            );
        }
        
        // Need to initialize the embedded connections after all the diagram node parts are created
        // For connections between "anonymous" nodes, we'd represent connections using node index based
        // mechanism.
        for (DiagramNodeTemplate nodeTemplate : this.nodeTemplates)
        {
            nodeTemplate.initEmbeddedConnections();
            if (nodeTemplate.getEmbeddedConnectionTemplate() != null)
            {
                nodeTemplate.getEmbeddedConnectionTemplate().addTemplateListener(this.connTemplateListener);
            }
        }
                
        this.connectionTemplates = new ArrayList<DiagramConnectionTemplate>();
        ModelElementList<IDiagramExplicitConnectionBindingDef> connectionBindings = this.diagramPageDef.getDiagramConnectionBindingDefs();
        for (IDiagramExplicitConnectionBindingDef connBinding : connectionBindings)
        {
            IDiagramConnectionDef connDef = getDiagramConnectionDef(connBinding.getConnectionId().getContent());
            DiagramConnectionTemplate connectionTemplate = new DiagramConnectionTemplate(connBinding);
            connectionTemplate.init(this, this.modelElement, connDef, Collections.<String,String>emptyMap());
            this.connectionTemplates.add(connectionTemplate);
            connectionTemplate.addTemplateListener(this.connTemplateListener);
        }
        
        // initialize implicit connections
        this.implicitConnectionTemplates = new ArrayList<DiagramImplicitConnectionTemplate>();
        ModelElementList<IDiagramImplicitConnectionBindingDef> implicitConnBindings = this.diagramPageDef.getImplicitConnectionBindingDefs();
        for (IDiagramImplicitConnectionBindingDef implicitConnBinding : implicitConnBindings)
        {
            IDiagramConnectionDef connDef = getDiagramConnectionDef(implicitConnBinding.getConnectionId().getContent());
            DiagramImplicitConnectionTemplate connectionTemplate = new DiagramImplicitConnectionTemplate(implicitConnBinding);
            connectionTemplate.init(this, this.modelElement, connDef, Collections.<String,String>emptyMap());
            this.implicitConnectionTemplates.add(connectionTemplate);
            connectionTemplate.addTemplateListener(this.implicitConnTemplateListener);
        }
        
        this.connectionImageDataFunctionResults = new ArrayList<FunctionResult>();
        for (IDiagramConnectionDef connectionDef : this.connectionDefs)
        {
            FunctionResult imageResult = initExpression
            ( 
                this.modelElement,
                connectionDef.getToolPaletteImage().getContent(),
                ImageData.class,
                null,
                new Runnable()
                {
                    public void run()
                    {
                        broadcast( new ImageChangedEvent( SapphireDiagramEditorPagePart.this ) );
                    }
                }
            );
            this.connectionImageDataFunctionResults.add(imageResult);
        }

        this.selections = new ArrayList<ISapphirePart>();
        this.selections.add(this);
        this.propertiesViewContributionManager = new PropertiesViewContributionManager( this, this.modelElement );
                
        attach
        (
            new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    if( event instanceof SelectionChangedEvent )
                    {
                        refreshPropertiesViewContribution();
                    }
                }
            }
        );
        
        refreshPropertiesViewContribution();
    }

    @Override
    public IModelElement getLocalModelElement()
    {
        return this.modelElement;
    }    
    
    public boolean isGridVisible()
    {
        return this.showGrid;
    }
            
    public void setGridVisible(boolean visible)
    {
    	if (visible != this.showGrid)
    	{
    		this.showGrid = visible;
    		this.notifyGridStateChange();
    	}
    }
    
    public boolean isShowGuides()
    {
    	return this.showGuides;
    }
    
    public void setShowGuides(boolean showGuides)
    {
    	if (this.showGuides != showGuides)
    	{
    		this.showGuides = showGuides;
    		notifyGuideStateChange();
    	}
    }
    
    public int getMinZoomLevel()
    {
        return 50;
    }
    
    public int getMaxZoomLevel()
    {
        return 400;
    }
    
    public int getZoomLevel()
    {
        return getState().getZoomLevel().getContent();
    }
    
    public void setZoomLevel( final int level )
    {
        final int currentZoomLevel = getState().getZoomLevel().getContent();
        
        if( currentZoomLevel != level )
        {
            int newZoomLevel = level;
            
            final int min = getMinZoomLevel();
            
            if( level < min )
            {
                newZoomLevel = min;
            }
            else
            {
                final int max = getMaxZoomLevel();
                
                if( level > max )
                {
                    newZoomLevel = max;
                }
            }
            
            if( currentZoomLevel != newZoomLevel )
            {
                getState().setZoomLevel( newZoomLevel );
                
                try
                {
                    getState().resource().save();
                }
                catch( ResourceStoreException e )
                {
                    SapphireUiFrameworkPlugin.log( e );
                }
                
                broadcast( new ZoomLevelEvent( this, currentZoomLevel, newZoomLevel ) );
            }
        }
    }
    
    public final DiagramEditorPageState getState()
    {
        return this.state;
    }
    
    public int getGridUnit()
    {
    	return this.gridUnit;
    }
    
    public int getVerticalGridUnit()
    {
    	if (this.verticalGridUnit > 0)
    	{
    		return this.verticalGridUnit;
    	}
    	else
    	{
    		return this.gridUnit;
    	}
    }

    public Point getMouseLocation()
    {
    	return this.mouseLocation;
    }
    
    public void setMouseLocation(int x, int y)
    {
    	this.mouseLocation.setX(x);
    	this.mouseLocation.setY(y);
    }
    
    public void selectAndDirectEdit(ISapphirePart part)
    {
    	notifyDirectEdit(part);
    }
    
    public void saveDiagram()
    {
    	notifyDiagramSave();
    }

    public IDiagramEditorPageDef getPageDef()
    {
    	return this.diagramPageDef;
    }
    
    public List<DiagramNodeTemplate> getNodeTemplates()
    {
        return this.nodeTemplates;
    }
    
    public List<DiagramNodeTemplate> getVisibleNodeTemplates()
    {
    	List<DiagramNodeTemplate> visibleNodeTemplates = new ArrayList<DiagramNodeTemplate>();
    	for (DiagramNodeTemplate nodeTemplate : getNodeTemplates())
    	{
    		if (nodeTemplate.visible())
    		{
    			visibleNodeTemplates.add(nodeTemplate);
    		}
    	}
    	return visibleNodeTemplates;
    }
    
    public List<ConnectionPalette> getConnectionPalettes() {
        List<ConnectionPalette> list = new ArrayList<ConnectionPalette>();
        for (int i = 0; i < this.connectionImageDataFunctionResults.size(); i++)
        {
            FunctionResult result = this.connectionImageDataFunctionResults.get(i);
            ImageData imageData = null;
            if (result != null)
            {
               	imageData = (ImageData)result.value();
            }
        	IDiagramConnectionDef def = this.connectionDefs.get(i);
            ConnectionPalette palette = new ConnectionPalette(imageData, def);
            list.add(palette);
        }
        return list;
    }
    
    public IDiagramConnectionDef getDiagramConnectionDef(String connId)
    {
        if (connId == null)
        {
            throw new IllegalArgumentException();
        }
        
        IDiagramConnectionDef connDef = null;
        for (IDiagramConnectionDef def : this.connectionDefs)
        {
            String id = def.getId().getContent();
            if (id != null && id.equalsIgnoreCase(connId))
            {
                connDef = def;
                break;
            }
        }
        return connDef;
    }
    
    public List<DiagramConnectionTemplate> getConnectionTemplates()
    {
        return this.connectionTemplates;
    }
            
    public List<DiagramImplicitConnectionTemplate> getImplicitConnectionTemplates()
    {
        return this.implicitConnectionTemplates;
    }
    
    @Override
    public void render(SapphireRenderingContext context)
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Set<String> getActionContexts()
    {
        Set<String> contextSet = new HashSet<String>();
        contextSet.add(SapphireActionSystem.CONTEXT_DIAGRAM);
        contextSet.add(SapphireActionSystem.CONTEXT_DIAGRAM_EDITOR);
        contextSet.add(SapphireActionSystem.CONTEXT_DIAGRAM_MULTIPLE_PARTS);
        contextSet.add(SapphireActionSystem.CONTEXT_DIAGRAM_HEADER);
        return contextSet;
    }
    
    @Override
    public String getMainActionContext()
    {
    	return SapphireActionSystem.CONTEXT_DIAGRAM_EDITOR;
    }
    
    public List<ISapphirePart> getSelections()
    {
        return this.selections;
    }
    
    public void setSelections(final List<ISapphirePart> selections)
    {
    	boolean selectionChanged = false;
    	if (this.selections.size() != selections.size())
    	{
    		selectionChanged = true;
    	}
    	else if (!this.selections.containsAll(selections) || !selections.containsAll(this.selections))
    	{
    		selectionChanged = true;
    	}
    	if (selectionChanged)
    	{
	    	this.selections.clear();
	    	this.selections.addAll(selections);
	    	broadcast( new SelectionChangedEvent( this ) );
    	}
    }
    
    public void selectAll()
    {
    	this.notifySelectAll();
    }
    
    public void selectAllNodes()
    {
    	this.notifySelectAllNodes();
    }
    
    private void refreshPropertiesViewContribution()
    {
        final List<ISapphirePart> selections = getSelections();
        
        PropertiesViewContributionPart propertiesViewContribution = null;
        
        if (selections.size() == 1)
        {
        	ISapphirePart selection = selections.get(0);
        
	        if( selection == SapphireDiagramEditorPagePart.this )
	        {
	            propertiesViewContribution = this.propertiesViewContributionManager.getPropertiesViewContribution();
	        }
	        else if( selection instanceof IPropertiesViewContributorPart )
	        {
	            propertiesViewContribution = ( (IPropertiesViewContributorPart) selection ).getPropertiesViewContribution();
	        }	        	        
        }
        if (propertiesViewContribution == null || !propertiesViewContribution.getLocalModelElement().disposed())
        {
        	setPropertiesViewContribution( propertiesViewContribution );
        }
    }
    
    private void refreshDiagramPalette(DiagramNodeTemplate nodeTemplate)
    {
    	if( nodeTemplate.visible() )
    	{
    		// Restore all the connection PEs if they are associated with the 
    		// nodes for the node template
    		nodeTemplate.showAllNodeParts();
        	List<DiagramConnectionTemplate> connTemplates = getConnectionTemplates();
        	for (DiagramConnectionTemplate connTemplate : connTemplates)
        	{
        		connTemplate.showAllConnectionParts(nodeTemplate);
        	}
        	
        	List<DiagramImplicitConnectionTemplate> implictConnTemplates = 
        			getImplicitConnectionTemplates();
        	for (DiagramImplicitConnectionTemplate implicitConnTemplate : implictConnTemplates)
        	{
        		implicitConnTemplate.refreshImplicitConnections();
        	}
    	}
    	else
    	{
    		// The connection PEs associated with nodes are removed when the node PEs get removed.
    		// So we don't need to explicitly remove those connection PEs
    		nodeTemplate.hideAllNodeParts();
    	}
    	notifyDiagramChange();
    	refreshPropertiesViewContribution();
    }
    
    public List<DiagramNodePart> getNodes()
    {
        final ListFactory<DiagramNodePart> nodes = ListFactory.start();
        
        for( DiagramNodeTemplate template : getNodeTemplates() )
        {
            nodes.add( template.getDiagramNodes() );
        }
        
        return nodes.result();
    }
    
    public List<DiagramConnectionPart> getConnections()
    {
        final ListFactory<DiagramConnectionPart> connections = ListFactory.start();
        
        for( DiagramConnectionTemplate template : getConnectionTemplates() )
        {
            connections.add( template.getDiagramConnections( null ) );
        }
        
        for( DiagramNodeTemplate nodeTemplate : getNodeTemplates() )
        {
            final DiagramConnectionTemplate embeddedConnectionTemplate = nodeTemplate.getEmbeddedConnectionTemplate();
            if (embeddedConnectionTemplate != null)
            {
                connections.add( embeddedConnectionTemplate.getDiagramConnections( null ) );
            }
        }
        
        return connections.result();
    }
    
    public DiagramNodePart getDiagramNodePart(IModelElement nodeElement)
    {
        if (nodeElement == null)
            return null;
        
        List<DiagramNodeTemplate> nodeTemplates = this.getNodeTemplates();
        for (DiagramNodeTemplate nodeTemplate : nodeTemplates)
        {
            List<DiagramNodePart> nodeParts = nodeTemplate.getDiagramNodes();
            for (DiagramNodePart nodePart : nodeParts)
            {
                if (nodePart.getLocalModelElement().equals(nodeElement))
                {
                    return nodePart;
                }
            }
        }
        return null;
    }
    
    public DiagramConnectionPart getDiagramConnectionPart(IModelElement connElement)
    {
    	if (connElement == null)
    	{
    		return null;
    	}
    	List<DiagramConnectionTemplate> connTemplates = this.getConnectionTemplates();
    	for (DiagramConnectionTemplate connTemplate : connTemplates)
    	{
    		 List<DiagramConnectionPart> connParts = connTemplate.getDiagramConnections(null);
    		 for (DiagramConnectionPart connPart : connParts)
    		 {
    			 if (connPart.getLocalModelElement().equals(connElement))
    			 {
    				 return connPart;
    			 }
    		 }
    	}
    	// Check for embedded connections
        List<DiagramNodeTemplate> nodeTemplates = this.getNodeTemplates();
        for (DiagramNodeTemplate nodeTemplate : nodeTemplates)
        {
        	DiagramEmbeddedConnectionTemplate connTemplate = nodeTemplate.getEmbeddedConnectionTemplate();
        	if (connTemplate != null)
        	{
        		List<DiagramConnectionPart> connParts = connTemplate.getDiagramConnections(null);
	       		 for (DiagramConnectionPart connPart : connParts)
	       		 {
	       			 if (connPart.getLocalModelElement().equals(connElement))
	       			 {
	       				 return connPart;
	       			 }
	       		 }        	
        	}
        }    	
    	return null;
    }
        
    /**
     * Returns the list of connections that are attached to a node on either end.
     * @param nodePart the sapphire node part
     * @return the list of connections
     */
    public List<DiagramConnectionPart> getAttachedConnections(DiagramNodePart nodePart)
    {
    	IModelElement nodeElement = nodePart.getLocalModelElement();
    	List<DiagramConnectionPart> attachedConnections = new ArrayList<DiagramConnectionPart>();
    	
    	List<DiagramConnectionTemplate> connTemplates = this.getConnectionTemplates();
    	for (DiagramConnectionTemplate connTemplate : connTemplates)
    	{
    		 List<DiagramConnectionPart> connParts = connTemplate.getDiagramConnections(null);
    		 for (DiagramConnectionPart connPart : connParts)
    		 {
    			 if ((connPart.getEndpoint1() != null && connPart.getEndpoint1().equals(nodeElement)) || 
    					 connPart.getEndpoint2() != null && connPart.getEndpoint2().equals(nodeElement))
    			 {
    				 attachedConnections.add(connPart);
    			 }
    		 }
    	}
    	// Check for embedded connections
        List<DiagramNodeTemplate> nodeTemplates = this.getNodeTemplates();
        for (DiagramNodeTemplate nodeTemplate : nodeTemplates)
        {
        	DiagramEmbeddedConnectionTemplate connTemplate = nodeTemplate.getEmbeddedConnectionTemplate();
        	if (connTemplate != null)
        	{
        		List<DiagramConnectionPart> connParts = connTemplate.getDiagramConnections(null);
	       		 for (DiagramConnectionPart connPart : connParts)
	       		 {
	    			 if (connPart.getEndpoint1().equals(nodeElement) || connPart.getEndpoint2().equals(nodeElement))
	    			 {
	    				 attachedConnections.add(connPart);
	    			 }
	       		 }        	
        	}
        }    	
    	
    	return attachedConnections;
    }
        
    @Override
    public void dispose() 
    {
        super.dispose();

        for (int i = 0; i < this.connectionImageDataFunctionResults.size(); i++)
        {
            FunctionResult result = this.connectionImageDataFunctionResults.get(i);
            if (result != null)
            {
                result.dispose();
            }
        }
        
        disposeParts();
    }
        
    private void disposeParts()
    {
    	for (DiagramNodeTemplate nodeTemplate : this.nodeTemplates)
        {
            nodeTemplate.dispose();
        }
        this.nodeTemplates.clear();
        for (DiagramConnectionTemplate connTemplate : this.connectionTemplates)
        {
            connTemplate.dispose();
        }
        this.connectionTemplates.clear();
        for (DiagramImplicitConnectionTemplate connTemplate : this.implicitConnectionTemplates)
        {
            connTemplate.dispose();
        }
        this.implicitConnectionTemplates.clear();
    }
    
    public DiagramNodeTemplate getNodeTemplate(ModelProperty modelProperty)
    {
    	for (DiagramNodeTemplate nodeTemplate : this.nodeTemplates)
    	{
    		if (nodeTemplate.getModelProperty() == modelProperty)
    		{
    			return nodeTemplate;
    		}
    	}
    	return null;
    }
    
    private void notifyNodeUpdate(DiagramNodePart nodePart)
	{
		Set<SapphirePartListener> listeners = this.getListeners();
		for(SapphirePartListener listener : listeners)
		{
			if (listener instanceof SapphireDiagramPartListener)
			{
				DiagramNodeEvent nue = new DiagramNodeEvent(nodePart);
				((SapphireDiagramPartListener)listener).handleNodeUpdateEvent(nue);
			}
		}		
	}
	
	private void notifyNodeAdd(DiagramNodePart nodePart)
	{
		Set<SapphirePartListener> listeners = this.getListeners();
		for(SapphirePartListener listener : listeners)
		{
			if (listener instanceof SapphireDiagramPartListener)
			{
				DiagramNodeEvent nue = new DiagramNodeEvent(nodePart);
				((SapphireDiagramPartListener)listener).handleNodeAddEvent(nue);
			}
		}
	}
	
	private void notifyNodeDelete(DiagramNodePart nodePart)
	{
		Set<SapphirePartListener> listeners = this.getListeners();
		for(SapphirePartListener listener : listeners)
		{
			if (listener instanceof SapphireDiagramPartListener)
			{
				DiagramNodeEvent nue = new DiagramNodeEvent(nodePart);
				((SapphireDiagramPartListener)listener).handleNodeDeleteEvent(nue);
			}
		}
	}
	
	private void notifyNodeMove(DiagramNodeEvent event)
	{
		Set<SapphirePartListener> listeners = this.getListeners();
		for(SapphirePartListener listener : listeners)
		{
			if (listener instanceof SapphireDiagramPartListener)
			{
				((SapphireDiagramPartListener)listener).handleNodeMoveEvent(event);
			}
		}
	}

	private void notifyConnectionUpdate(final DiagramConnectionEvent event)
	{
		Set<SapphirePartListener> listeners = this.getListeners();
		for(SapphirePartListener listener : listeners)
		{
			if (listener instanceof SapphireDiagramPartListener)
			{
				((SapphireDiagramPartListener)listener).handleConnectionUpdateEvent(event);
			}
		}		
	}
	
	private void notifyConnectionEndpointUpdate(final DiagramConnectionEvent event)
	{
		Set<SapphirePartListener> listeners = this.getListeners();
		for(SapphirePartListener listener : listeners)
		{
			if (listener instanceof SapphireDiagramPartListener)
			{
				((SapphireDiagramPartListener)listener).handleConnectionEndpointEvent(event);
			}
		}		
	}

    private void notifyConnectionAdd(final DiagramConnectionEvent event)
    {
        Set<SapphirePartListener> listeners = this.getListeners();
        for(SapphirePartListener listener : listeners)
        {
            if (listener instanceof SapphireDiagramPartListener)
            {
                ((SapphireDiagramPartListener)listener).handleConnectionAddEvent(event);
            }
        }        
    }

	private void notifyConnectionDelete(final DiagramConnectionEvent event)
	{
		Set<SapphirePartListener> listeners = this.getListeners();
		for(SapphirePartListener listener : listeners)
		{
			if (listener instanceof SapphireDiagramPartListener)
			{
				((SapphireDiagramPartListener)listener).handleConnectionDeleteEvent(event);
			}
		}		
	}
	
	private void notifyConnectionAddBendpoint(final DiagramConnectionEvent cue)
	{
		Set<SapphirePartListener> listeners = this.getListeners();
		for(SapphirePartListener listener : listeners)
		{
			if (listener instanceof SapphireDiagramPartListener)
			{
				((SapphireDiagramPartListener)listener).handleConnectionAddBendpointEvent(cue);
			}
		}		
	}

	private void notifyConnectionRemoveBendpoint(final DiagramConnectionEvent event)
	{
		Set<SapphirePartListener> listeners = this.getListeners();
		for(SapphirePartListener listener : listeners)
		{
			if (listener instanceof SapphireDiagramPartListener)
			{
				((SapphireDiagramPartListener)listener).handleConnectionRemoveBendpointEvent(event);
			}
		}		
	}

	private void notifyConnectionMoveBendpoint(final DiagramConnectionEvent event)
	{
		Set<SapphirePartListener> listeners = this.getListeners();
		for(SapphirePartListener listener : listeners)
		{
			if (listener instanceof SapphireDiagramPartListener)
			{
				((SapphireDiagramPartListener)listener).handleConnectionMoveBendpointEvent(event);
			}
		}		
	}
	
	private void notifyConnectionResetBendpoints(final DiagramConnectionEvent event)
	{
		Set<SapphirePartListener> listeners = this.getListeners();
		for(SapphirePartListener listener : listeners)
		{
			if (listener instanceof SapphireDiagramPartListener)
			{
				((SapphireDiagramPartListener)listener).handleConnectionResetBendpointsEvent(event);
			}
		}		
	}

	private void notifyConnectionMoveLabel(final DiagramConnectionEvent event)
	{
		Set<SapphirePartListener> listeners = this.getListeners();
		for(SapphirePartListener listener : listeners)
		{
			if (listener instanceof SapphireDiagramPartListener)
			{
				((SapphireDiagramPartListener)listener).handleConnectionMoveLabelEvent(event);
			}
		}		
	}

	private void notifyDirectEdit(ISapphirePart part)
	{
		Set<SapphirePartListener> listeners = this.getListeners();
		for(SapphirePartListener listener : listeners)
		{
			if (listener instanceof SapphireDiagramPartListener)
			{
				DiagramPartEvent cue = new DiagramPartEvent(part);
				((SapphireDiagramPartListener)listener).handleDirectEditEvent(cue);
			}
		}		

	}

	private void notifyGridStateChange()
	{
		Set<SapphirePartListener> listeners = this.getListeners();
		for(SapphirePartListener listener : listeners)
		{
			if (listener instanceof SapphireDiagramPartListener)
			{
				DiagramPageEvent pageEvent = new DiagramPageEvent(this);
				((SapphireDiagramPartListener)listener).handleGridStateChangeEvent(pageEvent);
			}
		}		
	}
	
	private void notifyGuideStateChange()
	{
		Set<SapphirePartListener> listeners = this.getListeners();
		for(SapphirePartListener listener : listeners)
		{
			if (listener instanceof SapphireDiagramPartListener)
			{
				DiagramPageEvent pageEvent = new DiagramPageEvent(this);
				((SapphireDiagramPartListener)listener).handleGuideStateChangeEvent(pageEvent);
			}
		}		
	}
	
	private void notifyDiagramChange()
	{
		Set<SapphirePartListener> listeners = this.getListeners();
		for(SapphirePartListener listener : listeners)
		{
			if (listener instanceof SapphireDiagramPartListener)
			{
				DiagramPageEvent pageEvent = new DiagramPageEvent(this);
				((SapphireDiagramPartListener)listener).handleDiagramUpdateEvent(pageEvent);
			}
		}		
	}
	
	private void notifyDiagramSave()
	{
		Set<SapphirePartListener> listeners = this.getListeners();
		for(SapphirePartListener listener : listeners)
		{
			if (listener instanceof SapphireDiagramPartListener)
			{
				DiagramPageEvent pageEvent = new DiagramPageEvent(this);
				((SapphireDiagramPartListener)listener).handleDiagramSaveEvent(pageEvent);
			}
		}		
	}
	
	private void notifySelectAll()
	{
		Set<SapphirePartListener> listeners = this.getListeners();
		for(SapphirePartListener listener : listeners)
		{
			if (listener instanceof SapphireDiagramPartListener)
			{
				DiagramPageEvent pageEvent = new DiagramPageEvent(this);
				((SapphireDiagramPartListener)listener).handleSelectAllEvent(pageEvent);
			}
		}				
	}
	
	private void notifySelectAllNodes()
	{
		Set<SapphirePartListener> listeners = this.getListeners();
		for(SapphirePartListener listener : listeners)
		{
			if (listener instanceof SapphireDiagramPartListener)
			{
				DiagramPageEvent pageEvent = new DiagramPageEvent(this);
				((SapphireDiagramPartListener)listener).handleSelectAllNodesEvent(pageEvent);
			}
		}				
	}

	// --------------------------------------------------------------------
	// Inner classes
	//---------------------------------------------------------------------
	
	private class NodeTemplateListener extends DiagramNodeTemplateListener
	{
        @Override
        public void handleNodeUpdate(final DiagramNodePart nodePart)
        {
            notifyNodeUpdate(nodePart);
        }
        
        @Override
        public void handleNodeAdd(final DiagramNodePart nodePart)
        {
            notifyNodeAdd(nodePart);
        }

        @Override
        public void handleNodeDelete(final DiagramNodePart nodePart)
        {
        	notifyNodeDelete(nodePart);
        }		

        @Override
        public void handleNodeMove(final DiagramNodeEvent event)
        {
        	notifyNodeMove(event);
        }		
	}
	
	private class ConnectionTemplateListener extends DiagramConnectionTemplateListener
	{
        @Override
        public void handleConnectionUpdate(final DiagramConnectionEvent event)
        {
            notifyConnectionUpdate(event);
        }
        
        @Override
        public void handleConnectionEndpointUpdate(final DiagramConnectionEvent event)
        {
            notifyConnectionEndpointUpdate(event);
        }

        @Override
        public void handleConnectionAdd(final DiagramConnectionEvent event)
        {
            notifyConnectionAdd(event);
        }

        @Override
        public void handleConnectionDelete(final DiagramConnectionEvent event)
        {
            notifyConnectionDelete(event);
        }
        
        @Override
        public void handleAddBendpoint(final DiagramConnectionEvent event)
        {
            notifyConnectionAddBendpoint(event);
        }

        @Override
        public void handleRemoveBendpoint(final DiagramConnectionEvent event)
        {
            notifyConnectionRemoveBendpoint(event);
        }

        @Override
        public void handleMoveBendpoint(final DiagramConnectionEvent event)
        {
            notifyConnectionMoveBendpoint(event);
        }

        @Override
        public void handleResetBendpoints(final DiagramConnectionEvent event)
        {
            notifyConnectionResetBendpoints(event);
        }

        @Override
        public void handleMoveLabel(final DiagramConnectionEvent event)
        {
            notifyConnectionMoveLabel(event);
        }
	}
    
    private class ImplicitConnectionTemplateListener extends DiagramImplicitConnectionTemplateListener
    {

        @Override
        public void handleConnectionAdd(final DiagramConnectionEvent event)
        {
            notifyConnectionAdd(event);
        }

        @Override
        public void handleConnectionDelete(final DiagramConnectionEvent event)
        {
            notifyConnectionDelete(event);
        }
        
    }
    
    public final static class ConnectionPalette {
    	
    	ImageData imageData;
    	IDiagramConnectionDef connectionDef;
    	
    	public ConnectionPalette(ImageData imageData, IDiagramConnectionDef connectionDef) {
    		this.imageData = imageData;
    		this.connectionDef = connectionDef;
    	}
    	
    	public ImageData getImageData() {
    		return this.imageData;
    	}
    	
    	public IDiagramConnectionDef getConnectionDef() {
    		return this.connectionDef;
    	}
    	
    }
    
    public static final class ZoomLevelEvent extends PartEvent
    {
        private final int before;
        private final int after;
        
        public ZoomLevelEvent( final SapphirePart part,
                               final int before,
                               final int after )
        {
            super( part );
            
            this.before = before;
            this.after = after;
        }
        
        public int before()
        {
            return this.before;
        }
        
        public int after()
        {
            return this.after;
        }
    }
    
}
