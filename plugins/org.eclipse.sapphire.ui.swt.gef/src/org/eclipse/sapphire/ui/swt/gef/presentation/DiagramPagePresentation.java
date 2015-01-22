/******************************************************************************
   * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.presentation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.diagram.ConnectionAddEvent;
import org.eclipse.sapphire.ui.diagram.ConnectionDeleteEvent;
import org.eclipse.sapphire.ui.diagram.ConnectionEndpointsEvent;
import org.eclipse.sapphire.ui.diagram.ConnectionEvent;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodeAddEvent;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodeDeleteEvent;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodeEvent;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodeMoveEvent;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodePart;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodeTemplate;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;
import org.eclipse.sapphire.ui.swt.gef.DiagramConfigurationManager;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramModel;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramResourceCache;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class DiagramPagePresentation extends DiagramPresentation 
{
	private DiagramResourceCache resourceCache;
	private List<DiagramNodePresentation> nodes = new ArrayList<DiagramNodePresentation>();
	private Listener diagramNodeListener;
	private Listener connectionListener;
	
	public DiagramPagePresentation(final SapphirePart part, final DiagramConfigurationManager configManager, final Shell shell)
	{
		super(part, null, configManager, shell);
		this.resourceCache = new DiagramResourceCache();
		constructNodes();
	}

	public void init(final DiagramModel diagramModel) {
		diagramNodeListener = new FilteredListener<DiagramNodeEvent>() {
			@Override
			protected void handleTypedEvent(DiagramNodeEvent event) {
		    	DiagramNodePart nodePart = (DiagramNodePart)event.part();
		    	if (event instanceof DiagramNodeAddEvent) {
		    		diagramModel.handleAddNode(nodePart);
		    	}
		    	else if (event instanceof DiagramNodeDeleteEvent) {
		    		diagramModel.handleRemoveNode(nodePart);
		    	}
		    	else if (event instanceof DiagramNodeMoveEvent) {
		    		diagramModel.handleMoveNode(nodePart);
		    	}
			}
		};
		part().attach(diagramNodeListener);
		
		connectionListener = new FilteredListener<ConnectionEvent>() 
		{
			@Override
			protected void handleTypedEvent(ConnectionEvent event) 
			{
				if (event instanceof ConnectionEndpointsEvent)
				{
					diagramModel.updateConnectionEndpoint(event.part());
				}
				else if (event instanceof ConnectionAddEvent)
				{
					diagramModel.addConnection(event.part());
				}
				else if (event instanceof ConnectionDeleteEvent)
				{
					diagramModel.removeConnection(event.part());
				}
			}
		};	
		part().attach(connectionListener);
	}
	
	@Override
	public void render()
	{
		Figure f = new FreeformLayer();
		f.setBorder(new MarginBorder(3));
		f.setLayoutManager(new FreeformLayout());
		setFigure(f);		
	}
	
	@Override
    public SapphireDiagramEditorPagePart part()
    {
        return (SapphireDiagramEditorPagePart) super.part();
    }
	
	public List<DiagramNodePresentation> getNodes()
	{
		return this.nodes;
	}
	
	public DiagramResourceCache getResourceCache() 
	{
		return resourceCache;
	}
	
	public void dispose() 
	{
		resourceCache.dispose();
		
		part().detach(diagramNodeListener);
		part().detach(connectionListener);

		super.dispose();
	}
	
	public DiagramNodePresentation addNode(DiagramNodePart nodePart)
	{
		DiagramNodePresentation nodePresentation = new DiagramNodePresentation(nodePart, this, 
				shell(), getConfigurationManager(), getResourceCache());
		nodes.add(nodePresentation);
		return nodePresentation;
	}
	
	private void constructNodes()
	{
		for (DiagramNodeTemplate nodeTemplate : part().getNodeTemplates()) 
		{
			if (nodeTemplate.visible()) 
			{
				for (DiagramNodePart nodePart : nodeTemplate.getDiagramNodes()) 
				{
					if (nodePart.visible() && nodePart.getShapePart().visible()) 
					{
						nodes.add(new DiagramNodePresentation(nodePart, this, shell(), getConfigurationManager(), getResourceCache()));
					}
				}
			}
		}
	}

}
