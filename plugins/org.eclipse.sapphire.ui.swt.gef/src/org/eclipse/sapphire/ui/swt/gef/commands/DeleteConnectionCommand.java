/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.commands;

import org.eclipse.gef.commands.Command;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.swt.gef.presentation.DiagramConnectionPresentation;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class DeleteConnectionCommand extends Command 
{
    private static final String DELETE_ACTION_ID = "Sapphire.Delete";

    private DiagramConnectionPresentation presentation;
    
	public DeleteConnectionCommand(DiagramConnectionPresentation presentation) 
	{
		this.presentation = presentation;
	}
	
	@Override
	public boolean canExecute() 
	{
		SapphireActionHandler deleteActionHandler = this.presentation.part().getAction(DELETE_ACTION_ID).getFirstActiveHandler();
		return deleteActionHandler.isEnabled();
	}
	

	@Override
	public void execute() 
	{
        SapphireActionHandler deleteActionHandler = this.presentation.part().getAction(DELETE_ACTION_ID).getFirstActiveHandler();
        deleteActionHandler.execute(this.presentation);		
	}
}
