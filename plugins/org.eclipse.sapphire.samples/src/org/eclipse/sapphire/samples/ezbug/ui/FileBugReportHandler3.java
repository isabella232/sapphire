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

package org.eclipse.sapphire.samples.ezbug.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.sapphire.samples.ezbug.IFileBugReportOp;
import org.eclipse.sapphire.ui.swt.SapphireWizard;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class FileBugReportHandler3
    
    extends AbstractHandler
    implements IHandler
    
{
    public Object execute( final ExecutionEvent event )
    
        throws ExecutionException
        
    {
        final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow( event );
        
        final IFileBugReportOp op = IFileBugReportOp.TYPE.instantiate();
        
        final SapphireWizard<IFileBugReportOp> wizard 
            = new SapphireWizard<IFileBugReportOp>( op, "org.eclipse.sapphire.samples/sdef/EzBug.sdef!wizard" );
        
        final WizardDialog dialog = new WizardDialog( window.getShell(), wizard );
        
        dialog.open();
        
        return null;
    }
    
}
