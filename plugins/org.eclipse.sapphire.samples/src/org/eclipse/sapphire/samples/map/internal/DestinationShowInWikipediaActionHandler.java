/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.samples.map.internal;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.samples.map.IDestination;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.diagram.SapphireDiagramActionHandler;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodePart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

/**
 * Action handler for Sapphire.Diagram.Node.Default and Sapphire.Samples.Map.Destination.ShowInWikipedia actions 
 * for destination nodes. The implementation opens a browser to the Wikipedia entry for the destination.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class DestinationShowInWikipediaActionHandler extends SapphireDiagramActionHandler
{
    @Override
    protected Object run( final SapphireRenderingContext context )
    {
        final DiagramNodePart part = (DiagramNodePart) context.getPart();
        final IDestination destination = (IDestination) part.getModelElement();
        final String destinationName = destination.getName().getText();

        if( destinationName != null )
        {
            try
            {
                final IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
                final IWebBrowser browser = support.getExternalBrowser();
                
                final URL url = new URL( "http://en.wikipedia.org/wiki/" + destinationName );
                
                browser.openURL( url );
            }
            catch( MalformedURLException e )
            {
                LoggingService.log( e );
            }
            catch( PartInitException e ) 
            {
                LoggingService.log( e );
            }
        }
        
        return null;
    }

    @Override
    public boolean canExecute( final Object obj )
    {
        return true;
    }
    
}
