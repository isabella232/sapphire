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

package org.eclipse.sapphire.tests.modeling.misc.t0011;

import org.eclipse.sapphire.services.DefaultValueService;
import org.eclipse.sapphire.services.DefaultValueServiceData;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ContentDefaultValueService extends DefaultValueService
{
    @Override
    public DefaultValueServiceData data()
    {
        refresh();
        return super.data();
    }

    @Override
    protected DefaultValueServiceData compute()
    {
        String value = null;
        
        final TestElementChild element = context( TestElementChild.class );
        final String ref = element.getReference().getText();
        
        if( ref != null )
        {
            final TestElementRoot root = context( TestElementRoot.class );
            
            for( TestElementChild child : root.getChildren() )
            {
                if( child != element && ref.equals( child.getId().getText() ) )
                {
                    value = child.getContent().getText();
                }
            }
        }
        
        return new DefaultValueServiceData( value );
    }

}
