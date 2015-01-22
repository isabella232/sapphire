/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class DefaultActionImage 
{
	private static Image defaultImage;
	
	public static Image getDefaultActionImage()
	{
		if (defaultImage == null)
		{
			Bundle bundle = Platform.getBundle("org.eclipse.sapphire.ui");
			URL url = bundle.getResource("org/eclipse/sapphire/ui/actions/Default.png");
			ImageDescriptor defaultImageDescriptor = ImageDescriptor.createFromURL(url);	
			defaultImage = defaultImageDescriptor.createImage();
		}
		return defaultImage;
	}
	
}
