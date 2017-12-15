/******************************************************************************
 * Copyright (c) 2013 Oracle
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

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireEditorPagePart.SelectionChangedEvent;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.diagram.editor.DiagramConnectionPart;
import org.eclipse.sapphire.ui.diagram.editor.DiagramConnectionTemplate;
import org.eclipse.sapphire.ui.diagram.editor.DiagramEmbeddedConnectionTemplate;
import org.eclipse.sapphire.ui.diagram.editor.DiagramImplicitConnectionPart;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodePart;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodeTemplate;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
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
    	
    	if( part instanceof DiagramNodePart ||
    			(part instanceof DiagramConnectionPart && !(part instanceof DiagramImplicitConnectionPart)))
    	{
    		enabled = true;
    	}
    	else
    	{
	    	SapphireDiagramEditorPagePart diagramPart = part.nearest(SapphireDiagramEditorPagePart.class);
	    	List<ISapphirePart> selectedParts = diagramPart.getSelections();
	    	for (ISapphirePart selectedPart : selectedParts)
	    	{
	    		if (selectedPart instanceof DiagramNodePart ||
	    				(selectedPart instanceof DiagramConnectionPart && !(part instanceof DiagramImplicitConnectionPart)))
	    		{
	    			enabled = true;
	    		}
	    	}
    	}
    	
    	setEnabled( enabled );
    }
    
    @Override
    protected Object run(SapphireRenderingContext context) 
    {
        ISapphirePart part = context.getPart();
        if (part instanceof DiagramConnectionPart)
        {
            DiagramConnectionPart connPart = (DiagramConnectionPart)part;
            deleteConnection(connPart);   
        }
        else if (part instanceof DiagramNodePart)
        {
            DiagramNodePart nodePart = (DiagramNodePart)part;
            deleteNode(nodePart);
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
                    deleteConnection(connPart);           			
        		}
                else if (selectedPart instanceof DiagramNodePart)
                {
                    DiagramNodePart nodePart = (DiagramNodePart)selectedPart;
                    deleteNode(nodePart);
                }        		
        	}
        }
        return null;
    }

    private void deleteNodeConnections(DiagramNodePart nodePart)
    {
        IModelElement nodeModel = nodePart.getLocalModelElement();
        SapphireDiagramEditorPagePart diagramPart = nodePart.getDiagramNodeTemplate().getDiagramEditorPart();
        
        // Look for embedded connections
        for (DiagramNodeTemplate nodeTemplate : diagramPart.getNodeTemplates())
        {
        	DiagramEmbeddedConnectionTemplate embeddedConn = nodeTemplate.getEmbeddedConnectionTemplate();
            if (embeddedConn != null)
            {
                for (DiagramConnectionPart connPart : embeddedConn.getDiagramConnections(null))
                {
                    if ((connPart.getEndpoint1() != null && connPart.getEndpoint1().equals(nodeModel)) || 
                            (connPart.getEndpoint2() != null && connPart.getEndpoint2().equals(nodeModel)))
                    {
                        deleteConnection(connPart);
                    }
                }
            }
        }

        // Look for top level connections        
        for (DiagramConnectionTemplate connTemplate : diagramPart.getConnectionTemplates())
        {
            for (DiagramConnectionPart connPart : connTemplate.getDiagramConnections(null))
            {
                if ((connPart.getEndpoint1() != null && connPart.getEndpoint1().equals(nodeModel)) || 
                        (connPart.getEndpoint2() != null && connPart.getEndpoint2().equals(nodeModel)))
                {
                    if (!(connPart instanceof DiagramImplicitConnectionPart))
                    {
                        deleteConnection(connPart);
                    }
                }
            }
        }
    }
    
    private void deleteConnection(DiagramConnectionPart connPart)
    {
        final IModelElement element = connPart.getLocalModelElement();
        final ModelElementList<?> list = (ModelElementList<?>) element.parent();
        list.remove(element);            
    }
    
    private void deleteNode(DiagramNodePart nodePart)
    {
        IModelElement nodeModel = nodePart.getLocalModelElement();
        // Need to remove connection parts that are associated with this node
        deleteNodeConnections(nodePart);
        
        // Check top level connections to see whether we need to remove the connection parent element
        SapphireDiagramEditorPagePart editorPart = nodePart.getDiagramNodeTemplate().getDiagramEditorPart();
        List<DiagramConnectionTemplate> connTemplates = editorPart.getConnectionTemplates();
        for (DiagramConnectionTemplate connTemplate : connTemplates)
        {
            if (connTemplate.getConnectionType() == DiagramConnectionTemplate.ConnectionType.OneToMany)
            {
                IModelElement connParentElement = connTemplate.getConnectionParentElement(nodeModel);
                if (connParentElement != null)
                {
                    ModelElementList<?> connParentList = (ModelElementList<?>)connParentElement.parent();
                    connParentList.remove(connParentElement);
                }
            }
        }
        
        ModelElementList<?> list = (ModelElementList<?>) nodeModel.parent();
        list.remove(nodeModel);            
    	
    }
}
