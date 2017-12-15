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
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.ElementDisposeEvent;
import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.PropertyEvent;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.ui.diagram.def.IDiagramConnectionDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramConnectionEndpointBindingDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramExplicitConnectionBindingDef;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class DiagramEmbeddedConnectionTemplate extends DiagramConnectionTemplate
{
    private DiagramNodeTemplate nodeTemplate;
    private Map<IModelElement, List<DiagramConnectionPart>> diagramConnectionMap;
    private Listener modelElementListener;
    private ModelPath endpointPath;
        
    public DiagramEmbeddedConnectionTemplate(IDiagramExplicitConnectionBindingDef connBindingDef)
    {
        super(connBindingDef);
    }
    
    @Override
    public void init()
    {
        this.nodeTemplate = (DiagramNodeTemplate)getParentPart();
        this.diagramEditor = this.nodeTemplate.getDiagramEditorPart();
        this.modelElement = getModelElement();
        this.connectionDef = (IDiagramConnectionDef)super.definition();
        
        this.diagramConnectionMap = new HashMap<IModelElement, List<DiagramConnectionPart>>();
        
        ListProperty nodeProperty = (ListProperty)this.nodeTemplate.getModelProperty();
        this.propertyName = this.bindingDef.getProperty().getContent();
        this.connListProperty = (ListProperty)nodeProperty.getType().property(this.propertyName);
        
        this.connPartListener = new ConnectionPartListener();
        
        this.templateListeners = new CopyOnWriteArraySet<DiagramConnectionTemplateListener>();
        
        this.modelPropertyListener = new FilteredListener<PropertyEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyEvent event )
            {
                handleModelPropertyChange( event );
            }
        };
        
        this.modelElementListener = new FilteredListener<ElementDisposeEvent>() 
        {
            @Override
            protected void handleTypedEvent( final ElementDisposeEvent event )
            {
                handleModelElementDispose(event);
            }
        };
        
        String endpointPropStr = this.bindingDef.getEndpoint2().element().getProperty().getContent();
        this.endpointPath = new ModelPath(endpointPropStr);
        
        ModelElementList<IModelElement> srcNodeList = this.modelElement.read(nodeProperty);
        for (IModelElement srcNodeModel : srcNodeList)
        {
            ModelProperty connProp = ModelUtil.resolve(srcNodeModel, this.propertyName);
            if (connProp instanceof ListProperty)
            {
                ListProperty connListProperty = (ListProperty)connProp;
                ModelElementList<?> connList = srcNodeModel.read(connListProperty);
                for (IModelElement endpointModel : connList)
                {
                	createNewConnectionPart(endpointModel, srcNodeModel);                	
                }                
                addModelListener(srcNodeModel);
            }
        }                
    }
    
    public void refreshConnections(IModelElement srcNodeModel)
    {
    	ModelProperty connProp = ModelUtil.resolve(srcNodeModel, this.propertyName);
    	handleConnectionListChange(srcNodeModel, (ListProperty)connProp);
    }
    
    @Override
    public boolean canStartNewConnection(DiagramNodePart srcNode)
    {
        IModelElement srcNodeModel = srcNode.getLocalModelElement();
        // check the source node type
        ModelElementType srcNodeType = srcNodeModel.type();
        ModelElementType desiredsrcNodeType = this.nodeTemplate.getNodeType();
        
        if (!srcNodeType.equals(desiredsrcNodeType))
        {
            return false;
        }
        return true;
    }
        
    @Override
    public boolean canCreateNewConnection(DiagramNodePart srcNode, DiagramNodePart targetNode)
    {    
        if (!canStartNewConnection(srcNode))
        {
            return false;
        }
        IModelElement srcNodeModel = srcNode.getLocalModelElement();
        // check the target node type
        ModelElementType targetType = targetNode.getLocalModelElement().type();
        
        ModelProperty connProp = ModelUtil.resolve(srcNodeModel, this.propertyName);        
        ModelElementType connType = connProp.getType();
        ModelProperty endpointProp = 
            connType.property(this.bindingDef.getEndpoint2().element().getProperty().getContent());
        if (endpointProp.getType() == null && endpointProp.hasAnnotation(Reference.class))
        {
            return endpointProp.getAnnotation(Reference.class).target().isAssignableFrom(targetType.getModelElementClass());
        }
        return false;
    }
        
    @Override
    public void setSerializedEndpoint2(IModelElement connModelElement, String endpoint2Value)
    {
        IDiagramConnectionEndpointBindingDef endpointDef = this.bindingDef.getEndpoint2().element();
        String endpointProperty = endpointDef.getProperty().getContent();

        setModelProperty(connModelElement, endpointProperty, endpoint2Value);
    }
    
    @Override
    public DiagramConnectionPart createNewDiagramConnection(DiagramNodePart srcNode, 
            DiagramNodePart targetNode)
    {
        IModelElement srcNodeModel = srcNode.getLocalModelElement();
        ModelProperty modelProperty = this.nodeTemplate.getModelProperty();
        boolean found = false;
        if (modelProperty instanceof ListProperty)
        {
            ListProperty listProperty = (ListProperty)modelProperty;
            ModelElementList<?> list = this.modelElement.read(listProperty);
            for (IModelElement listEntryModelElement : list)
            {
                if (listEntryModelElement.equals(srcNodeModel))
                {
                    found = true;
                    break;
                }                
            }
        }
        else if (modelProperty instanceof ElementProperty)
        {
            ElementProperty elementProperty = (ElementProperty)modelProperty;
            if (this.modelElement.read(elementProperty) != null)
            {
                IModelElement localModelElement = this.modelElement.read(elementProperty).element();
                if (localModelElement == srcNodeModel)
                {
                    found = true;
                }
            }
        }
        if (!found)
        {
            throw new RuntimeException( "Cannot locate the source node element");
        }
        
        ModelProperty connProp = ModelUtil.resolve(srcNodeModel, this.propertyName);
        IModelElement newEndpoint = null;
        if (connProp instanceof ListProperty)
        {
            ListProperty listProperty = (ListProperty)connProp;
            ModelElementList<?> list = srcNodeModel.read(listProperty);
            newEndpoint = list.insert();            
        }
        
        // Get serialized value of endpoint
        String endpointVal = getSerializedEndpoint2(targetNode);
        
        // Set the serialized value of endpoint
        setSerializedEndpoint2(newEndpoint, endpointVal);
        
        DiagramConnectionPart connPart = getConnectionPart(srcNodeModel, newEndpoint);
        if (connPart == null) 
        {
        	connPart = createNewConnectionPart(newEndpoint, srcNodeModel);
        }        
        return connPart;
    }
    
    @Override
    public DiagramConnectionPart createNewConnectionPart(IModelElement connElement, IModelElement srcNodeElement)
    {
        DiagramEmbeddedConnectionPart connPart = 
            new DiagramEmbeddedConnectionPart(this.bindingDef, srcNodeElement, this.endpointPath);
        connPart.init(this, connElement, this.connectionDef, Collections.<String,String>emptyMap());
        connPart.addListener(this.connPartListener);
        addConnectionPart(srcNodeElement, connPart);
        return connPart;
    }
        
    public void addModelListener(IModelElement srcNodeModel)
    {
        srcNodeModel.attach(this.modelPropertyListener, this.propertyName);
        srcNodeModel.attach(this.modelElementListener);
    }
    
    public void removeModelListener(IModelElement srcNodeModel)
    {
        srcNodeModel.detach(this.modelPropertyListener, this.propertyName);
        srcNodeModel.detach(this.modelElementListener);
    } 
    
    @Override
    public void addModelListener()
    {
        
    }
    
    @Override
    public void removeModelListener()
    {
        ListProperty nodeProperty = (ListProperty)this.nodeTemplate.getModelProperty();
        ModelElementList<IModelElement> srcNodeList = this.modelElement.read(nodeProperty);
        for (IModelElement srcNodeModel : srcNodeList)
        {
            removeModelListener(srcNodeModel);
        }
    }
    
    @Override
    public List<DiagramConnectionPart> getDiagramConnections(IModelElement srcNodeModel)
    {
        List<DiagramConnectionPart> allConnParts = new ArrayList<DiagramConnectionPart>();
        if (srcNodeModel != null)
        {
            if (this.diagramConnectionMap.get(srcNodeModel) != null)
            {
                allConnParts.addAll(this.diagramConnectionMap.get(srcNodeModel));
            }
        }
        else
        {   
            // return all the connection parts
        	
            for (List<DiagramConnectionPart> connParts : this.diagramConnectionMap.values())
            {
            	allConnParts.addAll(connParts);
            }
        }
        return allConnParts;
    }

    @Override
    public void addConnectionPart(IModelElement srcNodeModel, DiagramConnectionPart connPart)
    {
        List<DiagramConnectionPart> connParts = this.diagramConnectionMap.get(srcNodeModel);
        if (connParts == null)
        {
            connParts = new ArrayList<DiagramConnectionPart>();
            this.diagramConnectionMap.put(srcNodeModel, connParts);
        }
        connParts.add(connPart);
    }

    @Override
    public void disposeConnectionPart(DiagramConnectionPart connPart)
    {
    	connPart.dispose();
    	Collection<List<DiagramConnectionPart>> allConnParts = this.diagramConnectionMap.values();
    	for (List<DiagramConnectionPart> connParts : allConnParts)
    	{
    		if (connParts.contains(connPart))
    		{
    			connParts.remove(connPart);
    			break;
    		}
    	}
    }
    
    public void removeConnectionParts(IModelElement srcNodeModel)
    {
    	List<DiagramConnectionPart> connParts = new ArrayList<DiagramConnectionPart>();
    	connParts.addAll(this.getDiagramConnections(srcNodeModel));
    	for (DiagramConnectionPart connPart : connParts)
    	{
            notifyConnectionDelete(new DiagramConnectionEvent(connPart));
            disposeConnectionPart(connPart);    		
    	}
    }
    
    private void handleModelElementDispose(final ElementDisposeEvent event)
    {
        IModelElement element = event.element();
        List<DiagramConnectionPart> connParts = getDiagramConnections(null);
        
        for (DiagramConnectionPart connPart : connParts)
        {
            if (connPart.getEndpoint1() == element || connPart.getEndpoint2() == element)
            {
                notifyConnectionDelete(new DiagramConnectionEvent(connPart));
                disposeConnectionPart(connPart);
            }
        }
        removeModelListener(element);
    } 
    
}
