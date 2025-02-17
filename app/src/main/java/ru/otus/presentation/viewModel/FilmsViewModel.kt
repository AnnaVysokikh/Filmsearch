package ru.otus.presentation.viewModel

import androidx.lifecycle.*
import androidx.paging.*
import ru.otus.repositoty.FilmsRepository
import ru.otus.presentation.MainActivity.Companion.FILMS
import ru.otus.presentation.SingleLiveEvent
import ru.otus.presentation.model.FilmModel
import ru.otus.repositoty.FilmPages
import ru.otus.repositoty.FilmsPageLoader

class FilmsViewModel (
    private val filmsRepository: FilmsRepository,
    ) : ViewModel(){

    var fragmentName: String = FILMS
    private val mError = filmsRepository.repoError
    private val mSelectedFilm = MutableLiveData<FilmModel>()
    var highlightedFilms = MutableLiveData<Int>()
    private var favorite = false
    val pagedFilms: LiveData<PagingData<FilmModel>>
        get() {
            val loader: FilmsPageLoader = { pageIndex, pageSize ->
                filmsRepository.getFilms(pageIndex, pageSize)
            }
            return Pager(
                config = PagingConfig(
                    pageSize = FilmsRepository.PAGE_SIZE,
                    enablePlaceholders = false,
                    initialLoadSize = FilmsRepository.PAGE_SIZE
                ),
                pagingSourceFactory = {
                    filmsRepository.filmsSource = FilmPages(loader, FilmsRepository.PAGE_SIZE)
                    filmsRepository.filmsSource
                }
            ).liveData.cachedIn(viewModelScope)
        }

    val pagedFavoriteFilms: LiveData<PagingData<FilmModel>>
        get() {
            val loader: FilmsPageLoader = { pageIndex, pageSize ->
                filmsRepository.getFavoriteFilms(pageIndex, pageSize)
            }
            return Pager(
                config = PagingConfig(
                    pageSize = FilmsRepository.PAGE_SIZE,
                    enablePlaceholders = false,
                    initialLoadSize = FilmsRepository.PAGE_SIZE
                ),
                pagingSourceFactory = {
                    filmsRepository.filmsSource = FilmPages(loader, FilmsRepository.PAGE_SIZE)
                    filmsRepository.filmsSource
                }
            ).liveData.cachedIn(viewModelScope)
        }

    init {
        highlightedFilms.value = -1
    }
    val selectedFilm: LiveData<FilmModel> = mSelectedFilm
    val error: SingleLiveEvent<String> = mError

    fun onFilmClick(id: Int){
        highlightedFilms.value = id
        filmsRepository.getFilm(id) { mSelectedFilm.value = it }
    }

    fun setSelectedFilm(id: Int?){
        if (id != null){
            filmsRepository.getFilm(id){ mSelectedFilm.value = filmsRepository.getFilmFromDB(id) }
        }
    }

    fun onFavoriteChanged(id: Int){
        val changingFilm = filmsRepository.getFilmFromDB(id)
        if (changingFilm?.isFavorite != null) {
            changingFilm.isFavorite = !changingFilm.isFavorite!!
            favorite = changingFilm.isFavorite!!
            filmsRepository.updateFilmInDB(changingFilm)
            filmsRepository.filmsSource.invalidate()
        }
    }

    fun getFavoriteChanged(): Boolean {
        return favorite
    }
}
