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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sapphire.ui.Rectangle;
import org.eclipse.sapphire.ui.SapphireAction;

/**
 * A very simple implementation of {@link IContextButtonPadData} without any
 * real functionality.
 * 
 * Users may subclass this class.
 * <p>
 * NOTE: By doing so it is also possible to alter the standard behavior of the
 * editor (e.g. change the location of the standard context button pad). This
 * might lead to inconsistent behavior in different editor implemented on top of
 * Graphiti, which might be irritating to users. From a consistency point of
 * view it is advisable in such cases to stick to the Graphiti standard, and to
 * only change it in case you really need to.
 * 
 * @author SAP
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class ContextButtonPadData {

	private List<SapphireAction> topContextButtons;
	private List<SapphireAction> rightContextButtons;
	private Rectangle location;

	/**
	 * Creates a new {@link ContextButtonPadData}.
	 */
	public ContextButtonPadData() {
		this.topContextButtons = new ArrayList<SapphireAction>();
		this.rightContextButtons = new ArrayList<SapphireAction>();
		this.location = new Rectangle(0, 0, 0, 0);
	}

	/**
	 * Returns the context buttons to show along the top edge
	 * of context button pad. It can not be null, but it can be empty. 
	 * <p>
	 * The button list can be changed by working directly on the result list
	 * (e.g. getGenericContextButtons().add()).
	 * <p>
	 * 
	 * @return  The context buttons to show along the top edge of context button pad.
	 * 
	 */
	
	public List<SapphireAction> getTopContextButtons() {
		return this.topContextButtons;
	}

	/**
	 * Returns the context buttons to show along the right edge
	 * of context button pad. It can not be null, but it can be empty. 
	 * If the right edge of the context pad is not long enough, buttons
	 * will wrap to the bottom edge of the context pad
	 * <p>
	 * The button list can be changed by working directly on the result list
	 * (e.g. getGenericContextButtons().add()).
	 * <p>
	 * 
	 * @return  The context buttons to show along the right edge of context button pad.
	 * 
	 */
	public List<SapphireAction> getRightContextButtons() {
		return this.rightContextButtons;
	}

	/**
	 * Returns the location of the context button pad. It can not be null. These
	 * are not the outer bounds of the context button pad, but the inner
	 * rectangle, around which the context button pad is shown. Often these are
	 * the outer bounds of the figure, for which the context button pad is
	 * shown. But in some cases it makes sense to use the outer bounds of an
	 * inner figure or to shrink/enlarge the rectangle.
	 * <p>
	 * The location can be changed by working directly on the result rectangle
	 * (e.g. getPadLocation().setRectangle()).
	 * 
	 * @return The location of the context button pad.
	 */	
	public Rectangle getPadLocation() {
		return this.location;
	}
}
