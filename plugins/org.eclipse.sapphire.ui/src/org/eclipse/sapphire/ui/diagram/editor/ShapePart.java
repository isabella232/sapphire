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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.PartValidationEvent;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.diagram.shape.def.LayoutConstraintDef;
import org.eclipse.sapphire.ui.diagram.shape.def.SelectionPresentation;
import org.eclipse.sapphire.ui.diagram.shape.def.ShapeDef;
import org.eclipse.sapphire.ui.forms.PropertiesViewContributionManager;
import org.eclipse.sapphire.ui.forms.PropertiesViewContributionPart;
import org.eclipse.sapphire.ui.forms.PropertiesViewContributorPart;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class ShapePart extends SapphirePart implements PropertiesViewContributorPart
{
	private ShapeDef shapeDef;
	private Element modelElement;
	private boolean isActive = false;
	private boolean isEditable = false;
	private PropertiesViewContributionManager propertiesViewContributionManager; 
	private SelectionPresentation selectionPresentation = null;

	@Override
    protected void init()
    {
        super.init();
        this.shapeDef = (ShapeDef)super.definition;
        this.modelElement = getModelElement();
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

	public LayoutConstraintDef getLayoutConstraint()
	{
		return this.shapeDef.getSequenceLayoutConstraint();
	}
	
	public boolean isActive()
	{
		return this.isActive;
	}
	
	public void setActive(boolean isActive)
	{
		this.isActive = isActive;
	}

	public boolean isEditable()
	{
		return this.isEditable;
	}
	
	public void setEditable(boolean editable)
	{
		this.isEditable = editable;
	}
		
    @Override
    public Set<String> getActionContexts()
    {
        Set<String> contextSet = new HashSet<String>();
    	contextSet.add(SapphireActionSystem.CONTEXT_DIAGRAM_NODE_SHAPE);
    	contextSet.add(SapphireActionSystem.CONTEXT_DIAGRAM_SHAPE_HIDDEN);
        return contextSet;    	
    }
	
    @Override
    public Element getLocalModelElement()
    {
        return this.modelElement;
    }    
    
    public List<ShapePart> getActiveChildren()
    {
    	return Collections.emptyList();
    }
    
    public List<ShapePart> getChildren()
    {
    	return Collections.emptyList();
    }
    
    @SuppressWarnings( "unchecked" )
    public static <T extends ShapePart> List<T> getContainedShapeParts(ShapePart shapePart, Class<T> shapeType)
    {
    	List<T> containedShapeParts = new ArrayList<T>();
		for (ShapePart childPart : shapePart.getChildren())
		{
			if (shapeType.isAssignableFrom(childPart.getClass()))
			{
				containedShapeParts.add((T) childPart);
			}
			else if (childPart instanceof ContainerShapePart || childPart instanceof ShapeFactoryPart)
			{
				containedShapeParts.addAll(getContainedShapeParts(childPart, shapeType));
			}
		}
    	
    	return containedShapeParts;
    }

    public DiagramNodePart getNodePart() 
	{
		DiagramNodePart nodePart = null;
		ISapphirePart part = this;
		while (part != null) {
			if (part instanceof DiagramNodePart) {
				nodePart = (DiagramNodePart)part;
				break;
			}
			part = part.parent();
		}
		return nodePart;
	}
	
    public PropertiesViewContributionPart getPropertiesViewContribution()
    {
        if( this.propertiesViewContributionManager == null )
        {
            this.propertiesViewContributionManager = new PropertiesViewContributionManager( this, getLocalModelElement() );
        }
        
        return this.propertiesViewContributionManager.getPropertiesViewContribution();
    }
    
    public void setSelectionPresentation(SelectionPresentation selectionPresentation)
    {
    	this.selectionPresentation = selectionPresentation;
    }
    
    public SelectionPresentation getSelectionPresentation()
    {
    	return this.selectionPresentation;
    }
        
}
