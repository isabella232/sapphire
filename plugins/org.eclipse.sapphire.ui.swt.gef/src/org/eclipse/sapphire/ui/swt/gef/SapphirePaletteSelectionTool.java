/******************************************************************************
 * Copyright (c) 2016 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef;

import org.eclipse.gef.tools.SelectionTool;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class SapphirePaletteSelectionTool extends SelectionTool 
{
	protected boolean handleKeyDown(KeyEvent e) 
	{
		if (handleAbort(e)) {
			loadDefaultTool();
			return true;
		}
		return super.handleKeyDown(e);
	}

	private boolean handleAbort(KeyEvent e) 
	{
		if (e.keyCode == SWT.ESC) {
			return true;
		}
		return false;
	}
	
	private PaletteViewer getPaletteViewer() 
	{
		return (PaletteViewer) getCurrentViewer();
	}
	
	private void loadDefaultTool() 
	{
		getPaletteViewer().setActiveTool(
				getPaletteViewer().getPaletteRoot().getDefaultEntry());
	}
	
}
