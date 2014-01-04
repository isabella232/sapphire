/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.services;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.util.Comparators;
import org.eclipse.sapphire.util.Filters;
import org.eclipse.sapphire.util.SetFactory;
import org.eclipse.sapphire.util.SortedSetFactory;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class PossibleValuesService extends Service
{
    @Text( "\"{0}\" is not among possible values" )
    private static LocalizableText defaultInvalidValueMessage;
    
    static
    {
        LocalizableText.init( PossibleValuesService.class );
    }
    
    private final String invalidValueMessageTemplate;
    private final Status.Severity invalidValueSeverity;
    private final boolean caseSensitive;
    private final boolean ordered;
    
    public PossibleValuesService( final String invalidValueMessageTemplate,
                                  final Status.Severity invalidValueSeverity,
                                  final boolean caseSensitive,
                                  final boolean ordered )
    {
        if( invalidValueMessageTemplate == null || invalidValueMessageTemplate.length() == 0 )
        {
            this.invalidValueMessageTemplate = defaultInvalidValueMessage.text();
        }
        else
        {
            this.invalidValueMessageTemplate = invalidValueMessageTemplate;
        }

        this.invalidValueSeverity = ( invalidValueSeverity == null ? Status.Severity.ERROR : invalidValueSeverity );
        this.caseSensitive = caseSensitive;
        this.ordered = ordered;
    }
    
    public PossibleValuesService()
    {
        this( null, null, true, false );
    }
    
    public final Set<String> values()
    {
        if( ordered() )
        {
            final Set<String> values = new LinkedHashSet<String>();
            fillPossibleValues( values );
            return SetFactory.<String>start().filter( Filters.createNotEmptyFilter() ).add( values ).result();
        }
        else
        {
            final Comparator<String> comparator = ( isCaseSensitive() ? null : Comparators.createIgnoreCaseComparator() );
            final Set<String> values = new TreeSet<String>( comparator );
            fillPossibleValues( values );
            return SortedSetFactory.start( comparator ).filter( Filters.createNotEmptyFilter() ).add( values ).result();
        }
    }
    
    protected abstract void fillPossibleValues( Set<String> values );
    
    public String getInvalidValueMessage( final String invalidValue )
    {
        return MessageFormat.format( this.invalidValueMessageTemplate, invalidValue, context( PropertyDef.class ).getLabel( true, CapitalizationType.NO_CAPS, false ) );
    }
    
    public Status.Severity getInvalidValueSeverity( final String invalidValue )
    {
        return this.invalidValueSeverity;
    }
    
    public boolean isCaseSensitive()
    {
        return this.caseSensitive;
    }
    
    /**
     * Determines if the possible values are already ordered as intended. If the possible values
     * are not ordered, they will sorted alphabetically when presented.
     * 
     * @return true if the possible values are already ordered as intended and false otherwise
     */
    
    public boolean ordered()
    {
        return this.ordered;
    }
    
}
