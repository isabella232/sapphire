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

package org.eclipse.sapphire;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.el.FailSafeFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.eclipse.sapphire.services.DataService;
import org.eclipse.sapphire.util.Filters;
import org.eclipse.sapphire.util.SetFactory;
import org.eclipse.sapphire.util.SortedSetFactory;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class PossibleValuesService extends DataService<Set<String>>
{
    @Text( "\"{0}\" is not among possible values" )
    private static LocalizableText defaultInvalidValueMessage;
    
    static
    {
        LocalizableText.init( PossibleValuesService.class );
    }
    
    protected String invalidValueMessage;
    protected Function invalidValueMessageFunction;
    protected Status.Severity invalidValueSeverity = Status.Severity.ERROR;
    protected boolean ordered = false;
    private CollationService collationService;
    private Listener collationServiceListener;

    @Override
    protected final void initDataService()
    {
        this.collationService = service( CollationService.class );
        
        this.collationServiceListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                refresh();
            }
        };
        
        this.collationService.attach( this.collationServiceListener );
        
        initPossibleValuesService();
    }

    protected void initPossibleValuesService()
    {
    }
    
    public final Set<String> values()
    {
        return data();
    }
    
    @Override
    protected final Set<String> compute()
    {
        if( ordered() )
        {
            final Set<String> values = new LinkedHashSet<String>();
            compute( values );
            return SetFactory.<String>start().filter( Filters.createNotEmptyFilter() ).add( values ).result();
        }
        else
        {
            final Comparator<String> comparator = this.collationService.comparator();
            final Set<String> values = new TreeSet<String>( comparator );
            compute( values );
            return SortedSetFactory.start( comparator ).filter( Filters.createNotEmptyFilter() ).add( values ).result();
        }
    }
    
    protected abstract void compute( Set<String> values );
    
    public Status problem( final Value<?> value )
    {
        if( this.invalidValueSeverity != Status.Severity.OK )
        {
            synchronized( this )
            {
                if( this.invalidValueMessageFunction == null )
                {
                    final String def = MessageFormat.format( defaultInvalidValueMessage.text(), "${" + value.name() + "}" );
                    
                    if( this.invalidValueMessage == null )
                    {
                        this.invalidValueMessage = def;
                    }
                    
                    this.invalidValueMessageFunction = FailSafeFunction.create( ExpressionLanguageParser.parse( this.invalidValueMessage ), String.class, def );
                }
            }
            
            final FunctionResult messageFunctionResult = this.invalidValueMessageFunction.evaluate( new ModelElementFunctionContext( value.element() ) );
            
            try
            {
                return Status.createStatus( this.invalidValueSeverity, (String) messageFunctionResult.value() );
            }
            finally
            {
                messageFunctionResult.dispose();
            }
        }
        
        return Status.createOkStatus();
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
    
    /**
     * Determines whether the property value is strictly constrained to the set of possible values or if deviations are allowed.
     * 
     * @return true if the property value is strictly constrained to the set of possible values and false otherwise
     */
    
    public boolean strict()
    {
        return ( this.invalidValueSeverity == Status.Severity.ERROR );
    }

    @Override
    public void dispose()
    {
        if( this.collationService != null )
        {
            this.collationService.detach( this.collationServiceListener );
            this.collationService = null;
            this.collationServiceListener = null;
        }
        
        super.dispose();
    }
    
}
