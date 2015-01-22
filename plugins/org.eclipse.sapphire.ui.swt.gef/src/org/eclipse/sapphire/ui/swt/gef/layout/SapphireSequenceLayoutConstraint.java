/*******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 *******************************************************************************/
package org.eclipse.sapphire.ui.swt.gef.layout;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.sapphire.ui.def.HorizontalAlignment;
import org.eclipse.sapphire.ui.def.VerticalAlignment;
import org.eclipse.sapphire.ui.diagram.shape.def.SequenceLayoutConstraintDef;
import org.eclipse.swt.SWT;

/**
 * <code>SapphireSequenceLayoutConstraint</code> is the layout data object associated with
 * <code>SapphireSequenceLayout</code>. To set a <code>SapphireSequenceLayoutConstraint</code> object into a
 * <code>Figure</code>, you use the <code>setConstraint()</code> method of
 * <code>SapphireSequenceLayout</code> to map the <code>Figure</code> to its layout
 * <code>SapphireSequenceLayoutConstraint</code>.
 * <p>
 * NOTE: Do not reuse <code>SapphireSequenceLayoutConstraint</code> objects. Every child in the parent
 * <code>Figure</code> that is managed by the <code>SapphireSequenceLayout</code> must have
 * a unique <code>SapphireSequenceLayoutConstraint</code> object. If the layout data for a Grid member
 * in a <code>SapphireSequenceLayout</code> is null at layout time, a unique
 * <code>SapphireSequenceLayoutConstraint</code> object is created for it.
 * </p>
 * 
 * @see SapphireSequenceLayout
 * 
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 * 
 */
public final class SapphireSequenceLayoutConstraint {
	/**
	 * verticalAlignment specifies how figures will be positioned vertically
	 * within a cell.
	 * 
	 * The default value is CENTER.
	 * 
	 * Possible values are:
	 * 
	 * SWT.BEGINNING (or SWT.TOP): Position the figure at the top of the cell
	 * SWT.CENTER: Position the figure in the vertical center of the cell
	 * SWT.END (or SWT.BOTTOM): Position the figure at the bottom of the cell
	 * SWT.FILL: Resize the figure to fill the cell vertically
	 */
	public int verticalAlignment = SWT.CENTER;

	/**
	 * horizontalAlignment specifies how figures will be positioned horizontally
	 * within a cell.
	 * 
	 * The default value is BEGINNING.
	 * 
	 * Possible values are:
	 * 
	 * SWT.BEGINNING (or SWT.LEFT): Position the figure at the left of the cell
	 * SWT.CENTER: Position the figure in the horizontal center of the cell
	 * SWT.END (or SWT.RIGHT): Position the figure at the right of the cell
	 * SWT.FILL: Resize the figure to fill the cell horizontally
	 */
	public int horizontalAlignment = SWT.CENTER;

	/**
	 * widthHint specifies a minimum width for the column. A value of
	 * SWT.DEFAULT indicates that no minimum width is specified.
	 * 
	 * The default value is SWT.DEFAULT.
	 */
	public int widthHint = SWT.DEFAULT;

	/**
	 * heightHint specifies a minimum height for the row. A value of SWT.DEFAULT
	 * indicates that no minimum height is specified.
	 * 
	 * The default value is SWT.DEFAULT.
	 */
	public int heightHint = SWT.DEFAULT;
	
	public int minWidth = SWT.DEFAULT;
	
	public int minHeight = SWT.DEFAULT;

	public int maxWidth = SWT.DEFAULT;
	
	public int maxHeight = SWT.DEFAULT;
	
	public boolean expandHorizontally = false;

	public boolean expandVertically = false;
	
	private Insets marginInsets = null;

	int cacheWidth = -1, cacheHeight = -1;
	int[][] cache = new int[2][4];
	int cacheIndex = -1;

	/**
	 * Constructs a new instance of SapphireSequenceLayoutConstraint using default values.
	 */
	public SapphireSequenceLayoutConstraint() {
		this(null);
	}

	/**
	 * Constructs a new instance of GridData according to the SequenceLayoutConstraintDef. 

	 * @param def
	 *            SequenceLayoutConstraintDef definition
	 * 
	 */
	public SapphireSequenceLayoutConstraint(SequenceLayoutConstraintDef def) {
		if (def != null) {
			this.horizontalAlignment = getSwtHorizontalAlignment(def.getHorizontalAlignment().content());
			this.verticalAlignment = getSwtVerticalAlignment(def.getVerticalAlignment().content());
			this.expandHorizontally = def.isExpandHorizontally().content();
			this.expandVertically = def.isExpandVertically().content();
			this.marginInsets = LayoutUtil.calculateMargin(def);

			// cell sizes (min, max and preferred) should be after margins are applied. Substract margins in def, the margins will be added
			// back in SapphireSequenceLayout
			this.widthHint = def.getWidth().content() != null ? def.getWidth().content() - marginInsets.left - marginInsets.right : -1;
			this.heightHint = def.getHeight().content() != null ? def.getHeight().content() - marginInsets.top - marginInsets.bottom : -1;
			this.maxWidth = def.getMaxWidth().content() != null ? def.getMaxWidth().content() - marginInsets.left - marginInsets.right : -1;
			this.maxHeight = def.getMaxHeight().content() != null ? def.getMaxHeight().content() - marginInsets.top - marginInsets.bottom : -1;
			this.minWidth = def.getMinWidth().content() != null ? def.getMinWidth().content() - marginInsets.left - marginInsets.right : -1;
			this.minHeight = def.getMinHeight().content() != null ? def.getMinHeight().content() - marginInsets.top - marginInsets.bottom : -1;
		} else {
			this.marginInsets = new Insets(0, 0, 0, 0);
		}
	}
	
