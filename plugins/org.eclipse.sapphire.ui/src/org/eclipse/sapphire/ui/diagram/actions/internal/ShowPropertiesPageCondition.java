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

package org.eclipse.sapphire.ui.diagram.actions.internal;

import org.eclipse.sapphire.ui.SapphireCondition;
import org.eclipse.sapphire.ui.forms.PropertiesViewContributorPart;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class ShowPropertiesPageCondition extends SapphireCondition 
{

	@Override
	protected boolean evaluate() 
	{
    	if (getPart() instanceof PropertiesViewContributorPart)
    	{
    		if (((PropertiesViewContributorPart)getPart()).getPropertiesViewContribution() != null)
    		{
    			return true;
    		}
    	}
        return false;
	}

}
