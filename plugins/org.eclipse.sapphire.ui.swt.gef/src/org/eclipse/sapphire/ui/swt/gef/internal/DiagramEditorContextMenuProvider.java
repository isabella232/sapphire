/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [383924]  Flexible diagram node shapes
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.internal;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.localization.LabelTransformer;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.SapphireActionSystemPart;
import org.eclipse.sapphire.ui.diagram.DiagramConnectionPart;
import org.eclipse.sapphire.ui.forms.swt.ActionBridge;
import org.eclipse.sapphire.ui.forms.swt.ActionHandlerBridge;
import org.eclipse.sapphire.ui.forms.swt.ActionSystemPartBridge;
import org.eclipse.sapphire.ui.forms.swt.SwtUtil;
import org.eclipse.sapphire.ui.swt.gef.SapphireDiagramEditor;
import org.eclipse.sapphire.ui.swt.gef.parts.DiagramConnectionEditPart;
import org.eclipse.sapphire.ui.swt.gef.parts.DiagramConnectionLabelEditPart;
import org.eclipse.sapphire.ui.swt.gef.parts.DiagramNodeEditPart;
import org.eclipse.sapphire.ui.swt.gef.parts.SapphireDiagramEditorPageEditPart;
import org.eclipse.sapphire.ui.swt.gef.parts.ShapeEditPart;
import org.eclipse.sapphire.ui.swt.gef.presentation.DiagramPresentation;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public final class DiagramEditorContextMenuProvider extends ContextMenuProvider
{
    private static final String DIAGRAM_NODE_DEFAULT_ACTION = "Sapphire.Diagram.Node.Default";
	private static final String DIAGRAM_DEFAULT_GROUP = "Diagram.Default";
	private static final String DIAGRAM_DELETE_ALL_BEND_POINTS = "Sapphire.Diagram.DeleteAllBendPoints";
	
    private SapphireDiagramEditor editor;
	private Map<SapphireActionSystemPart,ActionSystemPartBridge> cache = Collections.emptyMap();
	
	public DiagramEditorContextMenuProvider( final SapphireDiagramEditor editor )
	{
		super( editor.getGraphicalViewer() );
		this.editor = editor;
	}
	
	@Override
	public void buildContextMenu( final IMenuManager menuManager ) 
	{
		final ISapphirePart part;
		final DiagramPresentation presentation;
        final GraphicalEditPart editPart;
        final String context;

        final List<GraphicalEditPart> selection = this.editor.getSelectedEditParts();
		
		if( selection.size() == 1 )
		{
			editPart = selection.get( 0 );
			
			if( editPart instanceof SapphireDiagramEditorPageEditPart )
			{
				context = SapphireActionSystem.CONTEXT_DIAGRAM_EDITOR;
				presentation = this.editor.getDiagramPresentation();
			}
			else if( editPart instanceof DiagramNodeEditPart )
			{
				DiagramNodeEditPart nodePart = (DiagramNodeEditPart)editPart;
				if (isMouseOnEditPart(nodePart))
				{
					context = SapphireActionSystem.CONTEXT_DIAGRAM_NODE;
					presentation = ((DiagramNodeEditPart)editPart).getPresentation();
				}
				else
				{
					context = SapphireActionSystem.CONTEXT_DIAGRAM_EDITOR;
					presentation = this.editor.getDiagramPresentation();
				}
			}
			else if (editPart instanceof ShapeEditPart)
			{
				DiagramNodeEditPart nodePart = ((ShapeEditPart)editPart).getNodeEditPart();
				if (isMouseOnEditPart(nodePart))
				{
					context = SapphireActionSystem.CONTEXT_DIAGRAM_NODE_SHAPE;
					presentation = ((ShapeEditPart)editPart).getShapePresentation();
				}
				else
				{
					context = SapphireActionSystem.CONTEXT_DIAGRAM_EDITOR;
					presentation = this.editor.getDiagramPresentation();
				}
			}
			else if( editPart instanceof DiagramConnectionEditPart )
			{
				if (isMouseOnEditPart(editPart))
				{
					context = SapphireActionSystem.CONTEXT_DIAGRAM_CONNECTION;
					presentation = ((DiagramConnectionEditPart)editPart).getPresentation();
				}
				else
				{
					context = SapphireActionSystem.CONTEXT_DIAGRAM_EDITOR;
					presentation = this.editor.getDiagramPresentation();
				}
				
			}
			else if( editPart instanceof DiagramConnectionLabelEditPart )
			{
			    return;
			}
			else
			{
			    throw new IllegalStateException();
			}
			
			part = presentation.part();
		}
		else if( selection.size() > 1 )
		{
			if (isMouseOnEditParts(selection))
			{
			    context = SapphireActionSystem.CONTEXT_DIAGRAM_MULTIPLE_PARTS;
			}
			else
			{
				context = SapphireActionSystem.CONTEXT_DIAGRAM_EDITOR;				
			}
			presentation = this.editor.getDiagramPresentation();
		    part = this.editor.getPart();			
		}
		else
		{
		    throw new IllegalStateException();
		}
		
		final Map<SapphireActionSystemPart,ActionSystemPartBridge> updatedCache = new IdentityHashMap<SapphireActionSystemPart,ActionSystemPartBridge>();
		String currentGroupId = null;
		
		for( SapphireAction action : part.getActions( context ).getActions() )
		{
		    if( action.getId().equals( DIAGRAM_NODE_DEFAULT_ACTION ) )
		    {
		        continue;
		    }
		    
		    if (skipMultipleConnectionAction(this.editor.getSelectedParts(), context, action)) 
		    {
		    	continue;
		    }
		    
	        final String groupId = action.getGroup();
	        
	        if( ( currentGroupId != null && groupId == null ) || 
	            ( currentGroupId == null && groupId != null ) ||
	            ( currentGroupId != null && groupId != null && ! currentGroupId.equals( groupId ) ) )
	        {
	            currentGroupId = groupId;
	            menuManager.add( new Separator( currentGroupId == null ? DIAGRAM_DEFAULT_GROUP : currentGroupId ) );
	        }
	        
	        final List<SapphireActionHandler> handlers = action.getActiveHandlers();
	        final int count = handlers.size();
	        
	        if( count == 1 )
	        {
	            ActionSystemPartBridge bridge = this.cache.get( action );
	            
	            if( bridge == null )
	            {
	                bridge = new ActionBridge( presentation, action );
	            }
	            
	            updatedCache.put( action, bridge );
	            
	            menuManager.add( bridge );
	        }
	        else if( count > 1 )
	        {
                final String childMenuText = LabelTransformer.transform( action.getLabel(), CapitalizationType.TITLE_STYLE, true );
                final ImageDescriptor childMenuImage = SwtUtil.toImageDescriptor( action.getImage( 16 ) );
                final MenuManager childMenuManager = new MenuManager( childMenuText, childMenuImage, action.getId() );
                
                childMenuManager.setParent( menuManager );
                menuManager.add( childMenuManager );
                
                for( SapphireActionHandler handler : action.getActiveHandlers() )
                {
                    ActionSystemPartBridge bridge = this.cache.get( handler );
                    
                    if( bridge == null )
                    {
                        bridge = new ActionHandlerBridge( presentation, handler );
                    }
                    
                    updatedCache.put( handler, bridge );
                    
                    childMenuManager.add( bridge );
                }
	        }
		}
		
		for( Map.Entry<SapphireActionSystemPart,ActionSystemPartBridge> entry : this.cache.entrySet() )
		{
		    if( ! updatedCache.containsKey( entry.getKey() ) )
		    {
		        entry.getValue().dispose();
		    }
		}
		
		this.cache = updatedCache;
	}
	
	private boolean skipMultipleConnectionAction(final List<ISapphirePart> selection, final String context, final SapphireAction action) {
		if (SapphireActionSystem.CONTEXT_DIAGRAM_MULTIPLE_PARTS.equals(context)) {
			if (DIAGRAM_DELETE_ALL_BEND_POINTS.equals(action.getId())) {
				for (ISapphirePart part : selection) {
		            if(part instanceof DiagramConnectionPart) {
		            	return false;
		            }
				}
				return true;
			}
		}
		
		return false;
	}

    @Override
    public void dispose()
    {
        super.dispose();
        
        for( ActionSystemPartBridge bridge : this.cache.values() )
        {
            bridge.dispose();
        }
    }
    
    @Override
	public void menuAboutToShow(IMenuManager menu) 
    {
		super.menuAboutToShow(menu);
		this.editor.getContextButtonManager().hideContextButtonsInstantly();
	}
    
    private boolean isMouseOnEditPart(GraphicalEditPart editPart)
    {
    	Point p = this.editor.getMouseLocation();
    	return editPart.getFigure().containsPoint(p);
    }
    
    private boolean isMouseOnEditParts(List<GraphicalEditPart> editParts)
    {
    	Point p = this.editor.getMouseLocation();
    	for (GraphicalEditPart editPart : editParts)
    	{
    		if (editPart.getFigure().containsPoint(p))
    		{
    			return true;
    		}
    	}
    	return false;
    }

}
