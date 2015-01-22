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
import org.eclipse.sapphire.ui.SapphireAction;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ActionBridge extends ActionSystemPartBridge 
{
	private Presentation presentation;
	private SapphireAction sapphireAction;
	
	public ActionBridge( final Presentation presentation, 
			             final SapphireAction sapphireAction)
	{
	    super( sapphireAction );
	    
	    this.presentation = presentation;
	    this.sapphireAction = sapphireAction;
	}
	
	@Override
	public void run() 
	{
	    this.sapphireAction.getFirstActiveHandler().execute( this.presentation );
	}

}
