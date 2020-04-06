package io.github.tonimheinonen.whattodonext.database

import com.google.firebase.database.IgnoreExtraProperties

/**
 * Represents an user.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0
 * @since 1.0
 */
@IgnoreExtraProperties
data class User(
        var username: String? = "",
        var email: String? = ""
)