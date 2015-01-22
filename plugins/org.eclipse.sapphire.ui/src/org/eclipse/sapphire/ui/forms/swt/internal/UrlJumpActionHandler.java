/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt.internal;

import java.net.URL;

import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.forms.JumpActionHandler;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class UrlJumpActionHandler extends JumpActionHandler
{
    public static final String ID = "Sapphire.Jump.URL";
    
    public UrlJumpActionHandler()
    {
        setId( ID );
    }
    
    @Override
    protected boolean computeEnablementState()
    {
        if( super.computeEnablementState() == true )
        {
            final Value<?> value = (Value<?>) property();
            return ( value.validation().severity() != Status.Severity.ERROR );
        }
        
        return false;
    }

    @Override
    protected Object run( final Presentation context )
    {
        final URL url = (URL) ( (Value<?>) property() ).content();
        
        if( url != null )
        {
            final IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
            
            try
            {
                final IWebBrowser browser = support.getExternalBrowser();
                browser.openURL( url );
            }
            catch( PartInitException e ) 
            {
                Sapphire.service( LoggingService.class ).log( e );
            }
        }
        
        return null;
    }
    
}