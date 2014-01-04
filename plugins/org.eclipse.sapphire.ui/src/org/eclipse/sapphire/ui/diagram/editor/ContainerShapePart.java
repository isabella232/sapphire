/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.ui.PartValidationEvent;
import org.eclipse.sapphire.ui.PartVisibilityEvent;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.diagram.shape.def.ContainerShapeDef;
import org.eclipse.sapphire.ui.diagram.shape.def.ImageDef;
import org.eclipse.sapphire.ui.diagram.shape.def.LineShapeDef;
import org.eclipse.sapphire.ui.diagram.shape.def.RectangleDef;
import org.eclipse.sapphire.ui.diagram.shape.def.ShapeDef;
import org.eclipse.sapphire.ui.diagram.shape.def.ShapeFactoryDef;
import org.eclipse.sapphire.ui.diagram.shape.def.ShapeLayoutDef;
import org.eclipse.sapphire.ui.diagram.shape.def.SpacerDef;
import org.eclipse.sapphire.ui.diagram.shape.def.TextDef;
import org.eclipse.sapphire.ui.diagram.shape.def.ValidationMarkerDef;
import org.eclipse.sapphire.ui.forms.PropertiesViewContributorPart;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class ContainerShapePart extends ShapePart 
{
	private ContainerShapeDef containerShapeDef;
	private Element modelElement;	
	private List<ShapePart> children;
	private ValidationMarkerPart validationMarkerPart;
	private List<ShapeFactoryPart> shapeFactoryParts;

	@Override
    protected void init()
    {
        super.init();
        this.containerShapeDef = (ContainerShapeDef)super.definition;
        this.modelElement = getModelElement();
        
        // create children parts
        this.children = new ArrayList<ShapePart>();
        this.shapeFactoryParts = new ArrayList<ShapeFactoryPart>();
        
        for (ShapeDef shape : this.containerShapeDef.getContent())
        {
        	ShapePart childPart = null;
        	if (shape instanceof TextDef)
        	{
    	        childPart = new TextPart();
        	}
        	else if (shape instanceof ImageDef)
        	{
        		childPart = new ImagePart();
        	}
        	else if (shape instanceof ValidationMarkerDef)
        	{
        		this.validationMarkerPart = new ValidationMarkerPart();
        		childPart = this.validationMarkerPart;
        	}
        	else if (shape instanceof LineShapeDef)
        	{
        		childPart = new LinePart();
        	}
        	else if (shape instanceof RectangleDef)
        	{
        		childPart = new RectanglePart();
        	}
        	else if (shape instanceof ShapeFactoryDef)
        	{
        		childPart = new ShapeFactoryPart();
        		childPart.setActive(true);
        		this.shapeFactoryParts.add((ShapeFactoryPart)childPart);
        	}
        	else if (shape instanceof SpacerDef) 
        	{
        		childPart = new SpacerPart();
        	}
        	if (childPart != null)
        	{
        		childPart.init(this, this.modelElement, shape, Collections.<String,String>emptyMap());
        		childPart.initialize();
        		this.children.add(childPart);
                childPart.attach
                (
                    new FilteredListener<TextChangeEvent>()
                    {
                        @Override
                        protected void handleTypedEvent( TextChangeEvent event )
                        {
                        	broadcast(event);
                        }
                    }
                );
                childPart.attach
                (
                    new FilteredListener<ShapeUpdateEvent>()
                    {
                        @Override
                        protected void handleTypedEvent( ShapeUpdateEvent event )
                        {
                        	broadcast(event);
                        }
                    }
                );
                childPart.attach
                (
                     new FilteredListener<PartVisibilityEvent>()
                     {
                        @Override
                        protected void handleTypedEvent( final PartVisibilityEvent event )
                        {
                        	broadcast(event);
                        }
                     }
                );
                childPart.attach
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
                
                childPart.attach
                (
                     new FilteredListener<ShapeReorderEvent>()
                     {
                        @Override
                        protected void handleTypedEvent( final ShapeReorderEvent event )
                        {
                        	broadcast(event);
                        }
                     }
                );        		
                childPart.attach
                (
                     new FilteredListener<ShapeAddEvent>()
                     {
                        @Override
                        protected void handleTypedEvent( final ShapeAddEvent event )
                        {
                        	broadcast(event);
                        }
                     }
                );
                childPart.attach
                (
                     new FilteredListener<ShapeDeleteEvent>()
                     {
                        @Override
                        protected void handleTypedEvent( final ShapeDeleteEvent event )
                        {
                        	broadcast(event);
                        }
                     }
                );        		
                
        	}
        }       
        refreshValidation();
    }
	
    @Override
    protected Status computeValidation()
    {
        final Status.CompositeStatusFactory factory = Status.factoryForComposite();

        for( SapphirePart child : this.children )
        {
        	if (!(child instanceof ValidationMarkerPart))
        	{
        		factory.merge( child.validation() );
        		if (child instanceof PropertiesViewContributorPart)
        		{
        			if (((PropertiesViewContributorPart)child).getPropertiesViewContribution() != null)
        			{
        				factory.merge(((PropertiesViewContributorPart)child).getPropertiesViewContribution().validation());
        			}
        		}
        	}
        }
        
        return factory.create();
    }
	

	public ShapeLayoutDef getLayout()
	{
		return this.containerShapeDef.getLayout();
	}
	
	@Override
	public List<ShapePart> getChildren()
	{
		return this.children;
	}
		
	public ValidationMarkerPart getValidationMarkerPart()
	{
		return this.validationMarkerPart;
	}
		
	@Override
	public boolean isEditable()
	{
		List<TextPart> textParts = getContainedShapeParts(this, TextPart.class);
		for (TextPart textPart : textParts)
		{
			if (textPart.isEditable())
			{
				return true;
			}
		}
		return false;
	}
	
	public List<ShapeFactoryPart> getShapeFactoryParts()
	{
		List<ShapeFactoryPart> shapeFactories = new ArrayList<ShapeFactoryPart>();
		shapeFactories.addAll(this.shapeFactoryParts);
		for (ShapePart shapePart : getChildren())
		{
			if (shapePart instanceof ContainerShapePart)
			{
				shapeFactories.addAll(((ContainerShapePart)shapePart).getShapeFactoryParts());
			}
		}
		return shapeFactories;		
	}
	
	@Override
    public List<ShapePart> getActiveChildren()
    {
		List<ShapePart> activeChildren = new ArrayList<ShapePart>();
		for (ShapePart child : getChildren())
		{
			if (child.isActive())
			{
				activeChildren.add(child);
			}
			activeChildren.addAll(child.getActiveChildren());
		}
    	return activeChildren;
    }	
	
    @Override
    public void dispose()
    {
        super.dispose();
        for (ShapePart child : getChildren())
        {
        	child.dispose();
        }
        
    }
        
}
