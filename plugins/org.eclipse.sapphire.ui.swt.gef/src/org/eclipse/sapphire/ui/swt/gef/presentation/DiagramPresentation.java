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

package org.eclipse.sapphire.ui.swt.gef.presentation;

import org.eclipse.draw2d.IFigure;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.sapphire.ui.swt.gef.DiagramConfigurationManager;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class DiagramPresentation extends SwtPresentation 
{
	private DiagramConfigurationManager configManager;
	private IFigure figure = null;
	
	public DiagramPresentation(final SapphirePart part, final SwtPresentation parent, final DiagramConfigurationManager configManager, final Shell shell)
	{
		super(part, parent, shell);		
		this.configManager = configManager;
	}
	
	@Override
	public void render()
	{
	}

    @Override
    public DiagramPresentation parent()
    {
        return (DiagramPresentation) super.parent();
    }
	
	public IFigure getFigure() 
	{
		return figure;
	}

	public void setFigure(IFigure figure) 
	{
		this.figure = figure;
	}
	
	public void removeFigure()
	{
		this.figure = null;
	}
	
	public DiagramConfigurationManager getConfigurationManager()
	{
		return this.configManager;
	}

}
