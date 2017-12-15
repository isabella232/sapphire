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

package org.eclipse.sapphire.samples.ezbug.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.sapphire.samples.ezbug.IBugReport;
import org.eclipse.sapphire.samples.ezbug.IFileBugReportOp;
import org.eclipse.sapphire.ui.swt.SapphireDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class FileBugReportHandler1
    
    extends AbstractHandler
    implements IHandler
    
{
    public Object execute( final ExecutionEvent event )
    
        throws ExecutionException
        
    {
        final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow( event );
        
        final IFileBugReportOp op = IFileBugReportOp.TYPE.instantiate();
        final IBugReport report = op.getBugReport();
        
        final SapphireDialog dialog 
            = new SapphireDialog( window.getShell(), report, "org.eclipse.sapphire.samples/org/eclipse/sapphire/samples/ezbug/EzBug.sdef!dialog1" );
        
        if( dialog.open() == Dialog.OK )
        {
            // Do something. User input is found in the bug report model.
        }
        
        return null;
    }
    
}
