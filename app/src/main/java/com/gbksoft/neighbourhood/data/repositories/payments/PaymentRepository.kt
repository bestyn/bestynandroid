package com.gbksoft.neighbourhood.data.repositories.payments

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.android.billingclient.api.*
import com.gbksoft.neighbourhood.data.models.request.payment.PaymentReq
import com.gbksoft.neighbourhood.data.models.response.payment.SubscriptionModel
import com.gbksoft.neighbourhood.data.network.ApiFactory
import com.gbksoft.neighbourhood.model.payment.IllegalSkuException
import com.gbksoft.neighbourhood.model.payment.Platform
import com.gbksoft.neighbourhood.model.payment.RestoreResult
import com.gbksoft.neighbourhood.model.payment.RestoreResult.State.*
import com.gbksoft.neighbourhood.model.payment.SkuType
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import okhttp3.internal.toImmutableList
import retrofit2.HttpException

class PaymentRepository(context: Context) {
    private val api = ApiFactory.apiPayment

    private val purchasePublishSubject = PublishSubject.create<Purchase>()

    private val purchaseUpdateListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases?.forEach { purchasePublishSubject.onNext(it) }
            }
        }

    private var billingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener(purchaseUpdateListener)
        .enablePendingPurchases()
        .build()

    fun queryAllSkusDetails(): Single<List<SkuDetails>> {
        return querySkuDetails(SkuType.values().map { it.sku })
    }

    private fun querySkuDetails(skuList: List<String>): Single<List<SkuDetails>> {
        return Single.create { emitter ->
            val params = SkuDetailsParams.newBuilder()
            params.setSkusList(skuList.toList()).setType(BillingClient.SkuType.SUBS)

            establishConnectionAndRun {
                billingClient.querySkuDetailsAsync(params.build()) { _, skuDetailsList ->
                    emitter.onSuccess(skuDetailsList?.toImmutableList() ?: emptyList())
                }
            }
        }
    }

    fun getSkuDetails(sku: String): Maybe<SkuDetails> {
        return Maybe.create { emitter ->
            establishConnectionAndRun {
                val params = SkuDetailsParams.newBuilder()
                params.setSkusList(listOf(sku)).setType(BillingClient.SkuType.SUBS)
                billingClient.querySkuDetailsAsync(params.build()) { _, skuDetailsList ->
                    if (skuDetailsList != null && skuDetailsList.isNotEmpty()) {
                        emitter.onSuccess(skuDetailsList[0])
                    } else {
                        emitter.onComplete()
                    }
                }
            }
        }
    }

    fun launchPurchase(activity: FragmentActivity, sku: String): Observable<Purchase> {
        return querySkuDetails(listOf(sku))
            .filter { it.isNotEmpty() }
            .map { it[0] }
            .flatMapObservable { launchPurchase(activity, it) }
    }

    private fun launchPurchase(activity: FragmentActivity, skuDetails: SkuDetails): Observable<Purchase> {
        val flowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()

        establishConnectionAndRun {
            billingClient.launchBillingFlow(activity, flowParams).responseCode
        }

        return purchasePublishSubject.filter { it.sku == skuDetails.sku }
    }

    fun getTheMostProfitablePurchase(justActivePurchase: Boolean): Maybe<Purchase> {
        return fetchActivePurchases()
            .flatMapMaybe { purchases ->
                if (purchases.isEmpty()) {
                    Maybe.empty()
                } else {
                    purchases.filter { it.isAutoRenewing || !justActivePurchase }
                        .sortedWith(Comparator { t, t2 ->
                            val skuType1 = SkuType.getSkuType(t.sku) ?: return@Comparator 0
                            val skuType2 = SkuType.getSkuType(t2.sku) ?: return@Comparator 0
                            skuType1.compareTo(skuType2)
                        })
                        .lastOrNull()
                        ?.let { Maybe.just(it) } ?: Maybe.empty()
                }
            }
    }

    private fun fetchActivePurchases(): Single<List<Purchase>> {
        return Single.create { emitter ->
            establishConnectionAndRun {
                val result = billingClient.queryPurchases(BillingClient.SkuType.SUBS)
                emitter.onSuccess(result?.purchasesList ?: emptyList())
            }
        }
    }

    fun saveSubscription(purchase: Purchase): Completable {
        return api.setPayment(PaymentReq(
            purchase.purchaseToken,
            SkuType.getSkuType(purchase.sku)?.productId ?: throw IllegalSkuException()
        ))
    }

    fun getSavedSubscription(): Maybe<SubscriptionModel> {
        return api.getLastPayment()
            .map { Maybe.just(it.result) }
            .onErrorReturn { throwable ->
                if (throwable is HttpException) {
                    if (throwable.response()?.code() == 404) {
                        return@onErrorReturn Maybe.empty()
                    }
                }
                throw throwable
            }.flatMapMaybe { it }
    }

    fun acknowledgePurchase(purchase: Purchase): Completable {
        return Completable.create { emitter ->
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken)
                establishConnectionAndRun {
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams.build()) {
                        if (it.responseCode == BillingClient.BillingResponseCode.OK) {
                            emitter.onComplete()
                        } else {
                            throw Exception(it.debugMessage)
                        }
                    }
                }
            } else {
                emitter.onComplete()
            }
        }
    }

    fun restorePurchase(): Single<RestoreResult> {
        //Fetching saved subscription from backend
        return getSavedSubscription()
            //Goes here if there is saved subscription
            .flatMap { subscription ->
                if (subscription.getPlatform() != Platform.ANDROID) {
                    Maybe.just(RestoreResult(BOUGHT_ON_ANOTHER_PLATFORM, subscription.platform))
                } else {
                    //Fetching purchased purchase from Play Market
                    getTheMostProfitablePurchase(false)
                        .observeOn(Schedulers.io())
                        .flatMap { purchase ->
                            //If purchase token from subscription saved on backend equals
                            // token from subscription from Play Market that purchase is already restored
                            if (purchase.purchaseToken == subscription.transactionToken) {
                                Maybe.just(RestoreResult(RESTORED))
                            } else {
                                //Send subscription to the backend
                                saveSubscription(purchase)
                                    //Activate subscription in play market
                                    .andThen(acknowledgePurchase(purchase))
                                    .andThen(Maybe.just(RestoreResult(RESTORED)))
                            }
                        }
                        .switchIfEmpty(
                            //Removing saved purchase token from backend if there is not
                            //purchased subscription in Play Market
                            api.deletePayment().andThen(Maybe.just(RestoreResult(DELETED)))
                        )
                }
            }
            //Goes here if there is not saved subscription
            .switchIfEmpty(
                getTheMostProfitablePurchase(false)
                    .observeOn(Schedulers.io())
                    .flatMap { purchase ->
                        //Send subscription to the backend
                        saveSubscription(purchase)
                            //Activate subscription in play market
                            .andThen(acknowledgePurchase(purchase))
                            .andThen(Maybe.just(RestoreResult(RESTORED)))
                    }
                    .switchIfEmpty(Maybe.just(RestoreResult(NOT_FOUND)))
            )
            .toSingle()
    }

    private fun establishConnectionAndRun(func: (() -> Unit)) {
        if (billingClient.isReady) {
            func.invoke()
        } else {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        func.invoke()
                    } /*else {
                        throw Exception(billingResult.debugMessage)
                    }*/
                }

                override fun onBillingServiceDisconnected() {
                }
            })
        }
    }
}