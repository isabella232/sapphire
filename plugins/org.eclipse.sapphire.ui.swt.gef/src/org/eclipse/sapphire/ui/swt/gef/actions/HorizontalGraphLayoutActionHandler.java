/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.actions;

import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.swt.gef.SapphireDiagramEditor;
import org.eclipse.sapphire.ui.swt.gef.layout.HorizontalGraphLayout;
import org.eclipse.sapphire.ui.swt.gef.presentation.DiagramPresentation;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class HorizontalGraphLayoutActionHandler extends SapphireActionHandler 
{
	@Override
	protected Object run( final Presentation context ) 
	{
		DiagramPresentation diagramPresentation = (DiagramPresentation)context;
		SapphireDiagramEditor diagramEditor = diagramPresentation.getConfigurationManager().getDiagramEditor();
		new HorizontalGraphLayout().layout(diagramEditor);
		
		return null;
	}
	
}
