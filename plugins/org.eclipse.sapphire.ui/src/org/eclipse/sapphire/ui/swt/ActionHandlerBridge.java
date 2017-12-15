/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt;

import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireRenderingContext;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ActionHandlerBridge extends ActionSystemPartBridge 
{
	private SapphireRenderingContext sapphireRenderingContext;
	private SapphireActionHandler sapphireActionHandler;
	
	public ActionHandlerBridge( final SapphireRenderingContext sapphireRenderingContext, 
			                    final SapphireActionHandler sapphireActionHandler)
	{
	    super( sapphireActionHandler );
	    
	    this.sapphireRenderingContext = sapphireRenderingContext;
	    this.sapphireActionHandler = sapphireActionHandler;
	}
	
	@Override
	public void run() 
	{
		this.sapphireActionHandler.execute( this.sapphireRenderingContext );
	}

}
