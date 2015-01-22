/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/
package org.eclipse.sapphire.ui.swt.gef.layout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.AbstractHintLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.geometry.Transposer;
import org.eclipse.sapphire.ui.diagram.shape.def.SequenceLayoutDef;
import org.eclipse.sapphire.ui.diagram.shape.def.SequenceLayoutOrientation;
import org.eclipse.sapphire.ui.swt.gef.figures.TextFigure;
import org.eclipse.swt.SWT;

/**
 * Arranges figures in a single row or column. Orientation can be set to produce
 * either a row or column layout. This layout tries to fit all children within
 * the parent's client area. To do this, it compresses the children by some
 * amount, but will not compress them smaller than their minimum size. If a
 * child's preferred size is smaller than the row's or column's minor dimension,
 * the layout can be configured to stretch the child.
 * 
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 *  
 */
public class SapphireSequenceLayout extends AbstractHintLayout {

	/** The layout contraints */
	private Map<IFigure,Object> constraints = new HashMap<IFigure,Object>();

	/**
	 * Transposer object that may be used in layout calculations. Will be
	 * automatically enabled/disabled dependent on the default and the actual
	 * orientation.
	 * 
	 * @noreference This field is not intended to be referenced by clients.
	 */
	private Transposer transposer = new Transposer();

	/**
	 * The horizontal property.
	 * 
	 */
	private boolean horizontal;

	/**
	 * Space in pixels between Figures   
	 * 
	 */
	private int spacing;
	
	/**
	 * Margin insets   
	 * 
	 */
	private Insets marginInsets;

	/**
	 * Constructs a SapphireSequenceLayout with a specified SequenceLayoutDef
	 */
	public SapphireSequenceLayout(SequenceLayoutDef def) {
		setHorizontal(def.getOrientation().content() == SequenceLayoutOrientation.HORIZONTAL);
		setSpacing(def.getSpacing().content());
		this.marginInsets = LayoutUtil.calculateMargin(def);
	}
	
	public void setHorizontal(boolean flag) {
		if (horizontal == flag)
			return;
		invalidate();
		horizontal = flag;
		updateTransposerEnabledState();
	}

	public boolean isHorizontal() {
		return horizontal;
	}

	private Dimension calculateChildrenSize(List<IFigure> children, int wHint, int hHint, boolean preferred) {
		Dimension childSize;
		IFigure child;
		int height = 0, width = 0;
		for (int i = 0; i < children.size(); i++) {
			child = children.get(i);
			childSize = transposer.t(preferred ? getChildPreferredSize(child,
					wHint, hHint) : getChildMinimumSize(child, wHint, hHint));
			Insets inset = new Insets();
			SapphireSequenceLayoutConstraint constraint = (SapphireSequenceLayoutConstraint)getConstraint(child);
			if (constraint != null) {
				inset = transposer.t(constraint.getMarginInset());
			}
			
			height += childSize.height + inset.top + inset.bottom;
			width = Math.max(width, childSize.width + inset.left + inset.right);
		}
		return new Dimension(width, height);
	}

	private Dimension calculateChildrenMaximumSize(List<IFigure> children) {
		Dimension childSize;
		IFigure child;
		int height = 0, width = Integer.MAX_VALUE;
		for (int i = 0; i < children.size(); i++) {
			child = children.get(i);
			childSize = transposer.t(getChildCellMaximumSize(child));
			Insets inset = new Insets();
			SapphireSequenceLayoutConstraint constraint = (SapphireSequenceLayoutConstraint)getConstraint(child);
			if (constraint != null) {
				inset = transposer.t(constraint.getMarginInset());
			}
			width = Math.min(width, childSize.width + (childSize.width < Integer.MAX_VALUE ? inset.left + inset.right : 0));
			if (childSize.height < Integer.MAX_VALUE ) {
				if (height < Integer.MAX_VALUE) {
					height += childSize.height + inset.top + inset.bottom;
				}
			}
			else {
				height = Integer.MAX_VALUE;
			}			
		}
		return new Dimension(width, height);
	}
	
