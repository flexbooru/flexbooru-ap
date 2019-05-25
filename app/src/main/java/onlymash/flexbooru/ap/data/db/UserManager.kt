package onlymash.flexbooru.ap.data.db

import android.database.sqlite.SQLiteException
import onlymash.flexbooru.ap.common.App
import onlymash.flexbooru.ap.data.db.dao.UserDao
import onlymash.flexbooru.ap.data.model.User
import org.kodein.di.generic.instance

object UserManager {

    private val userDao by App.app.instance<UserDao>()

    @Throws(SQLiteException::class)
    fun getAllUsers(): List<User> {
        return userDao.getAllUsers()
    }

    @Throws(SQLiteException::class)
    fun getUserByUid(uid: Long): User? {
        return userDao.getUserByUid(uid)
    }

    @Throws(SQLiteException::class)
    fun createUser(user: User): User {
        user.uid = userDao.insert(user)
        return user
    }
}