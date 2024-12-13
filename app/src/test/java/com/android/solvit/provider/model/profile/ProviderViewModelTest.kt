package com.android.solvit.provider.model.profile

import com.android.solvit.shared.model.authentication.AuthRepository
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.model.service.Services
import com.google.firebase.Timestamp
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ProviderViewModelTest {
  private lateinit var authRepository: AuthRepository
  private lateinit var authViewModel: AuthViewModel

  private lateinit var providerRepository: ProviderRepository
  private lateinit var providerViewModel: ProviderViewModel
  val provider =
      Provider(
          "test",
          "test",
          Services.PLUMBER,
          "",
          "",
          "",
          Location(0.0, 0.0, "EPFL"),
          "",
          false,
          0.0,
          0.0,
          Timestamp.now(),
          emptyList())

  @Before
  fun setUp() {
    authRepository = Mockito.mock(AuthRepository::class.java)
    authViewModel = AuthViewModel(authRepository)
    whenever(authRepository.getUserId()).thenReturn("1234")
    providerRepository = Mockito.mock(ProviderRepository::class.java)
    providerViewModel = ProviderViewModel(providerRepository)
    Mockito.`when`(providerRepository.getProvider(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(Provider) -> Unit>(1)
      onSuccess(provider)
    }
  }

  @Test
  fun getProvider() {
    providerViewModel.getProvider("1234")
    verify(providerRepository).getProvider(any(), any(), any())
  }

  @Test
  fun testOptimizeRouteBooking() {
    providerViewModel.getProvider("1234")
    val locations =
        listOf(
            Location(1.0, 1.0, "Location A"),
            Location(2.0, 2.0, "Location B"),
            Location(3.0, 3.0, "Location C"))

    val expectedRoute = listOf(locations[0], locations[1], locations[2])

    val optimizedRoute = providerViewModel.optimizeRouteBooking(locations)

    // Step 3: Verify that the result matches the expected route order
    TestCase.assertEquals(expectedRoute.size, optimizedRoute.size)
    for (i in expectedRoute.indices) {
      TestCase.assertEquals(expectedRoute[i].name, optimizedRoute[i].name)
    }
  }
}
