package de.ywegel.svenska.data.model

enum class SortOrder {
    Word,
    Translation,
    Created,
    LastEdited,
    ;

    companion object {
        val default = Created
    }
}
