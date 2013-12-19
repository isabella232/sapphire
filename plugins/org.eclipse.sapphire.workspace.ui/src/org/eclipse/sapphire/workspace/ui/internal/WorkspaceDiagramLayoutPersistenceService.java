/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - [382431] Inconsistent terminology: layout storage and layout persistence
 ******************************************************************************/

package org.eclipse.sapphire.workspace.ui.internal;

import java.io.File;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.modeling.FileResourceStore;
import org.eclipse.sapphire.modeling.StatusException;
import org.eclipse.sapphire.modeling.util.internal.FileUtil;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.diagram.def.DiagramEditorPageDef;
import org.eclipse.sapphire.ui.diagram.def.LayoutPersistence;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;
import org.eclipse.sapphire.ui.diagram.layout.standard.StandardDiagramLayout;
import org.eclipse.sapphire.ui.diagram.layout.standard.StandardDiagramLayoutPersistenceService;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class WorkspaceDiagramLayoutPersistenceService extends
		StandardDiagramLayoutPersistenceService 
{
	private static final String WORKSPACE_LAYOUT_FOLDER = ".metadata/.plugins/org.eclipse.sapphire.ui.diagram/layouts";
	
	@Override
	protected StandardDiagramLayout initLayoutModel() 
	{
		StandardDiagramLayout layoutModel = null;
		try
		{
			String fileName = computeLayoutFileName(this.editorInput);
			if (fileName != null)
			{
				File layoutFile = getLayoutPersistenceFile(fileName);
				final XmlResourceStore resourceStore = new XmlResourceStore( new FileResourceStore(layoutFile));
				layoutModel = StandardDiagramLayout.TYPE.instantiate(new RootXmlResource( resourceStore ));			
			}
		}
		catch (Exception e)
		{
		    Sapphire.service( LoggingService.class ).log( e );
		}
		return layoutModel;
	}
	
	private File getLayoutPersistenceFile(String fileName) throws StatusException
	{
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        File layoutFolder = workspaceRoot.getLocation().toFile();
        layoutFolder = new File(layoutFolder, WORKSPACE_LAYOUT_FOLDER);
        if (!layoutFolder.exists())
        {
        	FileUtil.mkdirs(layoutFolder);
        }
        File layoutFile = new File (layoutFolder, fileName);
        return layoutFile;
	}
	
    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
        	ISapphirePart part = context.find(ISapphirePart.class);
        	if (part instanceof SapphireDiagramEditorPagePart)
        	{
        		SapphireDiagramEditorPagePart diagramPagePart = (SapphireDiagramEditorPagePart)part;
        		DiagramEditorPageDef pageDef = diagramPagePart.getPageDef();
        		if (pageDef.getLayoutPersistence().content() == LayoutPersistence.WORKSPACE)
        		{
        			return true;
        		}
        	}
        	return false;
        }
    }
	
}
