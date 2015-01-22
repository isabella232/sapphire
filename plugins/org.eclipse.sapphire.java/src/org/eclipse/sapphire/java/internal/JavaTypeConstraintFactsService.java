/******************************************************************************
 * Copyright (c) 2015 Oracle and Liferay
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
import java.util.SortedSet;

import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.java.JavaTypeConstraintBehavior;
import org.eclipse.sapphire.java.JavaTypeConstraintService;
import org.eclipse.sapphire.java.JavaTypeKind;
import org.eclipse.sapphire.java.JavaTypeReferenceService;
import org.eclipse.sapphire.services.FactsService;
import org.eclipse.sapphire.services.ReferenceService;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * Creates fact statements about Java type property's constraints by using semantical information 
 * specified by @JavaTypeConstraints annotation.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public final class JavaTypeConstraintFactsService extends FactsService
{
    @Text( "a concrete class" )
    private static LocalizableText termConcreteClass;
    
    @Text( "an abstract class" )
    private static LocalizableText termAbstractClass;
    
    @Text( "an interface" )
    private static LocalizableText termInterface;
    
    @Text( "an annotation" )
    private static LocalizableText termAnnotation;
    
    @Text( "an enumeration" )
    private static LocalizableText termEnumeration;
    
    @Text( "Must be {0}" )
    private static LocalizableText statementKindOne;
    
    @Text( "Must be {0} or {1}" )
    private static LocalizableText statementKindTwo;
    
    @Text( "Must be {0}, {1} or {2}" )
    private static LocalizableText statementKindThree;
    
    @Text( "Must be {0}, {1}, {2} or {3}" )
    private static LocalizableText statementKindFour;
    
    @Text( "Must {0} {1}" )
    private static LocalizableText statementTypeOne;
    
    @Text( "Must implement or extend one of: {0}" )
    private static LocalizableText statementTypeOneOf;
    
    @Text( "Must implement or extend all: {0}" )
    private static LocalizableText statementTypeAll;
    
    @Text( "implement" )
    private static LocalizableText verbImplement;
    
    @Text( "extend" )
    private static LocalizableText verbExtend;
    
    @Text( "implement or extend" )
    private static LocalizableText verbImplementOrExtend;        
    
    static
    {
        LocalizableText.init( JavaTypeConstraintFactsService.class );
    }

    @Override
    protected void facts( final SortedSet<String> facts )
    {
        final Property property = context( Property.class );
        
        final JavaTypeConstraintService service = property.service( JavaTypeConstraintService.class );

        final List<JavaTypeKind> kinds = new ArrayList<JavaTypeKind>( service.kinds() );
        
        if( kinds.size() > 0 && kinds.size() < 5 )
        {
            if( kinds.size() == 1 )
            {
                facts.add( statementKindOne.format( term( kinds.get( 0 ) ) ) );
            }
            else if( kinds.size() == 2 )
            {
                facts.add( statementKindTwo.format( term( kinds.get( 0 ) ), term( kinds.get( 1 ) ) ) );
            }
            else if( kinds.size() == 3 )
            {
                facts.add( statementKindThree.format( term( kinds.get( 0 ) ), term( kinds.get( 1 ) ), term( kinds.get( 2 ) ) ) );
            }
            else if( kinds.size() == 4 )
            {
                facts.add( statementKindFour.format( term( kinds.get( 0 ) ), term( kinds.get( 1 ) ), term( kinds.get( 2 ) ), term( kinds.get( 3 ) ) ) );
            }
        }
        
        final List<String> types = new ArrayList<String>( service.types() );
        
        if( types.size() > 0 )
        {
            if( types.size() == 1 )
            {
                final String typeName = types.get( 0 );
                String verb = verbImplementOrExtend.text();
                
                final ReferenceService<?> referenceService = property.service( ReferenceService.class );
                
                if( referenceService != null && referenceService instanceof JavaTypeReferenceService )
                {
                    JavaType type = null;
                
                    try
                    {
                        type = ( (JavaTypeReferenceService) referenceService ).resolve( typeName );
                    }
                    catch( Exception e )
                    {
                        Sapphire.service( LoggingService.class ).log( e );
                    }
                    
                    if( type != null )
                    {
                        final JavaTypeKind k = type.kind();
                        
                        if( k == JavaTypeKind.CLASS || k == JavaTypeKind.ABSTRACT_CLASS )
                        {
                            verb = verbExtend.text();
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
                                verb = verbExtend.text();
                            }
                            else if( allowsClass && ! allowsInterface )
                            {
                                verb = verbImplement.text();
                            }
                        }
                    }
                }
                
                facts.add( statementTypeOne.format( verb, typeName ) );
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
                    facts.add( statementTypeOneOf.format( buf.toString() ) );
                }
                else if( behavior == JavaTypeConstraintBehavior.ALL )
                {
                    facts.add( statementTypeAll.format( buf.toString() ) );
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
            case CLASS:           return termConcreteClass.text();
            case ABSTRACT_CLASS:  return termAbstractClass.text();
            case INTERFACE:       return termInterface.text();
            case ANNOTATION:      return termAnnotation.text();
            case ENUM:            return termEnumeration.text();
            default:              throw new IllegalStateException();
        }
    }
    
    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final Property property = context.find( Property.class );
            return ( property != null && property.service( JavaTypeConstraintService.class ) != null );
        }
    }
    
}
