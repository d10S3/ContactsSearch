package com.d10s3.test.contactssearch

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import java.util.*
import kotlin.collections.ArrayList

class ContactRepository (private val context: Context) {
    fun fetchContacts(): MutableList<Contact> {
        val cleanList: MutableMap<String, Contact> = LinkedHashMap<String, Contact>()
        val cursor: Cursor = context.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")!!

        if (cursor.count > 0) {
            while (cursor.moveToNext()) {
                var name: String = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                var number: String = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                var photo: String = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)) ?: ""

                val user = Contact(name, number, photo)

                cleanList[number] = user
            }
        }

        cursor.close()

        return ArrayList<Contact>(cleanList.values)
    }
}