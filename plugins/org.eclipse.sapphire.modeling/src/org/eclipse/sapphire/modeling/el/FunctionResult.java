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

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.internal.SapphireModelingExtensionSystem;
import org.eclipse.sapphire.modeling.util.NLS;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class FunctionResult
{
    private final Function function;
    private final FunctionContext context;
    private final List<FunctionResult> operands;
    private final List<Listener> listeners;
    private Object value;
    private Status status;
    
    public FunctionResult( final Function function,
                           final FunctionContext context )
    {
        this.function = function;
        this.context = context;
        this.listeners = new CopyOnWriteArrayList<Listener>();
        this.operands = Collections.unmodifiableList( initOperands() );

        if( ! this.operands.isEmpty() )
        {
            final Listener listener = new Listener()
            {
                @Override
                public void handleValueChanged()
                {
                    refresh();
                }
            };
            
            for( FunctionResult operand : this.operands )
            {
                operand.addListener( listener );
            }
        }
        
        init();
        refresh();
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
        return this.operands;
    }
    
    public final FunctionResult operand( final int position )
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
    
    protected abstract Object evaluate() throws FunctionException;
    
    /**
     * Returns the value computed by the function. 
     * 
     * @return the value computed by the function
     * @throws FunctionException if function evaluation failed with an error; to avoid exception, check
     *   status first
     */
    
    public final Object value() 
    
        throws FunctionException
        
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
        Object newValue = null;
        Status newStatus = Status.createOkStatus();
        
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
            notifyListeners();
        }
    }
    
    public final void addListener( final Listener listener )
    {
        this.listeners.add( listener );
    }
    
    public final void removeListener( final Listener listener )
    {
        this.listeners.remove( listener );
    }
    
    private final void notifyListeners()
    {
        for( Listener listener : this.listeners )
        {
            try
            {
                listener.handleValueChanged();
            }
            catch( Exception e )
            {
                LoggingService.log( e );
            }
        }
    }
    
    public void dispose()
    {
        for( FunctionResult operand : this.operands )
        {
            operand.dispose();
        }
    }
    
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    
    protected final <X> X cast( Object obj,
                                final Class<X> type )
    {
        if( obj instanceof FunctionResult )
        {
            throw new IllegalArgumentException();
        }
        
        if( type == String.class )
        {
            if( obj instanceof String )
            {
                return (X) obj;
            }
            else if( obj == null )
            {
                return (X) "";
            }
            else if( obj instanceof Enum )
            {
                return (X) ( (Enum) obj ).name();
            }
            else if( obj instanceof Value )
            {
                String res = ( (Value) obj ).getText();
                res = ( res == null ? "" : res );
                return (X) res;
            }
            else if( obj instanceof List || obj instanceof Set )
            {
                final StringBuilder res = new StringBuilder();
                boolean first = true;
                
                for( Object entry : ( (Collection) obj ) )
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
                
                return (X) res.toString();
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
                
                return (X) res.toString();
            }
            else
            {
                return (X) obj.toString();
            }
        }
        else if( Number.class.isAssignableFrom( type ) )
        {
            if( obj instanceof Value )
            {
                obj = ( (Value<?>) obj ).getContent();
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
                throw new FunctionException( NLS.bind( Resources.cannotCastMessage, obj.getClass().getName(), type.getName() ) );
            }
            
            if( obj.getClass() == type )
            {
                return (X) obj;
            }
            else if( obj instanceof Number )
            {
                if( type == BigInteger.class )
                {
                    if( obj instanceof BigDecimal )
                    {
                        return (X) ( (BigDecimal) obj ).toBigInteger();
                    }
                    else
                    {
                        return (X) BigInteger.valueOf( ( (Number) obj ).longValue() );
                    }
                }
                else if( type == BigDecimal.class )
                {
                    if( obj instanceof BigInteger )
                    {
                        return (X) new BigDecimal( (BigInteger) obj );
                    }
                    else
                    {
                        return (X) new BigDecimal( ( (Number) obj ).doubleValue() );
                    }
                }
                else if( type == Byte.class )
                {
                    return (X) new Byte( ( (Number) obj ).byteValue() );
                }
                else if( type == Short.class )
                {
                    return (X) new Short( ( (Number) obj ).shortValue() );
                }
                else if( type == Integer.class )
                {
                    return (X) new Integer( ( (Number) obj ).intValue() );
                }
                else if( type == Long.class )
                {
                    return (X) new Long( ( (Number) obj ).longValue() );
                }
                else if( type == Float.class )
                {
                    return (X) new Float( ( (Number) obj ).floatValue() );
                }
                else if( type == Double.class )
                {
                    return (X) new Double( ( (Number) obj ).doubleValue() );
                }
            }
            else if( obj instanceof String )
            {
                if( type == BigDecimal.class )
                {
                    return (X) new BigDecimal( (String) obj );
                }
                else if( type == BigInteger.class )
                {
                    return (X) new BigInteger( (String) obj );
                }
                else if( type == Byte.class )
                {
                    return (X) Byte.valueOf( (String) obj );
                }
                else if( type == Short.class )
                {
                    return (X) Short.valueOf( (String) obj );
                }
                else if( type == Integer.class )
                {
                    return (X) Integer.valueOf( (String) obj );
                }
                else if( type == Long.class )
                {
                    return (X) Long.valueOf( (String) obj );
                }
                else if( type == Float.class )
                {
                    return (X) Float.valueOf( (String) obj );
                }
                else if( type == Double.class )
                {
                    return (X) Double.valueOf( (String) obj );
                }
            }

            throw new FunctionException( NLS.bind( Resources.cannotCastMessage, obj.getClass().getName(), type.getName() ) );
        }
        else if( type == Character.class )
        {
            if( obj instanceof Value )
            {
                obj = ( (Value<?>) obj ).getContent();
            }
            
            if( obj == null || ( obj instanceof String && ( (String) obj ).length() == 0 ) )
            {
                return (X) (Character) (char) 0;
            }
            else if( obj instanceof Character )
            {
                return (X) obj;
            }
            else if( obj instanceof Boolean )
            {
                throw new FunctionException( NLS.bind( Resources.cannotCastMessage, obj.getClass().getName(), type.getName() ) );
            }
            else if( obj instanceof Number )
            {
                return (X) (Character) (char) (short) cast( obj, Short.class );
            }
            else if( obj instanceof String )
            {
                return (X) (Character) ( (String) obj ).charAt( 0 );
            }

            throw new FunctionException( NLS.bind( Resources.cannotCastMessage, obj.getClass().getName(), type.getName() ) );
        }
        else if( type == Boolean.class )
        {
            if( obj instanceof Value )
            {
                obj = ( (Value<?>) obj ).getContent();
            }
            
            if( obj == null || ( obj instanceof String && ( (String) obj ).length() == 0 ) )
            {
                return (X) Boolean.FALSE;
            }
            else if( obj instanceof Boolean )
            {
                return (X) obj;
            }
            else if( obj instanceof String )
            {
                return (X) Boolean.valueOf( (String) obj );
            }

            throw new FunctionException( NLS.bind( Resources.cannotCastMessage, obj.getClass().getName(), type.getName() ) );
        }
        else if( Enum.class.isAssignableFrom( type ) )
        {
            if( obj instanceof Value )
            {
                obj = ( (Value<?>) obj ).getContent();
            }
            
            if( obj == null )
            {
                return null;
            }
            else if( type.isInstance( obj ) )
            {
                return (X) obj;
            }
            else if( obj instanceof String )
            {
                final String str = (String) obj;
                
                if( str.length() == 0 )
                {
                    return null;
                }
                else
                {
                    return (X) Enum.valueOf( (Class<Enum>) type, str );
                }
            }

            throw new FunctionException( NLS.bind( Resources.cannotCastMessage, obj.getClass().getName(), type.getName() ) );
        }
        else if( List.class.isAssignableFrom( type ) )
        {
            if( obj instanceof Value )
            {
                obj = ( (Value<?>) obj ).getContent();
            }
            
            if( obj == null )
            {
                return null;
            }
            else if( obj instanceof List )
            {
                return (X) obj;
            }
            else if( obj instanceof Collection )
            {
                return (X) new ArrayList<Object>( (Collection<?>) obj );
            }
            else if( obj.getClass().isArray() )
            {
                final List<Object> list = new ArrayList<Object>();
                
                for( int i = 0, n = Array.getLength( obj ); i < n; i++ )
                {
                    list.add( Array.get( obj, i ) );
                }
                
                return (X) list;
            }
            else if( obj instanceof String )
            {
                final String str = (String) obj;
                
                if( str.length() == 0 )
                {
                    return (X) Collections.emptyList();
                }
                else
                {
                    return (X) Arrays.asList( ( (String) obj ).split( "\\," ) );
                }
            }
            else
            {
                return (X) Collections.singletonList( obj );
            }
        }
        else
        {
            if( obj instanceof Value )
            {
                obj = ( (Value<?>) obj ).getContent();
            }
            
            if( obj == null )
            {
                return null;
            }
            else if( type.isInstance( obj ) )
            {
                return (X) obj;
            }
            else if( obj instanceof String && ( (String) obj ).length() == 0 )
            {
                return null;
            }
            else
            {
                for( TypeCast cast : SapphireModelingExtensionSystem.getTypeCasts() )
                {
                    if( cast.applicable( this.context, this.function, obj, type ) )
                    {
                        return (X) cast.evaluate( this.context, this.function, obj, type );
                    }
                }
            }
            
            throw new FunctionException( NLS.bind( Resources.cannotCastMessage, obj.getClass().getName(), type.getName() ) );
        }
    }
    
    protected final boolean equal( final Object a,
                                   final Object b )
    {
        if( a == b )
        {
            return true;
        }
        else if( a == null )
        {
            if( b instanceof Value<?> )
            {
                return ( ( (Value<?>) b ).getText() == null );
            }
            
            return false;
        }
        else if( b == null )
        {
            if( a instanceof Value<?> )
            {
                return ( ( (Value<?>) a ).getText() == null );
            }
            
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
            return ( x == y );
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
            return ( x == y );
        }
        else if( a instanceof Boolean || b instanceof Boolean )
        {
            final Boolean x = cast( a, Boolean.class );
            final Boolean y = cast( b, Boolean.class );
            return ( x == y );
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

    public static abstract class Listener
    {
        public abstract void handleValueChanged();
    }
    
    private static final class Resources extends NLS
    {
        public static String cannotCastMessage;
        public static String missingOperandMessage;
        
        static
        {
            initializeMessages( FunctionResult.class.getName(), Resources.class );
        }
    }
    
}
