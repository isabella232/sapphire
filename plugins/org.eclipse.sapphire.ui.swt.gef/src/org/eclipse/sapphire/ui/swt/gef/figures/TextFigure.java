/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Ling Hao - [383924] Extend Sapphire Diagram Framework to support SQL Schema diagram like editors
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.figures;

import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.editparts.ZoomListener;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.sapphire.ui.def.HorizontalAlignment;
import org.eclipse.sapphire.ui.def.VerticalAlignment;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramResourceCache;
import org.eclipse.sapphire.ui.swt.gef.presentation.TextPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class TextFigure extends Label implements IShapeFigure
{
	private DiagramResourceCache resourceCache;
	private TextPresentation textPresentation;
	private Rectangle availableArea;
	private int horizontalAlignment;
	private int verticalAlignment;
	private org.eclipse.draw2d.geometry.Point textLocation;
	private Text swtText;
	private double cachedZoom = -1.0;
	private Font scaledFont;
	private ZoomManager zoomMgr;
	private ZoomListener zoomListener = new ZoomListener() {
		public void zoomChanged(double newZoom) {
			updateScaledFont(newZoom);
		}
	};	
	
	public TextFigure(DiagramResourceCache resourceCache, TextPresentation textPresentation)
	{
		this.resourceCache = resourceCache;
		this.textPresentation = textPresentation;
		setForegroundColor(resourceCache.getColor(textPresentation.getTextColor()));
		this.horizontalAlignment = getSwtTextAlignment(textPresentation.getLayoutConstraint().getHorizontalAlignment().content());
		setLabelAlignment(PositionConstants.CENTER);
		// TODO how to reconcile both horizontal and vertical alignment with draw2d label alignment
		this.verticalAlignment = getSwtTextAlignment(textPresentation.getLayoutConstraint().getVerticalAlignment().content());
		setFont(this.resourceCache.getFont(textPresentation.getFontDef()));
		
		GraphicalViewer viewer = textPresentation.getConfigurationManager().getDiagramEditor().getGraphicalViewer();
		Composite composite = (Composite)viewer.getControl();
		swtText = new Text(composite, SWT.NONE);
		swtText.setVisible(false);
		zoomMgr = (ZoomManager) viewer.getProperty(ZoomManager.class.toString());
		// this will force the font to be set
		cachedZoom = -1.0;
		updateScaledFont(zoomMgr.getZoom());
		zoomMgr.addZoomListener(zoomListener);
		
		setText(textPresentation.getContent());
	}
	
	public Rectangle getAvailableArea() {
		Rectangle nodeBounds = getClientArea();
		if (this.availableArea != null) {
			return new Rectangle(this.availableArea.x + nodeBounds.x, this.availableArea.y + nodeBounds.y,
					this.availableArea.width, this.availableArea.height);
		} else {
			return nodeBounds;
		}
	}

	public void setAvailableArea(Rectangle availableArea) {
		// Translate the available area to relative to the client area. We don't need to
		// adjust the available area when node is moved.
		Rectangle nodeBounds = getClientArea();
		this.availableArea = new Rectangle(availableArea.x - nodeBounds.x, availableArea.y - nodeBounds.y,
				availableArea.width, availableArea.height);
		
	}

	public int getHorizontalAlignment() {
		return horizontalAlignment;
	}

	public int getVerticalAlignment() {
		return this.verticalAlignment;
	}
	
	public void setHorizontalAlignment(int horizontalAlignment) {
		this.horizontalAlignment = horizontalAlignment;
		setLabelAlignment(this.horizontalAlignment);
	}
	
	public TextPresentation getTextPresentation()
	{
		return this.textPresentation;
	}
	
	@Override
	public void setText(String s) {
		// "text" will never be null.
		if (s == null)
			s = "";//$NON-NLS-1$
		if (getText().equals(s))
			return;
		swtText.setText(s);
		super.setText(s);
	}

	public Dimension getMinimumSize(int w, int h) 
	{
		if (minSize != null)
			return minSize;
		
		minSize = new Dimension();
		if (getLayoutManager() != null)
			minSize.setSize(getLayoutManager().getMinimumSize(this, w, h));

		Dimension labelSize;
		if (getTextPresentation().truncatable())
		{
			labelSize = calculateLabelSize(getTextUtilities()
				.getTextExtents(getTruncationString(), getFont())
				.intersect(
						getTextUtilities().getTextExtents(getText(), getFont())));
		}
		else
		{
			labelSize = calculateLabelSize(getTextUtilities().getTextExtents(getText(), getFont()));
		}
		Insets insets = getInsets();
		labelSize.expand(insets.getWidth(), insets.getHeight());
		minSize.union(labelSize);
		return minSize;
	}
	
	@Override
	public Dimension getMaximumSize()
	{
		return getPreferredSize();
	}
	
	@Override
	public Dimension getPreferredSize(int wHint, int hHint) {
		if (prefSize == null) {
			Point textSize = swtText.computeSize(-1, -1);
			double zoom = zoomMgr.getZoom();
			if (zoom != 1.0) {
				textSize.x = (int)(textSize.x / zoom);
				textSize.y = (int)(textSize.y / zoom);
			}
			prefSize = new Dimension(textSize.x, textSize.y);
			Insets insets = getInsets();
			prefSize.expand(insets.getWidth(), insets.getHeight());
			if (getLayoutManager() != null)
				prefSize.union(getLayoutManager().getPreferredSize(this, wHint,
						hHint));
		}
		if (wHint >= 0 && wHint < prefSize.width) {
			Dimension minSize = getMinimumSize(wHint, hHint);
			Dimension result = prefSize.getCopy();
			result.width = Math.min(result.width, wHint);
			result.width = Math.max(minSize.width, result.width);
			return result;
		}
		return prefSize;
	}
	
	/**
	 * Returns the location of the label's text relative to the label.
	 * 
	 * @return the text location
	 * @since 2.0
	 */
	@Override
	protected org.eclipse.draw2d.geometry.Point getTextLocation() {
		if (textLocation != null)
			return textLocation;
		
		textLocation = new org.eclipse.draw2d.geometry.Point();
		Dimension offset = getSize().getDifference(getTextSize());
		offset.width += getTextSize().width - getSubStringTextSize().width;
		switch (getLabelAlignment()) {
		case CENTER:
			offset.scale(0.5f);
			break;
		case LEFT:
			offset.scale(0.0f);
			break;
		case RIGHT:
			offset.scale(1.0f);
			break;
		case TOP:
			offset.height = 0;
			offset.scale(0.5f);
			break;
		case BOTTOM:
			offset.height = offset.height * 2;
			offset.scale(0.5f);
			break;
		default:
			offset.scale(0.5f);
			break;
		}

		textLocation.translate(offset);
		return textLocation;
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		textLocation = null;
	}
	
	private int getSwtTextAlignment(HorizontalAlignment horizontalAlign)
	{
		int alignment = PositionConstants.CENTER;
		switch (horizontalAlign) 
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
	
	private int getSwtTextAlignment(VerticalAlignment verticalAlign)
	{
		int alignment = PositionConstants.CENTER;
		switch (verticalAlign) 
		{
			case TOP:
				alignment = PositionConstants.TOP;
				break;
			case BOTTOM:
				alignment = PositionConstants.BOTTOM;
				break;
			default:			
				break;
		}
		return alignment;
	}

	@Override
	public void setSelected(boolean b) 
	{
	}

	@Override
	public void setFocus(boolean b) 
	{
	}
	
	private void updateScaledFont(double zoom) {
		if (cachedZoom == zoom)
			return;
		Font font = this.getFont();

		disposeScaledFont();
		cachedZoom = zoom;
		if (zoom == 1.0) {
			swtText.setFont(font);
		}
		else {
			FontData fd = font.getFontData()[0];
			fd.setHeight((int) (fd.getHeight() * zoom));
			this.scaledFont = new Font(null, fd);
			swtText.setFont(this.scaledFont);
		}
	}
	
	private void disposeScaledFont() {
		if (scaledFont != null) {
			scaledFont.dispose();
			scaledFont = null;
		}
	}
	
	
}
