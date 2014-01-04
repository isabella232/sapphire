/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.parts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.sapphire.ui.def.HorizontalAlignment;
import org.eclipse.sapphire.ui.diagram.shape.def.LayoutConstraintDef;
import org.eclipse.sapphire.ui.diagram.shape.def.SequenceLayoutConstraintDef;
import org.eclipse.sapphire.ui.diagram.shape.def.SequenceLayoutDef;
import org.eclipse.sapphire.ui.diagram.shape.def.SequenceLayoutOrientation;
import org.eclipse.sapphire.ui.diagram.shape.def.ShapeLayoutDef;
import org.eclipse.sapphire.ui.swt.gef.DiagramConfigurationManager;
import org.eclipse.sapphire.ui.swt.gef.layout.SapphireSequenceLayoutConstraint;
import org.eclipse.sapphire.ui.swt.gef.layout.SapphireStackLayoutConstraint;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramResourceCache;
import org.eclipse.sapphire.ui.swt.gef.presentation.ContainerShapePresentation;
import org.eclipse.sapphire.ui.swt.gef.presentation.ShapePresentation;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class ShapeUtil {  
	
	
	public static boolean updateFigureForShape(ShapePresentation updateShape, DiagramResourceCache resourceCache, DiagramConfigurationManager configManager)
	{
		if (updateShape == null || updateShape instanceof ContainerShapePresentation) {
			return false;
		}
		
		IFigure updateFigure = updateShape.getFigure();
		IFigure containerFigure = updateShape.getParentFigure();
		if (updateShape.visible()) 
		{			
			ContainerShapePresentation containerPresentation = (ContainerShapePresentation)updateShape.parent();
			// find the parent figure
			if (containerFigure != null) 
			{
				int index = containerPresentation.getChildFigureIndex(updateShape);
				if (updateFigure != null) 
				{
					// first delete it
					containerFigure.remove(updateFigure);
				}
				// add it
				updateShape.render();
				updateFigure = updateShape.getFigure();

				Object layoutConstraint = getLayoutConstraint(updateShape, containerPresentation.getLayout());
				if (layoutConstraint != null)
				{
					containerFigure.add(updateFigure, layoutConstraint, index);
				}
				else
				{
					containerFigure.add(updateFigure, index);
				}
				containerFigure.revalidate();
			}
		} 
		else if (!updateShape.visible() && updateFigure != null) 
		{
			// remove it
			updateShape.removeFigure();
			containerFigure.remove(updateFigure);
			containerFigure.revalidate();
		}
		return true;		
	}
	
	public static Object getLayoutConstraint(ShapePresentation childShapePresentation, ShapeLayoutDef layoutDef)
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
	
	public static int getTextAlignment(LayoutConstraintDef constraint)
	{
		int alignment = PositionConstants.CENTER;
		HorizontalAlignment sapphireAlign = constraint.getHorizontalAlignment().content();
		switch (sapphireAlign) 
		{
			case LEFT:
				alignment = PositionConstants.LEFT;
				break;
			case RIGHT:
				alignment = PositionConstants.RIGHT;
				break;
			default:			
				break;
		}
		return alignment;
	}
	
	public static int getPresentationCount(ContainerShapePresentation parentPresentation, ShapePresentation shapePresentation) {
		int count = 0;
		for (ShapePresentation sp : parentPresentation.getChildren()) {
			if (shapePresentation.equals(sp)) {
				return count;
			}
			if (!(sp instanceof ContainerShapePresentation)) {
				count++;
			}
		}
		return count;
	}

}
