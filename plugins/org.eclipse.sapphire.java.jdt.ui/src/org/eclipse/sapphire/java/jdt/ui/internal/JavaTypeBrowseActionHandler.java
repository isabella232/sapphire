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

import java.util.EnumSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.java.JavaTypeConstraint;
import org.eclipse.sapphire.java.JavaTypeKind;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireBrowseActionHandler;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.def.ISapphireActionHandlerDef;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class JavaTypeBrowseActionHandler 

    extends SapphireBrowseActionHandler
    
{
    public static final String ID = "Sapphire.Browse.Java.Type";
    public static final String PARAM_KINDS = "kinds";
    
    private int browseDialogStyle;
    
    @Override
    public void init( final SapphireAction action,
                      final ISapphireActionHandlerDef def )
    {
        super.init( action, def );

        setId( ID );
        
        final String paramKinds = def.getParam( PARAM_KINDS );
        final EnumSet<JavaTypeKind> kinds = EnumSet.noneOf( JavaTypeKind.class );
        
        if( paramKinds != null )
        {
            for( String kindString : paramKinds.split( "," ) )
            {
                kindString = kindString.trim();
                
                if( kindString.equalsIgnoreCase( JavaTypeKind.CLASS.name() ) )
                {
                    kinds.add( JavaTypeKind.CLASS );
                }
                else if( kindString.equalsIgnoreCase( JavaTypeKind.ABSTRACT_CLASS.name() ) )
                {
                    kinds.add( JavaTypeKind.ABSTRACT_CLASS );
                }
                else if( kindString.equalsIgnoreCase( JavaTypeKind.INTERFACE.name() ) )
                {
                    kinds.add( JavaTypeKind.INTERFACE );
                }
                else if( kindString.equalsIgnoreCase( JavaTypeKind.ANNOTATION.name() ) )
                {
                    kinds.add( JavaTypeKind.ANNOTATION );
                }
                else if( kindString.equalsIgnoreCase( JavaTypeKind.ENUM.name() ) )
                {
                    kinds.add( JavaTypeKind.ENUM );
                }
                else
                {
                    final String msg = NLS.bind( Resources.typeKindNotRecognized, kindString );
                    SapphireUiFrameworkPlugin.logError( msg );
                }
            }
        }
        else
        {
            final JavaTypeConstraint javaTypeConstraintAnnotation = getProperty().getAnnotation( JavaTypeConstraint.class );
            
            if( javaTypeConstraintAnnotation != null )
            {
                for( JavaTypeKind kind : javaTypeConstraintAnnotation.kind() )
                {
                    kinds.add( kind );
                }
            }
        }
        
        this.browseDialogStyle = IJavaElementSearchConstants.CONSIDER_ALL_TYPES;        
        
        int count = kinds.size();
        
        if( count == 1 )
        {
            final JavaTypeKind kind = kinds.iterator().next();
            
            switch( kind )
            {
                case CLASS:           this.browseDialogStyle = IJavaElementSearchConstants.CONSIDER_CLASSES; break;
                case ABSTRACT_CLASS:  this.browseDialogStyle = IJavaElementSearchConstants.CONSIDER_CLASSES; break;
                case INTERFACE:       this.browseDialogStyle = IJavaElementSearchConstants.CONSIDER_INTERFACES; break;
                case ANNOTATION:      this.browseDialogStyle = IJavaElementSearchConstants.CONSIDER_ANNOTATION_TYPES; break;
                case ENUM:            this.browseDialogStyle = IJavaElementSearchConstants.CONSIDER_ENUMS; break;
                default:              throw new IllegalStateException();
            }
        }
        else if( count == 2 )
        {
            if( kinds.contains( JavaTypeKind.CLASS ) || kinds.contains( JavaTypeKind.ABSTRACT_CLASS ) )
            {
                if( kinds.contains( JavaTypeKind.INTERFACE ) )
                {
                    this.browseDialogStyle = IJavaElementSearchConstants.CONSIDER_CLASSES_AND_INTERFACES;
                }
                else if( kinds.contains( JavaTypeKind.ENUM ) )
                {
                    this.browseDialogStyle = IJavaElementSearchConstants.CONSIDER_CLASSES_AND_ENUMS;
                }
            }
        }
    }
    
    @Override
    public String browse( final SapphireRenderingContext context )
    {
        final IModelElement element = getModelElement();
        final ModelProperty property = getProperty();
        
        final IProject project = element.adapt( IProject.class );
        
        try 
        {
            final SelectionDialog dlg 
                = JavaUI.createTypeDialog( context.getShell(), null, project, this.browseDialogStyle, false );
            
            final String title = property.getLabel( true, CapitalizationType.TITLE_STYLE, false );
            dlg.setTitle(Resources.select + title);
            
            if (dlg.open() == SelectionDialog.OK) {
                Object results[] = dlg.getResult();
                assert results != null && results.length == 1;
                if (results[0] instanceof IType) {
                    return ((IType) results[0]).getFullyQualifiedName();
                }
            }
        } catch (JavaModelException e) {
            SapphireUiFrameworkPlugin.log( e );
        }
        
        return null;
    }

    private static final class Resources extends NLS 
    {
        public static String select;
        public static String typeKindNotRecognized;

        static 
        {
            initializeMessages( JavaTypeBrowseActionHandler.class.getName(), Resources.class );
        }
    }

}