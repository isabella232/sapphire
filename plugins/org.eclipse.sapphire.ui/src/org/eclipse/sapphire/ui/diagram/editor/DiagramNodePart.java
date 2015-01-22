/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - [342897] Integrate with properties view
 *    Konstantin Komissarchik - [342775] Support EL in MasterDetailsTreeNodeDef.ImagePath
 *    Ling Hao - [44319] Image specification for diagram parts inconsistent with the rest of sdef 
 *    Konstantin Komissarchik - [378756] Convert ModelElementListener and ModelPropertyListener to common listener infrastructure
 *    Ling Hao - [383924]  Flexible diagram node shapes
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.editor;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.ui.Bounds;
import org.eclipse.sapphire.ui.PartValidationEvent;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.diagram.def.IDiagramNodeDef;
import org.eclipse.sapphire.ui.diagram.shape.def.ImageDef;
import org.eclipse.sapphire.ui.diagram.shape.def.RectangleDef;
import org.eclipse.sapphire.ui.diagram.shape.def.ShapeDef;
import org.eclipse.sapphire.ui.diagram.shape.def.TextDef;
import org.eclipse.sapphire.ui.forms.PropertiesViewContributionManager;
import org.eclipse.sapphire.ui.forms.PropertiesViewContributionPart;
import org.eclipse.sapphire.ui.forms.PropertiesViewContributorPart;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class DiagramNodePart 

    extends SapphirePart
    implements PropertiesViewContributorPart
    
