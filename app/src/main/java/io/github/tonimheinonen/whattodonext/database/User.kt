package io.github.tonimheinonen.whattodonext.database

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
        var username: String? = "",
        var email: String? = ""
)