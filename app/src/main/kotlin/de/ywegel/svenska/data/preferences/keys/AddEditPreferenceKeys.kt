package de.ywegel.svenska.data.preferences.keys

import de.ywegel.svenska.data.preferences.PreferenceStore.AddEdit
import de.ywegel.svenska.data.preferences.booleanPreference

object AddEditPreferenceKeys {
    val AnnotationInformationHidden = booleanPreference(AddEdit, "add_edit_annotation_information_hidden", false)
}
