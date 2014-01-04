/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Ling Hao - [383924] Flexible diagram node shapes
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.editor;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class DiagramPageEvent extends DiagramPartEvent 
{
    public static enum DiagramPageEventType
    {
    	GridStateChange,
    	GuideStateChange,
    	DiagramChange,
    	DiagramSave,
    	SelectAll,
    	SelectAllNodes
    }
    
    private DiagramPageEventType diagramPageEventType;

    public DiagramPageEvent(final SapphireDiagramEditorPagePart part)
	{
		 super(part);
	}

	public DiagramPageEventType getDiagramPageEventType() {
		return diagramPageEventType;
	}

	public void setDiagramPageEventType(DiagramPageEventType diagramPageEventType) {
		this.diagramPageEventType = diagramPageEventType;
	}

}
