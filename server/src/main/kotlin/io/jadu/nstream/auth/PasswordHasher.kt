package io.jadu.nstream.auth

import de.mkammerer.argon2.Argon2Factory

class PasswordHasher {
    fun hash(password: String): String = argon2.hash(
        3,
        65_536,
        1,
        password.toCharArray()
    )

    fun matches(password: String, passwordHash: String) : Boolean = argon2.verify(
        passwordHash, password.toCharArray()
    )


    private val argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id)
}