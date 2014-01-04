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

import org.eclipse.sapphire.ui.Point;
import org.eclipse.sapphire.ui.diagram.shape.def.LayoutConstraintDef;
import org.eclipse.sapphire.ui.diagram.shape.def.SequenceLayoutConstraintDef;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class SpacerPart extends ShapePart 
{
	public Point getSize()
	{
		LayoutConstraintDef constraint = getLayoutConstraint();
		Point size = new Point(constraint.getWidth().content() != null ? constraint.getWidth().content() : -1, 
								constraint.getHeight().content() != null ? constraint.getHeight().content() : -1);
		return size;
	}
	
	public Point getMinimumSize()
	{
		SequenceLayoutConstraintDef constraint = (SequenceLayoutConstraintDef)getLayoutConstraint();
		Point size = new Point(constraint.getMinWidth().content() != null ? constraint.getMinWidth().content() : -1, 
								constraint.getMinHeight().content() != null ? constraint.getMinHeight().content() : -1);
		return size;
	}
	
	public Point getMaximumSize()
	{
		SequenceLayoutConstraintDef constraint = (SequenceLayoutConstraintDef)getLayoutConstraint();
		Point size = new Point(constraint.getMaxWidth().content() != null ? constraint.getMaxWidth().content() : -1, 
								constraint.getMaxHeight().content() != null ? constraint.getMaxHeight().content() : -1);
		return size;
	}
}
