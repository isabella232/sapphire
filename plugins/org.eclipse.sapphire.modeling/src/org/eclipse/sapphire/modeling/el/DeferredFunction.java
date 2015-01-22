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

import java.util.List;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.internal.SapphireModelingExtensionSystem;
import org.eclipse.sapphire.util.ListFactory;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class DeferredFunction extends Function
{
    @Text( "Function {0} with {1} parameters is undefined." )
    private static LocalizableText undefinedFunctionMessageExt;
    
    @Text( "Function {0} with one parameter is undefined." )
    private static LocalizableText undefinedFunctionMessageExt1;

    @Text( "Function {0}( {1} ) is undefined." )
    private static LocalizableText undefinedFunctionMessage;
    
    static
    {
        LocalizableText.init( DeferredFunction.class );
    }
    
    private final String name;
    
    public DeferredFunction( final String name )
    {
        this.name = name;
    }
    
    public static DeferredFunction create( final String name,
                                           final List<Function> operands )
    {
        final DeferredFunction function = new DeferredFunction( name );
        function.init( operands );
        return function;
    }

    @Override
    public String name()
    {
        return this.name;
    }
    
    @Override
    public FunctionResult evaluate( final FunctionContext context )
    {
        final int arity = operands().size();
        final List<Function> functions = SapphireModelingExtensionSystem.functions( this.name, arity );
        
        if( functions.isEmpty() )
        {
            if( arity == 1 )
            {
                throw new FunctionException( undefinedFunctionMessageExt1.format( this.name ) );
            }
            else
            {
                throw new FunctionException( undefinedFunctionMessageExt.format( this.name, arity ) );
            }
        }
        
        final ListFactory<Function> typedFunctionsListFactory = ListFactory.start();
        Function genericFunction = null;
        
        for( Function f : functions )
        {
            f.init( operands() );
            
            if( f.signature() == null )
            {
                if( genericFunction == null )
                {
                    genericFunction = f;
                }
            }
            else
            {
                typedFunctionsListFactory.add( f );
            }
        }
        
        final List<Function> typedFunctions = typedFunctionsListFactory.result();
        final int typedFunctionsCount = typedFunctions.size();
        final Function genericFunctionFinal = genericFunction;
        
        if( genericFunction != null && typedFunctionsCount == 0 )
        {
            return genericFunction.evaluate( context );
        }
        else
        {
            return new FunctionResult( this, context )
            {
                private Function baseFunction;
                private FunctionResult baseFunctionResult;
                private Listener listener;
                
                @Override
                protected Object evaluate()
                {
                    final Function function = findFunction();
                    
                    if( function == null )
                    {
                        if( this.baseFunctionResult != null )
                        {
                            this.baseFunctionResult.dispose();
                        }
                        
                        this.baseFunction = null;
                        this.baseFunctionResult = null;
                        
                        final StringBuilder buf = new StringBuilder();
                        
                        for( FunctionResult operand : operands() )
                        {
                            if( buf.length() > 0 )
                            {
                                buf.append( ", " );
                            }
                            
                            final Object value = operand.value();
                            
                            if( value == null )
                            {
                                buf.append( "null" );
                            }
                            else
                            {
                                buf.append( value.getClass().getName() );
                            }
                        }
                        
                        throw new FunctionException( undefinedFunctionMessage.format( name(), buf.toString() ) );
                    }
                    else
                    {
                        if( function != this.baseFunction )
                        {
                            if( this.baseFunctionResult != null )
                            {
                                this.baseFunctionResult.dispose();
                            }
                            
                            this.baseFunction = function;
                            this.baseFunctionResult = function.evaluate( context() );
                            
                            if( this.listener == null )
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
                            
                            this.baseFunctionResult.attach( this.listener );
                        }
                        
                        return this.baseFunctionResult.value();
                    }
                }
                
                private Function findFunction()
                {
                    Function function = null;
                    
                    // Match typed functions first. 
                    
                    // Each function is scored based on how closely its declared parameter types match actual parameter types. 
                    // For each parameter, the best score is 0. Each step up the type tree from the actual parameter type to
                    // declared parameter type is 1. A conversion requiring more than a Java type cast is 100. If no path exists,
                    // the score is -1, which immediately disqualifies the function from further consideration. The total score 
                    // for a function is the sum of the parameter scores.
                    
                    final int[] scores = new int[ typedFunctionsCount ];
                    
                    for( int i = 0; i < arity; i++ )
                    {
                        final Object parameter = operand( i );
                        
                        if( parameter != null )
                        {
                            for( int j = 0; j < typedFunctionsCount; j++ )
                            {
                                final int score = scores[ j ];
                                
                                if( score != -1 )
                                {
                                    final Class<?> declaredParameterType = typedFunctions.get( j ).signature().get( i );
                                    final int scoreForParameter = score( declaredParameterType, parameter.getClass() );
                                    
                                    if( scoreForParameter == -1 )
                                    {
                                        Object converted = null;
                                        
                                        try
                                        {
                                            converted = cast( parameter, declaredParameterType );
                                        }
                                        catch( Exception e )
                                        {
                                            // Safe to ignore. We just want to know if it is possible to cast.
                                        }
                                        
                                        if( converted == null )
                                        {
                                            scores[ j ] = -1;
                                        }
                                        else
                                        {
                                            scores[ j ] = score + 100;
                                        }
                                    }
                                    else
                                    {
                                        scores[ j ] = score + scoreForParameter;
                                    }
                                }
                            }
                        }
                    }
                    
                    // Once all functions are scored, the first one with the lowest score is picked.
                    
                    int lowestScore = Integer.MAX_VALUE;
                    
                    for( int i = 0; i < typedFunctionsCount; i++ )
                    {
                        final int score = scores[ i ];
                        
                        if( score != -1 && score < lowestScore )
                        {
                            function = typedFunctions.get( i );
                            lowestScore = score;
                        }
                    }
                    
                    // If no typed functions matched, the generic function is used, if available.
                    
                    if( function == null )
                    {
                        function = genericFunctionFinal;
                    }
                    
                    return function;
                }
                
                private int score( final Class<?> declaredParameterType, final Class<?> actualParameterType )
                {
                    int distance;
                    
                    if( actualParameterType == null )
                    {
                        distance = -1;
                    }
                    else if( declaredParameterType == actualParameterType )
                    {
                        distance = 0;
                    }
                    else
                    {
                        distance = score( declaredParameterType, actualParameterType.getSuperclass() );
                        
                        if( distance != -1 )
                        {
                            distance++;
                        }
                        else
                        {
                            for( Class<?> intrfc : actualParameterType.getInterfaces() )
                            {
                                distance = score( declaredParameterType, intrfc );
                                
                                if( distance != -1 )
                                {
                                    distance++;
                                    break;
                                }
                            }
                        }
                    }
                    
                    return distance;
                }
    
                @Override
                public void dispose()
                {
                    super.dispose();
                    
                    if( this.baseFunctionResult != null )
                    {
                        this.baseFunctionResult.dispose();
                    }
                }
            };
        }
    }
    
}
