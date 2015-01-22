/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - [341856] NPE when a diagram connection doesn't define a label
 *    Konstantin Komissarchik - [342897] Integrate with properties view
 *    Konstantin Komissarchik - [342775] Support EL in MasterDetailsTreeNodeDef.ImagePath
 *    Konstantin Komissarchik - [378756] Convert ModelElementListener and ModelPropertyListener to common listener infrastructure
 *    Ling Hao - [383924] Flexible diagram node shapes
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.PropertyEvent;
import org.eclipse.sapphire.ReferenceValue;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.ModelPath.ParentElementSegment;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.services.ReferenceService;
import org.eclipse.sapphire.ui.Point;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.diagram.ConnectionBendpointsEvent;
import org.eclipse.sapphire.ui.diagram.ConnectionEndpointsEvent;
import org.eclipse.sapphire.ui.diagram.ConnectionLabelEvent;
import org.eclipse.sapphire.ui.diagram.DiagramConnectionPart;
import org.eclipse.sapphire.ui.diagram.def.IDiagramConnectionDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramExplicitConnectionBindingDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramLabelDef;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodePart;
import org.eclipse.sapphire.ui.diagram.editor.FunctionUtil;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;
import org.eclipse.sapphire.ui.diagram.internal.DiagramConnectionTemplate.ConnectionType;
import org.eclipse.sapphire.ui.forms.PropertiesViewContributionManager;
import org.eclipse.sapphire.ui.forms.PropertiesViewContributionPart;
import org.eclipse.sapphire.ui.forms.PropertiesViewContributorPart;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class StandardDiagramConnectionPart 

    extends DiagramConnectionPart
    implements PropertiesViewContributorPart
    
