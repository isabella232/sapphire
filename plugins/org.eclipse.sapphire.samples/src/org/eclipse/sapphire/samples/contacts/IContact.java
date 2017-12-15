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

package org.eclipse.sapphire.samples.contacts;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IRemovable;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.DelegateImplementation;
import org.eclipse.sapphire.modeling.annotations.DependsOn;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.ListPropertyCustomBinding;
import org.eclipse.sapphire.modeling.annotations.NoDuplicates;
import org.eclipse.sapphire.modeling.annotations.NonNullValue;
import org.eclipse.sapphire.modeling.annotations.PossibleValuesProvider;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.annotations.Validator;
import org.eclipse.sapphire.modeling.validators.UniqueValueValidator;
import org.eclipse.sapphire.modeling.xml.IModelElementForXml;
import org.eclipse.sapphire.modeling.xml.annotations.GenerateXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.ListPropertyXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.ListPropertyXmlBindingMapping;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.samples.contacts.internal.ConnectionsListController;
import org.eclipse.sapphire.samples.contacts.internal.ContactCategoryValuesProvider;
import org.eclipse.sapphire.samples.contacts.internal.ContactMethods;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Image( small = "org.eclipse.sapphire.samples/images/person.png" )
@GenerateXmlBinding

public interface IContact

    extends IModelElementForXml, IRemovable

{
    ModelElementType TYPE = new ModelElementType( IContact.class );

    // *** Name ***
    
    @XmlBinding( path = "@name" )
    @Label( standard = "name" )
    @NonNullValue
    @DependsOn( "*/Name" )
    @Validator( impl = UniqueValueValidator.class )

    ValueProperty PROP_NAME = new ValueProperty( TYPE, "Name" );

    Value<String> getName();
    void setName( String name );
    
    // *** Category ***
    
    @XmlBinding( path = "%category" )
    @Label( standard = "category" )
    @DefaultValue( "Personal" )
    @PossibleValuesProvider( impl = ContactCategoryValuesProvider.class, invalidValueSeverity = IStatus.OK )

    ValueProperty PROP_CATEGORY = new ValueProperty( TYPE, "Category" );

    Value<String> getCategory();
    void setCategory( String category );
    
    // *** EMail ***
    
    @XmlBinding( path = "e-mail" )
    @Label( standard = "E-Mail" )

    ValueProperty PROP_E_MAIL = new ValueProperty( TYPE, "EMail" );

    Value<String> getEMail();
    void setEMail( String email );
    
    // *** PhoneNumbers ***
    
    @Type( base = IPhoneNumber.class )
    
    @ListPropertyXmlBinding( path = "phone-numbers",
                             mappings = { @ListPropertyXmlBindingMapping( element = "phone-number", type = IPhoneNumber.class ) } )
                             
    @Label( standard = "phone numbers" )
                             
    ListProperty PROP_PHONE_NUMBERS = new ListProperty( TYPE, "PhoneNumbers" );
    
    ModelElementList<IPhoneNumber> getPhoneNumbers();
    
    // *** WebSites ***
    
    @Type( base = IWebSite.class )
    
    @ListPropertyXmlBinding( path = "web-sites",
                             mappings = { @ListPropertyXmlBindingMapping( element = "web-site", type = IWebSite.class ) } )
                             
    @Label( standard = "web sites" )
                             
    ListProperty PROP_WEB_SITES = new ListProperty( TYPE, "WebSites" );
    
    ModelElementList<IWebSite> getWebSites();
    
    // *** METHOD: removePhoneNumbersByAreaCode ***
    
    @DelegateImplementation( ContactMethods.class )
    
    void removePhoneNumbersByAreaCode( String areaCode );
    
    // *** Address ***
    
    @Type( base = IAddress.class )
    
    ElementProperty PROP_ADDRESS = new ElementProperty( TYPE, "Address" );

    IAddress getAddress();

    // *** Assistant ***

    @Type( base = IAssistant.class )
    @XmlBinding( path = "assistant" )
    @Label( standard = "assistant" )
    
    ElementProperty PROP_ASSISTANT = new ElementProperty( TYPE, "Assistant" );

    IAssistant getAssistant();
    IAssistant getAssistant( boolean createIfNecessary );
    
    // *** Connections ***
    
    @Label( standard = "connections" )
    @Type( base = IConnection.class )
    @ListPropertyCustomBinding( impl = ConnectionsListController.class )
    @NoDuplicates
                             
    ListProperty PROP_CONNECTIONS = new ListProperty( TYPE, "Connections" );
    
    ModelElementList<IConnection> getConnections();
    
}
