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

package org.eclipse.sapphire.ui.swt.gef.commands;

import org.eclipse.gef.commands.Command;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.swt.gef.presentation.DiagramNodePresentation;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class DoubleClickNodeCommand extends Command
{
	private DiagramNodePresentation nodePresentation;
	
	public DoubleClickNodeCommand(DiagramNodePresentation nodePresentation)
	{
		this.nodePresentation = nodePresentation;
	}
	
	@Override
	public boolean canExecute()
	{
        SapphireActionHandler handler = (SapphireActionHandler)this.nodePresentation.part().getDefaultActionHandler();
        return (handler != null && handler.isEnabled());		
	}
	
	@Override
	public void execute() 
	{
        SapphireActionHandler handler = (SapphireActionHandler)this.nodePresentation.part().getDefaultActionHandler();
        if (handler != null && handler.isEnabled())
        {
            handler.execute(this.nodePresentation);
        }            
	}
	
}
