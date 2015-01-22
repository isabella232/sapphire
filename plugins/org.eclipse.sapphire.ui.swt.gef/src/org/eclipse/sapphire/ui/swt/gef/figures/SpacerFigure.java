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

package org.eclipse.sapphire.ui.swt.gef.figures;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.sapphire.ui.Point;
import org.eclipse.sapphire.ui.swt.gef.presentation.SpacerPresentation;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class SpacerFigure extends Figure 
{
	private SpacerPresentation spacerPresentation;
	
	public SpacerFigure(SpacerPresentation spacerPresentation)
	{
		this.spacerPresentation = spacerPresentation;
	}
	
	public Dimension getMinimumSize(int w, int h) 
	{
		Point size = this.spacerPresentation.getMinimunSize();
		Dimension d = new Dimension(size.getX(), size.getY());
		return d;
	}

	public Dimension getMaximumSize() 
	{
		Point size = this.spacerPresentation.getMaximumSize();
		Dimension d = new Dimension(size.getX(), size.getY());
		return d;
	}

	public Dimension getPreferredSize(int w, int h) 
	{
		Point size = this.spacerPresentation.getSize();
		Point minSize = this.spacerPresentation.getMinimunSize();
		Point maxSize = this.spacerPresentation.getMaximumSize();
		int w2 = size.getX() != -1 ? size.getX() : 
				(minSize.getX() != -1 ? minSize.getX() : (maxSize.getX() != -1 ? maxSize.getX() : -1));
		int h2 = size.getY() != -1 ? size.getY() : 
			(minSize.getY() != -1 ? minSize.getY() : (maxSize.getY() != -1 ? maxSize.getY() : -1));
		Dimension d = new Dimension(w2, h2);
		return d;
	}

	public Dimension getSize(int w, int h) 
	{
		return getPreferredSize(w, h);
	}
}
