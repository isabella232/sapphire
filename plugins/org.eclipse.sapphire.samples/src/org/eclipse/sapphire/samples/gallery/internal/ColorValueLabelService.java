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

package org.eclipse.sapphire.samples.gallery.internal;

import org.eclipse.sapphire.modeling.ValueLabelService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ColorValueLabelService extends ValueLabelService
{
    @Override
    public String provide( final String value )
    {
        if( value != null )
        {
            if( value.equals( "red" ) )
            {
                return "Red [FF0000]";
            }
            else if( value.equals( "orange" ) )
            {
                return "Orange [FF8A00]";
            }
            else if( value.equals( "yellow" ) )
            {
                return "Yellow [FFF200]";
            }
            else if( value.equals( "green" ) )
            {
                return "Green [00BC00]";
            }
            else if( value.equals( "blue" ) )
            {
                return "Blue [0000FF]";
            }
            else if( value.equals( "violet" ) )
            {
                return "Violet [8A00FF]";
            }
        }
        
        return value;
    }
    
}
