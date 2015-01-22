/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.figures;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LayoutListener;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.TextUtilities;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.sapphire.ui.Bounds;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramResourceCache;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class NodeFigure extends RoundedRectangle { 
	
	private static final org.eclipse.sapphire.Color DEFAULT_TEXT_FOREGROUND = new org.eclipse.sapphire.Color(51, 51, 153);
    private static final org.eclipse.sapphire.Color DEFAULT_NODE_FOREGROUND = new org.eclipse.sapphire.Color(51, 51, 153);
	//EEF6FD D0E6F9 ACD2F4 81B9EA 9ABFE0
    //F8FBFE EDF5FC DEEDFA D4E7F8 E2E5E9
    private static final org.eclipse.sapphire.Color DEFAULT_BACKGROUND_START = new org.eclipse.sapphire.Color(0xFF, 0xFF, 0xFF);
    private static final org.eclipse.sapphire.Color DEFAULT_BACKGROUND_END = new org.eclipse.sapphire.Color(0xD4, 0xE7, 0xF8);

    private static final org.eclipse.sapphire.Color SELECTED_BACKGROUND = new org.eclipse.sapphire.Color(0xAC, 0xD2, 0xF4);
    
	// drag/drop select color
    //private static final org.eclipse.sapphire.ui.Color DRAG_DROP_SELECT_BACKGROUND = new org.eclipse.sapphire.ui.Color(0xD0, 0xE6, 0xF9);
        
    private boolean hasImage;
    private DiagramResourceCache resourceCache;

    private Label labelFigure;
    private Label iconFigure;

    private boolean selected;
	private boolean hasFocus;

	public NodeFigure(boolean hasImage, DiagramResourceCache resourceCache) {
		this.hasImage = hasImage;
		this.resourceCache = resourceCache;
		
		this.setForegroundColor(resourceCache.getColor(DEFAULT_NODE_FOREGROUND));
		setLayoutManager(new XYLayout());

		labelFigure = new Label();
		labelFigure.setForegroundColor(resourceCache.getColor(DEFAULT_TEXT_FOREGROUND));
		labelFigure.setLabelAlignment(PositionConstants.CENTER);
		labelFigure.setFont(resourceCache.getDefaultFont());
		this.add(labelFigure);

		if (hasImage) {
			iconFigure = new Label();
			this.add(iconFigure);
		}
		
		this.addLayoutListener(new LayoutListener.Stub() {
			public void postLayout(IFigure container) {
				setTooltipText();
			}
		});
		
	}
	
	public boolean hasBorder() {
		return !hasImage;
	}
	
	public void refreshConstraints(Bounds labelBounds, Bounds iconBounds) {
		Rectangle labelFigureConstraint = new Rectangle(labelBounds.getX(), labelBounds.getY(), labelBounds.getWidth(), labelBounds.getHeight());
		getLayoutManager().setConstraint(labelFigure, labelFigureConstraint);
		if (iconBounds != null) {
			Rectangle iconFigureConstraint = new Rectangle(iconBounds.getX(), iconBounds.getY(), iconBounds.getWidth(), iconBounds.getHeight());
			getLayoutManager().setConstraint(iconFigure, iconFigureConstraint);
		}
	}
	
	public DiagramResourceCache getDiagramResourceCache() {
		return this.resourceCache;
	}
	
	public Label getLabelFigure() {
		return this.labelFigure;
	}
	
	public void setText(String text) {
		labelFigure.setText(text);
		setTooltipText(text);
	}
	
	private void setTooltipText() {
		setTooltipText(labelFigure.getText());
	}
 	
	private void setTooltipText(String text) {
		Dimension d = TextUtilities.INSTANCE.getStringExtents(text, labelFigure.getFont());
		if (d.width + 5 >= getBounds().width) {
			this.setToolTip(new Label(text));
		} else {
			this.setToolTip(null);
		}
	}
	
	public void setImage(Image image) {
		if (iconFigure != null && image != null) {
			iconFigure.setIcon(image);
		}
	}
	
	@Override
	protected void fillShape(Graphics graphics) {
		if (hasImage) {
			return;
		}
		
		final Dimension cornerDimension = new Dimension(1, 1);
		Rectangle fillRectangle = new Rectangle(getBounds());
		fillRectangle = fillRectangle.shrink(cornerDimension.width, cornerDimension.height);
		
		final Color foregroundSave = graphics.getForegroundColor();
		final Color backgroundSave = graphics.getBackgroundColor();
		
		if (selected) {
			graphics.setBackgroundColor(resourceCache.getColor(SELECTED_BACKGROUND));
			graphics.fillRectangle(fillRectangle);
		} else {
			graphics.setForegroundColor(resourceCache.getColor(DEFAULT_BACKGROUND_END));
			graphics.setBackgroundColor(resourceCache.getColor(DEFAULT_BACKGROUND_START));
			
			graphics.fillGradient(fillRectangle.x, fillRectangle.y, fillRectangle.width, fillRectangle.height, true/*vertical*/);
		}
		
		graphics.setForegroundColor(foregroundSave);
		graphics.setBackgroundColor(backgroundSave);
	}

	@Override
	protected void outlineShape(Graphics graphics) {

		float lineInset = Math.max(1.0f, getLineWidthFloat()) / 2.0f;
		int inset1 = (int) Math.floor(lineInset) + 1;
		int inset2 = (int) Math.ceil(lineInset) + 1;

		Rectangle r = Rectangle.SINGLETON.setBounds(getBounds());
		r.x += inset1;
		r.y += inset1;
		r.width -= inset1 + inset2;
		r.height -= inset1 + inset2;

		final Dimension cornerDimension = new Dimension(8, 8); 
		if (hasImage == false) {
			graphics.drawRoundRectangle(r,
					Math.max(0, cornerDimension.width - (int) lineInset),
					Math.max(0, cornerDimension.height - (int) lineInset));
		}
		
		if (hasFocus || selected) {
			final Color foregroundSave = graphics.getForegroundColor();
			graphics.setForegroundColor(resourceCache.getOutlineColor());
			Rectangle expanded = r.getExpanded(1, 1);
			graphics.setLineStyle(SWT.LINE_DASH);
			graphics.drawRoundRectangle(expanded,
					Math.max(0, cornerDimension.width - (int) lineInset),
					Math.max(0, cornerDimension.height - (int) lineInset));
			graphics.setForegroundColor(foregroundSave);
		}
	}

	public void setSelected(boolean b) {
		selected = b;
		repaint();
	}

	public void setFocus(boolean b) {
		hasFocus = b;
		repaint();
	}
}
