/******************************************************************************
 * Copyright (c) 2016 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.samples.po;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.forms.swt.SapphireWizard;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class PurchaseComputerActionHandler extends SapphireActionHandler
{
    @Override
    protected Object run( final Presentation context )
    {
        try( PurchaseComputerOp op = PurchaseComputerOp.TYPE.instantiate() )
        {
            op.setPurchaseOrder( context.part().getLocalModelElement().nearest( PurchaseOrder.class ) );
            
            final SapphireWizard<PurchaseComputerOp> wizard = new SapphireWizard<PurchaseComputerOp>
            ( 
                op,
                DefinitionLoader.context( PurchaseComputerOp.class ).sdef( "PurchaseOrderEditor" ).wizard( "PurchaseComputerWizard" )
            );
            
            final WizardDialog dialog = new WizardDialog( ( (SwtPresentation) context ).shell(), wizard );
            
            dialog.open();
        }
        
        return null;
    }
    
}
