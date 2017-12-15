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

package org.eclipse.sapphire.ui.def;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.modeling.IRemovable;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DelegateImplementation;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.NonNullValue;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.IModelElementForXml;
import org.eclipse.sapphire.modeling.xml.annotations.GenerateXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.ListPropertyXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.ListPropertyXmlBindingMapping;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.ui.def.internal.ImportDirectiveMethods;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Image( small = "org.eclipse.sapphire.ui/images/objects/bundle.gif" )
@GenerateXmlBinding

public interface IImportDirective

    extends IModelElementForXml, IRemovable
    
{
    ModelElementType TYPE = new ModelElementType( IImportDirective.class );
    
    // *** Bundle ***
    
    @Label( standard = "bundle" )
    @NonNullValue
    @XmlBinding( path = "bundle" )
    
    ValueProperty PROP_BUNDLE = new ValueProperty( TYPE, "Bundle" ); //$NON-NLS-1$
    
    Value<String> getBundle();
    void setBundle( String bundle );
    
    // *** Packages ***
    
    @Label( standard = "packages" )
    @Type( base = IPackageReference.class )
    @ListPropertyXmlBinding( mappings = { @ListPropertyXmlBindingMapping( element = "package", type = IPackageReference.class ) } )
                             
    ListProperty PROP_PACKAGES = new ListProperty( TYPE, "Packages" ); //$NON-NLS-1$
    
    ModelElementList<IPackageReference> getPackages();
    
    // *** Definitions ***
    
    @Label( standard = "definitions" )
    @Type( base = IDefinitionReference.class )
    @ListPropertyXmlBinding( mappings = { @ListPropertyXmlBindingMapping( element = "definition", type = IDefinitionReference.class ) } )
                             
    ListProperty PROP_DEFINITIONS = new ListProperty( TYPE, "Definitions" ); //$NON-NLS-1$
    
    ModelElementList<IDefinitionReference> getDefinitions();

    // *** Method : resolveClass ***
    
    @DelegateImplementation( ImportDirectiveMethods.class )
    
    Class<?> resolveClass( String className );
    
    // *** Method : resolveImage ***
    
    @DelegateImplementation( ImportDirectiveMethods.class )
    
    ImageDescriptor resolveImage( String imagePath );
    
}
