/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 *    Shenxue Zhou - Refreshing connections attached to the node
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.commands;

import org.eclipse.gef.commands.Command;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ui.diagram.editor.FunctionUtil;
import org.eclipse.sapphire.ui.diagram.editor.TextPart;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class LabelNodeCommand extends Command {
	
	private TextPart textPart;
	private String labelText;

	public LabelNodeCommand(TextPart textPart, String labelText) {
		this.textPart = textPart;
		this.labelText = labelText;
	}

	@Override
	public void execute() 
	{			
		Value<?> prop = FunctionUtil.getFunctionProperty(this.textPart.getLocalModelElement(), 
				this.textPart.getContentFunction());
		prop.write( this.labelText, true );
		
	}
	
}
