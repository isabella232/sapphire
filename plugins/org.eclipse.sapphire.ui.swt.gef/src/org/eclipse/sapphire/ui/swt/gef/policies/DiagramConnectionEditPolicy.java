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
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipse.sapphire.ui.swt.gef.commands.DeleteConnectionCommand;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramConnectionModel;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class DiagramConnectionEditPolicy extends ComponentEditPolicy 
{
	@Override
	protected Command createDeleteCommand(GroupRequest deleteRequest) 
	{
		Object child = getHost().getModel();
		if (child instanceof DiagramConnectionModel) 
		{
			DiagramConnectionModel connModel = (DiagramConnectionModel)child;
			return new DeleteConnectionCommand(connModel.getPresentation());
		}
		return super.createDeleteCommand(deleteRequest);
	}

}
