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

package org.eclipse.sapphire.samples.zoo.internal;

import org.eclipse.sapphire.modeling.annotations.ValuePropertyCustomBindingImpl;
import org.eclipse.sapphire.samples.zoo.IEmployee;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class EmployeeFullNamePropertyBinding

    extends ValuePropertyCustomBindingImpl
    
{
    @Override
    public String read()
    {
        final IEmployee employee = (IEmployee) getModelElement();
        final String firstName = employee.getFirstName().getText();
        final String lastName = employee.getLastName().getText();
        
        final String fullName;
        
        if( firstName == null && lastName == null )
        {
            fullName = null;
        }
        else
        {
            final StringBuffer buf = new StringBuffer();
            
            if( firstName != null )
            {
                buf.append( firstName );
            }
            
            if( lastName != null )
            {
                if( buf.length() > 0 )
                {
                    buf.append( ' ' );
                }
                
                buf.append( lastName );
            }
            
            fullName = buf.toString();
        }
        
        return fullName;
    }

    @Override
    public void write( final String value )
    {
        throw new UnsupportedOperationException();
    }
}
