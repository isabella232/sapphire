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

package org.eclipse.sapphire.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.CorruptedResourceExceptionInterceptor;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class CorruptedResourceExceptionInterceptorImpl extends CorruptedResourceExceptionInterceptor
{
    @Text( "Operation Failed" )
    private static LocalizableText malformedFileDialogTitle;
    
    @Text( "This file appears to be malformed. Do you want a fresh start?" )
    private static LocalizableText malformedFileDialogMsg;

    static 
    {
        LocalizableText.init( CorruptedResourceExceptionInterceptorImpl.class );
    }

    private final Shell shell;
    
    public CorruptedResourceExceptionInterceptorImpl( final Shell shell )
    {
        this.shell = shell;
    }

    @Override
    public boolean shouldAttemptRepair()
    {
        final boolean fixMalformedDescriptor
            = MessageDialog.openQuestion( this.shell, malformedFileDialogTitle.text(), malformedFileDialogMsg.text() );
        
        return fixMalformedDescriptor;
    }
    
    protected final Shell getShell()
    {
        return this.shell;
    }
    
}
