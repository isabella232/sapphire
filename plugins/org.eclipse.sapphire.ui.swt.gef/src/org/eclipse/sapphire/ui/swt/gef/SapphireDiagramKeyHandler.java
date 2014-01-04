/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.KeyHandler;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionGroup;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.def.KeyBindingBehavior;
import org.eclipse.sapphire.ui.def.SapphireKeySequence;
import org.eclipse.sapphire.ui.diagram.DiagramConnectionPart;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodePart;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;
import org.eclipse.sapphire.ui.diagram.editor.ShapePart;
import org.eclipse.sapphire.ui.forms.swt.SapphireActionPresentation;
import org.eclipse.sapphire.ui.forms.swt.SapphireActionPresentationManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Display;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class SapphireDiagramKeyHandler extends KeyHandler
{		
	private KeyHandler parent;
	private SapphireDiagramEditor diagramEditor;
	private List<ISapphirePart> sapphireParts;
	private ISapphirePart sapphirePart;
	private String actionContext;
	private String hiddenContext;
	private GraphicalEditPart editPart;
	private SapphireActionGroup tempActions;
	private SapphireActionGroup hiddenActions;
	private SapphireActionPresentationManager actionPresentationManager;
	private SapphireActionPresentationManager hiddenActionPresentationManager;
	private DiagramKeyboardActionPresentation actionPresentation;
	private DiagramKeyboardActionPresentation hiddenActionPresentation;
		
	public SapphireDiagramKeyHandler(final SapphireDiagramEditor diagramEditor, final List<ISapphirePart> sapphireParts)
	{
		this.diagramEditor = diagramEditor;
		this.sapphireParts = new ArrayList<ISapphirePart>();
		this.sapphireParts.addAll(sapphireParts);
		this.hiddenContext = null;
		
		if (sapphireParts.size() == 1)
		{
			this.sapphirePart = sapphireParts.get(0);
			if (this.sapphirePart instanceof SapphireDiagramEditorPagePart)
			{
				this.actionContext = SapphireActionSystem.CONTEXT_DIAGRAM_EDITOR;
			}
			else if (this.sapphirePart instanceof DiagramNodePart)
			{
				this.actionContext = SapphireActionSystem.CONTEXT_DIAGRAM_NODE;
				this.hiddenContext = SapphireActionSystem.CONTEXT_DIAGRAM_NODE_HIDDEN;
			}
			else if (this.sapphirePart instanceof ShapePart)
			{
				this.actionContext = SapphireActionSystem.CONTEXT_DIAGRAM_NODE_SHAPE;
				this.hiddenContext = SapphireActionSystem.CONTEXT_DIAGRAM_SHAPE_HIDDEN;
			}
			else if (this.sapphirePart instanceof DiagramConnectionPart)
			{
				this.actionContext = SapphireActionSystem.CONTEXT_DIAGRAM_CONNECTION;
				this.hiddenContext = SapphireActionSystem.CONTEXT_DIAGRAM_CONNECTION_HIDDEN;
			}
		
			this.editPart = diagramEditor.getSelectedEditParts().get(0);
		}
		else if (sapphireParts.size() > 1)
		{
			this.actionContext = SapphireActionSystem.CONTEXT_DIAGRAM_MULTIPLE_PARTS;
			this.sapphirePart = this.diagramEditor.getPart();
		}
	}
	
	@Override
	public boolean keyPressed(KeyEvent event) 
	{
		final SapphireActionPresentationManager manager = getManager();
		final SapphireActionGroup localGroupOfActions = manager.getActionGroup();
        
		if (!(this.sapphirePart instanceof DiagramConnectionPart) ||
				(((DiagramConnectionPart)this.sapphirePart).removable()))
		{
			if( handleKeyEvent( event, manager, localGroupOfActions, false ) )
	        {
	            return true;
	        }
			if (this.hiddenActions != null)
			{
				if( handleKeyEvent( event, getHiddenManager(), this.hiddenActions, false ) )
		        {
		            return true;
		        }
			}
		}		
		if (this.sapphireParts.size() == 1)
		{
	        ISapphirePart part = this.sapphirePart.parent();
	        
	        while( part != null )
	        {
	            final String mainActionContext = part.getMainActionContext();
	            
	            if( mainActionContext != null )
	            {
	                final SapphireActionGroup groupOfActions = part.getActions( mainActionContext );
	                
	                if( handleKeyEvent( event, getManager(), groupOfActions, true ) )
	                {
	                    return true;
	                }
	            }
	            
	            part = part.parent();
	        }
					
			return parent != null && parent.keyPressed(event);
		}
		return false;
	}
	
    private boolean handleKeyEvent( final KeyEvent event,
            final SapphireActionPresentationManager manager, 
            final SapphireActionGroup groupOfActions,
            final boolean onlyPropagatedKeyBindings )
    {
    	for( SapphireAction action : groupOfActions.getActions() )
    	{
    		if( action.hasActiveHandlers() && ( ! onlyPropagatedKeyBindings || action.getKeyBindingBehavior() == KeyBindingBehavior.PROPAGATED ) )
    		{
    			final SapphireKeySequence keySequence = action.getKeyBinding();
	
    			if( keySequence != null )
    			{
    				int expectedStateMask = 0;
	
    				for( SapphireKeySequence.Modifier modifier : keySequence.getModifiers() )
    				{
						if( modifier == SapphireKeySequence.Modifier.SHIFT )
						{
						    expectedStateMask = expectedStateMask | SWT.SHIFT;
						}
						else if( modifier == SapphireKeySequence.Modifier.ALT )
						{
						    expectedStateMask = expectedStateMask | SWT.ALT;
						}
						else if( modifier == SapphireKeySequence.Modifier.CONTROL )
						{
						    expectedStateMask = expectedStateMask | SWT.CONTROL;
						}
    				}
	
    				if( event.stateMask == expectedStateMask && event.keyCode == keySequence.getKeyCode() )
    				{
    					final List<SapphireActionHandler> handlers = action.getActiveHandlers();
	
    					if( handlers.size() == 1 )
    					{
    						final SapphireActionHandler handler = handlers.get( 0 );
	    
    						final Runnable runnable = new Runnable()
    						{
    							public void run()
    							{
    								handler.execute( manager.context() );
    							}
    						};
	    
    						Display.getDefault().asyncExec( runnable );
    					}
    					else
    					{
    						for( SapphireActionPresentation presentation : manager.getPresentations() )
    						{
    							if( presentation.displayActionHandlerChoice( action ) )
    							{
    								break;
    							}
    						}
    					}
	
    					return true;
    				}
    			}
    		}
    	}	
    	return false;
	}
		
	public KeyHandler setParent(KeyHandler parent) 
	{
		this.parent = parent;
		return this;
	}
	
	public void dispose()
	{
		if (this.actionPresentationManager != null)
		{
			this.actionPresentationManager.dispose();
			this.actionPresentationManager = null;			
		}
		if (this.hiddenActionPresentationManager != null)
		{
			this.hiddenActionPresentationManager.dispose();
			this.hiddenActionPresentationManager = null;			
		}
	}
	
	private SapphireActionPresentationManager getManager()
	{
		if (this.actionPresentationManager == null)
		{
			initActions();
		}
		return this.actionPresentationManager;
	}
	
	private SapphireActionPresentationManager getHiddenManager()
	{
		if (this.actionPresentationManager == null)
		{
			initActions();
		}
		return this.hiddenActionPresentationManager;
	}
	
	private void initActions()
	{		
		this.tempActions = this.sapphirePart.getActions(this.actionContext);
		this.actionPresentationManager = new SapphireActionPresentationManager(this.diagramEditor.getDiagramPresentation(),				
				this.tempActions);
		this.actionPresentation = new DiagramKeyboardActionPresentation(
				this.actionPresentationManager, this.diagramEditor, this.editPart);
		this.actionPresentation.render();
		if (this.hiddenContext != null)
		{
			this.hiddenActions = this.sapphirePart.getActions(this.hiddenContext);
			this.hiddenActionPresentationManager = new SapphireActionPresentationManager(
					this.diagramEditor.getDiagramPresentation(),
					this.hiddenActions);
			this.hiddenActionPresentation = new DiagramKeyboardActionPresentation(
					this.hiddenActionPresentationManager, this.diagramEditor, this.editPart);
			this.hiddenActionPresentation.render();
		}
	}
	
}
