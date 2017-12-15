/******************************************************************************
 * Copyright (c) 2012 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amerson - [363551] JavaTypeConstraintService
 ******************************************************************************/

package org.eclipse.sapphire.java.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.java.JavaTypeConstraintBehavior;
import org.eclipse.sapphire.java.JavaTypeConstraintService;
import org.eclipse.sapphire.java.JavaTypeKind;
import org.eclipse.sapphire.java.JavaTypeReferenceService;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.FactsService;
import org.eclipse.sapphire.services.ReferenceService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;

/**
 * Creates fact statements about Java type property's constraints by using semantical information 
 * specified by @JavaTypeConstraints annotation.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public final class JavaTypeConstraintFactsService extends FactsService
{
    @Override
    protected void facts( final List<String> facts )
    {
        final IModelElement element = context( IModelElement.class );
        final ValueProperty property = context( ValueProperty.class );
        
        final JavaTypeConstraintService service = element.service( property, JavaTypeConstraintService.class );

        final List<JavaTypeKind> kinds = new ArrayList<JavaTypeKind>( service.kinds() );
        
        if( kinds.size() > 0 && kinds.size() < 5 )
        {
            if( kinds.size() == 1 )
            {
                facts.add( NLS.bind( Resources.statementKindOne, term( kinds.get( 0 ) ) ) );
            }
            else if( kinds.size() == 2 )
            {
                facts.add( NLS.bind( Resources.statementKindTwo, term( kinds.get( 0 ) ), term( kinds.get( 1 ) ) ) );
            }
            else if( kinds.size() == 3 )
            {
                facts.add( NLS.bind( Resources.statementKindThree, term( kinds.get( 0 ) ), term( kinds.get( 1 ) ), term( kinds.get( 2 ) ) ) );
            }
            else if( kinds.size() == 4 )
            {
                facts.add( NLS.bind( Resources.statementKindFour, term( kinds.get( 0 ) ), term( kinds.get( 1 ) ), term( kinds.get( 2 ) ), term( kinds.get( 3 ) ) ) );
            }
        }
        
        final List<String> types = new ArrayList<String>( service.types() );
        
        if( types.size() > 0 )
        {
            if( types.size() == 1 )
            {
                final String typeName = types.get( 0 );
                String verb = Resources.verbImplementOrExtend;
                
                final ReferenceService referenceService = element.service( property, ReferenceService.class );
                
                if( referenceService != null && referenceService instanceof JavaTypeReferenceService )
                {
                    JavaType type = null;
                
                    try
                    {
                        type = ( (JavaTypeReferenceService) referenceService ).resolve( typeName );
                    }
                    catch( Exception e )
                    {
                        LoggingService.log( e );
                    }
                    
                    if( type != null )
                    {
                        final JavaTypeKind k = type.kind();
                        
                        if( k == JavaTypeKind.CLASS || k == JavaTypeKind.ABSTRACT_CLASS )
                        {
                            verb = Resources.verbExtend;
                        }
                        else if( k == JavaTypeKind.INTERFACE )
                        {
                            boolean allowsClass = false;
                            boolean allowsInterface = false;
                            
                            for( JavaTypeKind kind : kinds )
                            {
                                if( kind == JavaTypeKind.CLASS || kind == JavaTypeKind.ABSTRACT_CLASS )
                                {
                                    allowsClass = true;
                                }
                                else if( kind == JavaTypeKind.INTERFACE )
                                {
                                    allowsInterface = true;
                                }
                            }
                            
                            if( allowsInterface && ! allowsClass )
                            {
                                verb = Resources.verbExtend;
                            }
                            else if( allowsClass && ! allowsInterface )
                            {
                                verb = Resources.verbImplement;
                            }
                        }
                    }
                }
                
                facts.add( NLS.bind( Resources.statementTypeOne, verb, typeName ) );
            }
            else
            {
                final StringBuilder buf = new StringBuilder();
                
                for( String type : types )
                {
                    if( buf.length() > 0 )
                    {
                        buf.append( ", " );
                    }
                    
                    buf.append( type );
                }
                
                final JavaTypeConstraintBehavior behavior = service.behavior();
                
                if( behavior == JavaTypeConstraintBehavior.AT_LEAST_ONE )
                {
                    facts.add( NLS.bind( Resources.statementTypeOneOf, buf.toString() ) );
                }
                else if( behavior == JavaTypeConstraintBehavior.ALL )
                {
                    facts.add( NLS.bind( Resources.statementTypeAll, buf.toString() ) );
                }
                else
                {
                    throw new IllegalStateException();
                }
            }
        }
    }
    
    private static String term( final JavaTypeKind kind )
    {
        switch( kind )
        {
            case CLASS:           return Resources.termConcreteClass;
            case ABSTRACT_CLASS:  return Resources.termAbstractClass;
            case INTERFACE:       return Resources.termInterface;
            case ANNOTATION:      return Resources.termAnnotation;
            case ENUM:            return Resources.termEnumeration;
            default:              throw new IllegalStateException();
        }
    }
    
    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            final IModelElement element = context.find( IModelElement.class );

            return ( property != null && element != null && element.service( property, JavaTypeConstraintService.class ) != null );
        }
    
        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new JavaTypeConstraintFactsService();
        }
    }
    
    private static final class Resources extends NLS
    {
        public static String termConcreteClass;
        public static String termAbstractClass;
        public static String termInterface;
        public static String termAnnotation;
        public static String termEnumeration;
        public static String statementKindOne;
        public static String statementKindTwo;
        public static String statementKindThree;
        public static String statementKindFour;
        public static String statementTypeOne;
        public static String statementTypeOneOf;
        public static String statementTypeAll;
        public static String verbImplement;
        public static String verbExtend;
        public static String verbImplementOrExtend;        
        
        static
        {
            initializeMessages( JavaTypeConstraintFactsService.class.getName(), Resources.class );
        }
    }
    
}
