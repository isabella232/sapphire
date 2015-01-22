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

package org.eclipse.sapphire.ui.swt.gef.layout;

import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.sapphire.ui.def.MarginPresentation;

/**
 * 
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class LayoutUtil {
	
	private LayoutUtil() {
	}

	public static Insets calculateMargin(MarginPresentation def) {
		final int top = def.getMarginTop().content();
		final int bottom = def.getMarginBottom().content();
		final int left = def.getMarginLeft().content();
		final int right = def.getMarginRight().content();
	
		return new Insets(top, left, bottom, right);
	}

}