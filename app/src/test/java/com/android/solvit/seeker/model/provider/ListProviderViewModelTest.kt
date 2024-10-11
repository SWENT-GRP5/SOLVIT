package com.android.solvit.seeker.model.provider

import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.model.provider.Services
import com.google.firebase.Timestamp
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify

class ListProviderViewModelTest {
  private lateinit var providerRepository: ProviderRepository
  private lateinit var listProviderViewModel: ListProviderViewModel
  private lateinit var provider: Provider

  @Before
  fun setup() {
    providerRepository = Mockito.mock(ProviderRepository::class.java)
    listProviderViewModel = ListProviderViewModel(providerRepository)
    provider =
        Provider(
            "test",
            "test",
            Services.PLUMBER,
            "",
            Location(0.0, 0.0, "EPFL"),
            "",
            false,
            0.0,
            0.0,
            Timestamp.now(),
            emptyList()
        )
  }

  @Test
  fun getNewUid() {
    Mockito.`when`(providerRepository.getNewUid()).thenReturn("test uid")
      MatcherAssert.assertThat(listProviderViewModel.getNewUid(), CoreMatchers.`is`("test uid"))
  }

  @Test
  fun addProvider() {
    listProviderViewModel.addProvider(provider)
    verify(providerRepository).addProvider(eq(provider), any(), any())
  }

  @Test
  fun deleteProvider() {
    listProviderViewModel.deleteProvider(provider.uid)
    verify(providerRepository).deleteProvider(eq(provider.uid), any(), any())
  }

  @Test
  fun updateProvider() {
    listProviderViewModel.updateProvider(provider)
    verify(providerRepository).updateProvider(eq(provider), any(), any())
  }

  @Test
  fun getProviders() {
    listProviderViewModel.getProviders()
    verify(providerRepository).getProviders(eq(null), any(), any())
  }

  @Test
  fun getProvidersWithService() {
    listProviderViewModel.selectService(Services.TUTOR)
    listProviderViewModel.getProviders()
    verify(providerRepository).getProviders(eq(Services.TUTOR), any(), any())
  }

  @Test
  fun selectService() {
    listProviderViewModel.selectService(Services.PLUMBER)
      MatcherAssert.assertThat(
          listProviderViewModel.selectedService.value,
          CoreMatchers.`is`(Services.PLUMBER)
      )
  }
}