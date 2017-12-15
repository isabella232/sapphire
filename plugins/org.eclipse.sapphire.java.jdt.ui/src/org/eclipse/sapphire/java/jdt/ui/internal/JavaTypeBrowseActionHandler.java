/******************************************************************************
 * Copyright (c) 2013 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amerson - [363551] JavaTypeConstraintService
 ******************************************************************************/

package org.eclipse.sapphire.java.jdt.ui.internal;

import java.util.EnumSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.sapphire.java.JavaTypeConstraintService;
import org.eclipse.sapphire.java.JavaTypeKind;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireBrowseActionHandler;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public final class JavaTypeBrowseActionHandler extends SapphireBrowseActionHandler
{
    public static final String ID = "Sapphire.Browse.Java.Type";
    public static final String PARAM_KINDS = "kinds";
    
    private String paramKinds;
    
    @Override
    public void init( final SapphireAction action,
                      final ActionHandlerDef def )
    {
        super.init( action, def );

        setId( ID );
        
        this.paramKinds = def.getParam( PARAM_KINDS );
    }

    @Override
    public String browse( final SapphireRenderingContext context )
    {
        final IModelElement element = getModelElement();
        final ModelProperty property = getProperty();
        
        final EnumSet<JavaTypeKind> kinds = EnumSet.noneOf( JavaTypeKind.class );
        
        if( this.paramKinds != null )
        {
            for( String kindString : this.paramKinds.split( "," ) )
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
            final JavaTypeConstraintService javaTypeConstraintService = element.service( property, JavaTypeConstraintService.class );
            
            if( javaTypeConstraintService != null )
            {
                kinds.addAll( javaTypeConstraintService.kinds() );
            }
        }
        
        int browseDialogStyle = IJavaElementSearchConstants.CONSIDER_ALL_TYPES;        
        int count = kinds.size();
        
        if( count == 1 )
        {
            final JavaTypeKind kind = kinds.iterator().next();
            
            switch( kind )
            {
                case CLASS:           browseDialogStyle = IJavaElementSearchConstants.CONSIDER_CLASSES; break;
                case ABSTRACT_CLASS:  browseDialogStyle = IJavaElementSearchConstants.CONSIDER_CLASSES; break;
                case INTERFACE:       browseDialogStyle = IJavaElementSearchConstants.CONSIDER_INTERFACES; break;
                case ANNOTATION:      browseDialogStyle = IJavaElementSearchConstants.CONSIDER_ANNOTATION_TYPES; break;
                case ENUM:            browseDialogStyle = IJavaElementSearchConstants.CONSIDER_ENUMS; break;
                default:              throw new IllegalStateException();
            }
        }
        else if( count == 2 )
        {
            if( kinds.contains( JavaTypeKind.CLASS ) || kinds.contains( JavaTypeKind.ABSTRACT_CLASS ) )
            {
                if( kinds.contains( JavaTypeKind.INTERFACE ) )
                {
                    browseDialogStyle = IJavaElementSearchConstants.CONSIDER_CLASSES_AND_INTERFACES;
                }
                else if( kinds.contains( JavaTypeKind.ENUM ) )
                {
                    browseDialogStyle = IJavaElementSearchConstants.CONSIDER_CLASSES_AND_ENUMS;
                }
            }
        }

        final IProject project = element.adapt( IProject.class );
        
        try 
        {
            final SelectionDialog dlg 
                = JavaUI.createTypeDialog( context.getShell(), null, project, browseDialogStyle, false );
            
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