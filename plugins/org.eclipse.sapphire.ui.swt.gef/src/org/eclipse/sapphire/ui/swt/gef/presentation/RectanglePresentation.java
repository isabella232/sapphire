/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.presentation;

import org.eclipse.draw2d.IFigure;
import org.eclipse.sapphire.ui.diagram.editor.RectanglePart;
import org.eclipse.sapphire.ui.diagram.shape.def.BackgroundDef;
import org.eclipse.sapphire.ui.diagram.shape.def.BorderComponent;
import org.eclipse.sapphire.ui.diagram.shape.def.SequenceLayoutConstraintDef;
import org.eclipse.sapphire.ui.diagram.shape.def.SequenceLayoutDef;
import org.eclipse.sapphire.ui.diagram.shape.def.SequenceLayoutOrientation;
import org.eclipse.sapphire.ui.diagram.shape.def.ShapeLayoutDef;
import org.eclipse.sapphire.ui.swt.gef.figures.RectangleFigure;
import org.eclipse.sapphire.ui.swt.gef.layout.SapphireSequenceLayoutConstraint;
import org.eclipse.sapphire.ui.swt.gef.layout.SapphireStackLayoutConstraint;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramResourceCache;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class RectanglePresentation extends ContainerShapePresentation 
{
	public RectanglePresentation(DiagramPresentation parent, RectanglePart rectanglePart, 
								DiagramResourceCache resourceCache)
	{
		super(parent, rectanglePart, resourceCache);
	}

	@Override
	public RectanglePart part()
	{
		return (RectanglePart) super.part();
	}
	
	public BackgroundDef getBackground()
	{
		return part().getBackground();
	}
	
	public int getCornerRadius()
	{
		return part().getCornerRadius();
	}
	
	public BorderComponent getTopBorder() 
	{
		return part().getTopBorder();
	}
	
	public BorderComponent getBottomBorder() 
	{
		return part().getBottomBorder();
	}

	public BorderComponent getLeftBorder() 
	{
		return part().getLeftBorder();
	}
	
	public BorderComponent getRightBorder() 
	{
		return part().getRightBorder();
	}
		
	@Override
	public void render()
	{
		IFigure figure = new RectangleFigure(this, getResourceCache(), getConfigurationManager());
		ShapeLayoutDef layoutDef = getLayout();
		for (ShapePresentation childShapePresentation : getChildren())
		{
			if (!(childShapePresentation instanceof ContainerShapePresentation) && 
				!childShapePresentation.part().isActive()) {
				childShapePresentation.render();				
				IFigure childFigure = childShapePresentation.getFigure();
				if (childFigure != null)
				{
					Object layoutConstraint = getLayoutConstraint(childShapePresentation, layoutDef);
					if (layoutConstraint != null)
					{
						figure.add(childFigure, layoutConstraint);
					}
					else
					{
						figure.add(childFigure);
					}
				}
			}
			if (childShapePresentation instanceof ShapeFactoryPresentation) 
			{
				ShapeFactoryPresentation shapeFactoryPresentation = (ShapeFactoryPresentation)childShapePresentation;
				shapeFactoryPresentation.setIndex(figure.getChildren().size());
			}
		}
		
		setFigure(figure);
	}
	
	private Object getLayoutConstraint(ShapePresentation childShapePresentation, ShapeLayoutDef layoutDef)
	{
		Object layoutConstraint = null;
		if (layoutDef instanceof SequenceLayoutDef)
		{
			if (((SequenceLayoutDef)layoutDef).getOrientation().content() != SequenceLayoutOrientation.STACKED)
			{
				SequenceLayoutConstraintDef def = (SequenceLayoutConstraintDef)childShapePresentation.getLayoutConstraint();
				layoutConstraint = new SapphireSequenceLayoutConstraint(def);
			}
			else
			{
				SapphireStackLayoutConstraint constraint = null;
				if (childShapePresentation.getLayoutConstraint() != null)
				{
					SequenceLayoutConstraintDef constraintDef = 
							(SequenceLayoutConstraintDef)childShapePresentation.getLayoutConstraint();
					if (constraintDef != null)
					{
						constraint = new SapphireStackLayoutConstraint(
								constraintDef.getHorizontalAlignment().content(),
								constraintDef.getVerticalAlignment().content(),
								constraintDef.getMarginTop().content(),
								constraintDef.getMarginBottom().content(),
								constraintDef.getMarginLeft().content(),
								constraintDef.getMarginRight().content());
					}
				}	
				layoutConstraint = constraint != null ? constraint : new SapphireStackLayoutConstraint();
			}
		}
		return layoutConstraint;
	}

}
