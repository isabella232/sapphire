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

package org.eclipse.sapphire.workspace.ui.services.internal;

import java.io.File;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.sapphire.modeling.FileResourceStore;
import org.eclipse.sapphire.modeling.StatusException;
import org.eclipse.sapphire.modeling.util.internal.FileUtil;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.diagram.def.IDiagramEditorPageDef;
import org.eclipse.sapphire.ui.diagram.def.LayoutPersistence;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;
import org.eclipse.sapphire.ui.diagram.layout.standard.StandardDiagramLayout;
import org.eclipse.sapphire.ui.diagram.layout.standard.StandardDiagramLayoutPersistenceService;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
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
			SapphireUiFrameworkPlugin.log( e );
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
	
    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
        	ISapphirePart part = context.find(ISapphirePart.class);
        	if (part instanceof SapphireDiagramEditorPagePart)
        	{
        		SapphireDiagramEditorPagePart diagramPagePart = (SapphireDiagramEditorPagePart)part;
        		IDiagramEditorPageDef pageDef = diagramPagePart.getPageDef();
        		if (pageDef.getLayoutPersistence().getContent() == LayoutPersistence.WORKSPACE)
        		{
        			return true;
        		}
        	}
        	return false;
        }
    
        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new WorkspaceDiagramLayoutPersistenceService();
        }
    }
	
}
