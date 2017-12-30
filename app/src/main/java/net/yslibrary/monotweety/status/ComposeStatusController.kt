package net.yslibrary.monotweety.status

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ShortcutManager
import android.os.Build
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.view.*
import android.widget.FrameLayout
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import net.yslibrary.monotweety.R
import net.yslibrary.monotweety.analytics.Analytics
import net.yslibrary.monotweety.base.*
import net.yslibrary.monotweety.data.status.Tweet
import net.yslibrary.monotweety.status.adapter.ComposeStatusAdapter
import net.yslibrary.monotweety.status.adapter.EditorAdapterDelegate
import timber.log.Timber
import javax.inject.Inject
import kotlin.properties.Delegates


class ComposeStatusController(private var status: String? = null) : ActionBarController(),
                                                                    HasComponent<ComposeStatusComponent> {

  override val hasBackButton: Boolean = true
  override val hasOptionsMenu: Boolean = true

  override val component: ComposeStatusComponent by lazy {
    getComponentProvider<ComposeStatusComponent.ComponentProvider>(activity!!)
        .composeStatusComponent(ComposeStatusViewModule(status))
  }

  val adapterListener = object : ComposeStatusAdapter.Listener {
    override fun onEnableThreadChanged(enabled: Boolean) {
      viewModel.onEnableThreadChanged(enabled)
    }

    override fun onKeepOpenChanged(enabled: Boolean) {
      viewModel.onKeepOpenChanged(enabled)
    }

    override fun onStatusChanged(status: String) {
      viewModel.onStatusChanged(status)
    }
  }

  lateinit var bindings: Bindings

  val statusAdapter: ComposeStatusAdapter by lazy { ComposeStatusAdapter(adapterListener) }

  @set:[Inject]
  var viewModel by Delegates.notNull<ComposeStatusViewModel>()

  @set:[Inject]
  var refWatcherDelegate by Delegates.notNull<RefWatcherDelegate>()

  val sendButtonClicks: PublishSubject<Unit> = PublishSubject.create<Unit>()

  override fun onContextAvailable(context: Context) {
    super.onContextAvailable(context)
    Timber.d("status: $status")
    component.inject(this)

    reportShortcutUsedIfNeeded(status)
    analytics.viewEvent(Analytics.VIEW_COMPOSE_STATUS)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
    val view = inflater.inflate(R.layout.controller_compose_status, container, false)

    bindings = Bindings(view)

    setEvents()

    return view
  }

  override fun onAttach(view: View) {
    super.onAttach(view)
    initToolbar()
  }

  fun setEvents() {

    // https://code.google.com/p/android/issues/detail?id=161559
    // disable animation to avoid duplicated viewholder
    bindings.list.apply {
      if (itemAnimator is SimpleItemAnimator) {
        (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      }
      adapter = statusAdapter
      layoutManager = LinearLayoutManager(activity)
    }

    // FIXME: this should be handled in ViewModel
    Observable.combineLatest(
        viewModel.statusInfo.distinctUntilChanged(),
        viewModel.keepOpen,
        viewModel.tweetAsThread,
        { (status1, valid, length, maxLength), keepOpen, tweetAsThread ->
          EditorAdapterDelegate.Item(status = status1,
              keepOpen = keepOpen,
              enableThread = tweetAsThread,
              statusLength = length,
              maxLength = maxLength,
              valid = valid,
              clear = false)
        })
        .distinctUntilChanged()
        .bindToLifecycle()
        .subscribe {
          Timber.d("item updated: $it")
          statusAdapter.updateEditor(it)
        }

    viewModel.closeViewRequests
        .bindToLifecycle()
        .subscribe { activity?.finish() }

    viewModel.isSendableStatus
        .bindToLifecycle()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { activity?.invalidateOptionsMenu() }

    viewModel.progressEvents
        .skip(1)
        .bindToLifecycle()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          when (it) {
            ComposeStatusViewModel.ProgressEvent.IN_PROGRESS -> showLoadingState()
            ComposeStatusViewModel.ProgressEvent.FINISHED -> hideLoadingState()
            else -> hideLoadingState()
          }
        }

    viewModel.statusUpdated
        .switchMap { viewModel.previousStatus.first() }
        .switchMap { tweet ->
          viewModel.footerState.first()
              .map { Pair(tweet, it) }
        }
        .bindToLifecycle()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { (tweet, second) ->
          Timber.d("status updated, and previous status loaded: ${tweet?.text}")
          toast(getString(R.string.message_tweet_succeeded))?.show()
          analytics.tweetFromEditor()
          statusAdapter.updatePreviousTweetAndClearEditor(if (tweet == null) emptyList<Tweet>() else listOf(tweet), second)
        }

    viewModel.messages.bindToLifecycle()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { toastLong(it)?.show() }

    sendButtonClicks.bindToLifecycle()
        .subscribe { viewModel.onSendStatus() }
  }

  fun initToolbar() {
    actionBar?.apply {
      setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)
    }
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    super.onCreateOptionsMenu(menu, inflater)

    inflater.inflate(R.menu.menu_compose_status, menu)
  }

  override fun onPrepareOptionsMenu(menu: Menu) {
    super.onPrepareOptionsMenu(menu)

    Observable.zip(
        viewModel.isSendableStatus,
        viewModel.progressEvents,
        { sendable, progress -> Pair(sendable, progress) })
        .first()
        .toBlocking()
        .subscribe {
          menu.findItem(R.id.action_send_tweet)?.isEnabled = it.first && it.second == ComposeStatusViewModel.ProgressEvent.FINISHED
        }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    val id = item.itemId
    when (id) {
      R.id.action_send_tweet -> {
        Timber.d("option - action_send_tweet")
        sendButtonClicks.onNext(Unit)
        return true
      }
      else -> {
        return super.onOptionsItemSelected(item)
      }
    }
  }

  override fun handleBack(): Boolean {
    Timber.d("handleBack")
    if (viewModel.canClose) {
      return super.handleBack()
    }

    showConfirmCloseDialog()

    return true
  }

  override fun onChangeEnded(changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) {
    super.onChangeEnded(changeHandler, changeType)
    refWatcherDelegate.handleOnChangeEnded(isDestroyed, changeType)
  }

  override fun onDestroy() {
    super.onDestroy()
    viewModel.onDestroy()
    refWatcherDelegate.handleOnDestroy()
  }

  fun showConfirmCloseDialog() {
    activity?.let {
      Timber.tag("Dialog").i("showConfirmationCloseDialog")
      AlertDialog.Builder(it)
          .setTitle(R.string.label_confirm)
          .setMessage(R.string.label_cancel_confirm)
          .setCancelable(true)
          .setNegativeButton(
              R.string.label_no,
              { dialog, _ ->
                viewModel.onConfirmCloseView(allowCloseView = false)
                dialog.dismiss()
              })
          .setPositiveButton(
              R.string.label_quit,
              { _, _ ->
                viewModel.onConfirmCloseView(allowCloseView = true)
                activity?.onBackPressed()
              }).show()
    }
  }

  fun showLoadingState() {
    activity?.invalidateOptionsMenu()
    getChildRouter(bindings.overlayRoot, null)
        .setPopsLastView(true)
        .setRoot(RouterTransaction.with(ProgressController())
            .popChangeHandler(FadeChangeHandler())
            .pushChangeHandler(FadeChangeHandler()))
  }

  fun hideLoadingState() {
    val childRouter = getChildRouter(bindings.overlayRoot, null)
    if (childRouter.backstackSize == 0) {
      return
    }

    activity?.invalidateOptionsMenu()
    childRouter.popCurrentController()
  }

  @SuppressLint("NewApi")
  fun reportShortcutUsedIfNeeded(status: String?) {
    if (status.isNullOrEmpty()) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
        applicationContext?.getSystemService(ShortcutManager::class.java)?.reportShortcutUsed("newtweet")
      }
    }
  }

  inner class Bindings(view: View) {
    val list = view.findById<RecyclerView>(R.id.list)
    val overlayRoot = view.findById<FrameLayout>(R.id.overlay_root)
  }
}