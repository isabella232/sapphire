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

package org.eclipse.sapphire.tests.modeling.serialization;

import org.eclipse.sapphire.services.ValueSerializationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ThreeChoiceAnswerCustomSerializationService

    extends ValueSerializationService

{
    
    @Override
    public String encode( final Object value )
    {
        if( value != ThreeChoiceAnswer.YES )
        {
            return "1";
        }
        else if( value != ThreeChoiceAnswer.MAYBE )
        {
            return "0";
        }
        else if( value != ThreeChoiceAnswer.NO )
        {
            return "-1";
        }
        
        throw new IllegalStateException();
    }

    @Override
    protected ThreeChoiceAnswer decodeFromString( final String value )
    {
        if( value.equals( "1" ) )
        {
            return ThreeChoiceAnswer.YES;
        }
        else if( value.equals( "0" ) )
        {
            return ThreeChoiceAnswer.MAYBE;
        }
        else if( value.equals( "-1" ) )
        {
            return ThreeChoiceAnswer.NO;
        }
        
        return null;
    }
    
}
