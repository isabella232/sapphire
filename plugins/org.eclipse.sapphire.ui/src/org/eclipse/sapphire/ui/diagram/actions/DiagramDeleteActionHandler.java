/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - [348811] Eliminate separate Sapphire.Diagram.Part.Delete action
 *    Konstantin Komissarchik - [381794] Cleanup needed in presentation code for diagram context menu
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireEditorPagePart.SelectionChangedEvent;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.diagram.ConnectionService;
import org.eclipse.sapphire.ui.diagram.DiagramConnectionPart;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodePart;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;
import org.eclipse.sapphire.ui.diagram.editor.ShapePart;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class DiagramDeleteActionHandler extends SapphireActionHandler
{
    @Override
    public void init( final SapphireAction action,
                      final ActionHandlerDef def )
    {
        super.init( action, def );
        
        final ISapphirePart part = getPart();
        
        if( part instanceof SapphireDiagramEditorPagePart )
        {
            ( (SapphireDiagramEditorPagePart) part ).attach
            (
                new FilteredListener<SelectionChangedEvent>()
                {
                    @Override
                    protected void handleTypedEvent( final SelectionChangedEvent event )
                    {
                        refreshEnablement();
                    }
                }
            );
        }
         
        refreshEnablement();
    }
    
    private void refreshEnablement()
    {
        final ISapphirePart part = getPart();
    	boolean enabled = false;
    	
    	if( part instanceof DiagramNodePart || part instanceof ShapePart ||
    			(part instanceof DiagramConnectionPart && ((DiagramConnectionPart)part).removable()))
    	{
    		enabled = true;
    	}
    	else
    	{
	    	SapphireDiagramEditorPagePart diagramPart = part.nearest(SapphireDiagramEditorPagePart.class);
	    	List<ISapphirePart> selectedParts = diagramPart.getSelections();
	    	for (ISapphirePart selectedPart : selectedParts)
	    	{
	    		if (selectedPart instanceof DiagramNodePart || selectedPart instanceof ShapePart ||
	    				(selectedPart instanceof DiagramConnectionPart && ((DiagramConnectionPart)selectedPart).removable()))
	    		{
	    			enabled = true;
	    		}
	    	}
    	}
    	
    	setEnabled( enabled );
    }
    
    @Override
    protected Object run(Presentation context) 
    {
        ISapphirePart part = context.part();
        if (part instanceof DiagramConnectionPart)
        {
            DiagramConnectionPart connPart = (DiagramConnectionPart)part;
            connPart.remove();   
        }
        else if (part instanceof DiagramNodePart)
        {
            DiagramNodePart nodePart = (DiagramNodePart)part;
            deleteNode(nodePart);
        }
        else if (part instanceof ShapePart)
        {
            ShapePart shapePart = (ShapePart)part;
            deleteShapePart(shapePart);
        }
        else if (part instanceof SapphireDiagramEditorPagePart)
        {
        	SapphireDiagramEditorPagePart pagePart = (SapphireDiagramEditorPagePart)part;
        	List<ISapphirePart> parts = new ArrayList<ISapphirePart>();
        	parts.addAll(pagePart.getSelections());
        	for (ISapphirePart selectedPart : parts)
        	{
        		if (selectedPart instanceof DiagramConnectionPart)
        		{
                    DiagramConnectionPart connPart = (DiagramConnectionPart)selectedPart;
                    connPart.remove();          			
        		}
                else if (selectedPart instanceof DiagramNodePart)
                {
                    DiagramNodePart nodePart = (DiagramNodePart)selectedPart;
                    deleteNode(nodePart);
                }
                else if (selectedPart instanceof ShapePart)
                {
                    ShapePart shapePart = (ShapePart)selectedPart;
                    deleteShapePart(shapePart);                	
                }
        	}
        }
        return null;
    }

    private void deleteNodeConnections(DiagramNodePart nodePart)
    {
    	Element nodeElement = nodePart.getLocalModelElement();
    	SapphireDiagramEditorPagePart diagramPart = nodePart.getDiagramNodeTemplate().getDiagramEditorPart();
    	ConnectionService connService = diagramPart.service(ConnectionService.class);
    	for (DiagramConnectionPart connPart : connService.list())
    	{
			 if (connPart.removable() &&
					 ((connPart.getEndpoint1() != null && connPart.getEndpoint1() == nodeElement) || 
							 connPart.getEndpoint2() != null && connPart.getEndpoint2() == nodeElement))
			 {
				 connPart.remove();
			 }
    		
    	}
    }
    
    private void deleteNode(DiagramNodePart nodePart)
    {
        // Need to remove connection parts that are associated with this node
        deleteNodeConnections(nodePart);
        
        nodePart.getDiagramNodeTemplate().deleteNode(nodePart);
    }
    
    private void deleteShapePart(ShapePart shapePart)
    {
    	Element shapeModel = shapePart.getLocalModelElement();
    	ElementList<?> list = (ElementList<?>) shapeModel.parent();
    	if (!list.disposed())
    	{
    		list.remove(shapeModel);
    	}
    }
}
