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

package org.eclipse.sapphire.ui.swt.gef.layout;

import org.eclipse.sapphire.ui.def.HorizontalAlignment;
import org.eclipse.sapphire.ui.def.VerticalAlignment;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class SapphireStackLayoutConstraint 
{
	private HorizontalAlignment horizontalAlignment;
	private VerticalAlignment verticalAlignment;
	private int topMargin;
	private int bottomMargin;
	private int leftMargin;
	private int rightMargin;
	
	public SapphireStackLayoutConstraint()
	{
		this(HorizontalAlignment.CENTER, VerticalAlignment.CENTER, 0, 0, 0, 0);
	}
	
	public SapphireStackLayoutConstraint(HorizontalAlignment horizontalAlignment, 
										VerticalAlignment verticalAlignment)
	{
		this(horizontalAlignment, verticalAlignment, 0, 0, 0, 0);
	}

	public SapphireStackLayoutConstraint( final HorizontalAlignment horizontalAlignment, 
										  final VerticalAlignment verticalAlignment, 
										  final int topMargin,
										  final int bottomMargin,
										  final int leftMargin,
										  final int rightMargin )
	{
		this.horizontalAlignment = horizontalAlignment;
		this.verticalAlignment = verticalAlignment;
		this.topMargin = topMargin;
		this.bottomMargin = bottomMargin;
		this.leftMargin = leftMargin;
		this.rightMargin = rightMargin;
	}

	public HorizontalAlignment getHorizontalAlignment() 
	{
		return horizontalAlignment;
	}

	public VerticalAlignment getVerticalAlignment() 
	{
		return verticalAlignment;
	}

	public int getTopMargin() 
	{
		return this.topMargin;
	}

	public int getBottomMargin() 
	{
		return this.bottomMargin;
	}
	
	public int getLeftMargin()
	{
		return this.leftMargin;
	}
	
	public int getRightMargin()
	{
		return this.rightMargin;
	}
	
}
