/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 *    Shenxue Zhou - simplify the delete logic by calling Sapphire delete action
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.commands;

import org.eclipse.gef.commands.Command;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.swt.gef.presentation.DiagramNodePresentation;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class DeleteNodeCommand extends Command 
{
	
    private static final String DELETE_ACTION_ID = "Sapphire.Delete";

    private DiagramNodePresentation nodePresentation;

	public DeleteNodeCommand(DiagramNodePresentation nodePresentation) 
	{
		this.nodePresentation = nodePresentation;
	}

	@Override
	public boolean canExecute() 
	{
		SapphireActionHandler deleteActionHandler = this.nodePresentation.part().getAction(DELETE_ACTION_ID).getFirstActiveHandler();
		return deleteActionHandler.isEnabled();
	}
		
	@Override
	public void execute() 
	{
        SapphireActionHandler deleteActionHandler = this.nodePresentation.part().getAction(DELETE_ACTION_ID).getFirstActiveHandler();
        deleteActionHandler.execute(this.nodePresentation);		
		
	}
	
}
