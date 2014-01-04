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

package org.eclipse.sapphire.ui.swt.gef.internal;

import org.eclipse.draw2d.Label;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.services.PossibleValuesService;
import org.eclipse.sapphire.ui.diagram.editor.FunctionUtil;
import org.eclipse.sapphire.ui.diagram.editor.TextPart;
import org.eclipse.sapphire.ui.swt.gef.parts.NodeDirectEditManager;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class DirectEditorManagerFactory
{
	public static DirectEditManager createDirectEditorManager(GraphicalEditPart source, TextPart textPart, CellEditorLocator locator, Label label)
	{
		Value<?> property = FunctionUtil.getFunctionProperty(textPart.getLocalModelElement(), 
				textPart.getContentFunction());
		PropertyDef definition = property.definition();
		PossibleValuesService possibleValuesService = property.service(PossibleValuesService.class);
		if (possibleValuesService != null)
		{
			return new ComboBoxDirectEditorManager(source, textPart, locator, label);
		}
		else
		{
			return new NodeDirectEditManager(source, textPart, locator, label);
		}
	}
}
