/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.sdk.build.processor.internal.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ClassModel extends BaseModel
{
    private TypeReference name;
    private Set<TypeReference> imports;
    private TypeReference baseClass;
    private Set<TypeReference> interfaces;
    private boolean isAbstract;
    private List<FieldModel> fields;
    private List<StaticInitializerModel> staticInitializers;
    private List<MethodModel> methods;
    private boolean invalid;
    
    public ClassModel()
    {
        this.imports = new HashSet<TypeReference>();
        this.interfaces = new HashSet<TypeReference>();
        this.isAbstract = false;
        this.fields = new ArrayList<FieldModel>();
        this.staticInitializers = new ArrayList<StaticInitializerModel>();
        this.methods = new ArrayList<MethodModel>();
    }
    
    public TypeReference getName()
    {
        return this.name;
    }
    
    public void setName( final TypeReference name )
    {
        if( name instanceof ArrayTypeReference || name instanceof ParameterizedTypeReference )
        {
            throw new IllegalArgumentException();
        }
        
        this.name = name;
    }
    
    public Set<TypeReference> getImports()
    {
        final Set<TypeReference> result = new TreeSet<TypeReference>();
        result.addAll( this.imports );
        
        final Set<String> pkgToIgnore = new HashSet<String>();
        pkgToIgnore.add( "java.lang" );
        pkgToIgnore.add( this.name.getPackage() );
        
        if( this.baseClass != null )
        {
            this.baseClass.contributeNecessaryImports( result );
        }
        
        for( TypeReference intr : this.interfaces )
        {
            intr.contributeNecessaryImports( result );
        }
        
        for( FieldModel field : this.fields )
        {
            field.getType().contributeNecessaryImports( result );
        }
        
        for( MethodModel method : this.methods )
        {
            method.getReturnType().contributeNecessaryImports( result );
            
            for( MethodParameterModel param : method.getParameters() )
            {
                param.getType().contributeNecessaryImports( result );
            }
        }
        
        for( Iterator<TypeReference> itr = result.iterator(); itr.hasNext(); )
        {
            final TypeReference entry = itr.next();
            boolean keep = true;
            
            if( entry == null )
            {
                keep = false;
            }
            else
            {
                final String pkg = entry.getPackage();

                if( entry == TypeReference.VOID_TYPE || TypeReference.PRIMITIVE_TYPES.values().contains( entry ) ||
                    entry == TypeReference.WILDCARD_TYPE_PARAM )
                {
                    keep = false;
                }
                else if( pkgToIgnore.contains( pkg ) )
                {
                    keep = false;
                }
            }
            
            if( ! keep )
            {
                itr.remove();
            }
        }
        
        return result;
    }
    
    public void addImport( final TypeReference type )
    {
        type.contributeNecessaryImports( this.imports );
    }

    public void addImport( final Class<?> cl )
    {
        addImport( new TypeReference( cl ) );
    }
    
    public TypeReference getBaseClass()
    {
        return this.baseClass;
    }
    
    public void setBaseClass( final TypeReference baseClass )
    {
        this.baseClass = baseClass;
    }
    
    public Set<TypeReference> getInterfaces()
    {
        return this.interfaces;
    }
    
    public void addInterface( final TypeReference intrfc )
    {
        this.interfaces.add( intrfc );
    }
    
    public boolean isAbstract()
    {
        return this.isAbstract;
    }
    
    public void setAbstract( final boolean isAbstract )
    {
        this.isAbstract = isAbstract;
    }
    
    public boolean containsField( final String name )
    {
        for( FieldModel field : this.fields )
        {
            if( field.getName() != null && field.getName().equals( name ) )
            {
                return true;
            }
        }
        
        return false;
    }
    
    public void addField( final FieldModel field )
    {
        this.fields.add( field );
        field.setParent( this );
    }
    
    public FieldModel addField()
    {
        final FieldModel field = new FieldModel();
        addField( field );
        return field;
    }
    
    public FieldModel addConstant()
    {
        final FieldModel field = addField();
        field.setStatic( true );
        field.setFinal( true );
        return field;
    }
    
    public void removeField( final FieldModel field )
    {
        this.fields.remove( field );
    }
    
    public StaticInitializerModel getStaticInitializer( final String id )
    {
        return getStaticInitializer( id, false );
    }
    
    public StaticInitializerModel getStaticInitializer( final String id,
                                                        final boolean createIfNecessary )
    {
        for( StaticInitializerModel x : this.staticInitializers )
        {
            if( id.equals( x.getId() ) )
            {
                return x;
            }
        }
        
        if( createIfNecessary )
        {
            return addStaticInitializer( id );
        }
        
        return null;
    }
    
    public void addStaticInitializer( final StaticInitializerModel staticInitializer )
    {
        this.staticInitializers.add( staticInitializer );
        staticInitializer.setParent( this );
    }
    
    public StaticInitializerModel addStaticInitializer()
    {
        final StaticInitializerModel staticInitializer = new StaticInitializerModel();
        addStaticInitializer( staticInitializer );
        return staticInitializer;
    }
    
    public StaticInitializerModel addStaticInitializer( final String id )
    {
        final StaticInitializerModel staticInitializer = addStaticInitializer();
        staticInitializer.setId( id );
        return staticInitializer;
    }
    
    public List<MethodModel> getMethods()
    {
        return this.methods;
    }
    
    public boolean hasMethod( final String name,
                              final List<TypeReference> params )
    {
        final int paramCount = params.size();
        
        for( MethodModel m : this.methods )
        {
            final List<MethodParameterModel> p = m.getParameters();
            final String mName = m.getName();
                    
            if( mName != null && mName.equals( name ) && p.size() == paramCount )
            {
                boolean match = true;
                
                for( int i = 0; i < paramCount; i++ )
                {
                    if( ! p.get( i ).getType().equals( params.get( i ) ) )
                    {
                        match = false;
                        break;
                    }
                }
                
                if( match )
                {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public void addMethod( final MethodModel method )
    {
        this.methods.add( method );
        method.setParent( this );
    }
    
    public MethodModel addMethod()
    {
        final MethodModel method = new MethodModel();
        addMethod( method );
        return method;
    }
    
    public MethodModel addMethod( final String name )
    {
        final MethodModel m = addMethod();
        m.setName( name );
        return m;
    }
    
    public MethodModel addConstructor()
    {
        final MethodModel m = addMethod();
        m.setConstructor( true );
        return m;
    }
    
    public void removeMethod( final MethodModel method )
    {
        this.methods.remove( method );
        method.setParent( null );
    }
    
    public boolean isInvalid()
    {
        return this.invalid;
    }
    
    public void markInvalid()
    {
        this.invalid = true;
    }
    
    @Override
    public void write( final IndentingPrintWriter pw )
    {
        pw.print( "package " );
        pw.print( this.name.getPackage() );
        pw.print( ';' );
        pw.println();
        pw.println();
        
        final Set<TypeReference> imports = getImports();
        
        for( TypeReference entry : imports )
        {
            pw.print( "import " );
            pw.print( entry.getQualifiedName() );
            pw.print( ';' );
            pw.println();
        }
        
        if( ! imports.isEmpty() )
        {
            pw.println();
        }

        pw.print( "@SuppressWarnings( \"all\" )" );
        pw.println();
        pw.println();
        pw.printf( "public %s class ", ( this.isAbstract ? "abstract" : "final" ) );
        pw.print( this.name.getSimpleName() );
        pw.println();
        
        if( this.baseClass != null || ! this.interfaces.isEmpty() )
        {
            pw.increaseIndent();
            pw.println();
            
            if( this.baseClass != null )
            {
                pw.print( "extends " );
                pw.print( this.baseClass.getSimpleName() );
                pw.println();
            }
            
            if( ! this.interfaces.isEmpty() )
            {
                pw.print( "implements " );
                
                boolean first = true;
                
                for( TypeReference intrfc : this.interfaces )
                {
                    if( first )
                    {
                        first = false;
                    }
                    else
                    {
                        pw.print( ", " );
                    }
                    
                    pw.print( intrfc.getSimpleName() );
                }
                
                pw.println();
            }
            
            pw.println();
            pw.decreaseIndent();
        }
        
        pw.print( '{' );
        pw.println();
        pw.increaseIndent();
        
        final Comparator<FieldModel> fieldByNameComparator = new Comparator<FieldModel>()
        {
            public int compare( final FieldModel f1,
                                final FieldModel f2 )
            {
                final String n1 = f1.getName();
                final String n2 = f2.getName();
                
                if( n1 == n2 )
                {
                    return 0;
                }
                else if( n1 == null )
                {
                    return -1;
                }
                else if( n2 == null )
                {
                    return 1;
                }
                else
                {
                    return n1.compareTo( n2 );
                }
            }
        };
        
        final Set<FieldModel> constants = new TreeSet<FieldModel>( fieldByNameComparator );
        final Set<FieldModel> instanceFields = new TreeSet<FieldModel>( fieldByNameComparator );
        
        for( FieldModel field : this.fields )
        {
            if( field.isStatic() && field.isFinal() )
            {
                constants.add( field );
            }
            else
            {
                instanceFields.add( field );
            }
        }
        
        if( ! constants.isEmpty() )
        {
            for( FieldModel field : constants )
            {
                field.write( pw );
            }
            
            pw.println();
        }
        
        for( StaticInitializerModel staticInitializer : this.staticInitializers )
        {
            staticInitializer.write( pw );
        }

        if( ! instanceFields.isEmpty() )
        {
            for( FieldModel field : instanceFields )
            {
                field.write( pw );
            }
            
            pw.println();
        }
        
        for( MethodModel method : this.methods )
        {
            method.write( pw );
        }
        
        pw.decreaseIndent();
        pw.print( '}' );
        pw.println();
    }
    
}
