package com.gbksoft.neighbourhood.ui.fragments.payment

import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.data.analytics.Analytics
import com.gbksoft.neighbourhood.databinding.FragmentPaymentBinding
import com.gbksoft.neighbourhood.databinding.ItemSubscriptionHintBinding
import com.gbksoft.neighbourhood.databinding.ItemSubscriptionPlanBinding
import com.gbksoft.neighbourhood.model.payment.Platform
import com.gbksoft.neighbourhood.model.payment.SubscriptionPlan
import com.gbksoft.neighbourhood.ui.dialogs.YesNoDialog
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.utils.ToastUtils

class PaymentFragment : SystemBarsColorizeFragment() {

    private lateinit var layout: FragmentPaymentBinding
    private lateinit var viewModel: PaymentViewModel

    override fun getStatusBarColor(): Int = R.color.white
    override fun getNavigationBarColor(): Int = R.color.white

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(viewModelStore, PaymentViewModelFactory(requireActivity()))
            .get(PaymentViewModel::class.java)
        Analytics.onPaymentPlanOpened()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_payment, container, false)
        layout.btnRestore.paintFlags = (layout.btnRestore.paintFlags or Paint.UNDERLINE_TEXT_FLAG)
        layout.btnMain.isActivated = false
        hideNavigateBar()
        return layout.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layout.btnRestore.setOnClickListener {
            view.isEnabled = false
            viewModel.restorePurchase()
        }

        viewModel.subscriptionPlansLiveData.observe(viewLifecycleOwner, Observer {
            val activeSubscriptionPlan = it.find { subscriptionPlan -> subscriptionPlan.active }
            val userHasActiveSubscription = activeSubscriptionPlan != null
            initStaticText(userHasActiveSubscription)
            initSubscriptionPlans(it, userHasActiveSubscription)
            initMainButton(activeSubscriptionPlan)
        })

        viewModel.subscriptionIsNotFoundEvent.observe(viewLifecycleOwner, Observer {
            layout.btnRestore.isEnabled = true
            showNoSubscriptionFoundDialog()
        })

        viewModel.subscriptionRestoredEvent.observe(viewLifecycleOwner, Observer {
            layout.btnRestore.isEnabled = true
            showActiveSubscriptionFoundDialog()
        })

        viewModel.tokenAlreadyHasBeenTaken.observe(viewLifecycleOwner, Observer {
            layout.btnRestore.isEnabled = true
            showSubscriptionIsAlreadyOccupied()
        })

        viewModel.subscriptionBoughtOnOtherPlatformEvent.observe(viewLifecycleOwner, Observer {
            showRestoreSubscriptionOnIOSWarning()
        })
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchSubscriptionPlans()
    }

    private fun initMainButton(activeSubscriptionPlan: SubscriptionPlan?) {
        val userHasActiveSubscription = activeSubscriptionPlan != null

        layout.btnMain.setText(
            if (userHasActiveSubscription) R.string.manage_subscription_button
            else R.string.purchase_subscription_button
        )

        layout.btnMain.setOnClickListener { view ->
            if (view.isActivated) {
                if (userHasActiveSubscription) {
                    if (activeSubscriptionPlan?.managePlatform == Platform.ANDROID) {
                        redirectToPlayMarketSubscriptions(activeSubscriptionPlan.id)
                    } else {
                        showManageSubscriptionOnIOSWarning()
                    }
                } else {
                    activity?.let { viewModel.startPurchase(it) }
                }
            } else {
                ToastUtils.showToastMessage(requireContext(), R.string.hint_choose_plan_first)
            }
        }
        layout.btnMain.isActivated = userHasActiveSubscription || viewModel.selectedSubscriptionPlan != null
    }

    private fun redirectToPlayMarketSubscriptions(subscriptionPlanId: String) {
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/account/subscriptions?sku=$subscriptionPlanId&package=${requireActivity().packageName}"))
        startActivity(browserIntent)
    }

    private fun initStaticText(subscribed: Boolean) {
        layout.tvHeader.setText(
            if (subscribed) R.string.title_payment_screen_for_subscribed
            else R.string.title_payment_screen_for_not_subscribed
        )
        layout.tvDescription.setText(
            if (subscribed) R.string.description_payment_screen_for_subscribed
            else R.string.description_payment_screen_for_not_subscribed
        )
        initHints(resources.getStringArray(R.array.subscription_hints).asList())
    }

    private fun initHints(hints: List<String>) {
        layout.ltHints.removeAllViews()
        hints
            .map { hint ->
                createHintView(hint)
            }
            .forEach { layout.ltHints.addView(it) }

    }

    private fun createHintView(hint: String): View {
        val layout = DataBindingUtil.inflate<ItemSubscriptionHintBinding>(
            layoutInflater, R.layout.item_subscription_hint, layout.ltHints, false)
        layout.hint = hint
        return layout.root
    }

    private fun initSubscriptionPlans(subscriptionPlans: List<SubscriptionPlan>, hasActiveSubscription: Boolean) {
        layout.ltSubscriptionPlansContainer.removeAllViews()
        subscriptionPlans
            .map { subscriptionPlan ->
                createSubscriptionPlanView(subscriptionPlan, subscriptionPlans, hasActiveSubscription)
            }
            .forEach { layout.ltSubscriptionPlansContainer.addView(it) }
    }

    private fun createSubscriptionPlanView(subscriptionPlan: SubscriptionPlan,
                                           subscriptionPlans: List<SubscriptionPlan>,
                                           hasActiveSubscription: Boolean): View {
        val planLayout = DataBindingUtil.inflate<ItemSubscriptionPlanBinding>(layoutInflater,
            R.layout.item_subscription_plan, layout.ltSubscriptionPlansContainer, false)

        planLayout.subscriptionPlan = subscriptionPlan
        val isSelected = subscriptionPlan.active || subscriptionPlan == viewModel.selectedSubscriptionPlan
        planLayout.root.isSelected = isSelected
        planLayout.ivSelectedIcon.visibility = if (isSelected) View.VISIBLE else View.GONE

        //If user has active subscriptions he must cancel it before choosing another one
        planLayout.root.isActivated = !hasActiveSubscription
        planLayout.tvTitle.isEnabled = !hasActiveSubscription
        planLayout.tvPrice.isEnabled = !hasActiveSubscription

        if (hasActiveSubscription) {
            planLayout.root.setOnClickListener {
                val activeSubscriptionPlan = subscriptionPlans.find { it.active }
                if (activeSubscriptionPlan != null && activeSubscriptionPlan.managePlatform == Platform.ANDROID) {
                    showChangeSubscriptionDialog(activeSubscriptionPlan.id)
                } else {
                    showManageSubscriptionOnIOSWarning()
                }
            }
        } else {
            planLayout.root.setOnClickListener { clickedView ->
                layout.btnMain.isActivated = true
                layout.ltSubscriptionPlansContainer.children.forEach {
                    val taggedPlanLayout = it.getTag(R.id.subs_plan_layout)
                        as ItemSubscriptionPlanBinding
                    it.isSelected = it == clickedView
                    taggedPlanLayout.ivSelectedIcon.visibility = if (it == clickedView)
                        View.VISIBLE else View.GONE
                }
                viewModel.selectedSubscriptionPlan = subscriptionPlan
            }
        }

        planLayout.root.setTag(R.id.subs_plan_layout, planLayout)
        return planLayout.root
    }

    private fun showChangeSubscriptionDialog(activeSubscriptionPlanId: String) = YesNoDialog.Builder()
        .setTitle(R.string.title_change_subscription)
        .setMessage(R.string.msg_change_subscription)
        .setPositiveButton(R.string.manage_subscription_button) {
            redirectToPlayMarketSubscriptions(activeSubscriptionPlanId)
        }
        .build()
        .show(childFragmentManager, "ChangeSubscriptionDialog")

    private fun showActiveSubscriptionFoundDialog() = YesNoDialog.Builder()
        .setTitle(R.string.title_active_subscription_found_dialog)
        .setMessage(R.string.msg_active_subscription_found_dialog)
        .setPositiveButton(R.string.ok) {}
        .build()
        .show(childFragmentManager, "ActiveSubscriptionFoundDialog")

    private fun showNoSubscriptionFoundDialog() = YesNoDialog.Builder()
        .setTitle(R.string.title_no_subscription_found_dialog)
        .setMessage(R.string.msg_no_subscription_found_dialog)
        .setPositiveButton(R.string.ok) {}
        .build()
        .show(childFragmentManager, "NoSubscriptionFoundDialog")

    private fun showManageSubscriptionOnIOSWarning() = YesNoDialog.Builder()
        .setTitle(R.string.title_manage_subscription_on_ios_dialog)
        .setMessage(R.string.msg_manage_subscription_on_ios_dialog)
        .setPositiveButton(R.string.ok) {}
        .build()
        .show(childFragmentManager, "ManageSubscriptionOnIOSWarning")

    private fun showRestoreSubscriptionOnIOSWarning() = YesNoDialog.Builder()
        .setTitle(R.string.title_restore_subscription_on_ios_dialog)
        .setMessage(R.string.msg_restore_subscription_on_ios_dialog)
        .setPositiveButton(R.string.ok) {}
        .build()
        .show(childFragmentManager, "RestoreSubscriptionOnIOSWarning")

    private fun showSubscriptionIsAlreadyOccupied() = YesNoDialog.Builder()
        .setMessage(R.string.subscription_is_used_by_another_user)
        .setPositiveButton(R.string.ok) {}
        .build()
        .show(childFragmentManager, "SubscriptionIsAlreadyOccupied")
}