	/**
	 * Calculates the minimum size of the container based on the given hints. If
	 * this is a vertically-oriented sequence Layout, then only the widthHint is
	 * respected (which means that the children can be as tall as they desire).
	 * In this case, the minimum width is that of the widest child, and the
	 * minimum height is the sum of the minimum heights of all children, plus
	 * the spacing between them. The border and insets of the container figure
	 * are also accounted for.
	 * 
	 * @param container
	 *            the figure whose minimum size has to be calculated
	 * @param wHint
	 *            the width hint (the desired width of the container)
	 * @param hHint
	 *            the height hint (the desired height of the container)
	 * @return the minimum size of the container
	 * @see #getMinimumSize(IFigure, int, int)
	 * @since 2.1
	 */
	
	@SuppressWarnings( "unchecked" )
	
    protected Dimension calculateMinimumSize(IFigure container, int wHint, int hHint) {
		Insets insets = container.getInsets();
		if (isHorizontal()) {
			wHint = -1;
			if (hHint >= 0)
				hHint = Math.max(0, hHint - insets.getHeight());
		} else {
			hHint = -1;
			if (wHint >= 0)
				wHint = Math.max(0, wHint - insets.getWidth());
		}

		List<IFigure> children = container.getChildren();
		Dimension minSize = calculateChildrenSize(children, wHint, hHint, false);
		// Do a second pass, if necessary
		if (wHint >= 0 && minSize.width > wHint) {
			minSize = calculateChildrenSize(children, minSize.width, hHint, false);
		} else if (hHint >= 0 && minSize.width > hHint) {
			minSize = calculateChildrenSize(children, wHint, minSize.width, false);
		}

		minSize.height += Math.max(0, children.size() - 1) * spacing;
		
		Insets inset = transposer.t(this.marginInsets);
		minSize.width += inset.left + inset.right;
		minSize.height += inset.top + inset.bottom;
		
		return transposer.t(minSize)
				.expand(insets.getWidth(), insets.getHeight())
				.union(getBorderPreferredSize(container));
	}

	/**
	 * Calculates the preferred size of the container based on the given hints.
	 * If this is a vertically-oriented sequence Layout, then only the widthHint
	 * is respected (which means that the children can be as tall as they
	 * desire). In this case, the preferred width is that of the widest child,
	 * and the preferred height is the sum of the preferred heights of all
	 * children, plus the spacing between them. The border and insets of the
	 * container figure are also accounted for.
	 * 
	 * @param container
	 *            the figure whose preferred size has to be calculated
	 * @param wHint
	 *            the width hint (the desired width of the container)
	 * @param hHint
	 *            the height hint (the desired height of the container)
	 * @return the preferred size of the container
	 * @see #getPreferredSize(IFigure, int, int)
	 * @since 2.0
	 */
	
	@SuppressWarnings( "unchecked" )
	
    protected Dimension calculatePreferredSize(IFigure container, int wHint,
			int hHint) {
		Insets insets = container.getInsets();
		if (isHorizontal()) {
			wHint = -1;
			if (hHint >= 0)
				hHint = Math.max(0, hHint - insets.getHeight());
		} else {
			hHint = -1;
			if (wHint >= 0)
				wHint = Math.max(0, wHint - insets.getWidth());
		}

		List<IFigure> children = container.getChildren();
		Dimension prefSize = calculateChildrenSize(children, wHint, hHint, true);
		// Do a second pass, if necessary
		if (wHint >= 0 && prefSize.width > wHint) {
			prefSize = calculateChildrenSize(children, prefSize.width, hHint, true);
		} else if (hHint >= 0 && prefSize.width > hHint) {
			prefSize = calculateChildrenSize(children, wHint, prefSize.width, true);
		}

		// Constrain the preferred width by container's max width and min width
		Dimension maxSize = calculateChildrenMaximumSize(children);
		Dimension minSize = calculateChildrenSize(children, wHint, hHint, false);		
		prefSize.width = Math.min(maxSize.width, prefSize.width);
		prefSize.width = Math.max(minSize.width, prefSize.width);
		
		prefSize.height += Math.max(0, children.size() - 1) * spacing;

		Insets inset = transposer.t(this.marginInsets);
		prefSize.width += inset.left + inset.right;
		prefSize.height += inset.top + inset.bottom;

		return transposer.t(prefSize)
				.expand(insets.getWidth(), insets.getHeight())
				.union(getBorderPreferredSize(container));
	}

