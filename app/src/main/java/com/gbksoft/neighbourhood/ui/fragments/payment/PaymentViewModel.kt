package com.gbksoft.neighbourhood.ui.fragments.payment

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.Purchase
import com.gbksoft.neighbourhood.data.analytics.Analytics
import com.gbksoft.neighbourhood.data.models.response.payment.SubscriptionModel
import com.gbksoft.neighbourhood.data.repositories.payments.PaymentRepository
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.data.utils.ResponseStatusCodes
import com.gbksoft.neighbourhood.mappers.payment.SubscriptionPlanMapper
import com.gbksoft.neighbourhood.model.payment.Platform
import com.gbksoft.neighbourhood.model.payment.RestoreResult.State.*
import com.gbksoft.neighbourhood.model.payment.SkuType
import com.gbksoft.neighbourhood.model.payment.SubscriptionPlan
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.mvvm.SingleLiveEvent
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

class PaymentViewModel(
    private val paymentRepository: PaymentRepository,
    private val subscriptionPlanMapper: SubscriptionPlanMapper) : BaseViewModel() {

    private val _subscriptionPlansLiveData = MutableLiveData<List<SubscriptionPlan>>()
    val subscriptionPlansLiveData = _subscriptionPlansLiveData as LiveData<List<SubscriptionPlan>>

    private val _subscriptionIsNotFoundEvent = SingleLiveEvent<Unit>()
    val subscriptionIsNotFoundEvent = _subscriptionIsNotFoundEvent as LiveData<Unit>

    private val _subscriptionRestoredEvent = SingleLiveEvent<Unit>()
    val subscriptionRestoredEvent = _subscriptionRestoredEvent as LiveData<Unit>

    private val _subscriptionBoughtOnOtherPlatformEvent = SingleLiveEvent<Unit>()
    val subscriptionBoughtOnOtherPlatformEvent = _subscriptionBoughtOnOtherPlatformEvent as LiveData<Unit>

    private val _tokenAlreadyHasBeenTaken = SingleLiveEvent<Unit>()
    val tokenAlreadyHasBeenTaken = _tokenAlreadyHasBeenTaken as LiveData<Unit>

    var selectedSubscriptionPlan: SubscriptionPlan? = null

    fun fetchSubscriptionPlans() {
        addDisposable("fetchSkuDetails",
            paymentRepository.restorePurchase()
                .ignoreElement()
                .onErrorResumeNext {
                    //This is error which says `Transaction id has already been taken`
                    //Do nothing in this case
                    if (it is HttpException && it.code() == 422) Completable.complete()
                    else throw it
                }
                .andThen(paymentRepository.queryAllSkusDetails())
                .map { it -> it.sortedBy { it.priceAmountMicros } }
                .map { it.map { plan -> subscriptionPlanMapper.map(plan) }.toMutableList() }
                .observeOn(Schedulers.io())
                .flatMap { markActiveSubscription(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { setShowLoading(true) }
                .doAfterTerminate { setShowLoading(false) }
                .subscribe({
                    _subscriptionPlansLiveData.postValue(it)
                }, { ParseErrorUtils.parseError(it, errorsFuncs) })
        )
    }

    private fun markActiveSubscription(subscriptionsList: MutableList<SubscriptionPlan>) = paymentRepository.getSavedSubscription()
        .flatMap { savedSubscription ->
            if (savedSubscription.getPlatform() == Platform.ANDROID) {
                manageActiveSubscriptionFromPlayMarket(subscriptionsList)
            } else {
                manageActiveSubscriptionFromAnotherPlatform(subscriptionsList, savedSubscription)
            }
        }
        .toSingle(subscriptionsList)
        .doOnSuccess { subscriptions ->
            //If at least one item is active it means user might buy subscription and we can clear subscription plan
            if (subscriptions.any { it.active }) {
                selectedSubscriptionPlan = null
            }
        }

    private fun manageActiveSubscriptionFromPlayMarket(subscriptionsList: MutableList<SubscriptionPlan>)
        : Maybe<MutableList<SubscriptionPlan>> {
        return paymentRepository.getTheMostProfitablePurchase(false)
            .flatMapCompletable { purchase ->
                addPurchasedSubscriptionIfItIsNotInList(purchase, subscriptionsList)
                    .andThen(setActiveState(purchase, subscriptionsList))
            }.toSingleDefault(subscriptionsList).toMaybe()
    }

    private fun manageActiveSubscriptionFromAnotherPlatform(
        subscriptionsList: MutableList<SubscriptionPlan>,
        savedSubscription: SubscriptionModel
    ): Maybe<MutableList<SubscriptionPlan>> {
        val sku = SkuType.getSkuTypeByProductName(savedSubscription.productName)
        val platform = savedSubscription.getPlatform()
        val maybeSource = sku?.let { skuType ->
            //if there is the same product on android platform - fetch it to get info about product
            platform?.let { platform ->
                paymentRepository.getSkuDetails(skuType.sku)
                    .map { subscriptionPlanMapper.mapSubscriptionFromAnotherPlatform(it, platform) }
            }
        }
        //If there is not the same product on android just show item with `Product form another platform` label
            ?: Maybe.just(subscriptionPlanMapper.buildSubscriptionPlanFromAnotherPlatform(savedSubscription))
        return maybeSource
            .doOnSuccess { it.active = true }
            .map { subscriptionsList.apply { add(0, it) } }
    }

    private fun setActiveState(purchase: Purchase, subscriptions: List<SubscriptionPlan>) = Completable.create { emitter ->
        val purchased = purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        if (purchased && purchase.isAutoRenewing) {
            subscriptions.find { it.id == purchase.sku }?.active = true

            //If at least one item is active it means user might buy subscription and we can clear subscription plan
            if (subscriptions.any { it.active }) {
                selectedSubscriptionPlan = null
            }
        }

        emitter.onComplete()
    }

    private fun addPurchasedSubscriptionIfItIsNotInList(
        purchase: Purchase,
        subscriptions: MutableList<SubscriptionPlan>): Completable {
        val purchasedSubscriptionIsInList = subscriptions.find { it.id == purchase.sku } != null
        return if (purchasedSubscriptionIsInList) {
            Completable.complete()
        } else {
            paymentRepository.getSkuDetails(purchase.sku)
                .map { subscriptionPlanMapper.map(it) }
                .doOnSuccess { subscriptions.add(it) }
                .ignoreElement()
        }
    }

    fun startPurchase(activity: FragmentActivity) {
        selectedSubscriptionPlan?.let { subsPlan ->
            addDisposable("launchPurchase", paymentRepository.launchPurchase(activity, subsPlan.id)
                .doOnNext { setShowLoading(true) }
                .observeOn(Schedulers.io())
                .flatMapCompletable {
                    paymentRepository.saveSubscription(it)
                        .andThen(paymentRepository.acknowledgePurchase(it))
                        .andThen { fetchSubscriptionPlans() }
                        .andThen { Analytics.onSubscriptionPurchased(subsPlan.id, subsPlan.price) }
                }
                .doAfterTerminate { setShowLoading(false) }
                .subscribe({}, { ParseErrorUtils.parseError(it, errorsFuncs) }))
        }
    }

    fun restorePurchase() {
        addDisposable("restorePurchase", paymentRepository.restorePurchase()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { setShowLoading(true) }
            .doAfterTerminate { setShowLoading(false) }
            .subscribe({ restoreResult ->
                when (restoreResult.state) {
                    RESTORED -> _subscriptionRestoredEvent.call()
                    NOT_FOUND, DELETED -> _subscriptionIsNotFoundEvent.call()
                    BOUGHT_ON_ANOTHER_PLATFORM -> _subscriptionBoughtOnOtherPlatformEvent.call()
                }
            }, {
                ParseErrorUtils.parseError(it, HashMap(errorsFuncs).apply {
                    put(ResponseStatusCodes.CODE_422_VALIDATION_FAILED, Consumer {
                        _tokenAlreadyHasBeenTaken.call()
                    })
                })
            }))
    }
}