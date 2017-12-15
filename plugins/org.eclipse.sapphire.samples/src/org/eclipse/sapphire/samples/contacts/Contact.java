/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.samples.contacts;

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementHandle;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.DelegateImplementation;
import org.eclipse.sapphire.modeling.annotations.Documentation;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.NoDuplicates;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.Services;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.CustomXmlListBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlElementBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.samples.contacts.internal.ConnectionsListBinding;
import org.eclipse.sapphire.samples.contacts.internal.ContactCategoryPossibleValueService;
import org.eclipse.sapphire.samples.contacts.internal.ContactEqualityService;
import org.eclipse.sapphire.samples.contacts.internal.ContactImageService;
import org.eclipse.sapphire.samples.contacts.internal.ContactMethods;
import org.eclipse.sapphire.samples.contacts.internal.DuplicateContactValidationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Image( path = "Contact.png" )
@GenerateImpl

@Services
(
    { 
        @Service( impl = ContactImageService.class ), 
        @Service( impl = ContactEqualityService.class ),
        @Service( impl = DuplicateContactValidationService.class )
    }
)

public interface Contact extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( Contact.class );

    // *** Name ***
    
    @Label( standard = "name" )
    @Required
    @NoDuplicates
    @XmlBinding( path = "@name" )

    ValueProperty PROP_NAME = new ValueProperty( TYPE, "Name" );

    Value<String> getName();
    void setName( String name );
    
    // *** Category ***
    
    @Label( standard = "category" )
    @DefaultValue( text = "Personal" )
    @Service( impl = ContactCategoryPossibleValueService.class )
    @XmlBinding( path = "%category" )
    @Documentation( content = "This would be the help content for the category property." )

    ValueProperty PROP_CATEGORY = new ValueProperty( TYPE, "Category" );

    Value<String> getCategory();
    void setCategory( String category );
    
    // *** EMail ***
    
    @Label( standard = "E-Mail" )
    @XmlBinding( path = "e-mail" )

    ValueProperty PROP_E_MAIL = new ValueProperty( TYPE, "EMail" );

    Value<String> getEMail();
    void setEMail( String email );
    
    // *** PhoneNumbers ***
    
    @Type( base = PhoneNumber.class )
    @Label( standard = "phone numbers" )
    @XmlListBinding( path = "phone-numbers", mappings = @XmlListBinding.Mapping( element = "phone-number", type = PhoneNumber.class ) )
                             
    ListProperty PROP_PHONE_NUMBERS = new ListProperty( TYPE, "PhoneNumbers" );
    
    ModelElementList<PhoneNumber> getPhoneNumbers();
    
    // *** WebSites ***
    
    @Type( base = WebSite.class )
    @Label( standard = "web sites" )
    @XmlListBinding( path = "web-sites", mappings = @XmlListBinding.Mapping( element = "web-site", type = WebSite.class ) )
                             
    ListProperty PROP_WEB_SITES = new ListProperty( TYPE, "WebSites" );
    
    ModelElementList<WebSite> getWebSites();
    
    // *** METHOD: removePhoneNumbersByAreaCode ***
    
    @DelegateImplementation( ContactMethods.class )
    
    void removePhoneNumbersByAreaCode( String areaCode );
    
    // *** Address ***
    
    @Type( base = ContactAddress.class )
    @XmlBinding( path = "address" )
    
    ImpliedElementProperty PROP_ADDRESS = new ImpliedElementProperty( TYPE, "Address" );

    ContactAddress getAddress();

    // *** Assistant ***

    @Type( base = Assistant.class )
    @Label( standard = "assistant" )
    @XmlBinding( path = "assistant" )
    
    @Documentation
    (
        content = "The individual to whom the contact delegates certain tasks."
    )
    
    ElementProperty PROP_ASSISTANT = new ElementProperty( TYPE, "Assistant" );

    ModelElementHandle<Assistant> getAssistant();
    
    // *** Connections ***
    
    @Label( standard = "connections" )
    @Type( base = Connection.class )
    @CustomXmlListBinding( impl = ConnectionsListBinding.class )
                             
    ListProperty PROP_CONNECTIONS = new ListProperty( TYPE, "Connections" );
    
    ModelElementList<Connection> getConnections();
    
    // *** PrimaryOccupation ***
    
    @Type
    ( 
        base = Occupation.class, 
        possible = 
        { 
            JobOccupation.class, 
            StudentOccupation.class, 
            HomemakerOccupation.class 
        }
    )
    
    @Label( standard = "primary occupation" )
    
    @XmlElementBinding
    ( 
        path = "primary-occupation",
        mappings = 
        {
            @XmlElementBinding.Mapping( element = "job", type = JobOccupation.class ),
            @XmlElementBinding.Mapping( element = "student", type = StudentOccupation.class ),
            @XmlElementBinding.Mapping( element = "homemaker", type = HomemakerOccupation.class )
        }
    )
    
    @Documentation
    (
        content = "The contact's primary occupation, such as a job."
    )
    
    ElementProperty PROP_PRIMARY_OCCUPATION = new ElementProperty( TYPE, "PrimaryOccupation" );
    
    ModelElementHandle<Occupation> getPrimaryOccupation();
    
}