	@SuppressWarnings( "unchecked" )
    public Dimension calculateMaximumSize(IFigure container) {
		List<IFigure> children = container.getChildren();
		Dimension maxSize = calculateChildrenMaximumSize(children);
		Insets marginInsets = transposer.t(this.marginInsets);
		if (maxSize.width < Integer.MAX_VALUE) {
			maxSize.width += marginInsets.left + marginInsets.right;
		}
		if (maxSize.height < Integer.MAX_VALUE) {
			maxSize.height += Math.max(0, children.size() - 1) * spacing;
			maxSize.height += marginInsets.top + marginInsets.bottom;
		}
		maxSize = transposer.t(maxSize);
		Insets containerInsets = container.getInsets();
		Dimension borderSize = getBorderPreferredSize(container);
		if (maxSize.width < Integer.MAX_VALUE) {
			maxSize.width += containerInsets.getWidth();
			maxSize.width = Math.max(borderSize.width, maxSize.width);
		}
		if (maxSize.height < Integer.MAX_VALUE) {
			maxSize.height += containerInsets.getHeight();
			maxSize.height = Math.max(borderSize.height, maxSize.height);
		}
		return maxSize;
	}
	/**
	 * @param child
	 *            the figure whose minimum size is to be determined
	 * @param wHint
	 *            the width hint
	 * @param hHint
	 *            the height hint
	 * @return the given figure's minimum size
	 * @since 3.3
	 */
	protected Dimension getChildMinimumSize(IFigure child, int wHint, int hHint) {
		Dimension minSize =  child.getMinimumSize(wHint, hHint).getCopy();
		SapphireSequenceLayoutConstraint constraint = (SapphireSequenceLayoutConstraint)getConstraint(child);
		if (constraint.minWidth > minSize.width)
		{
			minSize.width = constraint.minWidth;
		}
		if (constraint.minHeight > minSize.height)
		{
			minSize.height = constraint.minHeight;
		}
		return minSize;
	}

	/**
	 * @param child
	 *            the figure whose preferred size is to be determined
	 * @param wHint
	 *            the width hint
	 * @param hHint
	 *            the height hint
	 * @return given figure's preferred size
	 * @since 3.3
	 */
	protected Dimension getChildPreferredSize(IFigure child, int wHint,	int hHint) {
		Dimension dimension = child.getPreferredSize(wHint, hHint).getCopy();
		Dimension minSize = child.getMinimumSize(wHint, hHint);
		SapphireSequenceLayoutConstraint constraint = (SapphireSequenceLayoutConstraint)getConstraint(child);
		if (constraint.widthHint > minSize.width ) {
			dimension.width = constraint.widthHint; 
		}
		if (constraint.heightHint > minSize.height) {
			dimension.height = constraint.heightHint; 
		}
		if (constraint.minWidth > SWT.DEFAULT && dimension.width < constraint.minWidth) {
			dimension.width = constraint.minWidth; 
		}
		if (constraint.minHeight > SWT.DEFAULT && dimension.height < constraint.minHeight) {
			dimension.height = constraint.minHeight; 
		}
		if (constraint.maxWidth > SWT.DEFAULT && dimension.width > constraint.maxWidth) {
			dimension.width = constraint.maxWidth; 
		}
		if (constraint.maxHeight > SWT.DEFAULT && dimension.height > constraint.maxHeight) {
			dimension.height = constraint.maxHeight; 
		}
		return dimension;
	}

	protected Dimension getChildCellMaximumSize(IFigure child) {
		Dimension dimension = child.getMaximumSize().getCopy();
		SapphireSequenceLayoutConstraint constraint = (SapphireSequenceLayoutConstraint)getConstraint(child);
		if (constraint.expandHorizontally) {
			dimension.width = Integer.MAX_VALUE;
		}
		if (constraint.expandVertically) {
			dimension.height = Integer.MAX_VALUE;
		}
		if (constraint.maxWidth > SWT.DEFAULT && constraint.maxWidth < Integer.MAX_VALUE ) {
			dimension.width = Math.min(constraint.maxWidth, dimension.width); 
		}
		if (constraint.maxHeight > SWT.DEFAULT && constraint.maxHeight < Integer.MAX_VALUE ) {
			dimension.height = Math.min(constraint.maxHeight, dimension.height); 
		}
		return dimension;
	}

