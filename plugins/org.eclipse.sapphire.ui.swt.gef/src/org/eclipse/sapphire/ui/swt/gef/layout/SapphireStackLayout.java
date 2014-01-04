/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Ling Hao - [383924]  Flexible diagram node shapes
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.layout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.AbstractLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.sapphire.ui.def.HorizontalAlignment;
import org.eclipse.sapphire.ui.def.VerticalAlignment;
import org.eclipse.sapphire.ui.diagram.shape.def.SequenceLayoutDef;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class SapphireStackLayout extends AbstractLayout 
{
	private Map<IFigure, Object> constraints = new HashMap<IFigure, Object>();
	
	private Insets marginInsets;
	
	public SapphireStackLayout(SequenceLayoutDef def)
	{
		this.marginInsets = LayoutUtil.calculateMargin(def);
	}
	
	@Override
	protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint) 
	{
		Insets insets = container.getInsets();
		Dimension size = getStackedChildrenSize(container.getChildren());
		size.width += this.marginInsets.left + this.marginInsets.right;
		size.height += this.marginInsets.top + this.marginInsets.bottom;
		return size.expand(insets.getWidth(), insets.getHeight())
				.union(getBorderPreferredSize(container));
	}

	public Dimension calculateMaximumSize(IFigure container) 
	{
		return calculatePreferredSize(container, -1, -1);
	}
	/**
	 * Returns the origin for the given figure.
	 * 
	 * @param parent
	 *            the figure whose origin is requested
	 * @return the origin
	 */
	public Point getOrigin(IFigure parent)
	{
		return parent.getClientArea().getLocation();
	}
	
	@SuppressWarnings("rawtypes")
	public void layout(IFigure parent) 
	{
		List children = parent.getChildren();
		if (children.size() == 0)
		{
			return;
		}
		Dimension baseSize = getStackedChildrenSize(children);
		Point offset = getOrigin(parent);
		offset.x += this.marginInsets.left;
		offset.y += this.marginInsets.top;	
//		// If the client space is larger than the combined image space, let's center
//		// the image space within the client space. 
//		// TODO we probably should have a specification in stack layout's definition.
//		Dimension clientSize = parent.getClientArea().getSize();
//		clientSize.width -= this.marginInsets.left + this.marginInsets.right;
//		clientSize.height -= this.marginInsets.top + this.marginInsets.bottom;
//		Point baseOffset = new Point((clientSize.width - baseSize.width + 1) >> 1,
//									(clientSize.height - baseSize.height + 1) >> 1);
		
		for (int i = 0; i < children.size(); i++)
		{
			IFigure child = (IFigure)children.get(i);
			Dimension childSize = child.getPreferredSize();
			SapphireStackLayoutConstraint constraint = (SapphireStackLayoutConstraint)getConstraint(child);
			Point childOffset = getOffset(baseSize, childSize, constraint);
			Rectangle childBounds = new Rectangle(childOffset.x, childOffset.y, childSize.width, childSize.height);
			childBounds = childBounds.getTranslated(offset);
			child.setBounds(childBounds);
		}
	}
	
	/**
	 * @see LayoutManager#getConstraint(IFigure)
	 */
	public Object getConstraint(IFigure figure) 
	{
		return constraints.get(figure);
	}
	
	/**
	 * @see LayoutManager#remove(IFigure)
	 */
	public void remove(IFigure figure) 
	{
		super.remove(figure);
		constraints.remove(figure);
	}

	/**
	 * Sets the layout constraint of the given figure. The constraints can only
	 * be of type {@link Rectangle}.
	 * 
	 * @see LayoutManager#setConstraint(IFigure, Object)
	 * @since 2.0
	 */
	public void setConstraint(IFigure figure, Object newConstraint) 
	{
		super.setConstraint(figure, newConstraint);
		if (newConstraint != null)
			constraints.put(figure, newConstraint);
	}

	private Point getOffset(Dimension baseSize, Dimension childSize, SapphireStackLayoutConstraint constraint)
	{
		int offsetX = 0; 
		int offsetY = 0;
		
		HorizontalAlignment horizontalAlign = constraint.getHorizontalAlignment();
		if (horizontalAlign == HorizontalAlignment.LEFT)
		{
			offsetX = constraint.getLeftMargin();
		}
		else if (horizontalAlign == HorizontalAlignment.RIGHT)
		{
			offsetX = baseSize.width - constraint.getRightMargin() - childSize.width;
		}
		else if (horizontalAlign == HorizontalAlignment.CENTER)
		{
			offsetX = (baseSize.width - childSize.width + 1) >> 1;
		}
		
		VerticalAlignment verticalAlign = constraint.getVerticalAlignment();
		if (verticalAlign == VerticalAlignment.TOP)
		{
			offsetY = constraint.getTopMargin();
		}
		else if (verticalAlign == VerticalAlignment.BOTTOM)
		{
			offsetY = baseSize.height - constraint.getBottomMargin() - childSize.height;
		}
		else if (verticalAlign == VerticalAlignment.CENTER)
		{
			offsetY = (baseSize.height - childSize.height + 1) >> 1;
		}
		return new Point(offsetX, offsetY);
	}
	
	private Dimension getStackedChildrenSize(List children)
	{
		Dimension size = new Dimension(0, 0);
		for (Object child : children)
		{
			IFigure childFigure = (IFigure)child;
			Dimension childSize = childFigure.getPreferredSize();
			size.width = Math.max(size.width, childSize.width);
			size.height = Math.max(size.height, childSize.height);
		}
		return size;
	}
}
