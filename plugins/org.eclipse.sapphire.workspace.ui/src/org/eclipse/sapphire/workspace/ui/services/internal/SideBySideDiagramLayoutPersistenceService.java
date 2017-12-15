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
import java.io.IOException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.sapphire.modeling.FileResourceStore;
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
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class SideBySideDiagramLayoutPersistenceService extends StandardDiagramLayoutPersistenceService 
{

	@Override
	protected StandardDiagramLayout initLayoutModel() 
	{
		StandardDiagramLayout layoutModel = null;
		try
		{
			String fileName = computeLayoutFileName(this.editorInput);
			if (fileName != null)
			{
				XmlResourceStore resourceStore = null;
				if (this.editorInput instanceof IFileEditorInput)
				{
					IFileEditorInput fileInput = (IFileEditorInput)this.editorInput;
					IFolder layoutFolder = (IFolder)fileInput.getFile().getParent();
					IFile layoutFile = layoutFolder.getFile(fileName);
					resourceStore = new XmlResourceStore( new WorkspaceFileResourceStore(layoutFile));
				}
		    	else if (this.editorInput instanceof FileStoreEditorInput)
		    	{
		    		FileStoreEditorInput fileStoreInput = (FileStoreEditorInput)this.editorInput;
		        	IFileStore store = EFS.getStore(fileStoreInput.getURI());
		        	File localFile = store.toLocalFile(EFS.NONE, null);
		    		//if no local file is available, obtain a cached file
		    		if (localFile == null)
		    			localFile = store.toLocalFile(EFS.CACHE, null);
		    		if (localFile == null)
		    			throw new IllegalArgumentException();
		    		File layoutFile = new File(localFile.getParentFile(), fileName);
		    		resourceStore = new XmlResourceStore( new FileResourceStore(layoutFile));
		    	}
				layoutModel = StandardDiagramLayout.TYPE.instantiate(new RootXmlResource( resourceStore ));
			}
		}
		catch (Exception e)
		{
			SapphireUiFrameworkPlugin.log( e );
		}
			
		return layoutModel;
	}

	@Override
	protected String computeLayoutFileName(IEditorInput editorInput) throws CoreException, IOException
	{
		String fileName = null;
    	if (editorInput instanceof FileEditorInput)
    	{
    		FileEditorInput fileEditorInput = (FileEditorInput)editorInput;
    		IFile ifile = fileEditorInput.getFile();
            fileName = ifile.getName();
    	}
    	else if (editorInput instanceof FileStoreEditorInput)
    	{
    		FileStoreEditorInput fileStoreInput = (FileStoreEditorInput)editorInput;
        	IFileStore store = EFS.getStore(fileStoreInput.getURI());
        	File localFile = store.toLocalFile(EFS.NONE, null);
    		//if no local file is available, obtain a cached file
    		if (localFile == null)
    			localFile = store.toLocalFile(EFS.CACHE, null);
    		if (localFile == null)
    			throw new IllegalArgumentException();
            fileName = localFile.getName();
    	}
    	if (fileName != null && fileName.endsWith(".xml"))
    	{
            fileName = fileName.substring(0, fileName.indexOf(".xml"));    		
    	}
    	if (fileName != null)
    	{
    		fileName += ".layout";
    	}
		return fileName;
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
        		if (pageDef.getLayoutPersistence().getContent() == LayoutPersistence.SIDE_BY_SIDE)
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
            return new SideBySideDiagramLayoutPersistenceService();
        }
    }
	
}
