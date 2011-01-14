/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.graphiti.providers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.graphiti.dt.AbstractDiagramTypeProvider;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.platform.IDiagramEditor;
import org.eclipse.graphiti.tb.IToolBehaviorProvider;
import org.eclipse.graphiti.ui.internal.platform.ExtensionManager;
import org.eclipse.graphiti.ui.platform.IImageProvider;
import org.eclipse.graphiti.ui.services.GraphitiUi;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.ui.def.IImportDirective;
import org.eclipse.sapphire.ui.def.ISapphireUiDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramImageChoice;
import org.eclipse.sapphire.ui.diagram.def.IDiagramNodeDef;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodeTemplate;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPart;
import org.eclipse.sapphire.ui.diagram.graphiti.editor.SapphireDiagramEditor;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.ui.internal.util.BundleUtility;
import org.osgi.framework.Bundle;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class SapphireDiagramTypeProvider extends AbstractDiagramTypeProvider 
{
	private SapphireDiagramEditorPart diagramPart;
	private SapphireDiagramFeatureProvider featureProvider;
	private IToolBehaviorProvider[] toolBehaviorProviders;
	
	public SapphireDiagramTypeProvider()
	{
		this.featureProvider = 
			new SapphireDiagramFeatureProvider(this, new SapphireDiagramSolver());
		setFeatureProvider(this.featureProvider);
	}
	
	public void setDiagramPart(SapphireDiagramEditorPart diagramPart)
	{
		this.diagramPart = diagramPart;
		this.featureProvider.setDiagramPart(diagramPart);		
	}
	
	public SapphireDiagramEditorPart getDiagramPart()
	{
		return this.diagramPart;
	}
	
	@Override
	public void init(Diagram diagram, IDiagramEditor diagramEditor)
	{
		super.init(diagram, diagramEditor);
		setDiagramPart(((SapphireDiagramEditor)diagramEditor).getDiagramEditorPart());
		ExtensionManager extManager = (ExtensionManager)GraphitiUi.getExtensionManager();
		IImageProvider imageProviders[] = extManager.getImageProviders();
		SapphireDiagramImageProvider sapphireImageProvider = null;
		for (IImageProvider imageProvider : imageProviders)
		{
			if (imageProvider instanceof SapphireDiagramImageProvider)
			{
				sapphireImageProvider = (SapphireDiagramImageProvider)imageProvider;
				break;
			}
		}
		if (sapphireImageProvider != null)
		{
			List<DiagramNodeTemplate> nodeTemplates = this.diagramPart.getNodeTemplates();
			for (DiagramNodeTemplate nodeTemplate : nodeTemplates)
			{
				IDiagramNodeDef nodeDef = nodeTemplate.getDefinition();
				if (nodeDef.getImage().element() != null)
				{
					ModelElementList<IDiagramImageChoice> images = nodeDef.getPossibleImages();
					for (IDiagramImageChoice imageChoice : images)
					{
						ISapphireUiDef uiDef = imageChoice.nearest(ISapphireUiDef.class);
						String imageId = imageChoice.getImageId().getContent();
						String imagePath = imageChoice.getImagePath().getContent();
						String bundleId = resolveImageBundle(uiDef, imagePath);
						// Graphiti's image provider doesn't support images from different plugins.
						// See http://www.eclipse.org/forums/index.php?t=tree&th=201973&start=0&S=3813ad4d99f2ac8bd56a0072ffa6ebd9
						if (bundleId != null)
						{
							sapphireImageProvider.setPluginId(bundleId);
						}
						
						if (imageId != null && imagePath != null)
						{
							sapphireImageProvider.registerImage(imageId, imagePath);
						}						
					}
				}
			}
		}
	}
	
    @Override
    public IToolBehaviorProvider[] getAvailableToolBehaviorProviders() 
    {
        if (this.toolBehaviorProviders == null) 
        {
            this.toolBehaviorProviders =
                new IToolBehaviorProvider[] { new SapphireDiagramToolBehaviorProvider(this) };
        }
        return this.toolBehaviorProviders;
    }
	
    @Override
    public void postInit() 
    {    	
    	SapphireDiagramEditor sapphireEditor = (SapphireDiagramEditor)getDiagramEditor();
    	sapphireEditor.syncDiagramWithModel();
    	sapphireEditor.doSave(null);
    }
    
    private String resolveImageBundle(ISapphireUiDef def, String imagePath)
    {
    	try
    	{
	    	for (IImportDirective directive : def.getImportDirectives())
	    	{
	            final String bundleId = directive.getBundle().getText();
	            Bundle bundle = Platform.getBundle(bundleId);
	            URL url = BundleUtility.find(bundle, imagePath);
				URL locatedURL = FileLocator.toFileURL(url);
				if ("file".equalsIgnoreCase(locatedURL.getProtocol()))
				{
					String fullPath = new Path(locatedURL.getPath()).toOSString();
					File f = new File(fullPath);
					if (f.exists())
					{
						return bundleId;
					}
				}
	    	}
    	}
    	catch (IOException e)
    	{
    		SapphireUiFrameworkPlugin.log(e);
    	}
    	return null;
    }
}
