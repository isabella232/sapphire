/******************************************************************************
 * Copyright (c) 2014 SAP and Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP - initial implementation
 *    Shenxue Zhou - adaptation for Sapphire and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.contextbuttons;

import java.awt.Rectangle;

import org.eclipse.sapphire.Color;
import org.eclipse.sapphire.ui.SapphireAction;

/**
 * @author SAP
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class PositionedContextButton {

	/**
	 * The context button entry as described in {@link #getSapphireAction()}
	 * .
	 */
	private SapphireAction sapphireAction;

	/**
	 * The position as described in {@link #getPosition()}.
	 */
	private Rectangle position;

	/**
	 * The line-width as described in {@link #getLineWidth()}.
	 */
	private int lineWidth = 1;

	/**
	 * The corner-radius as described in {@link #getCornerRadius()}.
	 */
	private int cornerRadius = 4;

	/**
	 * The outer line color as described in {@link #getOuterLineColor()}.
	 */
	private Color outerLineColor = new Color(0, 0, 0);

	/**
	 * The middle line color as described in {@link #getMiddleLineColor()}.
	 */
	private Color middleLineColor = new Color(128, 128, 128);

	/**
	 * The fill color as described in {@link #getFillColor()}.
	 */
	private Color fillColor = new Color(192, 192, 192);;

	/**
	 * The default opacity as described in {@link #getDefaultOpacity()}.
	 */
	private double defaultOpacity = 1.0;

	/**
	 * The mouse-over opacity as described in {@link #getMouseOverOpacity()}.
	 */
	private double mouseOverOpacity = 1.0;

	/**
	 * The mouse-down opacity as described in {@link #getMouseDownOpacity()}.
	 */
	private double mouseDownOpacity = 1.0;

	// =============================== constructor ============================

	/**
	 * Creates a new PositionedContextbutton.
	 * 
	 * @param contextButtonEntry
	 *            The context button entry as defined in
	 *            {@link #getSapphireAction()}.
	 * @param position
	 *            The position as described in {@link #getPosition()}.
	 */
	public PositionedContextButton(SapphireAction action, Rectangle position) {
		this.sapphireAction = action;
		this.position = position;
	}

	// =============================== set values =============================

	/**
	 * Sets the graphical attributes for the line(s).
	 * 
	 * @param lineWidth
	 *            The line-width as described in {@link #getLineWidth()}.
	 * @param cornerRadius
	 *            The corner radius as described in {@link #getCornerRadius()}.
	 */
	public final void setLine(int lineWidth, int cornerRadius) {
		this.lineWidth = lineWidth;
		this.cornerRadius = cornerRadius;
	}

	/**
	 * Sets the graphical color attributes.
	 * 
	 * @param outerLineColor
	 *            The color of the outer line as described in
	 *            {@link #getOuterLineColor()}.
	 * @param middleLineColor
	 *            The color of the middle line as described in
	 *            {@link #getMiddleLineColor()}.
	 * @param fillColor
	 *            The fill color as described in {@link #getFillColor()}.
	 */
	public final void setColors(Color outerLineColor, Color middleLineColor, Color fillColor) {
		this.outerLineColor = outerLineColor;
		this.middleLineColor = middleLineColor;
		this.fillColor = fillColor;
	}

	/**
	 * Sets the graphical opacity attributes.
	 * 
	 * @param defaultOpacity
	 *            The default opacity as described in
	 *            {@link #getDefaultOpacity()}.
	 * @param mouseOverOpacity
	 *            The mouse-over opacity as described in
	 *            {@link #getMouseOverOpacity()}.
	 * @param mouseDownOpacity
	 *            The mouse-down opacity as described in
	 *            {@link #getMouseDownOpacity()}.
	 */
	public final void setOpacity(double defaultOpacity, double mouseOverOpacity, double mouseDownOpacity) {
		this.defaultOpacity = defaultOpacity;
		this.mouseOverOpacity = mouseOverOpacity;
		this.mouseDownOpacity = mouseDownOpacity;
	}

	// =============================== get values =============================

	/**
	 * Returns the context button entry, which contains the logical definition
	 * of the context button. The context button entry is set in the constructor
	 * and can not be changed afterwards.
	 * 
	 * @return The context button entry, which contains the logical definition
	 *         of the context button.
	 */
	public final SapphireAction getSapphireAction() {
		return sapphireAction;
	}

	/**
	 * Returns the position (location and size) of the context button. The
	 * position is defined in absolute coordinates. It is not adjusted to the
	 * current zoom-level. The position is set in the constructor and can not be
	 * changed afterwards.
	 * 
	 * @return The position (location and size) of the context button.
	 */
	public final Rectangle getPosition() {
		return position;
	}

	/**
	 * Returns the line-width of the context button outline(s). It is not
	 * adjusted to the current zoom-level.
	 * 
	 * @return The line-width of the context button outline(s).
	 */
	public final int getLineWidth() {
		return lineWidth;
	}

	/**
	 * Returns the corner radius of the context button outline(s). It is not
	 * adjusted to the current zoom-level.
	 * 
	 * @return The corner radius of the context button outline(s).
	 */
	public final int getCornerRadius() {
		return cornerRadius;
	}

	/**
	 * Returns the color of the outer line of the context button. Directly
	 * inside this outer line the middle line is painted (see
	 * {@link #getMiddleLineColor()}).
	 * 
	 * @return The color of the outer line of the context button.
	 */
	public final Color getOuterLineColor() {
		return outerLineColor;
	}

	/**
	 * Returns the color of the middle line of the context button. It is painted
	 * directly inside the outer line (see {@link #getOuterLineColor()}).
	 * 
	 * @return The color of the middle line of the context button.
	 */
	public final Color getMiddleLineColor() {
		return middleLineColor;
	}

	/**
	 * Returns the fill color of the context button.
	 * 
	 * @return The fill color of the context button.
	 */
	public final Color getFillColor() {
		return fillColor;
	}

	/**
	 * Returns the default opacity of the context button. Values are between 0.0
	 * (invisible) and 1.0 (opaque).
	 * 
	 * @return The default opacity of the context button.
	 */
	public final double getDefaultOpacity() {
		return defaultOpacity;
	}

	/**
	 * Returns the mouse-over opacity of the context button. Values are between
	 * 0.0 (invisible) and 1.0 (opaque).
	 * <p>
	 * The mouse-over opacity is used when the mouse is over the context button,
	 * to give visual feedback to the user.
	 * 
	 * @return The mouse-over opacity of the context button.
	 */
	public final double getMouseOverOpacity() {
		return mouseOverOpacity;
	}

	/**
	 * Returns the mouse-down opacity of the context button. Values are between
	 * 0.0 (invisible) and 1.0 (opaque).
	 * <p>
	 * The mouse-down opacity is used when the context button is pressed, to
	 * give visual feedback to the user.
	 * 
	 * @return The mouse-down opacity of the context button.
	 */
	public final double getMouseDownOpacity() {
		return mouseDownOpacity;
	}
}
