package com.nxt.callguard.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

object ContactsHelper {
    /**
     * True if [number] matches a contact saved on the device. Android never
     * delivers calls from saved contacts to any CallScreeningService (ours or
     * anyone else's), so this is also used to explain that limitation in the UI.
     */
    fun isKnownContact(context: Context, number: String): Boolean {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) return false

        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(number)
        )
        return context.contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup._ID), null, null, null)
            ?.use { it.moveToFirst() }
            ?: false
    }
}
