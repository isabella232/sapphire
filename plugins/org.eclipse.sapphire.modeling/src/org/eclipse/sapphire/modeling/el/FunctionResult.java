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

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.sapphire.Disposable;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.ListenerContext;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.MasterConversionService;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.modeling.Status;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class FunctionResult implements Disposable
{
    @Text( "Cannot convert {0} to {1}." )
    private static LocalizableText cannotCastMessage;
    
    @Text( "Function {0} missing operand {1}." )
    private static LocalizableText missingOperandMessage;
    
    @Text( "Function {0} does not accept nulls in position {1}." )
    private static LocalizableText operandNullMessage;
    
    @Text( "Function {0} expects {2} in position {1}, but {3} was found. A conversion was not possible." )
    private static LocalizableText operandWrongTypeMessage;
    
    static
    {
        LocalizableText.init( FunctionResult.class );
    }

    private final Function function;
    private final FunctionContext context;
    private final List<FunctionResult> operands;
    private Set<Property> properties;
    private final ListenerContext listeners;
    private final Listener listener;
    private Object value;
    private Status status;
    
    public FunctionResult( final Function function,
                           final FunctionContext context )
    {
        this.function = function;
        this.context = context;
        this.listeners = new ListenerContext();
        this.operands = Collections.unmodifiableList( initOperands() );

        if( this.operands.isEmpty() )
        {
            this.listener = null;
        }
        else
        {
            this.listener = new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    refresh();
                }
            };
        }
        
        init();
        refresh( false );
    }
    
    protected List<FunctionResult> initOperands()
    {
        final List<Function> operands = function().operands();
        
        if( operands.size() == 0 )
        {
            return Collections.emptyList();
        }
        else if( operands.size() == 1 )
        {
            return Collections.singletonList( operands.get( 0 ).evaluate( this.context ) );
        }
        else
        {
            final List<FunctionResult> result = new ArrayList<FunctionResult>();
            
            for( Function operand : operands )
            {
                result.add( operand.evaluate( this.context ) );
            }
            
            return result;
        }
    }

    protected void init()
    {
    }
    
    public final Function function()
    {
        return this.function;
    }
    
    public final FunctionContext context()
    {
        return this.context;
    }
    
    public final List<FunctionResult> operands()
    {
        for( FunctionResult operand : this.operands )
        {
            listenToOperand( operand );
        }
        
        return this.operands;
    }
    
    public final Object operand( final int position )
    {
        if( position < this.operands.size() )
        {
            final FunctionResult operand = this.operands.get( position );
            
            listenToOperand( operand );
            
            return operand.value();
        }
        else
        {
            throw new FunctionException( missingOperandMessage.format( getClass().getName(), String.valueOf( position ) ) );
        }
    }
    
    public final <T> T operand( final int position, final Class<T> type, final boolean nullable )
    {
        final Object operand = operand( position );
        final T operandTyped;
        
        try
        {
            operandTyped = cast( operand, type );
        }
        catch( FunctionException e )
        {
            final String msg = operandWrongTypeMessage.format( function().name(), position, type.getName(), operand.getClass().getName() );
            throw new FunctionException( msg );
        }
        
        if( operandTyped == null && ! nullable )
        {
            final String msg = operandNullMessage.format( function().name(), position );
            throw new FunctionException( msg );
        }
        
        return operandTyped;
    }
    
    private void listenToOperand( final FunctionResult operand )
    {
        operand.attach( this.listener );
        
        Object obj = null;
        
        try
        {
            obj = operand.value();
        }
        catch( FunctionException e )
        {
            // Safe to ignore. When the function implementation accesses the value,
            // the exception will be thrown again.
        }
        
        if( obj instanceof Property )
        {
            final Property property = (Property) obj;
            
            property.attach( this.listener );
            
            if( this.properties == null )
            {
                this.properties = new HashSet<Property>( 1 );
            }
            
            this.properties.add( property );
        }           
    }
    
    protected abstract Object evaluate() throws FunctionException;
    
    /**
     * Returns the value computed by the function. 
     * 
     * @return the value computed by the function
     * @throws FunctionException if function evaluation failed with an error; to avoid exception, check
     *   status first
     */
    
    public final Object value() throws FunctionException
    {
        if( this.status.severity() == Status.Severity.ERROR )
        {
            throw new FunctionException( this.status );
        }
        
        return this.value;
    }
    
    /**
     * Returns the status of function execution. This will show if function executed without any issues or if it
     * encountered an error condition.
     * 
     * @return the status of function execution
     */
    
    public final Status status()
    {
        return this.status;
    }

    protected final void refresh()
    {
        refresh( true );
    }
    
    private final void refresh( final boolean broadcastIfNecessary )
    {
        Object newValue = null;
        Status newStatus = Status.createOkStatus();
        
        if( this.properties != null )
        {
            for( Property property : this.properties )
            {
                property.detach( this.listener );
            }
            
            this.properties.clear();
        }
        
        try
        {
            newValue = evaluate();
        }
        catch( FunctionException e )
        {
            newStatus = e.status();
        }
        catch( Exception e )
        {
            newStatus = Status.createErrorStatus( e );
        }
        
        if( newValue instanceof Function )
        {
            throw new IllegalStateException();
        }
        
        if( ! equal( this.value, newValue ) || ! equal( this.status, newStatus ))
        {
            this.value = newValue;
            this.status = newStatus;
            
            if( broadcastIfNecessary )
            {
                this.listeners.broadcast( new Event() );
            }
        }
    }
    
    public final boolean attach( final Listener listener )
    {
        return this.listeners.attach( listener );
    }
    
    public final boolean detach( final Listener listener )
    {
        return this.listeners.detach( listener );
    }
    
    @Override
    public void dispose()
    {
        for( FunctionResult operand : this.operands )
        {
            operand.dispose();
        }
        
        if( this.properties != null )
        {
            for( Property property : this.properties )
            {
                property.detach( this.listener );
            }
            
            this.properties.clear();
        }
    }
    
    protected final <X> X cast( Object obj,
                                final Class<X> type )
    {
        if( obj instanceof FunctionResult )
        {
            throw new IllegalArgumentException();
        }
        
        if( obj != null && type.isAssignableFrom( obj.getClass() ) )
        {
            return type.cast( obj );
        }
        
        try
        {
            if( type == String.class )
            {
                if( obj instanceof String )
                {
                    return type.cast( obj );
                }
                else if( obj == null )
                {
                    return type.cast( "" );
                }
                else if( obj instanceof Value )
                {
                    String res = ( (Value<?>) obj ).text();
                    res = ( res == null ? "" : res );
                    return type.cast( res );
                }
                else if( obj instanceof List || obj instanceof Set )
                {
                    final StringBuilder res = new StringBuilder();
                    boolean first = true;
                    
                    for( Object entry : ( (Collection<?>) obj ) )
                    {
                        if( first )
                        {
                            first = false;
                        }
                        else
                        {
                            res.append( ',' );
                        }
                        
                        final String str = cast( entry, String.class );
                        
                        if( str != null )
                        {
                            res.append( str );
                        }
                    }
                    
                    return type.cast( res.toString() );
                }
                else if( obj.getClass().isArray() )
                {
                    final StringBuilder res = new StringBuilder();
                    
                    for( int i = 0, n = Array.getLength( obj ); i < n; i++ )
                    {
                        if( i > 0 )
                        {
                            res.append( ',' );
                        }
                        
                        final String str = cast( Array.get( obj, i ), String.class );
                        
                        if( str != null )
                        {
                            res.append( str );
                        }
                    }
                    
                    return type.cast( res.toString() );
                }
            }
            
            if( Number.class.isAssignableFrom( type ) )
            {
                if( obj instanceof Value )
                {
                    obj = ( (Value<?>) obj ).content();
                }
                
                if( obj == null || ( obj instanceof String && ( (String) obj ).length() == 0 ) )
                {
                    obj = (short) 0;
                }
                else if( obj instanceof Character )
                {
                    obj = (short) ( (Character) obj ).charValue();
                }
                else if( obj instanceof Boolean )
                {
                    throw new FunctionException( cannotCastMessage.format( obj.getClass().getName(), type.getName() ) );
                }
                
                if( obj.getClass() == type )
                {
                    return type.cast( obj );
                }
                else if( obj instanceof Number )
                {
                    if( type == BigInteger.class )
                    {
                        if( obj instanceof BigDecimal )
                        {
                            return type.cast( ( (BigDecimal) obj ).toBigInteger() );
                        }
                        else
                        {
                            return type.cast( BigInteger.valueOf( ( (Number) obj ).longValue() ) );
                        }
                    }
                    else if( type == BigDecimal.class )
                    {
                        if( obj instanceof BigInteger )
                        {
                            return type.cast( new BigDecimal( (BigInteger) obj ) );
                        }
                        else
                        {
                            return type.cast( new BigDecimal( ( (Number) obj ).doubleValue() ) );
                        }
                    }
                    else if( type == Byte.class )
                    {
                        return type.cast( Byte.valueOf( ( (Number) obj ).byteValue() ) );
                    }
                    else if( type == Short.class )
                    {
                        return type.cast( Short.valueOf( ( (Number) obj ).shortValue() ) );
                    }
                    else if( type == Integer.class )
                    {
                        return type.cast( Integer.valueOf( ( (Number) obj ).intValue() ) );
                    }
                    else if( type == Long.class )
                    {
                        return type.cast( Long.valueOf( ( (Number) obj ).longValue() ) );
                    }
                    else if( type == Float.class )
                    {
                        return type.cast( Float.valueOf( ( (Number) obj ).floatValue() ) );
                    }
                    else if( type == Double.class )
                    {
                        return type.cast( Double.valueOf( ( (Number) obj ).doubleValue() ) );
                    }
                }
                else if( obj instanceof String )
                {
                    if( type == BigDecimal.class )
                    {
                        return type.cast( new BigDecimal( (String) obj ) );
                    }
                    else if( type == BigInteger.class )
                    {
                        return type.cast( new BigInteger( (String) obj ) );
                    }
                    else if( type == Byte.class )
                    {
                        return type.cast( Byte.valueOf( (String) obj ) );
                    }
                    else if( type == Short.class )
                    {
                        return type.cast( Short.valueOf( (String) obj ) );
                    }
                    else if( type == Integer.class )
                    {
                        return type.cast( Integer.valueOf( (String) obj ) );
                    }
                    else if( type == Long.class )
                    {
                        return type.cast( Long.valueOf( (String) obj ) );
                    }
                    else if( type == Float.class )
                    {
                        return type.cast( Float.valueOf( (String) obj ) );
                    }
                    else if( type == Double.class )
                    {
                        return type.cast( Double.valueOf( (String) obj ) );
                    }
                }
    
                throw new FunctionException( cannotCastMessage.format( obj.getClass().getName(), type.getName() ) );
            }
            else if( type == Character.class )
            {
                if( obj instanceof Value )
                {
                    obj = ( (Value<?>) obj ).content();
                }
                
                if( obj == null || ( obj instanceof String && ( (String) obj ).length() == 0 ) )
                {
                    return type.cast( (char) 0 );
                }
                else if( obj instanceof Character )
                {
                    return type.cast( obj );
                }
                else if( obj instanceof Boolean )
                {
                    throw new FunctionException( cannotCastMessage.format( obj.getClass().getName(), type.getName() ) );
                }
                else if( obj instanceof Number )
                {
                    return type.cast( (char) (short) cast( obj, Short.class ) );
                }
                else if( obj instanceof String )
                {
                    return type.cast( ( (String) obj ).charAt( 0 ) );
                }
    
                throw new FunctionException( cannotCastMessage.format( obj.getClass().getName(), type.getName() ) );
            }
            else if( type == Boolean.class )
            {
                if( obj instanceof Value )
                {
                    obj = ( (Value<?>) obj ).content();
                }
                
                if( obj == null || ( obj instanceof String && ( (String) obj ).length() == 0 ) )
                {
                    return type.cast( Boolean.FALSE );
                }
                else if( obj instanceof Boolean )
                {
                    return type.cast( obj );
                }
                else if( obj instanceof String )
                {
                    return type.cast( Boolean.valueOf( (String) obj ) );
                }
    
                throw new FunctionException( cannotCastMessage.format( obj.getClass().getName(), type.getName() ) );
            }
            else if( List.class.isAssignableFrom( type ) )
            {
                if( obj instanceof Value )
                {
                    obj = ( (Value<?>) obj ).content();
                }
                
                if( obj == null )
                {
                    return null;
                }
                else if( obj instanceof List )
                {
                    return type.cast( obj );
                }
                else if( obj instanceof Collection )
                {
                    return type.cast( new ArrayList<Object>( (Collection<?>) obj ) );
                }
                else if( obj.getClass().isArray() )
                {
                    final List<Object> list = new ArrayList<Object>();
                    
                    for( int i = 0, n = Array.getLength( obj ); i < n; i++ )
                    {
                        list.add( Array.get( obj, i ) );
                    }
                    
                    return type.cast( list );
                }
                else if( obj instanceof String )
                {
                    final String str = (String) obj;
                    
                    if( str.length() == 0 )
                    {
                        return type.cast( Collections.emptyList() );
                    }
                    else
                    {
                        return type.cast( Arrays.asList( ( (String) obj ).split( "\\," ) ) );
                    }
                }
                else
                {
                    return type.cast( Collections.singletonList( obj ) );
                }
            }
            else
            {
                if( obj instanceof Value )
                {
                    obj = ( (Value<?>) obj ).content();
                }
                
                if( obj == null )
                {
                    return null;
                }
                else if( type.isInstance( obj ) )
                {
                    return type.cast( obj );
                }
                else if( obj instanceof String && ( (String) obj ).length() == 0 )
                {
                    return null;
                }
                else
                {
                    final MasterConversionService masterConversionService;
                    final Object origin = this.function.origin();
                    
                    if( origin instanceof Element )
                    {
                        masterConversionService = ( (Element) origin ).service( MasterConversionService.class );
                    }
                    else
                    {
                        masterConversionService = Sapphire.service( MasterConversionService.class );
                    }

                    final X result = type.cast( masterConversionService.convert( obj, type ) );
                    
                    if( result != null )
                    {
                        return result;
                    }
                }
                
                throw new FunctionException( cannotCastMessage.format( obj.getClass().getName(), type.getName() ) );
            }
        }
        catch( FunctionException e )
        {
            if( ! ( obj instanceof String ) )
            {
                try
                {
                    return cast( cast( obj, String.class ), type );
                }
                catch( FunctionException ex )
                {
                    // Ignore the composite cast failure, since we want the original exception.
                }
            }
            
            throw e;
        }
    }
    
    protected final boolean equal( Object a, Object b )
    {
        if( a instanceof Value<?> )
        {
            final Value<?> value = (Value<?>) a;
            
            if( ! value.disposed() )
            {
                a = value.text();
            }
        }
        
        if( b instanceof Value<?> )
        {
            final Value<?> value = (Value<?>) b;
            
            if( ! value.disposed() )
            {
                b = value.text();
            }
        }
        
        if( a == b )
        {
            return true;
        }
        else if( a == null || b == null )
        {
            return false;
        }
        else if( a instanceof BigDecimal || b instanceof BigDecimal )
        {
            final BigDecimal x = cast( a, BigDecimal.class );
            final BigDecimal y = cast( b, BigDecimal.class );
            return x.equals( y );
        }
        else if( a instanceof Float || a instanceof Double || b instanceof Float || b instanceof Double )
        {
            final Double x = cast( a, Double.class );
            final Double y = cast( b, Double.class );
            return x.equals( y );
        }
        else if( a instanceof BigInteger || b instanceof BigInteger )
        {
            final BigInteger x = cast( a, BigInteger.class );
            final BigInteger y = cast( b, BigInteger.class );
            return x.equals( y );
        }
        else if( a instanceof Byte || a instanceof Short || a instanceof Character || a instanceof Integer || a instanceof Long || 
                 b instanceof Byte || b instanceof Short || b instanceof Character || b instanceof Integer || b instanceof Long )
        {
            final Long x = cast( a, Long.class );
            final Long y = cast( b, Long.class );
            return x.equals( y );
        }
        else if( a instanceof Boolean || b instanceof Boolean )
        {
            final Boolean x = cast( a, Boolean.class );
            final Boolean y = cast( b, Boolean.class );
            return x.equals( y );
        }
        else if( a instanceof Enum )
        {
            return ( a == cast( b, a.getClass() ) );
        }
        else if( b instanceof Enum )
        {
            return ( cast( a, b.getClass() ) == b );
        }
        else if( a instanceof String || b instanceof String )
        {
            final String x = cast( a, String.class );
            final String y = cast( b, String.class );
            return ( x.compareTo( y ) == 0 );
        }
        else
        {
            return a.equals( b );
        }
    }
    
}