{
	protected DiagramConnectionTemplate connectionTemplate;
	protected IDiagramExplicitConnectionBindingDef bindingDef;
	protected Element modelElement;
	private ModelPath endpoint1Path;
	private ModelPath endpoint2Path;
	private Element srcNodeModel;
	private Element targetNodeModel;
	private ReferenceValue<?, ?> endpointReferenceValue1;
	private ReferenceValue<?, ?> endpointReferenceValue2;
	private Listener referenceServiceListener1;
	private Listener referenceServiceListener2;
	private PropertyDef endpoint1Property;
	private PropertyDef endpoint2Property;
	protected FunctionResult labelFunctionResult;
	protected Value<?> labelProperty;
	protected FunctionResult idFunctionResult;
	protected Listener modelPropertyListener;
	private PropertiesViewContributionManager propertiesViewContributionManager;
	private List<Point> bendPoints = new ArrayList<Point>();
	private Point labelPosition;
	
	protected static final String CONNECTION_ID_SEPARATOR = "&";
	
	public StandardDiagramConnectionPart() {}
	
	public StandardDiagramConnectionPart(IDiagramExplicitConnectionBindingDef bindingDef, ModelPath endpoint1Path, ModelPath endpoint2Path)
	{				
		this.bindingDef = bindingDef;
		this.endpoint1Path = endpoint1Path;
		this.endpoint2Path = endpoint2Path;
	}
	
	protected void initLabelId()
	{
    	this.connectionTemplate = (DiagramConnectionTemplate) parent();
        
        this.modelElement = getModelElement();
        
        final IDiagramLabelDef labelDef = this.bindingDef.getLabel().content();
        if (labelDef != null)
        {
            this.labelFunctionResult = initExpression
            ( 
                labelDef.getText().content(), 
                String.class,
                null,
                new Runnable()
                {
                    public void run()
                    {
                        refreshLabel();
                    }
                }
            );
        
            this.labelProperty = FunctionUtil.getFunctionProperty( this.modelElement, this.labelFunctionResult );
        }
        
        this.idFunctionResult = initExpression
        ( 
            this.bindingDef.getInstanceId().content(), 
            String.class,
            null,
            new Runnable()
            {
                public void run()
                {
                }
            }
        ); 
        
    }
    
    @Override
    protected void init()
    {
        initLabelId();
        
        this.srcNodeModel = resolveEndpoint(this.modelElement, this.endpoint1Path);
        
        this.targetNodeModel = resolveEndpoint(this.modelElement, this.endpoint2Path);
        
        this.endpoint1Property = this.modelElement.property(this.endpoint1Path).definition();
        this.endpoint2Property = this.modelElement.property(this.endpoint2Path).definition();
        
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
        addReferenceServiceListeners();
    }
    
    public DiagramConnectionTemplate getDiagramConnectionTemplate()
    {
        return this.connectionTemplate;
    }
    
    @Override
    public Element getLocalModelElement()
    {
        return this.modelElement;
    }    
    
    public Element getEndpoint1()
    {
        return this.srcNodeModel;
    }
    
    public Element getEndpoint2()
    {
        return this.targetNodeModel;
    }

    public boolean canEditLabel()
    {
        return this.labelProperty != null;
    }
    
    public boolean removable()
    {
    	return true;
    }
    
    public void remove()
    {
        final Element element = getLocalModelElement();
        final ElementList<?> list = (ElementList<?>) element.parent();
        list.remove(element);  
        pruneListParentIfNecessary(list);
    }
    
    private void pruneListParentIfNecessary(ElementList<?> list)
    {
        // For 1->n type connections, if the target node list is empty, we need to remove the surrounding 
        // element. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=427710
        if (list.isEmpty() && this.connectionTemplate.getConnectionType() == ConnectionType.OneToMany)
        {
        	if (list.element().parent() instanceof ElementList<?>)
        	{
        		ElementList<?> grandParent = (ElementList<?>)list.element().parent();
        		grandParent.remove(list.element());
        	}
        }
    	
    }
    
    public String getLabel()
    {
        String label = null;
        
        if( this.labelFunctionResult != null )
        {
            label = (String) this.labelFunctionResult.value();
        }
        
        return label;
    }
    
    public void setLabel(String newValue)
    {
        if (this.labelProperty != null)
        {
            this.labelProperty.write( newValue, true );
        }        
    }
    
    public IDiagramConnectionDef getConnectionDef()
    {
        return (IDiagramConnectionDef) definition();
    }
    
    public void refreshLabel()
    {
        notifyUpdateLabel();
    }
    
    public String getConnectionTypeId()
    {
        return this.definition.getId().content();
    }
    
    public String getInstanceId()
    {
        String id = null;
        
        if( this.idFunctionResult != null )
        {
            id = (String) this.idFunctionResult.value();
        }
                
        return id;
    }
    
    public String getId()
    {
        StringBuffer buffer = new StringBuffer(getConnectionTypeId());
        buffer.append(CONNECTION_ID_SEPARATOR);
        String instanceId = getInstanceId();
        if (instanceId != null && instanceId.length() > 0)
        {
            buffer.append(getInstanceId());
            buffer.append(CONNECTION_ID_SEPARATOR);
        }
        List<StandardDiagramConnectionPart> connParts = getDiagramConnectionTemplate().getDiagramConnections(null);
        int index = connParts.indexOf(this);
        buffer.append(index);                
        return buffer.toString();            	
    }
    
    @Override
    public Set<String> getActionContexts()
    {
        Set<String> contextSet = new HashSet<String>();
        contextSet.add(SapphireActionSystem.CONTEXT_DIAGRAM_CONNECTION);
        contextSet.add(SapphireActionSystem.CONTEXT_DIAGRAM_CONNECTION_HIDDEN);
        return contextSet;    	
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        if (this.labelFunctionResult != null)
        {
            this.labelFunctionResult.dispose();
        }
        if (this.idFunctionResult != null)
        {
            this.idFunctionResult.dispose();
        }
        removeModelListener();
    }
    

    protected void resetEndpoint1(DiagramNodePart newSrcNode)
    {
    	this.srcNodeModel = newSrcNode.getLocalModelElement();
    	String endpoint1Value = this.connectionTemplate.getSerializedEndpoint1(newSrcNode);
    	this.connectionTemplate.setSerializedEndpoint1(this.modelElement, endpoint1Value);
    }
    
    protected void resetEndpoint2(DiagramNodePart newTargetNode)
    {
    	this.targetNodeModel = newTargetNode.getLocalModelElement();
    	String endpoint2Value = this.connectionTemplate.getSerializedEndpoint2(newTargetNode);
    	this.connectionTemplate.setSerializedEndpoint2(this.modelElement, endpoint2Value);
    }

    protected Element resolveEndpoint(Element modelElement, ModelPath endpointPath)
    {
        if (endpointPath.length() == 1)
        {
            String propertyName = ((ModelPath.PropertySegment)endpointPath.head()).getPropertyName();
            PropertyDef modelProperty = resolve(modelElement, propertyName);
            if (!(modelProperty instanceof ValueProperty))
            {
                throw new RuntimeException( "Property " + propertyName + " not a ValueProperty");
            }
            ValueProperty property = (ValueProperty)modelProperty;
            Value<?> valObj = modelElement.property(property);
            if (!(valObj instanceof ReferenceValue))
            {
                throw new RuntimeException( "Property " + propertyName + " value not a reference");
            }
            ReferenceValue<?,?> refVal = (ReferenceValue<?,?>)valObj;
            Object targetObj = refVal.target();
            if (targetObj == null)
            {
                if (refVal.text() != null)
                {
                    SapphireDiagramEditorPagePart diagramEditorPart = this.getDiagramConnectionTemplate().getDiagramEditor();
                    DiagramNodePart targetNode = diagramEditorPart.getNode(refVal.text());
                    if (targetNode != null)
                    {
                        targetObj = targetNode.getLocalModelElement();
                    }
                }
            }
            return (Element)targetObj;
        }
        else
        {
            ModelPath.Segment head = endpointPath.head();
            if( head instanceof ParentElementSegment )
            {
                final Property parent = modelElement.parent();                
                if( parent == null )
                {
                    throw new RuntimeException("Invalid model path: " + endpointPath);
                }
                return resolveEndpoint(parent.element(), endpointPath.tail());                
            }
            else
            {
                throw new RuntimeException("Invalid model path: " + endpointPath);
            }
        }
    }
    
    protected ReferenceValue<?, ?> resolveEndpointReferenceValue(Element modelElement, ModelPath endpointPath)
    {
        if (endpointPath.length() == 1)
        {
            String propertyName = ((ModelPath.PropertySegment)endpointPath.head()).getPropertyName();
            PropertyDef modelProperty = resolve(modelElement, propertyName);
            if (!(modelProperty instanceof ValueProperty))
            {
                throw new RuntimeException( "Property " + propertyName + " not a ValueProperty");
            }
            ValueProperty property = (ValueProperty)modelProperty;
            Value<?> valObj = modelElement.property(property);
            if (!(valObj instanceof ReferenceValue))
            {
                throw new RuntimeException( "Property " + propertyName + " value not a reference");
            }
            ReferenceValue<?,?> refVal = (ReferenceValue<?,?>)valObj;
            return refVal;
        }
        else
        {
            ModelPath.Segment head = endpointPath.head();
            if( head instanceof ParentElementSegment )
            {
                final Property parent = modelElement.parent();                
                if( parent == null )
                {
                    throw new RuntimeException("Invalid model path: " + endpointPath);
                }
                return resolveEndpointReferenceValue(parent.element(), endpointPath.tail());                
            }
            else
            {
                throw new RuntimeException("Invalid model path: " + endpointPath);
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
        
            modelElement.property( (ValueProperty) property ).write( value, true );
        }        
    }
    
    protected void setModelProperty(Element modelElement, ModelPath propertyPath, Object value)
    {
        if (propertyPath.length() == 1)
        {
            String propertyName = ((ModelPath.PropertySegment)propertyPath.head()).getPropertyName();
            setModelProperty(modelElement, propertyName, value);
        }
        else
        {
            if (propertyPath.head() instanceof ModelPath.ParentElementSegment)
            {
                final Property parent = modelElement.parent();
                setModelProperty(parent.element(), propertyPath.tail(), value);
            }
        }
    }
    
    public void addModelListener()
    {
        this.modelElement.attach(this.modelPropertyListener, this.endpoint1Path);
        this.modelElement.attach(this.modelPropertyListener, this.endpoint2Path);
    }
    
    public void removeModelListener()
    {
        this.modelElement.detach(this.modelPropertyListener, this.endpoint1Path);
        this.modelElement.detach(this.modelPropertyListener, this.endpoint2Path);
    }
    
    public void addReferenceServiceListeners()
    {
    	this.endpointReferenceValue1 = resolveEndpointReferenceValue(modelElement, this.endpoint1Path);
    	if (this.endpointReferenceValue1 != null)
    	{
    		ReferenceService<?> refService = endpointReferenceValue1.service(ReferenceService.class);
    		this.referenceServiceListener1 = new Listener()
			{
				@Override
				public void handle( Event event )
				{
					handleEndpoint1ReferenceChange();
				}
			};
    		if (refService != null)
    		{
    			refService.attach(this.referenceServiceListener1);
    		}
    	}
    	this.endpointReferenceValue2 = resolveEndpointReferenceValue(modelElement, this.endpoint2Path);
    	if (this.endpointReferenceValue2 != null)
    	{
    		ReferenceService<?> refService = endpointReferenceValue2.service(ReferenceService.class);
    		this.referenceServiceListener2 = new Listener()
			{
				@Override
				public void handle( Event event )
				{
					handleEndpoint2ReferenceChange();
				}
			};
    		if (refService != null)
    		{
    			refService.attach(this.referenceServiceListener2);
    		}
    	}
    }
    
    public StandardDiagramConnectionPart reconnect(DiagramNodePart newSrcNode, DiagramNodePart newTargetNode)
    {
    	/**
    	 * Optimization: no need to reconnect if the connection's endpoint model 
    	 * elements don't change. Connection listens on endpoint's ReferenceService and its
    	 * endpoint model elements are refreshed when the attaching nodes's elements change.
    	 */
    	if (newSrcNode != null && newSrcNode.getLocalModelElement() == getEndpoint1() && 
    			newTargetNode != null && newTargetNode.getLocalModelElement() == getEndpoint2())
    	{
    		return this;
    	}
    	if (newSrcNode != null && newTargetNode != null)
    	{
	        StandardDiagramConnectionPart newConnPart = getDiagramConnectionTemplate().createNewDiagramConnection(newSrcNode, newTargetNode);
	
	        final Element oldConnElement = this.getLocalModelElement();
	        newConnPart.getLocalModelElement().copy(oldConnElement);
	        // Bug 382912 - Reconnecting an existing connection adds a bend point 
	        // After the copy, connection endpoint event is triggered which causes SapphireConnectionRouter
	        // to be called. Since the old connection hasn't been deleted, a default bend point will be added. 
	        //newConnPart.removeAllBendpoints();
	        
	        newConnPart.resetBendpoints(getBendpoints());
			
			if (newSrcNode.getLocalModelElement() != getEndpoint1()) 
			{
				newConnPart.resetEndpoint1(newSrcNode);
			} 
			if (newTargetNode.getLocalModelElement() != getEndpoint2())
			{
				newConnPart.resetEndpoint2(newTargetNode);
			}
	        final ElementList<?> list = (ElementList<?>) oldConnElement.parent();
	        list.remove(oldConnElement);
	        pruneListParentIfNecessary(list);
	        
	        return newConnPart;
    	}
    	return null;
    }
    
    protected void handleModelPropertyChange(final PropertyEvent event)
    {
        final PropertyDef property = event.property().definition();
                
        if (property.name().equals(this.endpoint1Property.name()) || 
                property.name().equals(this.endpoint2Property.name()))
        {
            boolean sourceChange = property.name().equals(this.endpoint1Property.name()) ? true : false;
            if (sourceChange)
            {
            	this.srcNodeModel = resolveEndpoint(this.modelElement, this.endpoint1Path);
            }
            else
            {
            	this.targetNodeModel = resolveEndpoint(this.modelElement, this.endpoint2Path);
            }
            notifyConnectionEndpointUpdate();
        }
    }    
    
    protected void handleEndpoint1ReferenceChange()
    {
		Element newSourceModel = resolveEndpoint(this.modelElement, this.endpoint1Path);
		if (newSourceModel != this.srcNodeModel)
		{
			this.srcNodeModel = newSourceModel;
			notifyConnectionEndpointUpdate(); 
		}    		
    }
    
    protected void handleEndpoint2ReferenceChange()
    {
		Element newTargetModel = resolveEndpoint(this.modelElement, this.endpoint2Path);
		if (newTargetModel != this.targetNodeModel)
		{
			this.targetNodeModel = newTargetModel;
			notifyConnectionEndpointUpdate(); 
		}    		
    }
        
    protected void notifyUpdateLabel()
    {
		ConnectionLabelEvent labelEvent = new ConnectionLabelEvent(this, false);
    	this.broadcast(labelEvent);
    }
    
    protected void notifyConnectionEndpointUpdate()
    {
		ConnectionEndpointsEvent event = new ConnectionEndpointsEvent(this);
    	this.broadcast(event);
    }
    
    protected void notifyAddBendpoint()
    {
    	ConnectionBendpointsEvent event = new ConnectionBendpointsEvent(this);
    	this.broadcast(event);
    }
    
    protected void notifyRemoveBendpoint()
    {
    	ConnectionBendpointsEvent event = new ConnectionBendpointsEvent(this);
    	this.broadcast(event);
    }

    protected void notifyMoveBendpoint()
    {
    	ConnectionBendpointsEvent event = new ConnectionBendpointsEvent(this);
    	this.broadcast(event);
    }

    protected void notifyResetBendpoints()
    {
		ConnectionBendpointsEvent event = new ConnectionBendpointsEvent(this, true);
    	this.broadcast(event);
    }

    protected void notifyMoveConnectionLabel()
    {
		ConnectionLabelEvent labelEvent = new ConnectionLabelEvent(this, true);
    	this.broadcast(labelEvent);
    }
    
    public PropertiesViewContributionPart getPropertiesViewContribution()
    {
        if( this.propertiesViewContributionManager == null )
        {
            this.propertiesViewContributionManager = new PropertiesViewContributionManager( this, getLocalModelElement(), getConnectionDef() );
        }
        
        return this.propertiesViewContributionManager.getPropertiesViewContribution();
    }
	
    public void addBendpoint(int index, int x, int y)
    {
    	this.bendPoints.add(index, new Point(x, y));
    	notifyAddBendpoint();
    }
    
    public void removeBendpoint(int index)
    {
    	this.bendPoints.remove(index);
    	notifyRemoveBendpoint();
    }
    
    public void removeAllBendpoints()
    {
    	this.bendPoints.clear();
    	notifyRemoveBendpoint();
    }
    
    public void updateBendpoint(int index, int x, int y)
    {
    	if (index < this.bendPoints.size())
    	{
    		this.bendPoints.set(index, new Point(x, y));
    	}
    	notifyMoveBendpoint();
    }
        
    public void resetBendpoints(List<Point> bendpoints)
    {
    	
    	boolean changed = false;
    	if (bendpoints.size() != this.bendPoints.size())
    	{
    		changed = true;
    	}
    	else
    	{
			for (int i = 0; i < bendpoints.size(); i++)
			{
				Point newPt = bendpoints.get(i);
				Point oldPt = this.bendPoints.get(i);
				if (newPt.getX() != oldPt.getX() || newPt.getY() != oldPt.getY())
				{
					changed = true;
					break;
				}
			}    		
    	}
    	if (changed)
    	{
    		this.bendPoints.clear();
    		this.bendPoints.addAll(bendpoints);
    		notifyResetBendpoints();
    	}
    }
    
    public List<Point> getBendpoints()
    {
    	List<Point> bendPoints = new ArrayList<Point>();
    	bendPoints.addAll(this.bendPoints);
    	return bendPoints;
    }
    
    public Point getLabelPosition()
    {
    	return this.labelPosition;
    }
    
    public void setLabelPosition(Point newPos)
    {
    	boolean changed = false;
    	
    	if (this.labelPosition == null && newPos != null)
    	{
    		this.labelPosition = new Point(newPos);
    		changed = true;
    	}
    	else if (this.labelPosition != null && newPos == null)
    	{
    		this.labelPosition = null;
    		changed = true;
    	}
    	else if (this.labelPosition != null && newPos != null && !this.labelPosition.equals(newPos))
    	{
    		this.labelPosition.setX(newPos.getX());
    		this.labelPosition.setY(newPos.getY());
   			changed = true;
    	}
    	if (changed)
    	{
    		notifyMoveConnectionLabel();
    	}
    }
}
