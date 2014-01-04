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

package org.eclipse.sapphire.ui.diagram.actions;

import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.def.SapphireActionType;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class ToggleDiagramGuidesActionHandler extends SapphireActionHandler 
{
    @Override
    public void init( final SapphireAction action,
                      final ActionHandlerDef def )
    {
    	super.init(action, def);
    	if (action.getType() == SapphireActionType.TOGGLE)
    	{
    		SapphireDiagramEditorPagePart diagramPart = (SapphireDiagramEditorPagePart)this.getPart();
    		setChecked(diagramPart.isShowGuides());
    	}
    }
    		
	@Override
	protected Object run(Presentation context) 
	{
		SapphireDiagramEditorPagePart diagramPart = (SapphireDiagramEditorPagePart)this.getPart();
		boolean showGuides = !diagramPart.isShowGuides();
		diagramPart.setShowGuides(showGuides);
		return null;
	}
}