{
	
	private static final String DEFAULT_ACTION_ID = "Sapphire.Diagram.Node.Default";
	
	private DiagramNodeTemplate nodeTemplate;
	private IDiagramNodeDef definition;
	private Element modelElement;
	private FunctionResult idFunctionResult;
	private SapphireAction defaultAction;
	private SapphireActionHandler defaultActionHandler;
	private PropertiesViewContributionManager propertiesViewContributionManager; 
	private DiagramNodeBounds nodeBounds = new DiagramNodeBounds();
	private ShapePart shapePart;
	
    private static final String NODE_ID_SEPARATOR = "#";

	@Override
    protected void init()
    {
        super.init();
        this.nodeTemplate = (DiagramNodeTemplate) parent();
        this.definition = (IDiagramNodeDef)super.definition;
        this.modelElement = getModelElement();
        
        this.idFunctionResult = initExpression
        ( 
            this.definition.getInstanceId().content(), 
            String.class,
            null,
            new Runnable()
            {
                public void run()
                {
                }
            }
        );
                 
        // create shape part
        
        createShapePart();
        if (getPropertiesViewContribution() != null)
        {
        	getPropertiesViewContribution().attach
            (
                new FilteredListener<PartValidationEvent>()
                {
                    @Override
                    protected void handleTypedEvent( PartValidationEvent event )
                    {
                    	refreshValidation();
                    }
                }
            );        	
        }        
    }
    
    public DiagramNodeTemplate getDiagramNodeTemplate()
    {
        return this.nodeTemplate;
    }
    
    @Override
    public Element getLocalModelElement()
    {
        return this.modelElement;
    }    
         
    public ShapePart getShapePart()
    {
    	return this.shapePart;
    }
    
    public List<TextPart> getContainedTextParts()
    {    	
    	ShapePart shapePart = getShapePart();
    	if (shapePart instanceof TextPart)
    	{
    		return Collections.singletonList((TextPart)shapePart);
    	}
    	else
    	{
    		return ShapePart.getContainedShapeParts(shapePart, TextPart.class);
    	}
    }
    
    public List<ImagePart> getContainedImageParts()
    {    	
    	ShapePart shapePart = getShapePart();
    	if (shapePart instanceof ImagePart)
    	{
    		return Collections.singletonList((ImagePart)shapePart);
    	}
    	else
    	{
    		return ShapePart.getContainedShapeParts(shapePart, ImagePart.class);
    	}
    }

    @Override
    public Set<String> getActionContexts()
    {
        Set<String> contextSet = new HashSet<String>();
        contextSet.add(SapphireActionSystem.CONTEXT_DIAGRAM_NODE);
        contextSet.add(SapphireActionSystem.CONTEXT_DIAGRAM_NODE_HIDDEN);
        return contextSet;    	
    }
        
    public SapphireActionHandler getDefaultActionHandler()
    {        
    	if (this.defaultAction == null)
    	{
            // Default Action handler
            this.defaultAction = getAction(DEFAULT_ACTION_ID);
            this.defaultActionHandler = this.defaultAction.getFirstActiveHandler();    		
    	}
        return this.defaultActionHandler;
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        if (this.idFunctionResult != null)
        {
            this.idFunctionResult.dispose();
        }
        this.shapePart.dispose();
        
    }
    
    @Override
    protected Status computeValidation()
    {
        final Status.CompositeStatusFactory factory = Status.factoryForComposite();
        factory.merge(getShapePart().validation());
        if (getPropertiesViewContribution() != null)
        	factory.merge(getPropertiesViewContribution().validation());
        return factory.create();
    }
    
        
//    private void notifyShapeUpdate(ShapePart shapePart)
//    {
//		DiagramShapeEvent event = new DiagramShapeEvent(this, shapePart);
//    	event.setShapeEventType(ShapeEventType.ShapeUpdate);
//    	this.broadcast(event);
//    }
//
//    private void notifyTextChange(ShapePart shapePart)
//    {
//		DiagramShapeEvent event = new DiagramShapeEvent(this, shapePart);
//    	event.setShapeEventType(ShapeEventType.TextChange);
//    	this.broadcast(event);
//    }
//    
//    private void notifyShapeVisibility(ShapePart shapePart)
//    {
//		DiagramShapeEvent event = new DiagramShapeEvent(this, shapePart);
//    	event.setShapeEventType(ShapeEventType.ShapeVisibilityUpdate);
//    	this.broadcast(event);
//    }
//    
//    private void notifyShapeAdd(ShapePart shapePart)
//    {
//		DiagramShapeEvent event = new DiagramShapeEvent(this, shapePart);
//    	event.setShapeEventType(ShapeEventType.ShapeAdd);
//    	this.broadcast(event);
//    }
//    
//    private void notifyShapeDelete(ShapePart shapePart)
//    {    	
//		DiagramShapeEvent event = new DiagramShapeEvent(this, shapePart);
//    	event.setShapeEventType(ShapeEventType.ShapeDelete);
//    	this.broadcast(event);
//    }
//    
//    private void notifyShapeReorder(ShapeFactoryPart shapeFactory)
//    {
//		DiagramShapeEvent event = new DiagramShapeEvent(this, shapeFactory);
//    	event.setShapeEventType(ShapeEventType.ShapeReorder);
//    	this.broadcast(event);
//    }
    
    public String getNodeTypeId()
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
        StringBuffer buffer = new StringBuffer(getNodeTypeId());
        buffer.append(NODE_ID_SEPARATOR);
        String instanceId = getInstanceId();
        if (instanceId != null && instanceId.length() > 0)
        {
            buffer.append(getInstanceId());
            buffer.append(NODE_ID_SEPARATOR);
        }
        List<DiagramNodePart> nodeParts = getDiagramNodeTemplate().getDiagramNodes();            
        int index = nodeParts.indexOf(this);
        buffer.append(index);
        
        return buffer.toString();    	
    }
	
	public boolean canResizeShape()
	{
		return this.definition.isResizable().content();
	}
	
	public DiagramNodeBounds getNodeBounds()
	{
		DiagramNodeBounds bounds = new DiagramNodeBounds(this.nodeBounds);
		if (bounds.getWidth() < 0 && this.definition.getWidth().content() != null )
		{
			bounds.setWidth(this.definition.getWidth().content());
		}
		if (bounds.getHeight() < 0 && this.definition.getHeight().content() != null)
		{
			bounds.setHeight(this.definition.getHeight().content());
		}
		return bounds;
	}
	
	public void setNodeBounds(int x, int y)
	{
		setNodeBounds(new DiagramNodeBounds(x, y, -1, -1, false, false));
	}
	
	public void setNodeBounds(int x, int y, int width, int height)
	{
		setNodeBounds(new DiagramNodeBounds(x, y, width, height, false, false));
	}

	public void setNodeBounds(int x, int y, boolean autoLayout, boolean defaultPosition)
	{
		setNodeBounds(new DiagramNodeBounds(x, y, -1, -1, autoLayout, defaultPosition));
	}
	
	public void setNodeBounds(Bounds bounds)
	{
		DiagramNodeBounds nodeBounds = new DiagramNodeBounds(bounds);
		setNodeBounds(nodeBounds);
	}
	
	public void setNodeBounds(DiagramNodeBounds bounds)
	{
		// TODO handle node resizing events - rename move?
		if (!this.nodeBounds.equals(bounds))
		{
			this.nodeBounds.setX(bounds.getX());
			this.nodeBounds.setY(bounds.getY());
			this.nodeBounds.setWidth(bounds.getWidth());
			this.nodeBounds.setHeight(bounds.getHeight());
			this.nodeBounds.setAutoLayout(bounds.isAutoLayout());
			this.nodeBounds.setDefaultPosition(bounds.isDefaultPosition());
			notifyNodeMove();
		}			
	}
	
	private void notifyNodeMove()
	{
		DiagramNodeMoveEvent event = new DiagramNodeMoveEvent(this);
		this.broadcast(event);
	}
	
    public PropertiesViewContributionPart getPropertiesViewContribution()
    {
        if( this.propertiesViewContributionManager == null )
        {
            this.propertiesViewContributionManager = new PropertiesViewContributionManager( this, getLocalModelElement() );
        }
        
        return this.propertiesViewContributionManager.getPropertiesViewContribution();
    }
        
    private void createShapePart()
    {
    	ShapeDef shape = this.definition.getShape().content();
    	if (shape instanceof TextDef)
    	{
	        this.shapePart = new TextPart();
    	}
    	else if (shape instanceof ImageDef)
    	{
    		this.shapePart = new ImagePart();
    	}
    	else if (shape instanceof RectangleDef)
    	{
    		this.shapePart = new RectanglePart();
    	}
    	
    	if (definition.getSelectionPresentation() != null)
    	{
    		this.shapePart.setSelectionPresentation(definition.getSelectionPresentation());
    	}
        this.shapePart.init(this, this.modelElement, shape, Collections.<String,String>emptyMap());
        this.shapePart.initialize();
        this.shapePart.attach
        (
        	new FilteredListener<PartValidationEvent>()
	        {
	            @Override
	            protected void handleTypedEvent( PartValidationEvent event )
	            {
	            	refreshValidation();
	            }
	        }
        );

    }
}
