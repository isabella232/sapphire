/******************************************************************************
 * Copyright (c) 2015 Liferay and Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gregory Amerson - initial implementation
 *    Konstantin Komissarchik - initial implementation review and related changes
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.actions;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart.ZoomLevelEvent;

/**
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ZoomOutDiagramActionHandler extends SapphireActionHandler
{
    @Override
    public void init( SapphireAction action, ActionHandlerDef def )
    {
        super.init( action, def );
        
        getPart().nearest( SapphireDiagramEditorPagePart.class ).attach
        (
            new Listener()
            {
                @Override
                public void handle( Event event )
                {
                    if( event instanceof ZoomLevelEvent )
                    {
                        refreshEnablement();
                    }
                }
            }
        );
        
        refreshEnablement();
    }
    
    private void refreshEnablement()
    {
        final SapphireDiagramEditorPagePart diagramEditorPagePart = getPart().nearest( SapphireDiagramEditorPagePart.class );
        setEnabled( diagramEditorPagePart.getZoomLevel() > diagramEditorPagePart.getMinZoomLevel() );
    }
    
    @Override
    protected Object run( final Presentation context )
    {
        final SapphireDiagramEditorPagePart diagramEditorPagePart = getPart().nearest( SapphireDiagramEditorPagePart.class );
        diagramEditorPagePart.setZoomLevel( diagramEditorPagePart.getZoomLevel() - 25 );

        return null;
    }

}
