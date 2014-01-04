/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PanningSelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.tools.CreationTool;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.localization.LabelTransformer;
import org.eclipse.sapphire.ui.diagram.def.DiagramPaletteCompartmentConstants;
import org.eclipse.sapphire.ui.diagram.def.IDiagramConnectionDef;
import org.eclipse.sapphire.ui.diagram.def.DiagramEditorPageDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramNodeDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramPaletteCompartmentDef;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodeTemplate;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;
import org.eclipse.sapphire.ui.forms.swt.SwtUtil;
import org.eclipse.sapphire.ui.swt.gef.tools.SapphireMarqueeSelectionTool;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class SapphirePaletteRoot extends PaletteRoot 
{
	private SapphireDiagramEditorPagePart diagramPart;
	
	public SapphirePaletteRoot(SapphireDiagramEditorPagePart diagramPart)
	{
		this.diagramPart = diagramPart;
		updatePaletteEntries();
	}
	
	/**
	 * Creates resp. updates the PaletteEntries. All old PaletteEntries will be
	 * removed and new ones will be created by calling the corresponding
	 * create-methods.
	 */
	@SuppressWarnings("unchecked")
	public void updatePaletteEntries() 
	{
		// remove old entries
		setDefaultEntry(null);
		List<PaletteEntry> allEntries = new ArrayList<PaletteEntry>(getChildren()); // MUST make a copy
		for (Iterator<PaletteEntry> iter = allEntries.iterator(); iter.hasNext();) 
		{
			PaletteEntry entry = iter.next();
			remove(entry);
		}
		
		// create new entries
		DiagramEditorPageDef diagramPageDef = (DiagramEditorPageDef)diagramPart.definition();
		add(createModelIndependentTools());
		
		List<DiagramNodeTemplate> nodeTemplates = diagramPart.getVisibleNodeTemplates();
		if (nodeTemplates.isEmpty())
		{
			return;
		}
		
		List<DiagramPaletteDrawer> drawers = new ArrayList<DiagramPaletteDrawer>();
		Map<String, List<ToolEntry>> entries = new HashMap<String, List<ToolEntry>>();
		ElementList<IDiagramPaletteCompartmentDef> compartmentDefs = diagramPageDef.getPaletteCompartments();
		if (compartmentDefs.size() == 0)
		{
			String label = LabelTransformer.transform(DiagramPaletteCompartmentConstants.CONNECTIONS_COMPARTMENT_LABEL, 
					CapitalizationType.TITLE_STYLE, true);
			DiagramPaletteDrawer connectionDrawer = new DiagramPaletteDrawer(label, DiagramPaletteCompartmentConstants.CONNECTIONS_COMPARTMENT_ID);
			drawers.add(connectionDrawer);

			String label2 = LabelTransformer.transform(DiagramPaletteCompartmentConstants.NODES_COMPARTMENT_LABEL, 
					CapitalizationType.TITLE_STYLE, true);			
			DiagramPaletteDrawer nodesDrawer = new DiagramPaletteDrawer(label2, DiagramPaletteCompartmentConstants.NODES_COMPARTMENT_ID);
			drawers.add(nodesDrawer);
		}
		else
		{
			for (IDiagramPaletteCompartmentDef compartmentDef : compartmentDefs)
			{
				String label = LabelTransformer.transform(compartmentDef.getLabel().content(), 
						CapitalizationType.TITLE_STYLE, true);		
				DiagramPaletteDrawer drawer = new DiagramPaletteDrawer(label, compartmentDef.getId().content());
				drawers.add(drawer);
				
			}
		}
		
		for (SapphireDiagramEditorPagePart.ConnectionPalette connectionPalette : diagramPart.getConnectionPalettes()) 
		{
			IDiagramConnectionDef connDef = connectionPalette.getConnectionDef();
			ImageData imageData = connectionPalette.getImageData();
            ImageDescriptor imageDescriptor = SwtUtil.toImageDescriptor(imageData);

            CreationFactory factory = new ConnectionCreationFactory(connDef);
			String tpLabel = connDef.getToolPaletteLabel().content();
			if (tpLabel != null)
			{
				tpLabel = IDiagramConnectionDef.PROP_TOOL_PALETTE_LABEL.getLocalizationService().text(
								tpLabel, CapitalizationType.TITLE_STYLE, false);
			}
			String tpDesc = connDef.getToolPaletteDescription().content();
			if (tpDesc != null)
			{
				tpDesc = IDiagramConnectionDef.PROP_TOOL_PALETTE_DESCRIPTION.getLocalizationService().text(
								tpDesc, CapitalizationType.TITLE_STYLE, false);
			}
			if (tpLabel != null) {
	    		ToolEntry tool = new ConnectionCreationToolEntry(tpLabel, tpDesc, factory, imageDescriptor, imageDescriptor);
	    		
	    		DiagramPaletteDrawer drawer = getDiagramPaletteDrawer(drawers, connDef.getToolPaletteCompartment().content());
	    		List<ToolEntry> list = entries.get(drawer.getId());
	    		if (list == null) {
	    			list = new ArrayList<ToolEntry>();
	    			entries.put(drawer.getId(), list);
	    		}
	    		list.add(tool);
			} 
			else 
			{
				// TODO which case is this?? 
			}
		}
		
        for (DiagramNodeTemplate nodeTemplate : nodeTemplates) 
        {
        	IDiagramNodeDef nodeDef = nodeTemplate.definition();

            final ImageData imageData = nodeTemplate.getToolPaletteImage();
            ImageDescriptor imageDescriptor = SwtUtil.toImageDescriptor(imageData);

            CreationFactory factory = new NodeCreationFactory(nodeTemplate);

			String tpLabel = nodeDef.getToolPaletteLabel().content();
			if (tpLabel != null)
			{
				tpLabel = IDiagramNodeDef.PROP_TOOL_PALETTE_LABEL.getLocalizationService().text(
								tpLabel, CapitalizationType.TITLE_STYLE, false);
			}
			String tpDesc = nodeDef.getToolPaletteDescription().content();
			if (tpDesc != null)
			{
				tpDesc = IDiagramNodeDef.PROP_TOOL_PALETTE_DESCRIPTION.getLocalizationService().text(
								tpDesc, CapitalizationType.TITLE_STYLE, false);
			}
    		ToolEntry tool = new CombinedTemplateCreationEntry(tpLabel, tpDesc, factory, imageDescriptor, imageDescriptor);
    		tool.setToolClass(CreationTool.class);

    		// find the right drawer
    		DiagramPaletteDrawer drawer = getDiagramPaletteDrawer(drawers, nodeDef.getToolPaletteCompartment().content());
    		List<ToolEntry> list = entries.get(drawer.getId());
    		if (list == null) {
    			list = new ArrayList<ToolEntry>();
    			entries.put(drawer.getId(), list);
    		}
    		list.add(tool);
        }
        
        // sort the drawers
        for (DiagramPaletteDrawer drawer : drawers)
        {
    		List<ToolEntry> list = entries.get(drawer.getId());
    		if (list != null)
    		{
	    		Collections.sort(list, new Comparator<ToolEntry>() 
	    		{
					public int compare(ToolEntry x, ToolEntry y) {
			        	return x.getLabel().compareTo(y.getLabel());
					}
	    		});
	    		drawer.addAll(list);
    		}
    		add(drawer);
        }
		
	}
	
	private  DiagramPaletteDrawer getDiagramPaletteDrawer(List<DiagramPaletteDrawer> drawers, String id) 
	{
		for (DiagramPaletteDrawer drawer : drawers) {
			if (id.equals(drawer.getId())) {
				return drawer;
			}
		}
		return drawers.get(0);
	}
	
	/** Create the "Tools" group. */
	private PaletteContainer createModelIndependentTools() 
	{
		PaletteGroup group = new PaletteGroup("Tools");

		// Add a selection tool to the group
		ToolEntry tool = new PanningSelectionToolEntry();
		group.add(tool);
		setDefaultEntry(tool);

		// Add a marquee tool to the group
		MarqueeToolEntry toolEntry = new MarqueeToolEntry();
		toolEntry.setToolClass(SapphireMarqueeSelectionTool.class);
		group.add(toolEntry);

		return group;
	}
	
}
