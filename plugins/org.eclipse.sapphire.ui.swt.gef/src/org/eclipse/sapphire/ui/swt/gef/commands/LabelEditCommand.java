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
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ui.diagram.editor.FunctionUtil;
import org.eclipse.sapphire.ui.diagram.editor.TextPart;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class LabelEditCommand extends Command 
{
	private TextPart textPart;
	private String labelText;

	public LabelEditCommand(TextPart textPart, String labelText) 
	{
		this.textPart = textPart;
		this.labelText = labelText;
	}

	@Override
	public void execute() 
	{
		Value<?> prop = FunctionUtil.getFunctionProperty(this.textPart.getLocalModelElement(), 
				this.textPart.getContentFunction());
		prop.write( this.labelText );
	}

}
