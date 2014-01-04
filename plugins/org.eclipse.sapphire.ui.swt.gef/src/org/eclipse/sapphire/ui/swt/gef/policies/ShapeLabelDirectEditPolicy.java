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

package org.eclipse.sapphire.ui.swt.gef.policies;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.DirectEditPolicy;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.sapphire.ui.diagram.editor.TextPart;
import org.eclipse.sapphire.ui.swt.gef.commands.LabelEditCommand;
import org.eclipse.sapphire.ui.swt.gef.parts.DiagramNodeEditPart;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class ShapeLabelDirectEditPolicy extends DirectEditPolicy 
{

	@Override
	protected Command getDirectEditCommand(DirectEditRequest request) 
	{
		String labelText = (String) request.getCellEditor().getValue();
				
		TextPart textPart = (TextPart)request.getExtendedData().get(DiagramNodeEditPart.DIRECT_EDIT_REQUEST_PARAM);
		if (textPart != null)
		{
			LabelEditCommand command = new LabelEditCommand(textPart, labelText);
			return command;
		}
		return null;
	}

	@Override
	protected void showCurrentEditValue(DirectEditRequest request) 
	{
		// TODO Auto-generated method stub
		
	}

}
