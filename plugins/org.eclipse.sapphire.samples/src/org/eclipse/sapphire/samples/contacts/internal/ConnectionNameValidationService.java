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

package org.eclipse.sapphire.samples.contacts.internal;

import org.eclipse.sapphire.modeling.ModelPropertyValidationService;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.samples.contacts.IContact;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class ConnectionNameValidationService

    extends ModelPropertyValidationService<Value<?>>
    
{
    @Override
    public Status validate()
    {
        final Value<?> value = target();
        final String assistantName = value.getText();
        final String contactName = value.nearest( IContact.class ).getName().getText();
        
        if( assistantName != null && contactName != null && assistantName.equals( contactName ) )
        {
            return createErrorStatus();
        }
        
        return Status.createOkStatus();
    }
    
    protected abstract Status createErrorStatus();
    
}
