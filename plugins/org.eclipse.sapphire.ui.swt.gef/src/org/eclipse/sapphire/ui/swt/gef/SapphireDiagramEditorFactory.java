/******************************************************************************
 * Copyright (c) 2015 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 *    Shenxue Zhou - [365019] SapphireDiagramEditor does not work on non-workspace files 
 *    Gregory Amerson - [371576] Support non-local files in SapphireDiagramEditor
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef;

import org.eclipse.sapphire.ui.diagram.ConnectionService;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;
import org.eclipse.sapphire.ui.diagram.layout.DiagramLayoutPersistenceService;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class SapphireDiagramEditorFactory 
{    
    public static DiagramLayoutPersistenceService getLayoutPersistenceService(SapphireDiagramEditorPagePart diagramPart)
    {
    	DiagramLayoutPersistenceService layoutPersistentService = 
    			diagramPart.service(DiagramLayoutPersistenceService.class);
    	
        return layoutPersistentService;     	
    }
        
    public static ConnectionService getConnectionService(SapphireDiagramEditorPagePart diagramPart)
    {
    	ConnectionService connectionService = 
    			diagramPart.service(ConnectionService.class);
    	
        return connectionService;     	
    }
}
