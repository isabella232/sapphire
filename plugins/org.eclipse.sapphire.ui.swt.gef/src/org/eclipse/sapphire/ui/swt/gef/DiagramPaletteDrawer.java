/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef;

import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class DiagramPaletteDrawer extends PaletteDrawer {
	
	private String id;

	public DiagramPaletteDrawer(String label, String id) {
		super(label);
		this.id = id;
	}

	public DiagramPaletteDrawer(String label, ImageDescriptor icon, String id) {
		super(label, icon);
		this.id = id;
	}
	
	public String getId() {
		return this.id;
	}

}
