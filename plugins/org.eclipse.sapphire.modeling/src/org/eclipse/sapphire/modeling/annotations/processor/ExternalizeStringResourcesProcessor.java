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

package org.eclipse.sapphire.modeling.annotations.processor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

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
            final Properties resources = new Properties();

            addResources( resources, "$type$", type.getAnnotation( Label.class ) ); //$NON-NLS-1$
            
            for( FieldDeclaration field : type.getFields() )
            {
                final String fieldName = field.getSimpleName();
                
                if( fieldName != null )
                {
                    if( fieldName.startsWith( "PROP_" ) ) //$NON-NLS-1$
                    {
                        final String propName = getPropertyName( field );
                        final Label labelAnnotation = field.getAnnotation( Label.class );
                        
                        if( labelAnnotation != null )
                        {
                            addResources( resources, propName, labelAnnotation );
                        }
                        
                        final NamedValues namedValuesAnnotation = field.getAnnotation( NamedValues.class );
                        
                        if( namedValuesAnnotation != null )
                        {
                            resources.put( propName + ".arbitraryValue", //$NON-NLS-1$
                                           namedValuesAnnotation.arbitraryValueLabel() );
                            
                            for( NamedValues.NamedValue val : namedValuesAnnotation.namedValues() )
                            {
                                resources.put( propName + ".namedValue." + val.value(), val.label() ); //$NON-NLS-1$
                            }
                        }
                    }
                    else
                    {
                        final Label labelAnnotation = field.getAnnotation( Label.class );
                        
                        if( labelAnnotation != null )
                        {
                            addResources( resources, fieldName, labelAnnotation );
                        }
                    }
                }
            }
            
            if( ! resources.isEmpty() )
            {
                final String pkg = type.getPackage().getQualifiedName();
                final String relpath = type.getSimpleName() + ".properties"; //$NON-NLS-1$
                
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
	
    private static void addResources( final Properties resources,
                                      final String entityName,
                                      final Label labelAnnotation )
    {
        if( labelAnnotation != null )
        {
            final String standardLabel = labelAnnotation.standard();
            
            if( standardLabel.length() > 0 )
            {
                resources.put( entityName + ".standard", standardLabel ); //$NON-NLS-1$
            }
            
            final String fullLabel = labelAnnotation.full();
            
            if( fullLabel.length() > 0 )
            {
                resources.put( entityName + ".full", fullLabel ); //$NON-NLS-1$
            }
        }
    }
    
    private static final String getPropertyName( final FieldDeclaration propField )
    {
        final String propFieldName = propField.getSimpleName();
        
        final StringBuilder buf = new StringBuilder();
        boolean seenFirstSegment = false;
        
        for( String segment : propFieldName.split( "_" ) ) //$NON-NLS-1$
        {
            if( seenFirstSegment )
            {
                buf.append( segment.charAt( 0 ) );
                buf.append( segment.substring( 1 ).toLowerCase() );
            }
            else
            {
                // Skip the first segment that's always "PROP".
                
                seenFirstSegment = true;
            }
        }
        
        return buf.toString();
    }
	
}
