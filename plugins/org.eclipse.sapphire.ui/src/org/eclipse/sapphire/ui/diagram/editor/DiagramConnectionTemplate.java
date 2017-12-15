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
 *    Konstantin Komissarchik - [342775] Support EL in MasterDetailsTreeNodeDef.ImagePath
 *    Konstantin Komissarchik - [378756] Convert ModelElementListener and ModelPropertyListener to common listener infrastructure
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.PropertyContentEvent;
import org.eclipse.sapphire.modeling.PropertyEvent;
import org.eclipse.sapphire.modeling.ReferenceValue;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.el.FailSafeFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.diagram.def.IDiagramConnectionDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramConnectionEndpointBindingDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramExplicitConnectionBindingDef;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class DiagramConnectionTemplate extends SapphirePart
{
    public static abstract class DiagramConnectionTemplateListener
    {
        public void handleConnectionUpdate(final DiagramConnectionEvent event)
        {
        }  
        public void handleConnectionEndpointUpdate(final DiagramConnectionEvent event)
        {            
        }
        public void handleConnectionAdd(final DiagramConnectionEvent event)
        {            
        }
        public void handleConnectionDelete(final DiagramConnectionEvent event)
        {            
        }
        public void handleAddBendpoint(final DiagramConnectionEvent event)
        {        	
        }
        public void handleRemoveBendpoint(final DiagramConnectionEvent event)
        {        	
        }
        public void handleMoveBendpoint(final DiagramConnectionEvent event)
        {        	
        }
        public void handleResetBendpoints(final DiagramConnectionEvent event)
        {        	
        }
        public void handleMoveLabel(final DiagramConnectionEvent event)
        {        	
        }
        
    }
    
    protected SapphireDiagramEditorPagePart diagramEditor;
    protected IDiagramConnectionDef connectionDef;
    protected IDiagramExplicitConnectionBindingDef bindingDef;
    protected IModelElement modelElement;
    protected String propertyName;
    private ListProperty modelProperty;
    protected ListProperty connListProperty;
    protected Listener modelPropertyListener;
    protected SapphireDiagramPartListener connPartListener;
    protected Set<DiagramConnectionTemplateListener> templateListeners;
    // The model path specified in the sdef file
    private ModelPath originalEndpoint2Path;
    // The converted model path for the endpoints. The path is based on the connection element
    private ModelPath endpoint1Path;
    private ModelPath endpoint2Path;
    private ModelProperty endpoint1Property;
    private ModelProperty endpoint2Property;
    
    private List<DiagramConnectionPart> diagramConnections = new ArrayList<DiagramConnectionPart>();
    
    public DiagramConnectionTemplate() {}
    
    public DiagramConnectionTemplate(IDiagramExplicitConnectionBindingDef bindingDef)
    {
        this.bindingDef = bindingDef;
    }
    
    @Override
    public void init()
    {
        this.diagramEditor = (SapphireDiagramEditorPagePart)getParentPart();
        this.modelElement = getModelElement();
        this.connectionDef = (IDiagramConnectionDef)super.definition();
        
        this.propertyName = this.bindingDef.getProperty().getContent();
        this.modelProperty = (ListProperty)ModelUtil.resolve(this.modelElement, this.propertyName);
        
        this.connPartListener = new ConnectionPartListener(); 
        this.templateListeners = new CopyOnWriteArraySet<DiagramConnectionTemplateListener>();
                    
        String endpt1PropStr = this.bindingDef.getEndpoint1().element().getProperty().getContent();
        String endpt2PropStr = this.bindingDef.getEndpoint2().element().getProperty().getContent();
        this.originalEndpoint2Path = new ModelPath(endpt2PropStr);
        
        ModelElementType type = this.modelProperty.getType();
        this.endpoint1Property = type.property(endpt1PropStr);
        this.endpoint2Property = ModelUtil.resolve(type, this.originalEndpoint2Path);
                
        if (getConnectionType() == ConnectionType.OneToOne)
        {
            this.endpoint1Path = new ModelPath(endpt1PropStr);
            this.endpoint2Path = new ModelPath(endpt2PropStr);
            this.connListProperty = this.modelProperty;
        }
        else 
        {
            ModelPath.PropertySegment head = (ModelPath.PropertySegment)this.originalEndpoint2Path.head();
            ModelProperty prop = type.property(head.getPropertyName());
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
        ModelElementList<?> list = this.modelElement.read(this.modelProperty);
        for( IModelElement listEntryModelElement : list )
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
                ModelProperty connProp = ModelUtil.resolve(listEntryModelElement, head.getPropertyName());
                if (!(connProp instanceof ListProperty))
                {
                    throw new RuntimeException("Expecting " + connProp.getName() + " to be a list property");
                }
                // the connection is of type 1xn
                ModelElementList<?> connList = listEntryModelElement.read((ListProperty)connProp);                        
                for (IModelElement connElement : connList)
                {
                	createNewConnectionPart(connElement, null);
                }
            }
        }
                
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
    
    public String getConnectionId()
    {
        return this.bindingDef.getConnectionId().getContent();
    }
    
    public String getConnectionTypeId()
    {
        return this.connectionDef.getId().getContent();
    }
    
    public List<DiagramConnectionPart> getDiagramConnections(IModelElement connListParent)
    {
        List<DiagramConnectionPart> connList = new ArrayList<DiagramConnectionPart>();
        if (connListParent == null || getConnectionType() == ConnectionType.OneToOne)
        {
            connList.addAll(this.diagramConnections);
        }
        else
        {            
            for (DiagramConnectionPart connPart : this.diagramConnections)
            {
                IModelElement connModel = connPart.getLocalModelElement();
                if (connModel.parent().parent() == connListParent)
                {
                    connList.add(connPart);
                }
            }            
        }
        return connList;
    }
        
    public IModelElement getConnectionParentElement(IModelElement srcNodeModel)
    {
        if (getConnectionType() == ConnectionType.OneToMany)
        {
            ModelElementList<?> list = this.modelElement.read(this.modelProperty);
            for( IModelElement listEntryModelElement : list )
            {
                Object valObj = listEntryModelElement.read(this.endpoint1Property);
                if (valObj instanceof ReferenceValue)
                {
                    ReferenceValue<?,?> reference = (ReferenceValue<?,?>)valObj;
                    IModelElement model = (IModelElement)reference.resolve();
                    if (model == null)
                    {
                        if (reference.getText() != null)
                        {
                            SapphireDiagramEditorPagePart diagramEditorPart = getDiagramEditor();
                            DiagramNodePart targetNode = IdUtil.getNodePart(diagramEditorPart, reference.getText());
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
        ModelElementType srcType = srcNode.getModelElement().type();
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
        
        ModelElementType targetType = targetNode.getModelElement().type();
        
        if (this.endpoint2Property.getType() == null && this.endpoint2Property.hasAnnotation(Reference.class))
        {
            canCreate = this.endpoint2Property.getAnnotation(Reference.class).target().isAssignableFrom(targetType.getModelElementClass());
        }
        return canCreate;
    }
    
    public void addModelListener()
    {
        this.modelElement.attach(this.modelPropertyListener, this.propertyName);
        if (getConnectionType() == ConnectionType.OneToMany)
        {
            // it's a 1xn connection type; need to listen to each connection list property
            ModelElementList<?> list = this.modelElement.read(this.modelProperty);
            ModelPath.PropertySegment head = (ModelPath.PropertySegment)this.originalEndpoint2Path.head();
            for( IModelElement listEntryModelElement : list )
            {                
                listEntryModelElement.attach(this.modelPropertyListener, head.getPropertyName());                
            }
        }
    }
    
    public void removeModelListener()
    {
        this.modelElement.detach(this.modelPropertyListener, this.propertyName);
        if (getConnectionType() == ConnectionType.OneToMany)
        {
            // it's a 1xn connection type; need to listen to each connection list property
            ModelElementList<?> list = this.modelElement.read(this.modelProperty);
            for( IModelElement listEntryModelElement : list )
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
        IDiagramConnectionEndpointBindingDef srcAnchorDef = this.bindingDef.getEndpoint1().element();
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
            endpoint1Value = IdUtil.computeNodeId(srcNode);
        }
        return endpoint1Value;
    }
    
    public String getSerializedEndpoint2(DiagramNodePart targetNode)
    {
        String endpoint2Value = null;
        IDiagramConnectionEndpointBindingDef targetAnchorDef = this.bindingDef.getEndpoint2().element();
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
            endpoint2Value = IdUtil.computeNodeId(targetNode);
        }
        return endpoint2Value;
    }
    
    private IModelElement getOneToManyConnectionSrcElement(String endpoint1Value)
    {
        ModelElementList<?> list = this.modelElement.read(this.modelProperty);
        IModelElement srcElement = null;
        for (IModelElement element : list)
        {
            Object valObj = element.read(this.endpoint1Property);
            String val = null;
            if (valObj instanceof ReferenceValue)
            {
                val = (((ReferenceValue<?,?>)valObj).getText());
            }
            else
            {
                val = (String)valObj;
            }
            if (val != null && val.equals(endpoint1Value))
            {
                srcElement = element;
                break;
            }
        }
        return srcElement;
    }

    public void setSerializedEndpoint1(IModelElement connModelElement, String endpoint1Value)
    {
        IDiagramConnectionEndpointBindingDef srcAnchorDef = this.bindingDef.getEndpoint1().element();
        if (getConnectionType() == ConnectionType.OneToOne)
        {
            setModelProperty(connModelElement, ((ModelPath.PropertySegment)this.endpoint1Path.head()).getPropertyName(), endpoint1Value);
        }
        else
        {
        	IModelElement srcElement = getOneToManyConnectionSrcElement(endpoint1Value);
            if (srcElement != null)
            {
                String srcProperty = srcAnchorDef.getProperty().getContent();
                setModelProperty(srcElement, srcProperty, endpoint1Value);
            }
        }
    }
    
    public void setSerializedEndpoint2(IModelElement connModelElement, String endpoint2Value)
    {
        if (getConnectionType() == ConnectionType.OneToOne)
        {
            setModelProperty(connModelElement, ((ModelPath.PropertySegment)this.endpoint2Path.head()).getPropertyName(), endpoint2Value);
        }
        else
        {
        	setModelProperty(connModelElement, ((ModelPath.PropertySegment)this.endpoint2Path.head()).getPropertyName(), endpoint2Value);            
        }            	
    }
    
    public DiagramConnectionPart createNewDiagramConnection(DiagramNodePart srcNode, 
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
                ModelElementList<?> list = this.modelElement.read(this.modelProperty);
                IModelElement newElement = list.insert();
                setModelProperty(newElement, ((ModelPath.PropertySegment)this.endpoint1Path.head()).getPropertyName(), endpoint1Value);
                setModelProperty(newElement, ((ModelPath.PropertySegment)this.endpoint2Path.head()).getPropertyName(), endpoint2Value);

                DiagramConnectionPart newConn = getConnectionPart(srcNode.getModelElement(), newElement);
                if (newConn == null) {
                	newConn = createNewConnectionPart(newElement, null);
                }
                return newConn;                
            }
            else
            {
                IModelElement srcElement = getOneToManyConnectionSrcElement(endpoint1Value);
                IDiagramConnectionEndpointBindingDef srcAnchorDef = this.bindingDef.getEndpoint1().element();
	            String srcProperty = srcAnchorDef.getProperty().getContent();                
                
                if (srcElement == null)
                {
                	ModelElementList<?> list = this.modelElement.read(this.modelProperty);
                    srcElement = list.insert();
                    setModelProperty(srcElement, srcProperty, endpoint1Value);
                }
                
                ModelPath.PropertySegment head = (ModelPath.PropertySegment)this.originalEndpoint2Path.head();
                ModelProperty connProp = ModelUtil.resolve(srcElement, head.getPropertyName());
                if (!(connProp instanceof ListProperty))
                {
                    throw new RuntimeException("Expecting " + connProp.getName() + " to be a list property");
                }
                // the connection is of type 1xn
                ModelElementList<?> connList = srcElement.read((ListProperty)connProp);
                IModelElement newElement = connList.insert();
                setModelProperty(newElement, ((ModelPath.PropertySegment)this.endpoint2Path.head()).getPropertyName(), endpoint2Value);
                DiagramConnectionPart newConn = getConnectionPart(srcElement, newElement);
                if (newConn == null) {
                	newConn = createNewConnectionPart(newElement, null);
                }                
                return newConn;
            }
        } 
        return null;
    }
    
    public DiagramConnectionPart createNewConnectionPart(IModelElement connElement, IModelElement srcNodeElement)
    {
        DiagramConnectionPart connPart = new DiagramConnectionPart(this.bindingDef, this.endpoint1Path, this.endpoint2Path);
        addConnectionPart(srcNodeElement, connPart);
        connPart.init(this, connElement, this.connectionDef, 
                Collections.<String,String>emptyMap());
        connPart.addListener(this.connPartListener);
        return connPart;
    }
        
    public void showAllConnectionParts(DiagramNodeTemplate nodeTemplate)
    {
    	List<DiagramConnectionPart> connParts = getDiagramConnections(null);
    	for (DiagramConnectionPart connPart : connParts)
    	{
    		IModelElement endpt1 = connPart.getEndpoint1();
    		IModelElement endpt2 = connPart.getEndpoint2();
    		DiagramNodePart nodePart1 = this.diagramEditor.getDiagramNodePart(endpt1);
    		if (nodePart1 != null && nodePart1.getDiagramNodeTemplate() == nodeTemplate)
    		{
    			notifyConnectionAdd(new DiagramConnectionEvent(connPart));
    		}
    		else
    		{
    			DiagramNodePart nodePart2 = this.diagramEditor.getDiagramNodePart(endpt2);
        		if (nodePart2 != null && nodePart2.getDiagramNodeTemplate() == nodeTemplate)
        		{
        			notifyConnectionAdd(new DiagramConnectionEvent(connPart));
        		}    			
    		}
    	}
    }
    
    protected void setModelProperty(final IModelElement modelElement, 
                                    String propertyName, Object value)
    {
        if (propertyName != null)
        {
            final ModelElementType type = modelElement.type();
            final ModelProperty property = type.property( propertyName );
            if( property == null )
            {
                throw new RuntimeException( "Could not find property " + propertyName + " in " + type.getQualifiedName() );
            }
            if (!(property instanceof ValueProperty))
            {
                throw new RuntimeException( "Property " + propertyName + " not a ValueProperty");
            }
                    
            modelElement.write(property, value);
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
            f = function.getContent();
        }
        
        if( f != null )
        {
            
            f = FailSafeFunction.create( f, Literal.create( String.class ) );
            fr = f.evaluate( new ModelElementFunctionContext( nodePart.getLocalModelElement(), ls ));
        }
        return fr;
    }
    
    protected void handleConnectionListChange(IModelElement connListParent, ListProperty listProperty)
    {
    	ModelElementList<?> newList = connListParent.read(listProperty);
        List<DiagramConnectionPart> connParts = getDiagramConnections(connListParent);
        List<IModelElement> oldList = new ArrayList<IModelElement>(connParts.size());
        for (DiagramConnectionPart connPart : connParts)
        {
            oldList.add(connPart.getLocalModelElement());
        }
        List<IModelElement> deletedConns = ListUtil.ListDiff(oldList, newList);
        List<IModelElement> newConns = ListUtil.ListDiff(newList, oldList);
        
        // Handle deleted connections
        for (IModelElement deletedConn : deletedConns)
        {
            DiagramConnectionPart connPart = getConnectionPart(connListParent, deletedConn);
            if (connPart != null)
            {
                notifyConnectionDelete(new DiagramConnectionEvent(connPart));
                disposeConnectionPart(connPart);
            }
        }
        // Handle newly created connections
        for (IModelElement newConn : newConns)
        {                    
            DiagramConnectionPart connPart = createNewConnectionPart(newConn, connListParent);
            if (connPart.getEndpoint1() != null && connPart.getEndpoint2() != null)
            {
                notifyConnectionAdd(new DiagramConnectionEvent(connPart));
            }
            else
            {
                disposeConnectionPart(connPart);
            }
        }
        
    }
    protected void handleModelPropertyChange(final PropertyEvent event)
    {
        final IModelElement element = event.element();
        final ListProperty property = (ListProperty)event.property();
        ModelElementList<?> newList = element.read(property);
        
        if (property == this.connListProperty)
        {
        	handleConnectionListChange(element, property);
        }  
        else if (property == this.modelProperty)
        {
            // 1xn type connection and we are dealing with events on connection list parent
            List<DiagramConnectionPart> connParts = getDiagramConnections(null);
            List<IModelElement> oldList = new ArrayList<IModelElement>();
            Set<IModelElement> oldConnParents = new HashSet<IModelElement>(); 
            for (DiagramConnectionPart connPart : connParts)
            {
                IModelElement connElement = connPart.getLocalModelElement();                
                IModelElement connParentElement = (IModelElement)connElement.parent().parent();
                if (!(oldConnParents.contains(connParentElement)))
                {
                    oldConnParents.add(connParentElement);
                }                
            }
            Iterator <IModelElement> it = oldConnParents.iterator();
            while(it.hasNext())
            {
                oldList.add(it.next());
            }
            List<IModelElement> deletedConnParents = ListUtil.ListDiff(oldList, newList);
            List<IModelElement> newConnParents = ListUtil.ListDiff(newList, oldList);
            
            // connection parents are deleted and we need to dispose any connections associated with them
            List<DiagramConnectionPart> connPartsCopy = new ArrayList<DiagramConnectionPart>(connParts.size());
            connPartsCopy.addAll(connParts);
            for (DiagramConnectionPart connPart : connPartsCopy)
            {
                IModelElement connElement = connPart.getLocalModelElement();                
                IModelElement connParentElement = (IModelElement)connElement.parent().parent();
                if (deletedConnParents.contains(connParentElement))
                {
                    notifyConnectionDelete(new DiagramConnectionEvent(connPart));
                    disposeConnectionPart(connPart);                        
                }
            }            
            // new connection parents are added and we need to listen on their connection list property
            ModelPath.PropertySegment head = (ModelPath.PropertySegment)this.originalEndpoint2Path.head();
            for (IModelElement newConnParent : newConnParents)
            {
                newConnParent.attach(this.modelPropertyListener, head.getPropertyName());
                handleConnectionListChange(newConnParent, this.connListProperty);
            }

        }
    }
    
    public void addConnectionPart(IModelElement srcNodeModel, DiagramConnectionPart connPart)
    {
        this.diagramConnections.add(connPart);
    }
    
    public void disposeConnectionPart(DiagramConnectionPart connPart)
    {
    	connPart.dispose();
        this.diagramConnections.remove(connPart);
    }
    
    protected DiagramConnectionPart getConnectionPart(IModelElement srcNodeModel, IModelElement connModel)
    {
        List<DiagramConnectionPart> connParts = getDiagramConnections(srcNodeModel);
        for (DiagramConnectionPart connPart : connParts)
        {
            if (connPart.getLocalModelElement().equals(connModel))
            {
                return connPart;
            }
        }
        return null;
    }
    
    public void dispose()
    {
        removeModelListener();
        List<DiagramConnectionPart> connParts = getDiagramConnections(null);
        for (DiagramConnectionPart connPart : connParts)
        {
        	notifyConnectionDelete(new DiagramConnectionEvent(connPart));
            connPart.dispose();
        }
    }
    
    @Override
    public void render(SapphireRenderingContext context)
    {
        throw new UnsupportedOperationException();
    }    
    
    protected void notifyConnectionUpdate(DiagramConnectionEvent event)
    {
        for( DiagramConnectionTemplateListener listener : this.templateListeners )
        {
            listener.handleConnectionUpdate(event);
        }        
    }

    protected void notifyConnectionEndpointUpdate(DiagramConnectionEvent event)
    {
        for( DiagramConnectionTemplateListener listener : this.templateListeners )
        {
            listener.handleConnectionEndpointUpdate(event);
        }        
    }
    
    public void notifyConnectionAdd(DiagramConnectionEvent event)
    {
        for( DiagramConnectionTemplateListener listener : this.templateListeners )
        {
            listener.handleConnectionAdd(event);
        }        
    }

    protected void notifyConnectionDelete(DiagramConnectionEvent event)
    {
        for( DiagramConnectionTemplateListener listener : this.templateListeners )
        {
            listener.handleConnectionDelete(event);
        }        
    }
    
    protected void notifyAddBendpoint(DiagramConnectionEvent event)
    {
        for( DiagramConnectionTemplateListener listener : this.templateListeners )
        {
            listener.handleAddBendpoint(event);
        }        
    }

    protected void notifyRemoveBendpoint(DiagramConnectionEvent event)
    {
        for( DiagramConnectionTemplateListener listener : this.templateListeners )
        {
            listener.handleRemoveBendpoint(event);
        }        
    }
    
    protected void notifyMoveBendpoint(DiagramConnectionEvent event)
    {
        for( DiagramConnectionTemplateListener listener : this.templateListeners )
        {
            listener.handleMoveBendpoint(event);
        }        
    }
    
    protected void notifyResetBendpoints(DiagramConnectionEvent event)
    {
        for( DiagramConnectionTemplateListener listener : this.templateListeners )
        {
            listener.handleResetBendpoints(event);
        }        
    }

    protected void notifyMoveLabel(DiagramConnectionEvent event)
    {
        for( DiagramConnectionTemplateListener listener : this.templateListeners )
        {
            listener.handleMoveLabel(event);
        }        
    }

    // ******************************************************************
    // Inner classes
    //*******************************************************************
    
    public static enum ConnectionType
    {
        OneToOne,
        OneToMany
    }
    
    protected class ConnectionPartListener extends SapphireDiagramPartListener 
    {
        @Override
        public void handleConnectionUpdateEvent(final DiagramConnectionEvent event)
        {
            notifyConnectionUpdate(event);
        }  
        @Override
        public void handleConnectionEndpointEvent(final DiagramConnectionEvent event)
        {
            notifyConnectionEndpointUpdate(event);
        }            
        @Override
        public void handleConnectionAddBendpointEvent(final DiagramConnectionEvent event)
        {
            notifyAddBendpoint(event);
        }            
        @Override
        public void handleConnectionRemoveBendpointEvent(final DiagramConnectionEvent event)
        {
            notifyRemoveBendpoint(event);
        }            
        @Override
        public void handleConnectionMoveBendpointEvent(final DiagramConnectionEvent event)
        {
            notifyMoveBendpoint(event);
        }            
        @Override
        public void handleConnectionResetBendpointsEvent(final DiagramConnectionEvent event)
        {
            notifyResetBendpoints(event);
        }            
        @Override
        public void handleConnectionMoveLabelEvent(final DiagramConnectionEvent event)
        {
            notifyMoveLabel(event);
        }            
        
    };
}
