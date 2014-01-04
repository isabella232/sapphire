/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.samples.postcard;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.forms.swt.SapphireWizard;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class OpenSendPostcardWizardHandler extends AbstractHandler
{
    public Object execute( final ExecutionEvent event )
    {
        final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow( event );
        
        final SendPostcardOp operation = SendPostcardOp.TYPE.instantiate();
        
        try
        {
            final SapphireWizard<SendPostcardOp> wizard = new SapphireWizard<SendPostcardOp>
            (
                operation,
                DefinitionLoader.context( getClass() ).sdef( "SendPostcardWizard" ).wizard()
            );
            
            final WizardDialog dialog = new WizardDialog( window.getShell(), wizard );
                
            dialog.open();
        }
        finally
        {
            operation.dispose();
        }
        
        return null;
    }
    
}
