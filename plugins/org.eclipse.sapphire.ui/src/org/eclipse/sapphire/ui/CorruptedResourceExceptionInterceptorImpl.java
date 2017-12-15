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

package org.eclipse.sapphire.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.sapphire.modeling.CorruptedResourceExceptionInterceptor;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class CorruptedResourceExceptionInterceptorImpl

    extends CorruptedResourceExceptionInterceptor
    
{
    private final Shell shell;
    
    public CorruptedResourceExceptionInterceptorImpl( final Shell shell )
    {
        this.shell = shell;
    }

    @Override
    public boolean shouldAttemptRepair()
    {
        final boolean fixMalformedDescriptor
            = MessageDialog.openQuestion( this.shell, Resources.malformedFileDialogTitle, 
                                          Resources.malformedFileDialogMsg );
        
        return fixMalformedDescriptor;
    }
    
    protected final Shell getShell()
    {
        return this.shell;
    }
    
    private static final class Resources 
    
        extends NLS
        
    {
        public static String malformedFileDialogTitle;
        public static String malformedFileDialogMsg;
    
        static 
        {
            initializeMessages( CorruptedResourceExceptionInterceptorImpl.class.getName(), 
                                Resources.class );
        }
    }
    
}
