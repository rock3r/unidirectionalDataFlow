package com.odai.architecturedemo.cats

import com.odai.architecturedemo.cat.model.Cat
import com.odai.architecturedemo.cats.model.Cats
import com.odai.architecturedemo.cats.usecase.CatsUseCase
import com.odai.architecturedemo.cats.view.CatsView
import com.odai.architecturedemo.event.DataObserver
import com.odai.architecturedemo.event.Event
import com.odai.architecturedemo.event.EventObserver
import com.odai.architecturedemo.favourite.model.FavouriteCats
import com.odai.architecturedemo.favourite.model.FavouriteState
import com.odai.architecturedemo.favourite.usecase.FavouriteCatsUseCase
import com.odai.architecturedemo.loading.LoadingView
import com.odai.architecturedemo.loading.RetryClickedListener
import com.odai.architecturedemo.navigation.Navigator
import rx.Observer
import rx.subscriptions.CompositeSubscription

class CatsPresenter(
        val catsUseCase: CatsUseCase,
        val favouriteCatsUseCase: FavouriteCatsUseCase,
        val navigate: Navigator,
        val catsView: CatsView,
        val loadingView: LoadingView
) {

    var subscriptions = CompositeSubscription()

    fun startPresenting() {
        catsView.attach(catClickedListener)
        loadingView.attach(retryListener)
        subscriptions.add(
                catsUseCase.getCatsEvents()
                        .subscribe(catsEventsObserver)
        )
        subscriptions.add(
                catsUseCase.getCats()
                        .subscribe(catsObserver)
        )
        subscriptions.add(
                favouriteCatsUseCase.getFavouriteCats()
                        .subscribe(favouriteCatsObserver)
        )
    }

    fun stopPresenting() {
        subscriptions.clear()
        subscriptions = CompositeSubscription()
    }

    private val catsEventsObserver = object : EventObserver<Cats>() {
        override fun onLoading(event: Event<Cats>) {
            if (event.data != null) {
                loadingView.showLoadingIndicator()
            } else {
                loadingView.showLoadingScreen()
            }
        }

        override fun onIdle(event: Event<Cats>) {
            if (event.data != null) {
                loadingView.showData()
            } else {
                loadingView.showEmptyScreen()
            }
        }

        override fun onError(event: Event<Cats>) {
            if (event.data != null) {
                loadingView.showErrorIndicator()
            } else {
                loadingView.showErrorScreen()
            }
        }

    }

    private val catsObserver = object : DataObserver<Cats> {
        override fun onNext(p0: Cats) {
            catsView.display(p0);
        }
    }

    private val favouriteCatsObserver = object : DataObserver<FavouriteCats> {
        override fun onNext(p0: FavouriteCats) {
            catsView.display(p0)
        }
    }

    interface CatClickedListener {
        fun onFavouriteClicked(cat: Cat, state: FavouriteState)
        fun onCatClicked(cat: Cat)
    }

    val retryListener = object : RetryClickedListener {

        override fun onRetry() {
            catsUseCase.refreshCats()
        }

    }

    val catClickedListener = object : CatClickedListener {

        override fun onCatClicked(cat: Cat) {
            navigate.toCat(cat)
        }

        override fun onFavouriteClicked(cat: Cat, state: FavouriteState) {
            if (state == FavouriteState.FAVOURITE) {
                favouriteCatsUseCase.removeFromFavourite(cat)
            } else if (state == FavouriteState.UN_FAVOURITE) {
                favouriteCatsUseCase.addToFavourite(cat)
            }
        }

    }

}