	/**
	 * Returns {@link PositionConstants#VERTICAL} by default.
	 * 
	 * @see org.eclipse.draw2d.OrderedLayout#getDefaultOrientation()
	 */
	protected int getDefaultOrientation() {
		return PositionConstants.VERTICAL;
	}

	/**
	 * @return the spacing between children
	 */
	public int getSpacing() {
		return spacing;
	}

	/**
	 * @see org.eclipse.draw2d.AbstractHintLayout#isSensitiveHorizontally(IFigure)
	 */
	protected boolean isSensitiveHorizontally(IFigure parent) {
		return !isHorizontal();
	}

	/**
	 * @see org.eclipse.draw2d.AbstractHintLayout#isSensitiveVertically(IFigure)
	 */
	protected boolean isSensitiveVertically(IFigure parent) {
		return isHorizontal();
	}

	/**
	 * @see org.eclipse.draw2d.LayoutManager#layout(IFigure)
	 */
	
	@SuppressWarnings( "unchecked" )
	
    public void layout(IFigure parent) {
		List<IFigure> children = parent.getChildren();
		int numChildren = children.size();
		Rectangle clientArea = transposer.t(parent.getClientArea());
		Insets margins = transposer.t(this.marginInsets);
		
		clientArea.x += margins.left;
		clientArea.width -= (margins.left + margins.right);

		int x = clientArea.x;
		int y = clientArea.y;
		int availableHeight = clientArea.height;

		Dimension prefSizes[] = new Dimension[numChildren];
		Dimension minSizes[] = new Dimension[numChildren];
		Dimension maxCellSizes[] = new Dimension[numChildren];
		Dimension maxChildShapeSizes[] = new Dimension[numChildren];
		int extraHeights[] = new int[numChildren];
		
		SapphireSequenceLayoutConstraint constraints[] = new SapphireSequenceLayoutConstraint[numChildren];
		Insets marginInsets[] = new Insets[numChildren];

		// Calculate the width and height hints. If it's a vertical sequence layout,
		// then ignore the height hint (set it to -1); otherwise, ignore the
		// width hint. These hints will be passed to the children of the parent
		// figure when getting their preferred size.
		int wHint = -1;
		int hHint = -1;
		if (isHorizontal()) {
			hHint = parent.getClientArea(Rectangle.SINGLETON).height - (margins.top + margins.bottom);
		} else {
			wHint = parent.getClientArea(Rectangle.SINGLETON).width - (margins.left + margins.right);
		}

		/*
		 * Calculate sum of preferred heights of all children(totalHeight).
		 * Cache Preferred Sizes and Minimum Sizes of all children.
		 */
		IFigure child;
		int totalHeight = 0;
		int totalMinHeight = 0;
		int prefMinSumHeight = 0;
		int totalMargin = 0;
		int expandCount = 0;
		
		for (int i = 0; i < numChildren; i++) {
			child = children.get(i);
			
			SapphireSequenceLayoutConstraint constraint = (SapphireSequenceLayoutConstraint)getConstraint(child);
			if (constraint == null)
				setConstraint(child, constraint = new SapphireSequenceLayoutConstraint());
			constraints[i] = constraint;

			prefSizes[i] = transposer.t(getChildPreferredSize(child, wHint, hHint));
			minSizes[i] = transposer.t(getChildMinimumSize(child, wHint, hHint));
			maxCellSizes[i] = transposer.t(getChildCellMaximumSize(child));
			maxChildShapeSizes[i] = transposer.t(child.getMaximumSize());
			marginInsets[i] = transposer.t(constraint.getMarginInset());
			
			totalHeight += prefSizes[i].height;
			totalMinHeight += minSizes[i].height;
			totalMargin += marginInsets[i].top + marginInsets[i].bottom;
			// We need to expand the cell if the its constraint has "expand" bit on or
			// one of the children has "expand" bit on
			if (getMajorExpand(constraint) || maxChildShapeSizes[i].height > prefSizes[i].height) {
				expandCount++;
			} 
		}
		totalHeight += (numChildren - 1) * spacing;
		totalHeight += totalMargin + margins.top + margins.bottom;
		totalMinHeight += (numChildren - 1) * spacing;
		totalMinHeight += totalMargin + margins.top + margins.bottom;
		prefMinSumHeight = totalHeight - totalMinHeight;
		
		int amntShrinkHeight = totalHeight
				- Math.max(availableHeight, totalMinHeight);
		int extraHeight = -amntShrinkHeight;

		if (amntShrinkHeight < 0) {
			amntShrinkHeight = 0;
		}
		
		if (extraHeight <= 0) {
			extraHeight = 0;
		} else if (expandCount > 0) {
			int averageExtraHeight = extraHeight / expandCount;

			int limitedExpansionCount = 0;
			int limitedExpansionHeightTotal = 0;
			if (expandCount > 1) {
				for (int i = 0; i < numChildren; i++) {				
					int prefHeight = prefSizes[i].height;
					int maxCellHeight = maxCellSizes[i].height;
					child = children.get(i);
					SapphireSequenceLayoutConstraint constraint = constraints[i];
					if (getMajorExpand(constraint) || maxCellHeight > prefHeight) {
						// only limited expansion since the child figure has max size constraint.
						if (maxCellHeight - prefHeight < averageExtraHeight) {
							limitedExpansionCount++;
							limitedExpansionHeightTotal += maxCellHeight - prefHeight;
						}
					}
				}				
			}
			int unlimitedExpansionAverage = limitedExpansionCount < expandCount ? 
					(extraHeight - limitedExpansionHeightTotal) / (expandCount - limitedExpansionCount) : 0;

			for (int i = 0; i < numChildren; i++) {				
				int prefHeight = prefSizes[i].height;
				int maxCellHeight = maxCellSizes[i].height;
				child = children.get(i);
				SapphireSequenceLayoutConstraint constraint = constraints[i];
				if (getMajorExpand(constraint) || maxCellHeight > prefHeight) {
					// only limited expansion
					if (expandCount > 1 && (maxCellHeight - prefHeight < averageExtraHeight)) {
						extraHeights[i] = maxCellHeight - prefHeight;
					}
					else {
						extraHeights[i] = unlimitedExpansionAverage;
					}
				}
				else {
					extraHeights[i] = 0;
				}
			}					
			
		}
		
		y += margins.top;

		for (int i = 0; i < numChildren; i++) {
			int amntShrinkCurrentHeight = 0;
			child = children.get(i);

			int prefHeight = prefSizes[i].height;
			int minHeight = minSizes[i].height;
			int prefWidth = prefSizes[i].width;
			int minWidth = minSizes[i].width;
			Insets marginInset = marginInsets[i];
			
			Rectangle newBounds, availableBounds;
			int availableBoundHeight;
			SapphireSequenceLayoutConstraint constraint = constraints[i];
			if (prefMinSumHeight != 0)
				amntShrinkCurrentHeight = (prefHeight - minHeight)
						* amntShrinkHeight / (prefMinSumHeight);
			int height = prefHeight;
			if (amntShrinkCurrentHeight != 0) {			
				height -= amntShrinkCurrentHeight;
				newBounds = new Rectangle(x, y + marginInset.top, prefWidth, height);
				amntShrinkHeight -= amntShrinkCurrentHeight;
				prefMinSumHeight -= (prefHeight - minHeight);				
			}			
			else if (getMajorExpand(constraint) || maxChildShapeSizes[i].height > prefSizes[i].height ) {
				height += extraHeights[i];
				// If the expansion comes from child shape, let the child shape take up the extra space.
				// Otherwise, the virtual cell takes up the extra space and we use its alignment to place
				// the child shape
				if (maxChildShapeSizes[i].height > prefSizes[i].height) {
					newBounds = new Rectangle(x, y + marginInset.top, prefWidth, height);
				} else {
    				int offset = 0;
					// alignment
					switch (getMajorAlignment(constraint)) {
					case SWT.CENTER:
						offset = extraHeights[i] >> 1;
						break;
					case SWT.RIGHT:
					case SWT.BOTTOM:
						offset = extraHeights[i];
						break;
					default:
					    break;
					}
					newBounds = new Rectangle(x, y + marginInset.top + offset, prefWidth, prefHeight);
				}
			} else {
				newBounds = new Rectangle(x, y + marginInset.top, prefWidth, height);
			}
			availableBoundHeight = height;
			
			int width = Math.min(prefWidth,	maxCellSizes[i].width);
//			if (getMinorExpand(constraint) && (child instanceof RectangleFigure))
//				width = maxSizes[i].width;
			width = Math.max(minWidth, Math.min(clientArea.width, width));
			newBounds.width = width;

//			// Shenxue: include the margins in the available area. Otherwise the direct edit box could be
//			// too narrow for text that doesn't expand. It doesn't have any extra space around the text in 
//			// direct edit mode even when there are margins around the text.
//			availableBounds = new Rectangle(x, y, clientArea.width, 
//					availableBoundHeight + marginInset.top + marginInset.bottom);
			availableBounds = new Rectangle(x + marginInset.left, y + marginInset.top, 
					clientArea.width - marginInset.left - marginInset.right, availableBoundHeight);
			
			if (maxChildShapeSizes[i].width > prefSizes[i].width) 
			{
				newBounds.x += marginInset.left;
				newBounds.width = clientArea.width - marginInset.left - marginInset.right;
			} 
			else 
			{
				// Honor alignment if there is extra space. It's consistent with how the direct cell
				// editor locator places direct editor cell when direct editing text. 
				// Shenxue - based on discussion with Ling 7/10/2013
				int adjust = clientArea.width - width - marginInset.left - marginInset.right;
				if (adjust <= 0)
				{
					adjust = 0;
				}
				else 
				{
					switch (getMinorAlignment(constraint)) 
					{
					case SWT.TOP:
					case SWT.LEFT:
						adjust = 0;
						break;
					case SWT.CENTER:
						adjust = (adjust + 1) >> 1;
						break;
					default:
						break;   
					}
				}
				newBounds.x += adjust + marginInset.left;
			}
			child.setBounds(transposer.t(newBounds));
			
			if (child instanceof TextFigure) {
				((TextFigure) child).setAvailableArea(transposer.t(availableBounds));
				((TextFigure) child).setHorizontalAlignment(constraint.horizontalAlignment);
			}

			y += availableBoundHeight + spacing + marginInset.top + marginInset.bottom;
		}
	}
	
