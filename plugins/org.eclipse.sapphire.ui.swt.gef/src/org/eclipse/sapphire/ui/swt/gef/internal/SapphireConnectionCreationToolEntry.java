/******************************************************************************
 * Copyright (c) 2016 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.internal;

import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class SapphireConnectionCreationToolEntry extends ConnectionCreationToolEntry 
{
	/**
	 * Constructor for SapphireConnectionCreationToolEntry.
	 * 
	 * @param label
	 *            the label
	 * @param shortDesc
	 *            the description
	 * @param factory
	 *            the CreationFactory
	 * @param iconSmall
	 *            the small icon
	 * @param iconLarge
	 *            the large icon
	 */
	public SapphireConnectionCreationToolEntry(String label, String shortDesc,
			CreationFactory factory, ImageDescriptor iconSmall,
			ImageDescriptor iconLarge) 
	{
		super(label, shortDesc, factory, iconSmall, iconLarge);
		setToolClass(SapphireConnectionCreationTool.class);
		setUserModificationPermission(PERMISSION_NO_MODIFICATION);
	}


}
