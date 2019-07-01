package onlymash.flexbooru.ap.ui.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import onlymash.flexbooru.ap.data.db.dao.TagFilterDao
import onlymash.flexbooru.ap.data.model.TagFilter

class TagFilterViewModel(private val tagFilterDao: TagFilterDao) : ScopeViewModel() {

    val tags = MediatorLiveData<List<TagFilter>>()

    fun loadAll() {
        tags.addSource(tagFilterDao.getAllLiveData()) {
            tags.postValue(it)
        }
    }

    fun create(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            tagFilterDao.insert(TagFilter(name = name))
        }
    }

    fun delete(tagFilter: TagFilter) {
        viewModelScope.launch(Dispatchers.IO) {
            tagFilterDao.delete(tagFilter)
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            tagFilterDao.deleteAll()
        }
    }
}