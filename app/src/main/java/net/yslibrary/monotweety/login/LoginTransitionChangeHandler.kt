package net.yslibrary.monotweety.login

import android.annotation.TargetApi
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.transition.ArcMotion
import androidx.transition.Fade
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import androidx.transition.TransitionSet
import com.bluelinelabs.conductor.changehandler.androidxtransition.TransitionChangeHandler
import net.yslibrary.monotweety.R

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class LoginTransitionChangeHandler : TransitionChangeHandler() {
    override fun getTransition(
        container: ViewGroup,
        from: View?,
        to: View?,
        isPush: Boolean
    ): Transition {
        val context = container.context

        val move = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        move.addTarget(context.getString(R.string.transition_name_logo))

        val fade = Fade(Fade.MODE_IN)
        fade.addTarget(R.id.login)
        fade.addTarget(R.id.app_icon)

        val transitionSet = TransitionSet()
            .setOrdering(TransitionSet.ORDERING_SEQUENTIAL)
            .addTransition(move)
            .addTransition(fade)

        transitionSet.setPathMotion(ArcMotion())
        transitionSet.interpolator = DecelerateInterpolator()

        return transitionSet
    }
}
