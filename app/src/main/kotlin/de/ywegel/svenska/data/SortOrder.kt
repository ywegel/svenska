package de.ywegel.svenska.data

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
