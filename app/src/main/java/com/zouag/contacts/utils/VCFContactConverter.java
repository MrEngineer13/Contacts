package com.zouag.contacts.utils;

import android.content.Context;
import android.os.Environment;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.zouag.contacts.R;
import com.zouag.contacts.models.Contact;

import java.io.File;
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
     * @return the extracted list of VCards.
     */
    public static List<VCard> parseContacts(List<Contact> contacts) {
        return Stream.of(contacts)
                .map(contact -> {
                    VCard vCard = new VCard();

                    vCard.setVersion(VCardVersion.V4_0);
                    vCard.setExtendedProperty("id", String.valueOf(contact.getId()));
                    vCard.setFormattedName(contact.getName());
                    vCard.addTelephoneNumber(contact.getPhoneNumber());
                    vCard.addEmail(new Email(contact.getEmail()));
                    Address address = new Address();
                    address.setLabel(contact.getAddress());
                    vCard.addAddress(address);
                    vCard.addPhoto(new Photo(contact.getImgPath(), ImageType.PNG));

                    return vCard;
                })
                .collect(Collectors.toList());
    }

    /**
     * Converts a list of VCards to a list of contacts.
     *
     * @param cards to be parsed
     * @return the list of extracted contacts.
     */
    public static List<Contact> parseVCards(List<VCard> cards) {
        return Stream.of(cards)
                .map(card -> {
                    Contact.Builder builder = new Contact.Builder()
                            .id(Integer.parseInt(card.getExtendedProperty("id").getValue()))
                            .name(card.getFormattedName().getValue())
                            .phoneNumber(card.getTelephoneNumbers().get(0).getText());

                    if (card.getPhotos() != null && card.getPhotos().size() != 0)
                        builder.imgPath(card.getPhotos().get(0).getUrl());

                    if (card.getEmails() != null && card.getEmails().size() != 0)
                        builder.email(card.getEmails().get(0).getValue());

                    if (card.getAddresses() != null && card.getAddresses().size() != 0)
                        builder.address(card.getAddresses().get(0).getLabel());

                    return builder.createContact();
                })
                .collect(Collectors.toList());
    }

    /**
     * @param context
     * @return the path of the file where the contacts' .vcf file will be created.
     */
    public static String getVCFSavePath(Context context) {
        String appName = context.getString(R.string.app_name);
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                appName);

        return mediaStorageDir.getPath() + File.separator + "contacts_save.vcf";
    }
}
