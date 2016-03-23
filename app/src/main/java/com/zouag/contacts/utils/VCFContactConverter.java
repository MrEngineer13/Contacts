package com.zouag.contacts.utils;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.zouag.contacts.models.Contact;

import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameter.ImageType;
import ezvcard.property.Address;
import ezvcard.property.Email;
import ezvcard.property.Photo;

/**
 * Created by Mohammed Aouf ZOUAG on 23/03/2016.
 */
public class VCFContactConverter {
    /**
     * Converts a list of contacts to a list of VCards.
     *
     * @param contacts to be converted.
     * @return
     */
    public static List<VCard> parse(List<Contact> contacts) {
        return Stream.of(contacts)
                .map(contact -> {
                    VCard vCard = new VCard();

                    vCard.setVersion(VCardVersion.V4_0);
                    vCard.setFormattedName(contact.getName());
                    vCard.addTelephoneNumber(contact.getPhoneNumber());
                    vCard.addEmail(new Email(contact.getEmail()));
                    Address address = new Address();
                    address.setLabel(contact.getAddress());
                    vCard.addPhoto(new Photo(contact.getImgPath(), ImageType.PNG));

                    return vCard;
                })
                .collect(Collectors.toList());
    }
}
