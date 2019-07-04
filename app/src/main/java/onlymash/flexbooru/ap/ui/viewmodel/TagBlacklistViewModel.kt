package onlymash.flexbooru.ap.ui.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import onlymash.flexbooru.ap.data.db.dao.TagBlacklistDao
import onlymash.flexbooru.ap.data.model.TagBlacklist

class TagBlacklistViewModel(private val tagBlacklistDao: TagBlacklistDao) : ScopeViewModel() {

    val tags = MediatorLiveData<List<TagBlacklist>>()

    fun loadAll() {
        tags.addSource(tagBlacklistDao.getAllLiveData()) {
            tags.postValue(it)
        }
    }

    fun create(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            tagBlacklistDao.insert(TagBlacklist(name = name))
        }
    }

    fun delete(tag: TagBlacklist) {
        viewModelScope.launch(Dispatchers.IO) {
            tagBlacklistDao.delete(tag)
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            tagBlacklistDao.deleteAll()
        }
    }
}