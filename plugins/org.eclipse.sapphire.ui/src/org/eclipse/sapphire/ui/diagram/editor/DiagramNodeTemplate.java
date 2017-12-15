/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - [342897] Integrate with properties view
 *    Konstantin Komissarchik - [348813] Generalize Sapphire.Diagram.Drop action
 *    Ling Hao - [44319] Image specification for diagram parts inconsistent with the rest of sdef 
 *    Konstantin Komissarchik - [378756] Convert ModelElementListener and ModelPropertyListener to common listener infrastructure
 *    Konstantin Komissarchik - [381794] Cleanup needed in presentation code for diagram context menu
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImageData;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.PropertyEvent;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.diagram.def.IDiagramConnectionDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramExplicitConnectionBindingDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramNodeDef;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class DiagramNodeTemplate extends SapphirePart
{
    public static abstract class DiagramNodeTemplateListener
    {
        public void handleNodeUpdate(final DiagramNodePart nodePart)
        {
        }       
        public void handleNodeAdd(final DiagramNodePart nodePart)
        {            
        }
        public void handleNodeDelete(final DiagramNodePart nodePart)
        {            
        }
        public void handleNodeMove(final DiagramNodeEvent event)
        {        	
        }
    }
    
	private SapphireDiagramEditorPagePart diagramEditor;
	private IDiagramNodeDef definition;
	private IModelElement modelElement;	
	private String propertyName;
	private ListProperty modelProperty;
	private JavaType modelElementType;
	private String toolPaletteLabel;
	private String toolPaletteDesc;
	private FunctionResult toolPaletteImageFunctionResult;
	private DiagramEmbeddedConnectionTemplate embeddedConnTemplate;
	private Listener modelPropertyListener;
	private SapphireDiagramPartListener nodePartListener;
	private Set<DiagramNodeTemplateListener> listeners;	
	private List<DiagramNodePart> diagramNodes;
	private FunctionResult visibleWhenFunctionResult;
	    
	@Override
    public void init()
    {
        this.diagramEditor = (SapphireDiagramEditorPagePart)getParentPart();
        this.modelElement = getModelElement();
        this.definition = (IDiagramNodeDef)super.definition;
        
        if (this.definition.getToolPaletteLabel().getContent() != null)
        {
            ValueProperty tpLabelProperty = IDiagramNodeDef.PROP_TOOL_PALETTE_LABEL;
            this.toolPaletteLabel = tpLabelProperty.getLocalizationService().text(
                            this.definition.getToolPaletteLabel().getContent(), CapitalizationType.TITLE_STYLE, false);
        }        
        this.toolPaletteDesc = this.definition.getToolPaletteDescription().getContent();
        
        this.diagramNodes = new ArrayList<DiagramNodePart>();
        this.listeners = new CopyOnWriteArraySet<DiagramNodeTemplateListener>();
        
        this.propertyName = this.definition.getProperty().getContent();
        this.modelProperty = (ListProperty)resolve(this.modelElement, this.propertyName);
        this.modelElementType = this.definition.getElementType().resolve();
        
        this.visibleWhenFunctionResult = initExpression
        ( 
            this.modelElement,
            this.definition.getVisibleWhen().getContent(),
            Boolean.class,
            Literal.create( Boolean.TRUE ),
            new Runnable()
            {
                public void run()
                {
                    broadcast( new VisibilityChangedEvent( DiagramNodeTemplate.this ) );
                }
            }
        );
        
        this.nodePartListener = new SapphireDiagramPartListener() 
        {
        	@Override
        	public void handleNodeUpdateEvent(final DiagramNodeEvent event)
        	{
        		notifyNodeUpdate((DiagramNodePart)event.getPart());
        	}        	
        	@Override
	    	public void handleNodeMoveEvent(final DiagramNodeEvent event)
	       	{
        		notifyNodeMoveEvent(event);
	       	}        	
		};
		
    	ModelElementList<?> list = this.modelElement.read(this.modelProperty);
        for( IModelElement listEntryModelElement : list )
        {
            if (this.modelElementType == null)
            {
                createNewNodePart(listEntryModelElement);                
            }
            else 
            {
                final Class<?> cl = this.modelElementType.artifact();
                if( cl == null || cl.isAssignableFrom( listEntryModelElement.getClass() ) )
                {
                	createNewNodePart(listEntryModelElement);
                }
            }
        }

        this.toolPaletteImageFunctionResult = initExpression
        (
        	this.modelElement,
            this.definition.getToolPaletteImage().getContent(),
            ImageData.class,
            null,
            new Runnable()
            {
                public void run()
                {
                    broadcast( new ImageChangedEvent( DiagramNodeTemplate.this ) );
                }
            }
        );

        // Add model property listener
        this.modelPropertyListener = new FilteredListener<PropertyEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyEvent event )
            {
                handleModelPropertyChange( event );
            }
        };
        addModelListener();        
    }
    
    /*
     * We need to initialize all the node parts before we can initialize embedded connections.
     * Connections between "anonymous" nodes are represented using node index based mechanisms.
     */
    public void initEmbeddedConnections()
    {
        // handle embedded connections
        if (!this.definition.getEmbeddedConnections().isEmpty())
        {
            IDiagramExplicitConnectionBindingDef embeddedConnDef = 
                        this.definition.getEmbeddedConnections().get( 0 );
            this.embeddedConnTemplate = new DiagramEmbeddedConnectionTemplate(embeddedConnDef);
            IDiagramConnectionDef connDef = this.diagramEditor.getDiagramConnectionDef(embeddedConnDef.getConnectionId().getContent());
            this.embeddedConnTemplate.init(this, this.modelElement, connDef, Collections.<String,String>emptyMap());
        }
    }
    
    public IDiagramNodeDef definition()
    {
        return this.definition;
    }
    
    public boolean visible()
    {
        return (Boolean) this.visibleWhenFunctionResult.value();
    }
    
    public List<DiagramNodePart> getDiagramNodes()
    {
        return this.diagramNodes;
    }
    
    public String getToolPaletteLabel()
    {
        return this.toolPaletteLabel;
    }
    
    public String getToolPaletteDesc()
    {
        return this.toolPaletteDesc;
    }
    
    public ImageData getToolPaletteImage()
    {
        return (ImageData) this.toolPaletteImageFunctionResult.value();
    }
    
    public String getNodeTypeId()
    {
        return this.definition.getId().getContent();
    }
    
    @Override
    public Set<String> getActionContexts()
    {
        Set<String> ret = new HashSet<String>();
        ret.add(SapphireActionSystem.CONTEXT_DIAGRAM_NODE);
        ret.add(SapphireActionSystem.CONTEXT_DIAGRAM);
        return ret;
    }
    
    public DiagramNodePart createNewDiagramNode()
    {
    	IModelElement newElement = null;
		ModelElementList<?> list = this.modelElement.read(this.modelProperty);
		if (this.modelElementType == null)
		{
			newElement = list.insert();
		}
		else
		{
			final Class cl = this.modelElementType.artifact();
			if (cl != null)
			{
				newElement = list.insert(cl);
			}
			else
			{
				newElement = list.insert();
			}
		}
		DiagramNodePart newNodePart = getNodePart(newElement);
    	return newNodePart;
    }
    
    public ModelProperty getModelProperty()
    {
        return this.modelProperty;
    }
    
    public ModelElementType getNodeType()
    {
        if (this.modelElementType == null)
        {
            return this.modelProperty.getType();
        }
        else 
        {
            final Class<?> cl = this.modelElementType.artifact();
            return ModelElementType.read(cl);
        }
    }
    
    public DiagramEmbeddedConnectionTemplate getEmbeddedConnectionTemplate()
    {
        return this.embeddedConnTemplate;
    }
    
    public void addModelListener()
    {
        this.modelElement.attach(this.modelPropertyListener, this.propertyName);
    }
    
    public void removeModelLister()
    {
        this.modelElement.detach(this.modelPropertyListener, this.propertyName);
    }
    
    public void addTemplateListener( final DiagramNodeTemplateListener listener )
    {
        this.listeners.add( listener );
    }
    
    public void removeTemplateListener( final DiagramNodeTemplateListener listener )
    {
        this.listeners.remove( listener );
    }
    	
	@Override
	public void render(SapphireRenderingContext context)
	{
		throw new UnsupportedOperationException();
	}	
    
    private void handleModelPropertyChange(final PropertyEvent event)
    {
    	final IModelElement element = event.element();
    	final ModelProperty property = event.property();
    	ModelElementList<?> tempList = (ModelElementList<?>)element.read(property);
    	
    	// filter the list property with specified element type
    	List<IModelElement> newList = new ArrayList<IModelElement>();
		newList = new ArrayList<IModelElement>();    	
    	for (IModelElement ele : tempList)
    	{
    		if (this.modelElementType == null)
    		{
    			newList.add(ele);
    		}
    		else
    		{
        		final Class<?> cl = this.modelElementType.artifact();
        		if( cl == null || cl.isAssignableFrom( ele.getClass()))
        		{
        			newList.add(ele);
        		}
    		}
    	}
    	List<DiagramNodePart> nodeParts = getDiagramNodes();
		List<IModelElement> oldList = new ArrayList<IModelElement>(nodeParts.size());
		for (DiagramNodePart nodePart : nodeParts)
		{
			oldList.add(nodePart.getLocalModelElement());
		}
    	
    	List<IModelElement> deletedNodes = ListUtil.ListDiff(oldList, newList);
    	List<IModelElement> newNodes = ListUtil.ListDiff(newList, oldList);
		for (IModelElement deletedNode : deletedNodes)
		{
			DiagramNodePart nodePart = getNodePart(deletedNode);
			if (nodePart != null)
			{
				notifyNodeDelete(nodePart);
				nodePart.dispose();
				this.diagramNodes.remove(nodePart);
			}
		}    	    	
		for (IModelElement newNode : newNodes)
		{
	    	DiagramNodePart nodePart = createNewNodePart(newNode);
	    	if (visible())
	    	{
	    		notifyNodeAdd(nodePart);
	    	}
	    	if (this.embeddedConnTemplate != null)
	    	{
	    		this.embeddedConnTemplate.refreshConnections(newNode);
	    	}
	    	// If a connection part is created before the two endpoint node parts are created,
	    	// the connection part hasn't been visually displayed on the diagram canvas.
	    	// We need to refresh those connections in case they become valid.
	    	refreshAttachedConnections(nodePart);	    
		}
    }
    
    public DiagramNodePart getNodePart(IModelElement element)
    {
        List<DiagramNodePart> nodeParts = getDiagramNodes();
        for (DiagramNodePart nodePart : nodeParts)
        {
            if (nodePart.getLocalModelElement().equals(element))
            {
                return nodePart;
            }
        }
        return null;
    }
    
    public DiagramNodePart createNewNodePart(IModelElement element)
    {
        DiagramNodePart newNode = new DiagramNodePart();
        newNode.init(this, element, this.definition, 
                Collections.<String,String>emptyMap());
        newNode.addListener(this.nodePartListener);
        this.diagramNodes.add(newNode);
        if (this.embeddedConnTemplate != null)
        {
            this.embeddedConnTemplate.addModelListener(element);
        }
        return newNode;        
    }
    
    public void hideAllNodeParts()
    {
        List<DiagramNodePart> nodeParts = getDiagramNodes();
        for (DiagramNodePart nodePart : nodeParts)
        {
        	notifyNodeDelete(nodePart);
        }
    }
    
    public void showAllNodeParts()
    {
        List<DiagramNodePart> nodeParts = getDiagramNodes();
        for (DiagramNodePart nodePart : nodeParts)
        {
        	notifyNodeAdd(nodePart);
        }
        if (this.embeddedConnTemplate != null)
        {
        	this.embeddedConnTemplate.showAllConnectionParts(this);
        }
    }
    
    @Override
    public void dispose()
    {
        removeModelLister();
        List<DiagramNodePart> nodeParts = getDiagramNodes();
        for (DiagramNodePart nodePart : nodeParts)
        {
        	notifyNodeDelete(nodePart);
            nodePart.dispose();            
        }
        this.diagramNodes.clear();
        if (this.embeddedConnTemplate != null)
        {
            this.embeddedConnTemplate.dispose();
        }
        
        if( this.toolPaletteImageFunctionResult != null )
        {
            this.toolPaletteImageFunctionResult.dispose();
        }
        
        if( this.visibleWhenFunctionResult != null )
        {
            this.visibleWhenFunctionResult.dispose();
        }
    }
    
    public SapphireDiagramEditorPagePart getDiagramEditorPart()
    {
        return this.diagramEditor;
    }
    
    private void notifyNodeUpdate(DiagramNodePart nodePart)
    {
        for( DiagramNodeTemplateListener listener : this.listeners )
        {
            listener.handleNodeUpdate(nodePart);
        }        
    }
    
    private void notifyNodeAdd(DiagramNodePart nodePart)
    {
        for( DiagramNodeTemplateListener listener : this.listeners )
        {
            listener.handleNodeAdd(nodePart);
        }                
    }
    
    private void notifyNodeDelete(DiagramNodePart nodePart)
    {
        for( DiagramNodeTemplateListener listener : this.listeners )
        {
            listener.handleNodeDelete(nodePart);
        }				
	}

	private void notifyNodeMoveEvent(DiagramNodeEvent event)
	{
		for( DiagramNodeTemplateListener listener : this.listeners )
        {
            listener.handleNodeMove(event);
        }				
	}
	
    /**
     * In the case where the entire Sapphire model is reconstructed (revert source file in the source editor), 
     * connection properties may have triggered events before the node properties change events
     * are sent out. So those connection parts will be created before the endpoint node
     * parts are created. But those connection parts won't be displayed visually on diagram canvas
     * until those corresponding endpoint nodes are created on the canvas.
     * 
     * [Bug 376245] Revert action in StructuredTextEditor does not revert diagram nodes and connections
     * in SapphireDiagramEditor
     * 
     * @param nodePart
     */
    private void refreshAttachedConnections(DiagramNodePart nodePart)
    {
    	List<DiagramConnectionPart> attachedConnections = this.diagramEditor.getAttachedConnections(nodePart);
    	for (DiagramConnectionPart connPart : attachedConnections)
    	{
    		connPart.getDiagramConnectionTemplate().notifyConnectionAdd(new DiagramConnectionEvent(connPart));
    	}
    }
	
}
