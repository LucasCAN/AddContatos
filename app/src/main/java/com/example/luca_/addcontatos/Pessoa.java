package com.example.luca_.addcontatos;

import android.content.Context;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.provider.ContactsContract;

import java.util.ArrayList;

public class Pessoa {


    private String nome;
    private String telefone;
    private String email;

    //private Context mContext;


    public Pessoa(String nome, String telefone, String email, Context mContext){
            this.nome = nome;
            this.telefone = telefone;
            this.email = email;
            //this.mContext = mContext;

        }


    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }


    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /*
    public Context getmContext() {
        return mContext;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }
    */

/*
    public ArrayList<ContentProviderOperation> AddContact()
    {
        final ArrayList<ContentProviderOperation> contacts = new ArrayList<ContentProviderOperation>();
        final int rawContactInsertIndex = contacts.size();
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(mContext);
        Log.d("AddMeNow", preference.getString("accountKey", null) + "  "+preference.getString("emailKey", null));
        contacts.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, preference.getString("accountKey",null))
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, preference.getString("emailKey",null)).build());

        //Add Name
        contacts.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,0 )
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, nome)
                .build());


        //Add Phone Number
        contacts.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
                        0 )
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, telefone)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build());



        return contacts;

    }
*/

}
