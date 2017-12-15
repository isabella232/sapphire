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

package org.eclipse.sapphire.java.jdt.ui.internal;

import static java.lang.Math.min;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.actions.AddUnimplementedConstructorsAction;
import org.eclipse.jdt.ui.actions.FormatAllAction;
import org.eclipse.jdt.ui.actions.OrganizeImportsAction;
import org.eclipse.jdt.ui.actions.OverrideMethodsAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.java.JavaTypeConstraint;
import org.eclipse.sapphire.java.JavaTypeConstraintBehavior;
import org.eclipse.sapphire.java.JavaTypeKind;
import org.eclipse.sapphire.java.JavaTypeName;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.ModelPropertyListener;
import org.eclipse.sapphire.modeling.ReferenceValue;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphirePropertyEditor;
import org.eclipse.sapphire.ui.SapphirePropertyEditorActionHandler;
import org.eclipse.sapphire.ui.SapphirePropertyEditorCondition;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.def.ISapphireActionHandlerDef;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class JavaTypeCreateActionHandler extends SapphirePropertyEditorActionHandler
{
    private final JavaTypeKind kind;
    private JavaTypeName base;
    private final List<JavaTypeName> interfaces = new ArrayList<JavaTypeName>();
    private ModelPropertyListener listener;
    
    public JavaTypeCreateActionHandler( final JavaTypeKind kind )
    {
        this.kind = kind;
    }
    
    @Override
    public void init( final SapphireAction action,
                      final ISapphireActionHandlerDef def )
    {
        super.init( action, def );

        final IModelElement element = getModelElement();
        final ValueProperty property = (ValueProperty) getProperty();
        
        final IProject proj = element.adapt( IProject.class );
        final IJavaProject jproj = JavaCore.create( proj );
        
        final JavaTypeConstraint javaTypeConstraintAnnotation = property.getAnnotation( JavaTypeConstraint.class );
        
        if( javaTypeConstraintAnnotation != null )
        {
            final String[] types = javaTypeConstraintAnnotation.type();
            
            for( int i = 0, n = ( javaTypeConstraintAnnotation.behavior() == JavaTypeConstraintBehavior.ALL ? types.length : min( 1, types.length ) ); i < n; i++ )
            {
                final String typeName = types[ i ];
                
                try
                {
                    final IType type = jproj.findType( typeName.replace( '$', '.' ) );
                    
                    if( type != null && type.exists() )
                    {
                        if( type.isClass() )
                        {
                            this.base = new JavaTypeName( typeName );
                        }
                        else if( type.isInterface() )
                        {
                            this.interfaces.add( new JavaTypeName( typeName ) );
                        }
                    }
                }
                catch( Exception e )
                {
                    LoggingService.log( e );
                }
            }
        }
        
        this.listener = new ModelPropertyListener()
        {
            @Override
            public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event )
            {
                refreshEnablementState();
            }
        };
        
        element.addListener( this.listener, property.getName() );
    }
    
    @Override
    protected Object run( final SapphireRenderingContext context )
    {
        final IModelElement element = getModelElement();
        final ModelProperty property = getProperty();
        
        final Value<JavaTypeName> javaTypeNameValue = element.read( (ValueProperty) property );
        
        if( javaTypeNameValue.isMalformed() )
        {
            return null;
        }
        
        final JavaTypeName javaTypeName = javaTypeNameValue.getContent();
        
        if( javaTypeName.pkg() == null )
        {
            if( ! MessageDialog.openConfirm( context.getShell(), Resources.discourageDialogTitle, Resources.discourageDefaultPackage ) )
            {
                return null;
            }
        }
        
        if( ! Character.isUpperCase( javaTypeName.simple().charAt( 0 ) ) )
        {
            if( ! MessageDialog.openConfirm( context.getShell(), Resources.discourageDialogTitle, Resources.discourageLowerCase ) )
            {
                return null;
            }
        }
        
        final IRunnableWithProgress op = new IRunnableWithProgress()
        {
            public void run( final IProgressMonitor monitor ) throws InvocationTargetException, InterruptedException
            {
                monitor.beginTask( "", 7 );
                
                final JavaTypeKind kind = JavaTypeCreateActionHandler.this.kind;
                
                final StringBuilder buf = new StringBuilder();
                
                if( javaTypeName.pkg() != null )
                {
                    buf.append( "package " );
                    buf.append( javaTypeName.pkg() );
                    buf.append( ";\n\n" );
                }
                
                final List<JavaTypeName> imports = new ArrayList<JavaTypeName>();
                imports.add( javaTypeName );
                
                String base = null;
                
                if( kind == JavaTypeKind.CLASS && JavaTypeCreateActionHandler.this.base != null )
                {
                    base = deriveSafeLocalName( JavaTypeCreateActionHandler.this.base, imports );
                }
                
                final List<String> interfaces = new ArrayList<String>();
                
                if( kind == JavaTypeKind.CLASS || kind == JavaTypeKind.INTERFACE )
                {
                    for( JavaTypeName t : JavaTypeCreateActionHandler.this.interfaces )
                    {
                        interfaces.add( deriveSafeLocalName( t, imports ) );
                    }
                }
                
                imports.remove( javaTypeName );
                
                if( ! imports.isEmpty() )
                {
                    for( JavaTypeName t : imports )
                    {
                        buf.append( "import " );
                        buf.append( t.qualified().replace( '$', '.' ) );
                        buf.append( ";\n" );
                    }
                    
                    buf.append( '\n' );
                }
                
                buf.append( "public " );
                
                switch( kind )
                {
                    case ANNOTATION:  buf.append( "@interface" ); break;
                    case ENUM:        buf.append( "enum" ); break;
                    case INTERFACE:   buf.append( "interface" ); break;
                    default:          buf.append( "class" );
                }
                
                buf.append( ' ' );
                buf.append( javaTypeName.simple() );
                
                if( base != null )
                {
                    buf.append( " extends " );
                    buf.append( base );
                }
                
                if( ! interfaces.isEmpty() )
                {
                    buf.append( kind == JavaTypeKind.INTERFACE ? " extends " : " implements " );
                    
                    boolean first = true;
                    
                    for( String implementsType : interfaces )
                    {
                        if( first )
                        {
                            first = false;
                        }
                        else
                        {
                            buf.append( ", " );
                        }
                        
                        buf.append( implementsType );
                    }
                }
                
                buf.append( "\n{\n}\n" );
                
                monitor.worked( 1 );
                
                final IProject proj = element.adapt( IProject.class );
                final IJavaProject jproj = JavaCore.create( proj );
                
                try
                {
                    final IPackageFragmentRoot src = src( jproj );
                    final IPackageFragment pkg = src.createPackageFragment( ( javaTypeName.pkg() == null ? "" : javaTypeName.pkg() ), true, null );
                    final ICompilationUnit cu = pkg.createCompilationUnit( javaTypeName.simple() + ".java", buf.toString(), true, null );
                    
                    cu.save( null, true );
                    
                    monitor.worked( 1 );
                    
                    final IEditorPart editor = JavaUI.openInEditor( cu );
                    final IWorkbenchPartSite site = editor.getSite();
                    
                    monitor.worked( 1 );
                    
                    if( kind == JavaTypeKind.CLASS )
                    {
                        final IType type = cu.getType( javaTypeName.simple() );

                        final ASTParser parser = ASTParser.newParser( AST.JLS3 );
                        parser.setResolveBindings( true );
                        parser.setSource( cu );
                        
                        final CompilationUnit ast = (CompilationUnit) parser.createAST( null );
                        
                        ASTNode node = NodeFinder.perform( ast, type.getNameRange() );
                        
                        while( ! ( node instanceof AbstractTypeDeclaration ) )
                        {
                            node = node.getParent();
                        }
                        
                        final ITypeBinding typeBinding = ( (AbstractTypeDeclaration) node ).resolveBinding();
                        
                        final IWorkspaceRunnable addUnimplementedConstructorsOp 
                            = AddUnimplementedConstructorsAction.createRunnable( ast, typeBinding, null, -1, false, Flags.AccPublic, false );
                        
                        addUnimplementedConstructorsOp.run( null );
                        
                        final IWorkspaceRunnable overrideMethodsOp 
                            = OverrideMethodsAction.createRunnable( ast, typeBinding, null, -1, false );
                        
                        overrideMethodsOp.run( null );
                    }
                    
                    monitor.worked( 1 );
                    
                    final OrganizeImportsAction organizeImportsAction = new OrganizeImportsAction( site );
                    organizeImportsAction.run( cu );
                    
                    monitor.worked( 1 );
                    
                    final FormatAllAction formatAllAction = new FormatAllAction( site );
                    formatAllAction.runOnMultiple( new ICompilationUnit[] { cu } );
                    
                    monitor.worked( 1 );
                    
                    editor.doSave( null );
                    
                    monitor.worked( 1 );                    
                }
                catch( CoreException e )
                {
                    LoggingService.log( e );
                }
                
                monitor.done();
            }
        };
        
        try
        {
            ( new ProgressMonitorDialog( context.getShell() ) ).run( false, false, op );
        }
        catch( InvocationTargetException e )
        {
            LoggingService.log( e );
        }
        catch( InterruptedException e )
        {
            // Should not happen.

            LoggingService.log( e );
        }
        
        return null;
    }
    
    @Override
    protected boolean computeEnablementState()
    {
        boolean enabled = super.computeEnablementState();
        
        if( enabled )
        {
            final ReferenceValue<?,?> ref = (ReferenceValue<?,?>) getModelElement().read( getProperty() );
            
            enabled = false;
            
            if( ! ref.isMalformed() )
            {
                final String typeName = ref.getText();
                
                if( typeName != null && typeName.indexOf( '$' ) == -1 && ref.resolve() == null )
                {
                    enabled = true;
                }
            }
        }
        
        return enabled;
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        getModelElement().removeListener( this.listener, getProperty().getName() );
    }

    private final IPackageFragmentRoot src( final IJavaProject project ) throws JavaModelException
    {
        for( IPackageFragmentRoot root : project.getPackageFragmentRoots() )
        {
            if( root.getKind() == IPackageFragmentRoot.K_SOURCE )
            {
                return root;
            }
        }
        
        return null;
    }
    
    private static String deriveSafeLocalName( final JavaTypeName type,
                                               final Collection<JavaTypeName> imports )
    {
        boolean collision = false;
        
        for( JavaTypeName n : imports )
        {
            if( n.simple().equals( type.simple() ) )
            {
                collision = true;
                break;
            }
        }
        
        if( collision )
        {
            return type.qualified().replace( '$', '.' );
        }
        else
        {
            imports.add( type );
            return type.simple();
        }
    }
    
    protected static abstract class Condition extends SapphirePropertyEditorCondition
    {
        @Override
        protected final boolean evaluate( final SapphirePropertyEditor part )
        {
            final ModelProperty property = part.getProperty();
            
            if( property instanceof ValueProperty && property.isOfType( JavaTypeName.class ) )
            {
                final Reference referenceAnnotation = property.getAnnotation( Reference.class );
                
                if( referenceAnnotation != null && referenceAnnotation.target() == JavaType.class )
                {
                    return evaluate( property.getAnnotation( JavaTypeConstraint.class ) );
                }
            }
            
            return false;
        }
        
        protected abstract boolean evaluate( JavaTypeConstraint javaTypeConstraint );
    }

    private static final class Resources extends NLS 
    {
        public static String discourageDialogTitle;
        public static String discourageDefaultPackage;
        public static String discourageLowerCase;

        static 
        {
            initializeMessages( JavaTypeCreateActionHandler.class.getName(), Resources.class );
        }
    }
    
}