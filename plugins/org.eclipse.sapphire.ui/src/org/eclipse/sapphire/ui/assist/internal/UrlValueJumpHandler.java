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

package org.eclipse.sapphire.ui.assist.internal;

import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.assist.JumpHandler;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class UrlValueJumpHandler 

    extends JumpHandler
    
{
    @Override
    public boolean isApplicable( final ValueProperty property )
    {
        return property.isOfType( URL.class );
    }
    
    @Override
    public boolean canLocateJumpTarget( final SapphirePart part,
                                        final SapphireRenderingContext context,
                                        final IModelElement modelElement,
                                        final ValueProperty property )
    {
        final Value<URL> value = ( property.<Value<URL>>invokeGetterMethod( modelElement ) );
        return ( value.validate().getSeverity() != IStatus.ERROR );
    }

    @Override
    public void jump( final SapphirePart part,
                      final SapphireRenderingContext context,
                      final IModelElement modelElement,
                      final ValueProperty property )
    {
        final URL url = ( property.<Value<URL>>invokeGetterMethod( modelElement ) ).getContent();
        
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
    }
    
}