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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.internal.SapphireModelingFrameworkPlugin;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class FunctionResult
{
    private final Function function;
    private final FunctionContext context;
    private final List<FunctionResult> operands;
    private final List<FunctionResult> operandsReadOnly;
    private final List<Listener> listeners;
    private Object value;
    
    public FunctionResult( final Function function,
                           final FunctionContext context )
    {
        this.function = function;
        this.context = context;
        this.listeners = new CopyOnWriteArrayList<Listener>();
        
        final List<Function> operands = function.operands();
        
        if( operands.size() == 0 )
        {
            this.operands = Collections.emptyList();
            this.operandsReadOnly = this.operands;
        }
        else if( operands.size() == 1 )
        {
            this.operands = Collections.singletonList( operands.get( 0 ).evaluate( this.context ) );
            this.operandsReadOnly = this.operands;
        }
        else
        {
            this.operands = new ArrayList<FunctionResult>();
            
            for( Function operand : operands )
            {
                this.operands.add( operand.evaluate( this.context ) );
            }
            
            this.operandsReadOnly = Collections.unmodifiableList( this.operands );
        }

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
        return this.operandsReadOnly;
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
    
    protected abstract Object evaluate();
    
    public final Object value()
    {
        return this.value;
    }

    protected final void refresh()
    {
        final Object newValue;
        
        try
        {
            newValue = evaluate();
        }
        catch( Exception e )
        {
            SapphireModelingFrameworkPlugin.log( e );
            return;
        }
        
        if( ! equal( this.value, newValue ) )
        {
            this.value = newValue;
            notifyListeners();
        }
    }
    
    protected boolean equal( final Object a,
                             final Object b )
    {
        if( a == b )
        {
            return true;
        }
        else if( a == null || b == null )
        {
            return false;
        }
        else
        {
            return a.equals( b );
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
                SapphireModelingFrameworkPlugin.log( e );
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
    
    protected <X> X cast( Object obj,
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
            else if( obj instanceof String )
            {
                if( ( (String) obj ).length() == 0 )
                {
                    return null;
                }
            }
            
            throw new FunctionException( NLS.bind( Resources.cannotCastMessage, obj.getClass().getName(), type.getName() ) );
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
