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

package org.eclipse.sapphire.modeling.el;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.osgi.util.NLS;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class Function
{
    private List<Function> operands;
    private List<Function> operandsReadOnly;
    
    public final void init( final Function... operands )
    {
        final int count = operands.length;
        
        if( count == 0 )
        {
            this.operands = Collections.emptyList();
            this.operandsReadOnly = this.operands;
        }
        else if( count == 1 )
        {
            this.operands = Collections.singletonList( operands[ 0 ] );
            this.operandsReadOnly = this.operands;
        }
        else
        {
            this.operands = new ArrayList<Function>( count );
            
            for( Function operand : operands )
            {
                this.operands.add( operand );
            }
            
            this.operandsReadOnly = Collections.unmodifiableList( this.operands );
        }
    }

    public final void init( final List<Function> operands )
    {
        final int count = operands.size();
        
        if( count == 0 )
        {
            this.operands = Collections.emptyList();
            this.operandsReadOnly = this.operands;
        }
        else if( count == 1 )
        {
            this.operands = Collections.singletonList( operands.get( 0 ) );
            this.operandsReadOnly = this.operands;
        }
        else
        {
            this.operands = new ArrayList<Function>( count );
            this.operands.addAll( operands );
            this.operandsReadOnly = Collections.unmodifiableList( this.operands );
        }
    }
    
    public final List<Function> operands()
    {
        return this.operandsReadOnly;
    }
    
    public final Function operand( final int position )
    {
        if( position < this.operands.size() )
        {
            return this.operands.get( position );
        }
        else
        {
            throw new FunctionException( NLS.bind( Resources.missingOperandMessage, getClass().getName(), String.valueOf( position ) ) );
        }
    }
    
    public abstract FunctionResult evaluate( FunctionContext context );

    
    private static final class Resources extends NLS
    {
        public static String missingOperandMessage;
        
        static
        {
            initializeMessages( Function.class.getName(), Resources.class );
        }
    }
    
}
