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

package org.eclipse.sapphire.modeling.el;

import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.VersionConstraint;

/**
 * Determines whether Sapphire version matches a version constraint. A version constraint is a comma-separated
 * list of specific versions, closed ranges (expressed using "[1.2.3-4.5)" syntax and open ranges
 * (expressed using "[1.2.3" or "4.5)" syntax). The square brackets indicate that the range includes
 * the specified version. The parenthesis indicate that the range goes up to, but does not actually
 * include the specified version. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SapphireVersionMatchesFunction extends Function
{
    @Override
    public String name()
    {
        return "SapphireVersionMatches";
    }

    @Override
    public FunctionResult evaluate( final FunctionContext context )
    {
        return new FunctionResult( this, context )
        {
            @Override
            protected Object evaluate()
            {
                boolean result = false;
                
                final Version version = Sapphire.version();
                final VersionConstraint constraint = cast( operand( 0 ), VersionConstraint.class );
                
                if( constraint != null )
                {
                    result = constraint.check( version );
                }
                
                return result;
            }
        };
    }

}
