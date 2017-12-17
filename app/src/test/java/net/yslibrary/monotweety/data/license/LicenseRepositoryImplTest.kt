package net.yslibrary.monotweety.data.license

import net.yslibrary.licenseadapter.Library
import net.yslibrary.monotweety.ConfiguredRobolectricTestRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import rx.observers.TestSubscriber
import kotlin.properties.Delegates

@RunWith(ConfiguredRobolectricTestRunner::class)
class LicenseRepositoryImplTest {

  var repository: LicenseRepositoryImpl by Delegates.notNull<LicenseRepositoryImpl>()

  @Before
  fun setup() {
    repository = LicenseRepositoryImpl()
  }

  @Test
  fun get() {
    val ts = TestSubscriber<List<Library>>()

    repository.get().subscribe(ts)

    ts.assertValueCount(1)
    ts.assertCompleted()
  }
}