	private int getMajorAlignment(SapphireSequenceLayoutConstraint constraint) {
		return isHorizontal() ? constraint.horizontalAlignment : constraint.verticalAlignment;
	}
	
	private int getMinorAlignment(SapphireSequenceLayoutConstraint constraint) {
		return isHorizontal() ? constraint.verticalAlignment : constraint.horizontalAlignment;
	}

	private boolean getMajorExpand(SapphireSequenceLayoutConstraint constraint) {
		return isHorizontal() ? constraint.expandHorizontally : constraint.expandVertically;
	}
	
	/**
	 * Sets the amount of space between children.
	 * 
	 * @param space
	 *            the amount of space between children
	 * @since 2.0
	 */
	public void setSpacing(int space) {
		spacing = space;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.draw2d.LayoutManager#getConstraint(org.eclipse.draw2d.IFigure
	 * )
	 */
	public Object getConstraint(IFigure child) {
		return constraints.get(child);
	}

	/**
	 * Sets the layout constraint of the given figure. The constraints can only
	 * be of type {@link org.eclipse.draw2d.GridData}.
	 */
	public void setConstraint(IFigure figure, Object newConstraint) {
		super.setConstraint(figure, newConstraint);
		if (newConstraint != null) {
			constraints.put(figure, newConstraint);
		}
	}
	
	/**
	 * Updates the enabled state of the {@link #transposer} in case the layout
	 * has a different orientation that its default one.
	 */
	private void updateTransposerEnabledState() {
		// enable transposer if the current orientation differs from the default
		// orientation, disable it otherwise
		transposer.setEnabled((isHorizontal()
				&& getDefaultOrientation() == PositionConstants.VERTICAL)
				|| (!isHorizontal()
				&& getDefaultOrientation() == PositionConstants.HORIZONTAL));
	}
}
