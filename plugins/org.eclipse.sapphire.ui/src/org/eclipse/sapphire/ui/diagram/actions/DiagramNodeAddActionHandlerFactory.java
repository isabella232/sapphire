/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 *    Shenxue Zhou - [374433] DiagramNodeAddActionHandlerFactory issues
 *    Konstantin Komissarchik - [381794] Cleanup needed in presentation code for diagram context menu 
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.actions;

import java.util.List;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.ui.PartVisibilityEvent;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireActionHandlerFactory;
import org.eclipse.sapphire.ui.def.ActionHandlerFactoryDef;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodeTemplate;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;
import org.eclipse.sapphire.util.ListFactory;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class DiagramNodeAddActionHandlerFactory extends SapphireActionHandlerFactory 
{
    private Listener nodeTemplateVisibilityListener;
    
	@Override
    public void init( final SapphireAction action,
                      final ActionHandlerFactoryDef def )
    {
        super.init( action, def );
        
        this.nodeTemplateVisibilityListener = new FilteredListener<PartVisibilityEvent>()
        {
            @Override
            protected void handleTypedEvent( final PartVisibilityEvent event )
            {
                broadcast( new Event() );
            }
        };
        
        for( DiagramNodeTemplate nodeTemplate : ( (SapphireDiagramEditorPagePart) getPart() ).getNodeTemplates() )
        {
            nodeTemplate.attach( this.nodeTemplateVisibilityListener );
        }
    }

    @Override
	public List<SapphireActionHandler> create() 
	{
        final ListFactory<SapphireActionHandler> handlers = ListFactory.start();
        
        for( DiagramNodeTemplate nodeTemplate : ( (SapphireDiagramEditorPagePart) getPart() ).getNodeTemplates() )
        {
            if( nodeTemplate.visible() )
            {
                final DiagramNodeAddActionHandler addNodeHandler = new DiagramNodeAddActionHandler(nodeTemplate);
                handlers.add(addNodeHandler);
            }
        }
        
		return handlers.result();
	}

    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.nodeTemplateVisibilityListener != null )
        {
            for( DiagramNodeTemplate nodeTemplate : ( (SapphireDiagramEditorPagePart) getPart() ).getNodeTemplates() )
            {
                nodeTemplate.detach( this.nodeTemplateVisibilityListener );
            }
        }
    }

}
