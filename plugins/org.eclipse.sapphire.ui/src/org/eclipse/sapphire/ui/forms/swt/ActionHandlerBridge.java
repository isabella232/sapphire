/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt;

import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireActionHandler;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ActionHandlerBridge extends ActionSystemPartBridge 
{
	private Presentation presentation;
	private SapphireActionHandler sapphireActionHandler;
	
	public ActionHandlerBridge( final Presentation presentation, 
			                    final SapphireActionHandler sapphireActionHandler)
	{
	    super( sapphireActionHandler );
	    
	    this.presentation = presentation;
	    this.sapphireActionHandler = sapphireActionHandler;
	}
	
	@Override
	public void run() 
	{
		this.sapphireActionHandler.execute( this.presentation );
	}

}
