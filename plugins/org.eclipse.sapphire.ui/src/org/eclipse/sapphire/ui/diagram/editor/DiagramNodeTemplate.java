/******************************************************************************
 * Copyright (c) 2014 Oracle
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
 *    Ling Hao - [383924] Flexible diagram node shapes
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.PropertyEvent;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.diagram.ConnectionService;
import org.eclipse.sapphire.ui.diagram.def.IDiagramConnectionDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramExplicitConnectionBindingDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramNodeDef;
import org.eclipse.sapphire.ui.diagram.internal.DiagramEmbeddedConnectionTemplate;
import org.eclipse.sapphire.ui.diagram.internal.StandardDiagramConnectionPart;
import org.eclipse.sapphire.util.CollectionsUtil;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public final class DiagramNodeTemplate extends SapphirePart
{
    public static abstract class DiagramNodeTemplateListener
    {
        public void handleNodeValidation(final DiagramNodeEvent event)
        {
        }       
        public void handleNodeAdd(final DiagramNodePart nodePart)
        {            
        }
        public void handleNodeAdded(final DiagramNodePart nodePart)
        {            
        }
        public void handleNodeDelete(final DiagramNodePart nodePart)
        {            
        }
        public void handleNodeAboutToBeDeleted(final DiagramNodePart nodePart)
        {            
        }
        public void handleNodeMove(final DiagramNodeEvent event)
        {        	
        }
    }
    
	private SapphireDiagramEditorPagePart diagramEditor;
	private IDiagramNodeDef definition;
	private Element modelElement;	
	private String propertyName;
	private ListProperty modelProperty;
	private JavaType modelElementType;
	private String toolPaletteLabel;
	private String toolPaletteDesc;
	private FunctionResult toolPaletteImageFunctionResult;
	private DiagramEmbeddedConnectionTemplate embeddedConnTemplate;
	private Listener modelPropertyListener;
	private Listener nodePartListener;
	private Set<DiagramNodeTemplateListener> listeners;	
	private List<DiagramNodePart> diagramNodes;
	    
	@Override
    public void init()
    {
        this.diagramEditor = (SapphireDiagramEditorPagePart)parent();
        this.modelElement = getModelElement();
        this.definition = (IDiagramNodeDef)super.definition;
        
        if (this.definition.getToolPaletteLabel().content() != null)
        {
            ValueProperty tpLabelProperty = IDiagramNodeDef.PROP_TOOL_PALETTE_LABEL;
            this.toolPaletteLabel = tpLabelProperty.getLocalizationService().text(
                            this.definition.getToolPaletteLabel().content(), CapitalizationType.TITLE_STYLE, false);
        }        
        this.toolPaletteDesc = this.definition.getToolPaletteDescription().content();
        
        this.diagramNodes = new ArrayList<DiagramNodePart>();
        this.listeners = new CopyOnWriteArraySet<DiagramNodeTemplateListener>();
        
        this.propertyName = this.definition.getProperty().content();
        this.modelProperty = (ListProperty)resolve(this.modelElement, this.propertyName);
        this.modelElementType = this.definition.getElementType().resolve();
        
		initNodePartListener();
		
    	ElementList<?> list = this.modelElement.property(this.modelProperty);
        for( Element listEntryModelElement : list )
        {
            if (this.modelElementType == null)
            {
                createNewNodePart(listEntryModelElement);                
            }
            else 
            {
                final Class<?> cl = (Class<?>) this.modelElementType.artifact();
                if( cl == null || cl.isAssignableFrom( listEntryModelElement.getClass() ) )
                {
                	createNewNodePart(listEntryModelElement);
                }
            }
        }

        this.toolPaletteImageFunctionResult = initExpression
        (
            this.definition.getToolPaletteImage().content(),
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
	
	private void initNodePartListener() {
        this.nodePartListener = new Listener() {
			@Override
			public void handle(Event e) {
                if (e instanceof DiagramNodeEvent) {
                	DiagramNodeEvent event = (DiagramNodeEvent)e;
                	switch(event.getNodeEventType()) {
		    	    	case NodeMove:
		            		notifyNodeMoveEvent(event);
		    	    		break;
		    	    	default:
		    	    		break;
                	}
				}
			}
        };
		
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
            IDiagramConnectionDef connDef = this.diagramEditor.getDiagramConnectionDef(embeddedConnDef.getConnectionId().content());
            this.embeddedConnTemplate.init(this, this.modelElement, connDef, Collections.<String,String>emptyMap());
            this.embeddedConnTemplate.initialize();
        }
    }
    
    public IDiagramNodeDef definition()
    {
        return this.definition;
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
        return this.definition.getId().content();
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
    	Element newElement = null;
		ElementList<?> list = this.modelElement.property(this.modelProperty);
		if (this.modelElementType == null)
		{
			newElement = list.insert();
		}
		else
		{
			final Class cl = (Class<?>) this.modelElementType.artifact();
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
    
    public void deleteNode(DiagramNodePart nodePart)
    {
        notifyNodeAboutToBeDeleted(nodePart);
		Element nodeModel = nodePart.getLocalModelElement();		
        ElementList<?> list = (ElementList<?>) nodeModel.parent();
        list.remove(nodeModel);                	
    }
    
    public PropertyDef getModelProperty()
    {
        return this.modelProperty;
    }
    
    public ElementType getNodeType()
    {
        if (this.modelElementType == null)
        {
            return this.modelProperty.getType();
        }
        else 
        {
            final Class<?> cl = (Class<?>) this.modelElementType.artifact();
            return ElementType.read(cl);
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
    	
    private void handleModelPropertyChange(final PropertyEvent event)
    {
    	ElementList<?> tempList = (ElementList<?>) event.property();
    	
    	// filter the list property with specified element type
    	List<Element> newList = new ArrayList<Element>();
    	for (Element ele : tempList)
    	{
    		if (this.modelElementType == null)
    		{
    			newList.add(ele);
    		}
    		else
    		{
        		final Class<?> cl = (Class<?>) this.modelElementType.artifact();
        		if( cl == null || cl.isAssignableFrom( ele.getClass()))
        		{
        			newList.add(ele);
        		}
    		}
    	}
    	List<DiagramNodePart> nodeParts = getDiagramNodes();
		List<Element> oldList = new ArrayList<Element>(nodeParts.size());
		for (DiagramNodePart nodePart : nodeParts)
		{
			oldList.add(nodePart.getLocalModelElement());
		}
    	
    	List<Element> deletedNodes = CollectionsUtil.removedBasedOnEntryIdentity(oldList, newList);
    	List<Element> newNodes = CollectionsUtil.removedBasedOnEntryIdentity(newList, oldList);
		for (Element deletedNode : deletedNodes)
		{
			DiagramNodePart nodePart = getNodePart(deletedNode);
			if (nodePart != null)
			{
				notifyNodeDelete(nodePart);
				nodePart.dispose();
				nodePart.detach(this.nodePartListener);
				this.diagramNodes.remove(nodePart);
				if (this.embeddedConnTemplate != null)
				{
					// remove embedded connection parts that are attached to this node
					this.embeddedConnTemplate.removeConnectionParts(deletedNode);
				}
			}
		}    	    	
		for (Element newNode : newNodes)
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
	    	if (visible())
	    	{
	    		notifyNodeAdded(nodePart);
	    	}
		}
    }
    
    public DiagramNodePart getNodePart(Element element)
    {
        List<DiagramNodePart> nodeParts = getDiagramNodes();
        for (DiagramNodePart nodePart : nodeParts)
        {
            if (nodePart.getLocalModelElement() == element)
            {
                return nodePart;
            }
        }
        return null;
    }
    
    private DiagramNodePart createNewNodePart(Element element)
    {
        DiagramNodePart newNode = new DiagramNodePart();
        newNode.init(this, element, this.definition, 
                Collections.<String,String>emptyMap());
        newNode.initialize();
        newNode.attach(this.nodePartListener);
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
    }
    
    public SapphireDiagramEditorPagePart getDiagramEditorPart()
    {
        return this.diagramEditor;
    }
        
    private void notifyNodeAdd(DiagramNodePart nodePart)
    {
        for( DiagramNodeTemplateListener listener : this.listeners )
        {
            listener.handleNodeAdd(nodePart);
        }                
    }
    
    private void notifyNodeAdded(DiagramNodePart nodePart)
    {
        for( DiagramNodeTemplateListener listener : this.listeners )
        {
            listener.handleNodeAdded(nodePart);
        }                
    }
    
    private void notifyNodeAboutToBeDeleted(DiagramNodePart nodePart)
    {
        for( DiagramNodeTemplateListener listener : this.listeners )
        {
            listener.handleNodeAboutToBeDeleted(nodePart);
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
		
	
}
