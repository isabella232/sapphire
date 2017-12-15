/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [bugzilla 329114] RE-WRITE CONTEXT HELP BINDING FEATURE
 ******************************************************************************/

package org.eclipse.sapphire.sdk.build.processor.internal;

import static org.eclipse.sapphire.modeling.util.MiscUtil.createStringDigest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.eclipse.sapphire.modeling.annotations.Documentation;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.NamedValues;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Filer.Location;
import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ExternalizeStringResourcesProcessor 
{
    public void process( final AnnotationProcessorEnvironment env ) 
    {
        for( TypeDeclaration type : getAnnotatedTypes( env ) )
        {
            final Set<String> strings = new HashSet<String>();

            gather( strings, type.getAnnotation( Label.class ) );
            gather( strings, type.getAnnotation( Documentation.class ) );

            for( FieldDeclaration field : type.getFields() )
            {
                final String fieldName = field.getSimpleName();
                
                if( fieldName != null )
                {
                    gather( strings, field.getAnnotation( Label.class ) );
                    
                    if( fieldName.startsWith( "PROP_" ) )
                    {
                        gather( strings, field.getAnnotation( Documentation.class ) );
                        gather( strings, field.getAnnotation( NamedValues.class ) );
                    }
                }
            }
            
            if( ! strings.isEmpty() )
            {
                final Properties resources = new Properties();
                
                for( String string : strings )
                {
                    resources.put( createStringDigest( string ), string );
                }
                
                final String pkg = type.getPackage().getQualifiedName();
                final String relpath = generateFileName( type );
                
                PrintWriter pw = null;
                
                try
                {
                    pw = env.getFiler().createTextFile( Location.CLASS_TREE, pkg, new File( relpath ), null );
                    
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    resources.store( baos, null );
                    String s = baos.toString();
                    pw.print( s );
                }
                catch( IOException e )
                {
                    e.printStackTrace();
                }
                finally
                {
                    if( pw != null )
                    {
                        try
                        {
                            pw.close();
                        }
                        catch( Exception e ) {}
                    }
                }
            }
        }
    }
    
    private static String generateFileName( final TypeDeclaration type )
    {
        final String qname = type.getQualifiedName();
        final String pkg = type.getPackage().getQualifiedName();
        
        if( pkg.length() == 0 )
        {
            return qname; 
        }
        else
        {
            return qname.substring( pkg.length() + 1 ).replace( '.', '$' ) + ".properties";
        }
    }

    private static Set<TypeDeclaration> getAnnotatedTypes( final AnnotationProcessorEnvironment env )
    {
        final Set<TypeDeclaration> annotatedTypes = new HashSet<TypeDeclaration>(); 
        
        final AnnotationTypeDeclaration annotationDeclaration 
            = (AnnotationTypeDeclaration) env.getTypeDeclaration( Label.class.getName() );
        
        if( annotationDeclaration == null ) 
        {
            return annotatedTypes;                         
        }
        
        final Collection<Declaration> annotatedDeclarations 
            = env.getDeclarationsAnnotatedWith( annotationDeclaration );
        
        if( annotatedDeclarations == null ) 
        {
            return annotatedTypes;
        }
        
        for( Declaration decl : annotatedDeclarations ) 
        {
            for( AnnotationMirror annotation : decl.getAnnotationMirrors() ) 
            {
                if( annotation.getAnnotationType().getDeclaration().getQualifiedName().equals( Label.class.getName() ) ) 
                {
                    if( decl instanceof TypeDeclaration )
                    {
                        annotatedTypes.add( (TypeDeclaration ) decl );
                    }
                    else if( decl instanceof FieldDeclaration )
                    {
                        annotatedTypes.add( ( (FieldDeclaration) decl ).getDeclaringType() );
                    }
                    else
                    {
                        throw new IllegalStateException( decl.getClass().getName() );
                    }
                }
            }
        }
        
        return annotatedTypes;
    }
    
    private static void gather( final Set<String> strings,
                                final Label labelAnnotation )
    {
        if( labelAnnotation != null )
        {
            final String standardLabel = labelAnnotation.standard();
            
            if( standardLabel.length() > 0 )
            {
                strings.add( standardLabel );
            }
            
            final String fullLabel = labelAnnotation.full();
            
            if( fullLabel.length() > 0 )
            {
                strings.add( fullLabel );
            }
        }
    }
    
    private static void gather( final Set<String> strings,
                                final Documentation documentationAnnotation ) 
    {
        if( documentationAnnotation != null )
        {
            final String content = documentationAnnotation.content();
            
            if( content.length() > 0 )
            {
                strings.add( content );
            }

            for( Documentation.Topic topic : documentationAnnotation.topics() )
            {
                strings.add( topic.label() );
            }
        }
    }
    
    private static void gather( final Set<String> strings,
                                final NamedValues namedValuesAnnotation )
    {
        if( namedValuesAnnotation != null )
        {
            strings.add( namedValuesAnnotation.arbitraryValueLabel() );
            
            for( NamedValues.NamedValue val : namedValuesAnnotation.namedValues() )
            {
                strings.add( val.label() );
            }
        }
    }
    
}
