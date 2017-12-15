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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.annotations.ModelPropertyValidator;
import org.eclipse.sapphire.samples.contacts.IContact;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class AssistantNameValidator

    extends ModelPropertyValidator<Value<String>>
    
{
    @Override
    public IStatus validate( final Value<String> value )
    {
        final String assistantName = value.getText();
        final String contactName = ( (IContact) value.getParent().getParent() ).getName().getText();
        
        if( assistantName != null && contactName != null && assistantName.equals( contactName ) )
        {
            return createErrorStatus( Resources.cannotBeYourOwnAssistant );
        }
        
        return Status.OK_STATUS;
    }
    
    private static final class Resources
    
        extends NLS
    
    {
        public static String cannotBeYourOwnAssistant;
        
        static
        {
            initializeMessages( AssistantNameValidator.class.getName(), Resources.class );
        }
    }
    
}
