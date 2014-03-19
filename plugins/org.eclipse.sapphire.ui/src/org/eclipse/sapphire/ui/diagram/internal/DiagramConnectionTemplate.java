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
 *    Konstantin Komissarchik - [342775] Support EL in MasterDetailsTreeNodeDef.ImagePath
 *    Konstantin Komissarchik - [378756] Convert ModelElementListener and ModelPropertyListener to common listener infrastructure
 *    Ling Hao - [383924] Flexible diagram node shapes
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.PropertyEvent;
import org.eclipse.sapphire.ReferenceValue;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.el.FailSafeFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.diagram.ConnectionAddEvent;
import org.eclipse.sapphire.ui.diagram.ConnectionDeleteEvent;
import org.eclipse.sapphire.ui.diagram.ConnectionEndpointsEvent;
import org.eclipse.sapphire.ui.diagram.def.IDiagramConnectionDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramConnectionEndpointBindingDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramExplicitConnectionBindingDef;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodePart;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodeTemplate;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;
import org.eclipse.sapphire.util.CollectionsUtil;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class DiagramConnectionTemplate extends SapphirePart
{
    public static abstract class DiagramConnectionTemplateListener
    {
        public void handleConnectionEndpointUpdate(final ConnectionEndpointsEvent event)
        {            
        }    	
        public void handleConnectionAddEvent(final ConnectionAddEvent event)
        {            
        }
        public void handleConnectionDeleteEvent(final ConnectionDeleteEvent event)
        {            
        }
    }
    
    protected SapphireDiagramEditorPagePart diagramEditor;
    protected IDiagramConnectionDef connectionDef;
    protected IDiagramExplicitConnectionBindingDef bindingDef;
    protected String propertyName;
    private ListProperty modelProperty;
    protected ListProperty connListProperty;
    protected Listener modelPropertyListener;
    protected Listener connPartListener;
    protected Set<DiagramConnectionTemplateListener> templateListeners;
    // The model path specified in the sdef file
    private ModelPath originalEndpoint2Path;
    // The converted model path for the endpoints. The path is based on the connection element
    private ModelPath endpoint1Path;
    private ModelPath endpoint2Path;
    private ValueProperty endpoint1Property;
    private ValueProperty endpoint2Property;
    
    private List<StandardDiagramConnectionPart> diagramConnections = new ArrayList<StandardDiagramConnectionPart>();
    
    public DiagramConnectionTemplate() {}
    
    public DiagramConnectionTemplate(IDiagramExplicitConnectionBindingDef bindingDef)
    {
        this.bindingDef = bindingDef;
    }
    
    @Override
    public void init()
    {
        final Element element = getModelElement();
        
        this.diagramEditor = (SapphireDiagramEditorPagePart)parent();
        this.connectionDef = (IDiagramConnectionDef)super.definition();
        
        this.propertyName = this.bindingDef.getProperty().content();
        this.modelProperty = (ListProperty) element.property(this.propertyName).definition();
        
        initConnPartListener();
        
        this.templateListeners = new CopyOnWriteArraySet<DiagramConnectionTemplateListener>();
                    
        String endpt1PropStr = this.bindingDef.getEndpoint1().content().getProperty().content();
        String endpt2PropStr = this.bindingDef.getEndpoint2().content().getProperty().content();
        this.originalEndpoint2Path = new ModelPath(endpt2PropStr);
        
        ElementType type = this.modelProperty.getType();
        this.endpoint1Property = type.property(endpt1PropStr);
        this.endpoint2Property = type.property(this.originalEndpoint2Path);
                
        if (getConnectionType() == ConnectionType.OneToOne)
        {
            this.endpoint1Path = new ModelPath(endpt1PropStr);
            this.endpoint2Path = new ModelPath(endpt2PropStr);
            this.connListProperty = this.modelProperty;
        }
        else 
        {
            ModelPath.PropertySegment head = (ModelPath.PropertySegment)this.originalEndpoint2Path.head();
            PropertyDef prop = type.property(head.getPropertyName());
            if (prop instanceof ListProperty)
            {
                this.endpoint1Path = new ModelPath("../" + endpt1PropStr);
                this.endpoint2Path = this.originalEndpoint2Path.tail();
                this.connListProperty = (ListProperty)prop;
            }
            else 
            {
                throw new RuntimeException("Invaid Model Path:" + this.originalEndpoint2Path);
            }
        }
        
        // initialize the connection parts
        ElementList<?> list = element.property(this.modelProperty);
        for( Element listEntryModelElement : list )
        {
            // check the type of connection: 1x1 connection versus 1xn connection            
            if (getConnectionType() == ConnectionType.OneToOne)
            {    
                // The connection model element specifies a 1x1 connection
                createNewConnectionPart(listEntryModelElement, null);
            }
            else
            {
                ModelPath.PropertySegment head = (ModelPath.PropertySegment)this.originalEndpoint2Path.head();
                PropertyDef connProp = listEntryModelElement.property(head.getPropertyName()).definition();
                if (!(connProp instanceof ListProperty))
                {
                    throw new RuntimeException("Expecting " + connProp.name() + " to be a list property");
                }
                // the connection is of type 1xn
                ElementList<?> connList = listEntryModelElement.property((ListProperty)connProp);                        
                for (Element connElement : connList)
                {
                	createNewConnectionPart(connElement, null);
                }
            }
        }
                
        // Add model property listener
        this.modelPropertyListener = new FilteredListener<PropertyContentEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyContentEvent event )
            {
                handleModelPropertyChange( event );
            }
        };
        addModelListener();        
    }
           
    protected void initConnPartListener() 
    {
        this.connPartListener = new FilteredListener<ConnectionEndpointsEvent>() 
        {
			@Override
			public void handleTypedEvent(ConnectionEndpointsEvent e) 
			{
				notifyConnectionEndpointUpdate(e);				
			}
        };
    }
    
    public String getConnectionTypeId()
    {
        return this.connectionDef.getId().content();
    }
    
    public List<StandardDiagramConnectionPart> getDiagramConnections(Element connListParent)
    {
        List<StandardDiagramConnectionPart> connList = new ArrayList<StandardDiagramConnectionPart>();
        if (connListParent == null || getConnectionType() == ConnectionType.OneToOne)
        {
            connList.addAll(this.diagramConnections);
        }
        else
        {            
            for (StandardDiagramConnectionPart connPart : this.diagramConnections)
            {
                Element connModel = connPart.getLocalModelElement();
                if (connModel.parent().element() == connListParent)
                {
                    connList.add(connPart);
                }
            }            
        }
        return connList;
    }
        
    public Element getConnectionParentElement(Element srcNodeModel)
    {
        if (getConnectionType() == ConnectionType.OneToMany)
        {
            ElementList<?> list = getModelElement().property(this.modelProperty);
            for( Element listEntryModelElement : list )
            {
                Object valObj = listEntryModelElement.property(this.endpoint1Property);
                if (valObj instanceof ReferenceValue)
                {
                    ReferenceValue<?,?> reference = (ReferenceValue<?,?>)valObj;
                    Element model = (Element)reference.resolve();
                    if (model == null)
                    {
                        if (reference.text() != null)
                        {
                            SapphireDiagramEditorPagePart diagramEditorPart = getDiagramEditor();
                            DiagramNodePart targetNode = diagramEditorPart.getNode(reference.text());
                            if (targetNode != null)
                            {
                                model = targetNode.getLocalModelElement();
                            }
                        }                        
                    }
                    if (srcNodeModel == model)
                    {
                        return listEntryModelElement;
                    }
                }
            }
        }
        return null;
    }
    
    public boolean canStartNewConnection(DiagramNodePart srcNode)
    {
        boolean canStart = false;
        ElementType srcType = srcNode.getModelElement().type();
        if (this.endpoint1Property.getType() == null && this.endpoint1Property.hasAnnotation(Reference.class))
        {
            canStart = this.endpoint1Property.getAnnotation(Reference.class).target().isAssignableFrom(srcType.getModelElementClass());
        }
        return canStart;
    }
    
    public boolean canCreateNewConnection(DiagramNodePart srcNode, DiagramNodePart targetNode)
    {
        boolean canCreate = false;
        
        canCreate = canStartNewConnection(srcNode);
        if (!canCreate)
            return false;
        
        ElementType targetType = targetNode.getModelElement().type();
        
        if (this.endpoint2Property.getType() == null && this.endpoint2Property.hasAnnotation(Reference.class))
        {
            canCreate = this.endpoint2Property.getAnnotation(Reference.class).target().isAssignableFrom(targetType.getModelElementClass());
        }
        return canCreate;
    }
    
    public void addModelListener()
    {
        getModelElement().attach(this.modelPropertyListener, this.propertyName);
        if (getConnectionType() == ConnectionType.OneToMany)
        {
            // it's a 1xn connection type; need to listen to each connection list property
            ElementList<?> list = getModelElement().property(this.modelProperty);
            ModelPath.PropertySegment head = (ModelPath.PropertySegment)this.originalEndpoint2Path.head();
            for( Element listEntryModelElement : list )
            {                
                listEntryModelElement.attach(this.modelPropertyListener, head.getPropertyName());                
            }
        }
    }
    
    public void removeModelListener()
    {
        getModelElement().detach(this.modelPropertyListener, this.propertyName);
        if (getConnectionType() == ConnectionType.OneToMany)
        {
            // it's a 1xn connection type; need to listen to each connection list property
            ElementList<?> list = getModelElement().property(this.modelProperty);
            for( Element listEntryModelElement : list )
            {
                ModelPath.PropertySegment head = (ModelPath.PropertySegment)this.originalEndpoint2Path.head();
                listEntryModelElement.detach(this.modelPropertyListener, head.getPropertyName());                
            }
        }
    }
    
    public void addTemplateListener( final DiagramConnectionTemplateListener listener )
    {
        this.templateListeners.add( listener );
    }
    
    public void removeTemplateListener( final DiagramConnectionTemplateListener listener )
    {
        this.templateListeners.remove( listener );
    }
    
    public SapphireDiagramEditorPagePart getDiagramEditor()
    {
        return this.diagramEditor;
    }
    
    public ConnectionType getConnectionType()
    {
        if (this.originalEndpoint2Path.length() > 1)
        {
            return ConnectionType.OneToMany;
        }
        else
        {
            return ConnectionType.OneToOne;
        }
    }
    
    public String getSerializedEndpoint1(DiagramNodePart srcNode)
    {
        String endpoint1Value = null;
        IDiagramConnectionEndpointBindingDef srcAnchorDef = this.bindingDef.getEndpoint1().content();
        Value<Function> srcFunc = srcAnchorDef.getValue();
        FunctionResult srcFuncResult = getNodeReferenceFunction(srcNode, srcFunc, 
                                this.bindingDef.adapt( LocalizationService.class ));
        if (srcFuncResult != null)
        {
            endpoint1Value = (String)srcFuncResult.value();
            srcFuncResult.dispose();            
        }
        if (endpoint1Value == null || endpoint1Value.length() == 0)
        {
            endpoint1Value = srcNode.getId();
        }
        return endpoint1Value;
    }
    
    public String getSerializedEndpoint2(DiagramNodePart targetNode)
    {
        String endpoint2Value = null;
        IDiagramConnectionEndpointBindingDef targetAnchorDef = this.bindingDef.getEndpoint2().content();
        Value<Function> targetFunc = targetAnchorDef.getValue();;
        FunctionResult targetFuncResult = getNodeReferenceFunction(targetNode, targetFunc,
                                this.bindingDef.adapt( LocalizationService.class ));
        
        if (targetFuncResult != null)
        {
            endpoint2Value = (String)targetFuncResult.value();
            targetFuncResult.dispose();
        }
        if (endpoint2Value == null || endpoint2Value.length() == 0)
        {
            endpoint2Value = targetNode.getId();
        }
        return endpoint2Value;
    }
    
    private Element getOneToManyConnectionSrcElement(String endpoint1Value)
    {
        ElementList<?> list = getModelElement().property(this.modelProperty);
        Element srcElement = null;
        for (Element element : list)
        {
            final String val = element.property(this.endpoint1Property).text();
            if (val != null && val.equals(endpoint1Value))
            {
                srcElement = element;
                break;
            }
        }
        return srcElement;
    }

    public void setSerializedEndpoint1(Element connModelElement, String endpoint1Value)
    {
        IDiagramConnectionEndpointBindingDef srcAnchorDef = this.bindingDef.getEndpoint1().content();
        if (getConnectionType() == ConnectionType.OneToOne)
        {
            setModelProperty(connModelElement, ((ModelPath.PropertySegment)this.endpoint1Path.head()).getPropertyName(), endpoint1Value);
        }
        else
        {
        	Element srcElement = getOneToManyConnectionSrcElement(endpoint1Value);
            if (srcElement != null)
            {
                String srcProperty = srcAnchorDef.getProperty().content();
                setModelProperty(srcElement, srcProperty, endpoint1Value);
            }
        }
    }
    
    public void setSerializedEndpoint2(Element connModelElement, String endpoint2Value)
    {
    	setModelProperty(connModelElement, ((ModelPath.PropertySegment)this.endpoint2Path.head()).getPropertyName(), endpoint2Value);
    }
    
    public StandardDiagramConnectionPart createNewDiagramConnection(DiagramNodePart srcNode, 
                                                            DiagramNodePart targetNode)
    {        
        // Get the serialized value of endpoint1
        String endpoint1Value = getSerializedEndpoint1(srcNode);
        
        // get the serialized value of endpoint2
        String endpoint2Value = getSerializedEndpoint2(targetNode);
        
        if (endpoint1Value != null && endpoint2Value != null)
        {
            if (getConnectionType() == ConnectionType.OneToOne)
            {
                ElementList<?> list = getModelElement().property(this.modelProperty);
                Element newElement = list.insert();
                setModelProperty(newElement, ((ModelPath.PropertySegment)this.endpoint1Path.head()).getPropertyName(), endpoint1Value);
                setModelProperty(newElement, ((ModelPath.PropertySegment)this.endpoint2Path.head()).getPropertyName(), endpoint2Value);

                StandardDiagramConnectionPart newConn = getConnectionPart(srcNode.getModelElement(), newElement);
                if (newConn == null) {
                	newConn = createNewConnectionPart(newElement, null);
                }
                return newConn;                
            }
            else
            {
                Element srcElement = getOneToManyConnectionSrcElement(endpoint1Value);
                IDiagramConnectionEndpointBindingDef srcAnchorDef = this.bindingDef.getEndpoint1().content();
	            String srcProperty = srcAnchorDef.getProperty().content();                
                
                if (srcElement == null)
                {
                	ElementList<?> list = getModelElement().property(this.modelProperty);
                    srcElement = list.insert();
                    setModelProperty(srcElement, srcProperty, endpoint1Value);
                }
                
                ModelPath.PropertySegment head = (ModelPath.PropertySegment)this.originalEndpoint2Path.head();
                PropertyDef connProp = srcElement.property(head.getPropertyName()).definition();
                if (!(connProp instanceof ListProperty))
                {
                    throw new RuntimeException("Expecting " + connProp.name() + " to be a list property");
                }
                // the connection is of type 1xn
                ElementList<?> connList = srcElement.property((ListProperty)connProp);
                Element newElement = connList.insert();
                setModelProperty(newElement, ((ModelPath.PropertySegment)this.endpoint2Path.head()).getPropertyName(), endpoint2Value);
                StandardDiagramConnectionPart newConn = getConnectionPart(srcElement, newElement);
                if (newConn == null) {
                	newConn = createNewConnectionPart(newElement, null);
                }                
                return newConn;
            }
        } 
        return null;
    }
    
    protected StandardDiagramConnectionPart createNewConnectionPart(Element connElement, Element srcNodeElement)
    {
        StandardDiagramConnectionPart connPart = new StandardDiagramConnectionPart(this.bindingDef, this.endpoint1Path, this.endpoint2Path);
        addConnectionPart(srcNodeElement, connPart);
        connPart.init(this, connElement, this.connectionDef, 
                Collections.<String,String>emptyMap());
        connPart.initialize();
        connPart.attach(this.connPartListener);
        notifyConnectionAddEvent(new ConnectionAddEvent(connPart));
        return connPart;
    }
        
    public void showAllConnectionParts(DiagramNodeTemplate nodeTemplate)
    {
    	List<StandardDiagramConnectionPart> connParts = getDiagramConnections(null);
    	for (StandardDiagramConnectionPart connPart : connParts)
    	{
    		Element endpt1 = connPart.getEndpoint1();
    		Element endpt2 = connPart.getEndpoint2();
    		DiagramNodePart nodePart1 = this.diagramEditor.getDiagramNodePart(endpt1);
    		if (nodePart1 != null && nodePart1.getDiagramNodeTemplate() == nodeTemplate)
    		{
    			notifyConnectionAddEvent(new ConnectionAddEvent(connPart));
    		}
    		else
    		{
    			DiagramNodePart nodePart2 = this.diagramEditor.getDiagramNodePart(endpt2);
        		if (nodePart2 != null && nodePart2.getDiagramNodeTemplate() == nodeTemplate)
        		{
        			notifyConnectionAddEvent(new ConnectionAddEvent(connPart));
        		}    			
    		}
    	}
    }
    
    public void hideAllConnectionParts(DiagramNodeTemplate nodeTemplate)
    {
    	List<StandardDiagramConnectionPart> connParts = getDiagramConnections(null);
    	for (StandardDiagramConnectionPart connPart : connParts)
    	{
    		Element endpt1 = connPart.getEndpoint1();
    		Element endpt2 = connPart.getEndpoint2();
    		DiagramNodePart nodePart1 = this.diagramEditor.getDiagramNodePart(endpt1);
    		if (nodePart1 != null && nodePart1.getDiagramNodeTemplate() == nodeTemplate)
    		{
    			notifyConnectionDeleteEvent(new ConnectionDeleteEvent(connPart));
    		}
    		else
    		{
    			DiagramNodePart nodePart2 = this.diagramEditor.getDiagramNodePart(endpt2);
        		if (nodePart2 != null && nodePart2.getDiagramNodeTemplate() == nodeTemplate)
        		{
        			notifyConnectionDeleteEvent(new ConnectionDeleteEvent(connPart));
        		}    			
    		}
    	}
    }

    protected void setModelProperty(final Element modelElement, 
                                    String propertyName, Object value)
    {
        if (propertyName != null)
        {
            final ElementType type = modelElement.type();
            final PropertyDef property = type.property( propertyName );
            if( property == null )
            {
                throw new RuntimeException( "Could not find property " + propertyName + " in " + type.getQualifiedName() );
            }
            if (!(property instanceof ValueProperty))
            {
                throw new RuntimeException( "Property " + propertyName + " not a ValueProperty");
            }
                    
            modelElement.property( (ValueProperty) property ).write( value );
        }        
    }
    
    protected FunctionResult getNodeReferenceFunction(final DiagramNodePart nodePart,
                                                final Value<Function> function,
                                                LocalizationService ls)
    {
        Function f = null;
        FunctionResult fr = null;
        
        if( function != null )
        {
            f = function.content();
        }
        
        if( f != null )
        {
            
            f = FailSafeFunction.create( f, Literal.create( String.class ) );
            fr = f.evaluate( new ModelElementFunctionContext( nodePart.getLocalModelElement(), ls ));
        }
        return fr;
    }
    
    protected void handleConnectionListChange(Element connListParent, ListProperty listProperty)
    {
    	ElementList<?> newList = connListParent.property(listProperty);
        List<StandardDiagramConnectionPart> connParts = getDiagramConnections(connListParent);
        List<Element> oldList = new ArrayList<Element>(connParts.size());
        for (StandardDiagramConnectionPart connPart : connParts)
        {
            oldList.add(connPart.getLocalModelElement());
        }
        List<Element> deletedConns = CollectionsUtil.removedBasedOnEntryIdentity(oldList, newList);
        List<Element> newConns = CollectionsUtil.removedBasedOnEntryIdentity(newList, oldList);
        
        // Handle deleted connections
        for (Element deletedConn : deletedConns)
        {
            StandardDiagramConnectionPart connPart = getConnectionPart(connListParent, deletedConn);
            if (connPart != null)
            {
                notifyConnectionDeleteEvent(new ConnectionDeleteEvent(connPart));
                disposeConnectionPart(connPart);
            }
        }
        // Handle newly created connections
        for (Element newConn : newConns)
        {                    
            createNewConnectionPart(newConn, connListParent);
        }        
    }
    
    protected void handleModelPropertyChange(final PropertyEvent event)
    {
        final Element element = event.property().element();
        final ListProperty property = (ListProperty)event.property().definition();
        ElementList<?> newList = element.property(property);
        
        if (property == this.connListProperty)
        {
        	handleConnectionListChange(element, property);
        }  
        else if (property == this.modelProperty)
        {
            // 1xn type connection and we are dealing with events on connection list parent
            List<StandardDiagramConnectionPart> connParts = getDiagramConnections(null);
            List<Element> oldList = new ArrayList<Element>();
            Set<Element> oldConnParents = new HashSet<Element>(); 
            for (StandardDiagramConnectionPart connPart : connParts)
            {
                Element connElement = connPart.getLocalModelElement();                
                Element connParentElement = connElement.parent().element();
                if (!(oldConnParents.contains(connParentElement)))
                {
                    oldConnParents.add(connParentElement);
                }                
            }
            Iterator <Element> it = oldConnParents.iterator();
            while(it.hasNext())
            {
                oldList.add(it.next());
            }
            List<Element> deletedConnParents = CollectionsUtil.removedBasedOnEntryIdentity(oldList, newList);
            List<Element> newConnParents = CollectionsUtil.removedBasedOnEntryIdentity(newList, oldList);
            
            // connection parents are deleted and we need to dispose any connections associated with them
            List<StandardDiagramConnectionPart> connPartsCopy = new ArrayList<StandardDiagramConnectionPart>(connParts.size());
            connPartsCopy.addAll(connParts);
            for (StandardDiagramConnectionPart connPart : connPartsCopy)
            {
                Element connElement = connPart.getLocalModelElement();                
                Element connParentElement = connElement.parent().element();
                if (deletedConnParents.contains(connParentElement))
                {
                    notifyConnectionDeleteEvent(new ConnectionDeleteEvent(connPart));
                    disposeConnectionPart(connPart);                        
                }
            }            
            // new connection parents are added and we need to listen on their connection list property
            ModelPath.PropertySegment head = (ModelPath.PropertySegment)this.originalEndpoint2Path.head();
            for (Element newConnParent : newConnParents)
            {
                newConnParent.attach(this.modelPropertyListener, head.getPropertyName());
                handleConnectionListChange(newConnParent, this.connListProperty);
            }

        }
    }
    
    protected void addConnectionPart(Element srcNodeModel, StandardDiagramConnectionPart connPart)
    {
        this.diagramConnections.add(connPart);
    }
    
    protected void disposeConnectionPart(StandardDiagramConnectionPart connPart)
    {
    	connPart.dispose();
    	connPart.detach(this.connPartListener);
        this.diagramConnections.remove(connPart);
    }
    
    protected StandardDiagramConnectionPart getConnectionPart(Element srcNodeModel, Element connModel)
    {
        List<StandardDiagramConnectionPart> connParts = getDiagramConnections(srcNodeModel);
        for (StandardDiagramConnectionPart connPart : connParts)
        {
            if (connPart.getLocalModelElement() == connModel)
            {
                return connPart;
            }
        }
        return null;
    }
    
    public void dispose()
    {
        removeModelListener();
        List<StandardDiagramConnectionPart> connParts = getDiagramConnections(null);
        for (StandardDiagramConnectionPart connPart : connParts)
        {
        	notifyConnectionDeleteEvent(new ConnectionDeleteEvent(connPart));
            connPart.dispose();
        }
    }
    
    protected void notifyConnectionEndpointUpdate(ConnectionEndpointsEvent event)
    {
        for( DiagramConnectionTemplateListener listener : this.templateListeners )
        {
            listener.handleConnectionEndpointUpdate(event);
        }        
    }
    
    protected void notifyConnectionAddEvent(ConnectionAddEvent event)
    {
        for( DiagramConnectionTemplateListener listener : this.templateListeners )
        {
            listener.handleConnectionAddEvent(event);
        }        
    }

    protected void notifyConnectionDeleteEvent(ConnectionDeleteEvent event)
    {
        for( DiagramConnectionTemplateListener listener : this.templateListeners )
        {
            listener.handleConnectionDeleteEvent(event);
        }        
    }

    // ******************************************************************
    // Inner classes
    //*******************************************************************
    
    public static enum ConnectionType
    {
        OneToOne,
        OneToMany,
        Embedded
    }
    
}
