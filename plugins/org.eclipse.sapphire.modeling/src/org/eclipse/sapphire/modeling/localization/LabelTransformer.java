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

package org.eclipse.sapphire.modeling.localization;

import org.eclipse.sapphire.modeling.CapitalizationType;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class LabelTransformer
{
    public static String transform( final String label,
                                    final CapitalizationType capitalizationType,
                                    final boolean includeMnemonic )
    {
        final StringBuilder result = new StringBuilder( label );
        
        final int mnemonicDesignatorPos = label.indexOf( '&' );
        
        if( mnemonicDesignatorPos != -1 )
        {
            result.deleteCharAt( mnemonicDesignatorPos );
        }
        
        capitalizationType.changeTo( result );
        
        if( includeMnemonic && mnemonicDesignatorPos != -1 )
        {
            result.insert( mnemonicDesignatorPos, '&' );
        }
        
        return result.toString();
    }
    
}
