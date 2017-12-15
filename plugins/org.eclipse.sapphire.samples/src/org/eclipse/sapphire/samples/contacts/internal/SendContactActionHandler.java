/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.samples.contacts.internal;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.sapphire.samples.contacts.IContact;
import org.eclipse.sapphire.samples.contacts.ISendContactOp;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.swt.SapphireWizard;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SendContactActionHandler

    extends SapphireActionHandler
    
{
    @Override
    protected Object run( final SapphireRenderingContext context )
    {
        final IContact contact = (IContact) getModelElement();
        
        final ISendContactOp op = ISendContactOp.TYPE.instantiate();
        op.setContact( contact );
        
        final SapphireWizard<ISendContactOp> wizard 
            = new SapphireWizard<ISendContactOp>( op, "org.eclipse.sapphire.samples/org/eclipse/sapphire/samples/contacts/ContactsDatabaseEditor.sdef!SendContactWizard" );
        
        final WizardDialog dialog = new WizardDialog( context.getShell(), wizard );
        
        dialog.open();
        
        return null;
    }
    
}
