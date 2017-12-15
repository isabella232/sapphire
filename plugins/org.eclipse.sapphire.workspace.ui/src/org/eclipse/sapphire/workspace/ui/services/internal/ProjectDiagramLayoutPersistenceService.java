/******************************************************************************
 * Copyright (c) 2013 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Gregory Amerson - [377381] Cannot share diagram layout between projects
 *    Konstantin Komissarchik - [382431] Inconsistent terminology: layout storage and layout persistence
 ******************************************************************************/

package org.eclipse.sapphire.workspace.ui.services.internal;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.sapphire.modeling.StatusException;
import org.eclipse.sapphire.modeling.util.MiscUtil;
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
import org.eclipse.sapphire.workspace.WorkspaceFileResourceStore;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.part.FileEditorInput;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class ProjectDiagramLayoutPersistenceService extends StandardDiagramLayoutPersistenceService
{

    @Override
    protected String computeLayoutFileName( IEditorInput editorInput ) throws CoreException, IOException
    {
        if (editorInput instanceof FileEditorInput)
        {
            FileEditorInput fileEditorInput = (FileEditorInput)editorInput;
            IFile ifile = fileEditorInput.getFile();
            String uniquePath = ifile.getProjectRelativePath().toPortableString();

            return MiscUtil.createStringDigest(uniquePath);
        }

        return super.computeLayoutFileName( editorInput );
    }

	@Override
	protected StandardDiagramLayout initLayoutModel()
	{
		StandardDiagramLayout layoutModel = null;
		try
		{
			String fileName = computeLayoutFileName(this.editorInput);
			if (fileName != null)
			{
				IFile layoutFile = getLayoutPersistenceFile(fileName);
				if (layoutFile != null)
				{
					final XmlResourceStore resourceStore = new XmlResourceStore( new WorkspaceFileResourceStore(layoutFile));
					layoutModel = StandardDiagramLayout.TYPE.instantiate(new RootXmlResource( resourceStore ));
				}
			}
		}
		catch (Exception e)
		{
			SapphireUiFrameworkPlugin.log( e );
		}
		return layoutModel;
	}

	protected IFile getLayoutPersistenceFile(String fileName) throws StatusException, CoreException
	{
		if (this.editorInput instanceof IFileEditorInput)
		{
			IFileEditorInput fileInput = (IFileEditorInput)this.editorInput;
			IProject proj = fileInput.getFile().getProject();
			if (proj != null)
			{
				IFolder layoutIFolder = proj.getFolder(".settings/org.eclipse.sapphire.ui.diagram/layouts");
				File layoutFolder = layoutIFolder.getLocation().toFile();
	            if (!layoutFolder.exists())
	            {
	                FileUtil.mkdirs(layoutFolder);
	                layoutIFolder.refreshLocal(IResource.DEPTH_ONE, null);
	            }
	            IFile layoutFile = layoutIFolder.getFile(fileName);
	            layoutFile.refreshLocal(IResource.DEPTH_ONE, null);
	            return layoutFile;
			}
		}
		return null;
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
        		if (pageDef.getLayoutPersistence().getContent() == LayoutPersistence.PROJECT)
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
            return new ProjectDiagramLayoutPersistenceService();
        }
    }

}
