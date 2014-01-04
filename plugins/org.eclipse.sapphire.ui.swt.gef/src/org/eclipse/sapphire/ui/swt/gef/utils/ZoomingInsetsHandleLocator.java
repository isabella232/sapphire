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

package org.eclipse.sapphire.ui.swt.gef.utils;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Locator;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.handles.HandleBounds;

/*
 * @author SAP
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class ZoomingInsetsHandleLocator implements Locator
{
	private IFigure reference;
	private Insets handleInsets;

	/**
	 * Creates a new ZoomingInsetsHandleLocator.
	 * 
	 * @param reference
	 *            The target bounds are calculated depending on this reference
	 *            figure.
	 * @param configurationProvider
	 *            The configuration provider which can be used to access the
	 *            environment.
	 * @param handleInsets
	 *            The insets to apply to the reference figure bounds.
	 */
	public ZoomingInsetsHandleLocator(IFigure reference, Insets handleInsets) {
		this.reference = reference;
		this.handleInsets = handleInsets;
	}

	/**
	 * Sets the bounds of the target figure as described above.
	 * 
	 * @param target
	 *            The target figure for which to set the bounds.
	 */
	public void relocate(IFigure target) {
		Insets insets = new Insets(handleInsets);
		Rectangle bounds;
		if (reference instanceof HandleBounds)
			bounds = ((HandleBounds) reference).getHandleBounds();
		else
			bounds = reference.getBounds();

		bounds = new PrecisionRectangle(bounds.getCopy());
		reference.translateToAbsolute(bounds);
		target.translateToRelative(bounds);
		bounds.translate(-insets.left, -insets.top);
		bounds.resize(insets.getWidth(), insets.getHeight());

		target.setBounds(bounds);
	}

}
