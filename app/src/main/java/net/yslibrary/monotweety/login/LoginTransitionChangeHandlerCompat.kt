package net.yslibrary.monotweety.login

import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.bluelinelabs.conductor.changehandler.TransitionChangeHandlerCompat

class LoginTransitionChangeHandlerCompat :
    TransitionChangeHandlerCompat(LoginTransitionChangeHandler(), FadeChangeHandler())
