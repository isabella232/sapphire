/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.renderer.actions.internal;

import java.net.URL;

import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.ui.SapphireJumpActionHandler;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class UrlJumpActionHandler 

    extends SapphireJumpActionHandler
    
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
            final Value<URL> value = getModelElement().read( getProperty() );
            return ( value.validation().severity() != Status.Severity.ERROR );
        }
        
        return false;
    }

    @Override
    protected Object run( final SapphireRenderingContext context )
    {
        final URL url = getModelElement().<URL>read( getProperty() ).getContent();
        
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
                SapphireUiFrameworkPlugin.log( e );
            }
        }
        
        return null;
    }
    
}