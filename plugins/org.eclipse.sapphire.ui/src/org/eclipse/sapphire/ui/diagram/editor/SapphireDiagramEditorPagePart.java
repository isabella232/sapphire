/******************************************************************************
 * Copyright (c) 2015 Oracle and Liferay
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
 *    Ling Hao - [383924] Flexible diagram node shapes
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.PartVisibilityEvent;
import org.eclipse.sapphire.ui.Point;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.SapphireEditorPagePart;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.diagram.ConnectionEvent;
import org.eclipse.sapphire.ui.diagram.ConnectionService;
import org.eclipse.sapphire.ui.diagram.DiagramConnectionPart;
import org.eclipse.sapphire.ui.diagram.def.DiagramEditorPageDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramConnectionDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramNodeDef;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodeTemplate.DiagramNodeTemplateListener;
import org.eclipse.sapphire.ui.diagram.editor.DiagramPageEvent.DiagramPageEventType;
import org.eclipse.sapphire.ui.diagram.state.DiagramEditorPageState;
import org.eclipse.sapphire.ui.forms.PropertiesViewContributionManager;
import org.eclipse.sapphire.ui.forms.PropertiesViewContributionPart;
import org.eclipse.sapphire.ui.forms.PropertiesViewContributorPart;
import org.eclipse.sapphire.util.ListFactory;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public final class SapphireDiagramEditorPagePart extends SapphireEditorPagePart
{
    private Element modelElement;
    private DiagramEditorPageDef diagramPageDef = null;
    private List<IDiagramNodeDef> nodeDefs;
    private List<IDiagramConnectionDef> connectionDefs;
    private List<DiagramNodeTemplate> nodeTemplates;
    private NodeTemplateListener nodeTemplateListener;
    private PropertiesViewContributionManager propertiesViewContributionManager;
    private List<ISapphirePart> selections;
    private boolean showGrid;
    private boolean showGuides;
    private int gridUnit;
    private int verticalGridUnit;
	private List<FunctionResult> connectionImageDataFunctionResults;
	private Point mouseLocation;

    @Override
    protected void init()
    {
        this.diagramPageDef = (DiagramEditorPageDef)super.definition;
        ImpliedElementProperty modelElementProperty = (ImpliedElementProperty)resolve(this.diagramPageDef.getProperty().content());
        if (modelElementProperty != null)
        {
            this.modelElement = getModelElement().property( modelElementProperty ).content();            
        }
        else
        {
            this.modelElement = getModelElement();
        }
    	        
        super.init();

        this.showGrid = this.diagramPageDef.getGridDefinition().isVisible().content();
        this.showGuides = this.diagramPageDef.getGuidesDefinition().isVisible().content();
        this.gridUnit = this.diagramPageDef.getGridDefinition().getGridUnit().content();
        this.verticalGridUnit = this.diagramPageDef.getGridDefinition().getVerticalGridUnit().content();
        this.mouseLocation = new Point(0, 0);
        
        this.nodeTemplateListener = new NodeTemplateListener();
        
        this.nodeTemplates = new ArrayList<DiagramNodeTemplate>();
        this.nodeDefs = this.diagramPageDef.getDiagramNodeDefs();
        this.connectionDefs = this.diagramPageDef.getDiagramConnectionDefs();
        
        for (final IDiagramNodeDef nodeDef : this.nodeDefs)
        {
            final DiagramNodeTemplate nodeTemplate = new DiagramNodeTemplate();
            nodeTemplate.init(this, this.modelElement, nodeDef, Collections.<String,String>emptyMap());
            nodeTemplate.initialize();
            this.nodeTemplates.add(nodeTemplate);
            nodeTemplate.addTemplateListener(this.nodeTemplateListener);
            
            nodeTemplate.attach
            (
                 new FilteredListener<PartVisibilityEvent>()
                 {
                    @Override
                    protected void handleTypedEvent( final PartVisibilityEvent event )
                    {
                        handleNodeTemplateVisibilityChange( (DiagramNodeTemplate) event.part() );
                    }
                 }
            );
        }
                                
        this.connectionImageDataFunctionResults = new ArrayList<FunctionResult>();
        for (IDiagramConnectionDef connectionDef : this.connectionDefs)
        {
            FunctionResult imageResult = initExpression
            ( 
                connectionDef.getToolPaletteImage().content(),
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
        
        // Listen on connection service
		this.service(ConnectionService.class).attach(new FilteredListener<ConnectionEvent>() 
		{
			@Override
			protected void handleTypedEvent(ConnectionEvent event) 
			{
				broadcast(event);
			}
		});
    }

    @Override
    public Element getLocalModelElement()
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
        return state().getZoomLevel().content();
    }
    
    public void setZoomLevel( final int level )
    {
        final int currentZoomLevel = state().getZoomLevel().content();
        
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
                state().setZoomLevel( newZoomLevel );
                
                broadcast( new ZoomLevelEvent( this, currentZoomLevel, newZoomLevel ) );
            }
        }
    }
    
    public void autoLayout(boolean horizontal)
    {
    	broadcast(new PreAutoLayoutEvent(this));
    	broadcast(new AutoLayoutEvent(this, horizontal));
    	broadcast(new PostAutoLayoutEvent(this));
    }
    
    public final DiagramEditorPageState state()
    {
        return (DiagramEditorPageState) super.state();
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

    public DiagramEditorPageDef getPageDef()
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
            String id = def.getId().content();
            if (id != null && id.equalsIgnoreCase(connId))
            {
                connDef = def;
                break;
            }
        }
        return connDef;
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
    
    public void setSelections(final List<ISapphirePart> selections, boolean force)
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
    	if (selectionChanged || force)
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
	        else if( selection instanceof PropertiesViewContributorPart )
	        {
	            propertiesViewContribution = ( (PropertiesViewContributorPart) selection ).getPropertiesViewContribution();
	        }	        	        
        }
        if (propertiesViewContribution == null || !propertiesViewContribution.getLocalModelElement().disposed())
        {
        	setPropertiesViewContribution( propertiesViewContribution );
        }
    }
    
    private void handleNodeTemplateVisibilityChange(DiagramNodeTemplate nodeTemplate)
    {
    	//ConnectionService connService = this.service(ConnectionService.class);
    	if( nodeTemplate.visible() )
    	{
    		nodeTemplate.showAllNodeParts();
    		// Restore all the connection parts if they are associated with the 
    		// nodes for the node template    		
    		//connService.showAllAttachedConnections(nodeTemplate.getNodeTypeId());
    		notifyNodeTemplateVisibilityChange(nodeTemplate);
    	}
    	else
    	{
    		//connService.hideAllAttachedConnections(nodeTemplate.getNodeTypeId());
    		notifyNodeTemplateVisibilityChange(nodeTemplate);
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
    	return this.service(ConnectionService.class).list();
    }
    
    public DiagramNodePart getDiagramNodePart(Element nodeElement)
    {
        if (nodeElement == null)
            return null;
        
        List<DiagramNodeTemplate> nodeTemplates = this.getNodeTemplates();
        for (DiagramNodeTemplate nodeTemplate : nodeTemplates)
        {
            List<DiagramNodePart> nodeParts = nodeTemplate.getDiagramNodes();
            for (DiagramNodePart nodePart : nodeParts)
            {
                if (nodePart.getLocalModelElement() == nodeElement)
                {
                    return nodePart;
                }
            }
        }
        return null;
    }
    
    public DiagramNodePart getNode(String nodeId)
    {
    	if (nodeId != null)
    	{
			List<DiagramNodePart> nodes = getNodes();
			for (DiagramNodePart node : nodes)
			{
				if (node.getId() != null && node.getId().equals(nodeId))
				{
					return node;
				}
			}
    	}
		return null;
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
    }
    
    public List<DiagramNodeTemplate> getNodeTemplates(PropertyDef modelProperty)
    {
    	List<DiagramNodeTemplate> nodeTemplates = new ArrayList<DiagramNodeTemplate>();
    	for (DiagramNodeTemplate nodeTemplate : this.nodeTemplates)
    	{
    		if (nodeTemplate.getModelProperty() == modelProperty)
    		{
    			nodeTemplates.add(nodeTemplate);
    		}
    	}
    	return nodeTemplates;
    }
    
    public DiagramNodeTemplate getNodeTemplate(String nodeType)
    {    	
    	for (DiagramNodeTemplate nodeTemplate : this.nodeTemplates)
    	{
    		if (nodeTemplate.getNodeTypeId().equalsIgnoreCase(nodeType))
    		{
    			return nodeTemplate;
    		}
    	}
    	return null;
    }
    
    private void notifyNodeAdd(DiagramNodePart nodePart)
	{
		DiagramNodeAddEvent event = new DiagramNodeAddEvent(nodePart);
    	this.broadcast(event);
	}
	
	private void notifyNodeDelete(DiagramNodePart nodePart)
	{
		DiagramNodeDeleteEvent event = new DiagramNodeDeleteEvent(nodePart);
    	this.broadcast(event);
	}
	
	private void notifyNodeAboutToBeDeleted(DiagramNodePart nodePart)
	{
		DiagramNodePreDeleteEvent event = new DiagramNodePreDeleteEvent(nodePart);
    	this.broadcast(event);
	}

	private void notifyNodeAdded(DiagramNodePart nodePart)
	{
		DiagramNodePostAddEvent event = new DiagramNodePostAddEvent(nodePart);
    	this.broadcast(event);
	}

	private void notifyNodeTemplateVisibilityChange(DiagramNodeTemplate nodeTemplate)
	{
		NodeTemplateVisibilityEvent event = new NodeTemplateVisibilityEvent(nodeTemplate);
    	this.broadcast(event);
	}
	
	private void notifyNodeMove(DiagramNodeMoveEvent event)
	{
    	this.broadcast(event);
	}

	private void notifyDirectEdit(ISapphirePart part)
	{
		DiagramDirectEditPartEvent event = new DiagramDirectEditPartEvent(part);
    	this.broadcast(event);
	}

	private void notifyGridStateChange()
	{
		DiagramPageEvent event = new DiagramPageEvent(this);
		event.setDiagramPageEventType(DiagramPageEventType.GridStateChange);
    	this.broadcast(event);
	}
	
	private void notifyGuideStateChange()
	{
		DiagramPageEvent event = new DiagramPageEvent(this);
		event.setDiagramPageEventType(DiagramPageEventType.GuideStateChange);
    	this.broadcast(event);
	}
	
	private void notifyDiagramChange()
	{
		DiagramPageEvent event = new DiagramPageEvent(this);
		event.setDiagramPageEventType(DiagramPageEventType.DiagramChange);
    	this.broadcast(event);
	}
	
	private void notifyDiagramSave()
	{
		DiagramPageEvent event = new DiagramPageEvent(this);
		event.setDiagramPageEventType(DiagramPageEventType.DiagramSave);
    	this.broadcast(event);
	}
	
	private void notifySelectAll()
	{
		DiagramPageEvent event = new DiagramPageEvent(this);
		event.setDiagramPageEventType(DiagramPageEventType.SelectAll);
    	this.broadcast(event);
	}
	
	private void notifySelectAllNodes()
	{
		DiagramPageEvent event = new DiagramPageEvent(this);
		event.setDiagramPageEventType(DiagramPageEventType.SelectAllNodes);
    	this.broadcast(event);
	}

	// --------------------------------------------------------------------
	// Inner classes
	//---------------------------------------------------------------------
	
	private class NodeTemplateListener extends DiagramNodeTemplateListener
	{
        @Override
        public void handleNodeAdd(final DiagramNodePart nodePart)
        {
            notifyNodeAdd(nodePart);
        }

        @Override
        public void handlePostNodeAdd(final DiagramNodePart nodePart)
        {
            notifyNodeAdded(nodePart);
        }

        @Override
        public void handleNodeDelete(final DiagramNodePart nodePart)
        {
        	notifyNodeDelete(nodePart);
        }		

        @Override
        public void handlePreNodeDelete(final DiagramNodePart nodePart)
        {
        	notifyNodeAboutToBeDeleted(nodePart);
        }		

        @Override
        public void handleNodeMove(final DiagramNodeMoveEvent event)
        {
        	notifyNodeMove(event);
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
    
    
    public static final class PreAutoLayoutEvent extends PartEvent
    {
    	public PreAutoLayoutEvent(final SapphirePart part)
    	{
    		super(part);
    	}    	    	
    }

    public static final class AutoLayoutEvent extends PartEvent
    {
    	private boolean horizontal;
    	
    	public AutoLayoutEvent(final SapphirePart part, boolean horizontal)
    	{
    		super(part);
    		this.horizontal = horizontal;
    	}
    	
    	public boolean horizontal()
    	{
    		return this.horizontal;
    	}
    }
    
    public static final class PostAutoLayoutEvent extends PartEvent
    {
    	public PostAutoLayoutEvent(final SapphirePart part)
    	{
    		super(part);
    	}    	    	
    }
    
}
