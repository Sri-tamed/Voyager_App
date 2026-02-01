package com.example.voyager.data.local.datastore



import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.voyager.data.model.EmergencyContact
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.emergencyContactsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "emergency_contacts"
)

/**
 * DataStore for emergency contacts (offline-first storage)
 */
@Singleton
class EmergencyContactsStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.emergencyContactsDataStore

    companion object {
        private val CONTACTS_KEY = stringPreferencesKey("emergency_contacts")
    }

    /**
     * Get emergency contacts as Flow
     */
    fun getContacts(): Flow<List<EmergencyContact>> {
        return dataStore.data.map { preferences ->
            val contactsJson = preferences[CONTACTS_KEY] ?: return@map emptyList()
            try {
                Json.decodeFromString<List<SerializableEmergencyContact>>(contactsJson)
                    .map { it.toEmergencyContact() }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /**
     * Save emergency contacts
     */
    suspend fun saveContacts(contacts: List<EmergencyContact>) {
        dataStore.edit { preferences ->
            val serializableContacts = contacts.map { SerializableEmergencyContact.from(it) }
            preferences[CONTACTS_KEY] = Json.encodeToString(serializableContacts)
        }
    }

    /**
     * Add a contact
     */
    suspend fun addContact(contact: EmergencyContact) {
        dataStore.edit { preferences ->
            val currentJson = preferences[CONTACTS_KEY] ?: "[]"
            val currentContacts = try {
                Json.decodeFromString<List<SerializableEmergencyContact>>(currentJson)
            } catch (e: Exception) {
                emptyList()
            }

            // Limit to 5 contacts
            val newContacts = if (currentContacts.size >= 5) {
                currentContacts.take(4) + SerializableEmergencyContact.from(contact)
            } else {
                currentContacts + SerializableEmergencyContact.from(contact)
            }

            preferences[CONTACTS_KEY] = Json.encodeToString(newContacts)
        }
    }

    /**
     * Remove a contact
     */
    suspend fun removeContact(contactId: Int) {
        dataStore.edit { preferences ->
            val currentJson = preferences[CONTACTS_KEY] ?: return@edit
            val currentContacts = try {
                Json.decodeFromString<List<SerializableEmergencyContact>>(currentJson)
            } catch (e: Exception) {
                return@edit
            }

            val updatedContacts = currentContacts.filter { it.id != contactId }
            preferences[CONTACTS_KEY] = Json.encodeToString(updatedContacts)
        }
    }

    /**
     * Clear all contacts
     */
    suspend fun clearContacts() {
        dataStore.edit { preferences ->
            preferences.remove(CONTACTS_KEY)
        }
    }
}

/**
 * Serializable version of EmergencyContact for JSON storage
 */
@kotlinx.serialization.Serializable
private data class SerializableEmergencyContact(
    val id: Int,
    val name: String,
    val phoneNumber: String,
    val relationship: String,
    val isPrimary: Boolean,
    val position: Int
) {
    fun toEmergencyContact() = EmergencyContact(
        id = id,
        name = name,
        phoneNumber = phoneNumber,
        relationship = relationship,
        isPrimary = isPrimary,
        position = position
    )

    companion object {
        fun from(contact: EmergencyContact) = SerializableEmergencyContact(
            id = contact.id,
            name = contact.name,
            phoneNumber = contact.phoneNumber,
            relationship = contact.relationship,
            isPrimary = contact.isPrimary,
            position = contact.position
        )
    }
}