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

package org.eclipse.sapphire.internal;

import java.util.List;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Validation;
import org.eclipse.sapphire.Validations;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.Status.CompositeStatusFactory;
import org.eclipse.sapphire.modeling.el.FailSafeFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ValidationService;
import org.eclipse.sapphire.util.ListFactory;

/**
 * {@link ValidationService} implementation that derives its behavior from @{@link Validation} annotation.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class DeclarativeValidationService extends ValidationService
{
    private List<Rule> rules;
    
    @Override
    protected void initValidationService()
    {
        final Element element = context( Element.class );
        final PropertyDef property = context( PropertyDef.class );
        
        final ListFactory<Validation> annotations = ListFactory.start();
        
        if( property == null )
        {
            final ElementType type = element.type();
            
            annotations.add( type.getAnnotations( Validation.class ) );
            
            for( final Validations v : type.getAnnotations( Validations.class ) )
            {
                annotations.add( v.value() );
            }
        }
        else
        {
            annotations.add( property.getAnnotations( Validation.class ) );
            
            for( final Validations v : property.getAnnotations( Validations.class ) )
            {
                annotations.add( v.value() );
            }
        }
        
        final Listener listener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                refresh();
            }
        };
        
        final ListFactory<Rule> rulesListFactory = ListFactory.start();
        
        for( final Validation annotation : annotations.result() )
        {
            if( annotation.severity() != Status.Severity.OK )
            {
                Rule rule = null;
                
                try
                {
                    rule = new Rule( element, annotation.rule(), annotation.message(), annotation.severity() );
                }
                catch( Exception e )
                {
                    Sapphire.service( LoggingService.class ).log( e );
                }
                
                if( rule != null )
                {
                    rule.attach( listener );
                    rulesListFactory.add( rule );
                }
            }
        }
        
        this.rules = rulesListFactory.result();
    }

    @Override
    protected Status compute()
    {
        final CompositeStatusFactory factory = Status.factoryForComposite();
        
        for( Rule rule : this.rules )
        {
            factory.merge( rule.validation() );
        }
        
        return factory.create();
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        for( Rule rule : this.rules )
        {
            rule.dispose();
        }
        
        this.rules = null;
    }
    
    private static final class Rule
    {
        private final Element element;
        private final FunctionResult ruleFunctionResult;
        private final Function messageFunction;
        private final Status.Severity severity;
        
        public Rule( final Element element,
                     final String rule,
                     final String message,
                     final Status.Severity severity )
        {
            this.element = element;
            this.severity = severity;

            Function messageFunction = ExpressionLanguageParser.parse( message );
            messageFunction = FailSafeFunction.create( messageFunction, String.class, false );
            
            this.messageFunction = messageFunction;
            
            Function ruleFunction = ExpressionLanguageParser.parse( rule );
            ruleFunction = FailSafeFunction.create( ruleFunction, Boolean.class, false );
            
            this.ruleFunctionResult = ruleFunction.evaluate( new ModelElementFunctionContext( element ) );
        }
        
        public Status validation()
        {
            if( ( (Boolean) this.ruleFunctionResult.value() ) == false )
            {
                final FunctionResult messageFunctionResult = this.messageFunction.evaluate( new ModelElementFunctionContext( this.element ) );
                
                try
                {
                    return Status.createStatus( this.severity, (String) messageFunctionResult.value() );
                }
                finally
                {
                    messageFunctionResult.dispose();
                }
            }
            else
            {
                return Status.createOkStatus();
            }
        }
        
        public void attach( final Listener listener )
        {
            this.ruleFunctionResult.attach( listener );
        }
        
        public void dispose()
        {
            this.ruleFunctionResult.dispose();
        }
    }

    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final PropertyDef property = context.find( PropertyDef.class );
            
            if( property == null )
            {
                final ElementType type = context.find( Element.class ).type();
                return ( type.hasAnnotation( Validation.class ) || type.hasAnnotation( Validations.class ) );
            }
            else
            {
                return ( property.hasAnnotation( Validation.class ) || property.hasAnnotation( Validations.class ) );
            }
        }
    }

}