	Dimension computeSize(IFigure figure, boolean flushCache) {
		if (cacheWidth != -1 && cacheHeight != -1) {
			return new Dimension(cacheWidth, cacheHeight);
		}
		for (int i = 0; i < cacheIndex + 1; i++) {
			if (cache[i][0] == widthHint && cache[i][1] == heightHint) {
				cacheWidth = cache[i][2];
				cacheHeight = cache[i][3];
				return new Dimension(cacheWidth, cacheHeight);
			}
		}

		Dimension size = figure.getPreferredSize(widthHint, heightHint)
				.getCopy();
		if (widthHint != -1)
			size.width = widthHint;
		if (heightHint != -1)
			size.height = heightHint;

		if (cacheIndex < cache.length - 1)
			cacheIndex++;
		cache[cacheIndex][0] = widthHint;
		cache[cacheIndex][1] = heightHint;
		cacheWidth = cache[cacheIndex][2] = size.width;
		cacheHeight = cache[cacheIndex][3] = size.height;
		return size;
	}

	void flushCache() {
		cacheWidth = cacheHeight = -1;
		cacheIndex = -1;
	}

	String getName() {
		String string = getClass().getName();
		int index = string.lastIndexOf('.');
		if (index == -1)
			return string;
		return string.substring(index + 1, string.length());
	}

	public String toString() {

		String hAlign = ""; //$NON-NLS-1$
		switch (horizontalAlignment) {
		case SWT.FILL:
			hAlign = "SWT.FILL"; //$NON-NLS-1$
			break;
		case SWT.BEGINNING:
			hAlign = "SWT.BEGINNING"; //$NON-NLS-1$
			break;
		case SWT.LEFT:
			hAlign = "SWT.LEFT"; //$NON-NLS-1$
			break;
		case SWT.END:
			hAlign = "SWT.END"; //$NON-NLS-1$
			break;
		case SWT.RIGHT:
			hAlign = "SWT.RIGHT"; //$NON-NLS-1$
			break;
		case SWT.CENTER:
			hAlign = "SWT.CENTER"; //$NON-NLS-1$
			break;
		default:
			hAlign = "Undefined " + horizontalAlignment; //$NON-NLS-1$
			break;
		}
		String vAlign = ""; //$NON-NLS-1$
		switch (verticalAlignment) {
		case SWT.FILL:
			vAlign = "SWT.FILL"; //$NON-NLS-1$
			break;
		case SWT.BEGINNING:
			vAlign = "SWT.BEGINNING"; //$NON-NLS-1$
			break;
		case SWT.TOP:
			vAlign = "SWT.TOP"; //$NON-NLS-1$
			break;
		case SWT.END:
			vAlign = "SWT.END"; //$NON-NLS-1$
			break;
		case SWT.BOTTOM:
			vAlign = "SWT.BOTTOM"; //$NON-NLS-1$
			break;
		case SWT.CENTER:
			vAlign = "SWT.CENTER"; //$NON-NLS-1$
			break;
		default:
			vAlign = "Undefined " + verticalAlignment; //$NON-NLS-1$
			break;
		}
		String string = getName() + " {"; //$NON-NLS-1$
		string += "horizontalAlignment=" + hAlign + " "; //$NON-NLS-1$ //$NON-NLS-2$
		if (widthHint != SWT.DEFAULT)
			string += "widthHint=" + widthHint + " "; //$NON-NLS-1$ //$NON-NLS-2$
		string += "verticalAlignment=" + vAlign + " "; //$NON-NLS-1$ //$NON-NLS-2$
		if (heightHint != SWT.DEFAULT)
			string += "heightHint=" + heightHint + " "; //$NON-NLS-1$ //$NON-NLS-2$
		if (marginInsets != null)
			string += "marginInsets=" + marginInsets + " "; //$NON-NLS-1$ //$NON-NLS-2$
		string += expandHorizontally ? "expandHorizontally " : "!expandHorizontally ";
		string += expandVertically ? "expandVertically " : "!expandVertically ";
		string = string.trim();
		string += "}"; //$NON-NLS-1$
		return string;

	}
	
	public Insets getMarginInset() {
		return marginInsets;
	}
	
	private static int getSwtHorizontalAlignment(HorizontalAlignment horizontalAlign)
	{
		int swtAlign = SWT.CENTER;
		if (horizontalAlign == HorizontalAlignment.LEFT)
		{
			swtAlign = SWT.LEFT;
		}
		else if (horizontalAlign == HorizontalAlignment.RIGHT)
		{
			swtAlign = SWT.RIGHT;
		}
		return swtAlign;
	}

	private static int getSwtVerticalAlignment(VerticalAlignment verticalAlign)
	{
		int swtAlign = SWT.CENTER;
		if (verticalAlign == VerticalAlignment.TOP)
		{
			swtAlign = SWT.TOP;
		}
		else if (verticalAlign == VerticalAlignment.BOTTOM)
		{
			swtAlign = SWT.BOTTOM;
		}
		return swtAlign;
	}

}
