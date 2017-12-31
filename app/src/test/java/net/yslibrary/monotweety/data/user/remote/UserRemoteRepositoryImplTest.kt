package net.yslibrary.monotweety.data.user.remote

import com.google.gson.Gson
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.services.AccountService
import net.yslibrary.monotweety.data.user.User
import net.yslibrary.monotweety.readJsonFromAssets
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import retrofit2.Call
import com.twitter.sdk.android.core.models.User as TwitterUser

class UserRemoteRepositoryImplTest {

  lateinit var repository: UserRemoteRepositoryImpl
  lateinit var mockAccountService: AccountService
  lateinit var mockCall: Call<TwitterUser>
  lateinit var callbackCaptor: ArgumentCaptor<Callback<TwitterUser>>

  val gson = Gson()


  @Suppress("UNCHECKED_CAST")
  @Before
  fun setup() {
    mockAccountService = mock<AccountService>()
    mockCall = mock<Call<TwitterUser>>()
    callbackCaptor = ArgumentCaptor.forClass(Callback::class.java) as ArgumentCaptor<Callback<TwitterUser>>

    repository = UserRemoteRepositoryImpl(mockAccountService)
  }

  @Test
  fun get() {
    val user = gson.fromJson(readJsonFromAssets("user.json"), TwitterUser::class.java)
    val result = User(
        id = user.id,
        name = user.name,
        screenName = user.screenName,
        profileImageUrl = user.profileImageUrl,
        _updatedAt = -1)

    whenever(mockAccountService.verifyCredentials(any(), any(), any()))
        .thenReturn(mockCall)

    repository.get().test()
        .apply {
          verify(mockCall).enqueue(callbackCaptor.capture())
          callbackCaptor.value.success(Result(user, null))

          assertValue(result)
          assertComplete()
        }
  }
}