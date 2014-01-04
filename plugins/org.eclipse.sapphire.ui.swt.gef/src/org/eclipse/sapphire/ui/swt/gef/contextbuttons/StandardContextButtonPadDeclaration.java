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
 * An implementation of {@link IContextButtonPadDeclaration} which is based on
 * the UX guidelines.
 * 
 * @author SAP
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class StandardContextButtonPadDeclaration extends AbstractContextButtonPadDeclaration {

	private static final Color PAD_OUTER_LINE_COLOR = new Color(173, 191, 204);

	private static final Color PAD_MIDDLE_LINE_COLOR = new Color(255, 255, 255);

	private static final Color PAD_INNER_LINE_COLOR = new Color(245, 249, 251);

	private static final Color PAD_FILL_COLOR = new Color(235, 243, 247);

	private static final Color BUTTON_OUTER_LINE_COLOR = new Color(46, 101, 140);

	private static final Color BUTTON_MIDDLE_LINE_COLOR = new Color(255, 255, 255);

	private static final Color BUTTON_FILL_COLOR = PAD_FILL_COLOR;

	public StandardContextButtonPadDeclaration(ContextButtonPadData contextButtonPadData) {
		super(contextButtonPadData);
	}

	// ======================== overwritten size getter =======================

	@Override
	protected int getButtonSize() {
		return 20;
	}

	@Override
	protected int getButtonPadding() {
		return 1;
	}

	@Override
	protected int getCollapseButtonPadding() {
		return 10;
	}

	@Override
	protected int getPadPaddingOutside() {
		return 10;
	}

	@Override
	protected int getPadPaddingInside() {
		return 4;
	}

	@Override
	protected int getPadHorizontalOverlap() {
		return 4;
	}

	@Override
	protected int getPadVerticalOverlap() {
		return 4;
	}

	@Override
	public int getPadAppendageLength() {
		return 8;
	}

	// ===================== overwritten drawing getter =======================

	public int getPadLineWidth() {
		return 1;
	}

	public int getPadCornerRadius() {
		return 12;
	}

	public Color getPadOuterLineColor() {
		return PAD_OUTER_LINE_COLOR;
	}

	public Color getPadMiddleLineColor() {
		return PAD_MIDDLE_LINE_COLOR;
	}

	public Color getPadInnerLineColor() {
		return PAD_INNER_LINE_COLOR;
	}

	public Color getPadFillColor() {
		return PAD_FILL_COLOR;
	}

	public double getPadDefaultOpacity() {
		return 0.9;
	}

	// ===================== overwritten button creators ======================

	@Override
	public PositionedContextButton createButton(SapphireAction action, Rectangle position) {
		PositionedContextButton ret = new PositionedContextButton(action, position);
		ret.setLine(1, 4);
		ret.setColors(BUTTON_OUTER_LINE_COLOR, BUTTON_MIDDLE_LINE_COLOR, BUTTON_FILL_COLOR);
		ret.setOpacity(0.0, 0.7, 1.0);
		return ret;
	}
}
