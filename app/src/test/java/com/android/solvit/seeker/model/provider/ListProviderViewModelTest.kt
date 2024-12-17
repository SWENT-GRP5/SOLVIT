package com.android.solvit.seeker.model.provider

import android.net.Uri
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.provider.Language
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.model.service.Services
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
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
            "",
            "",
            Location(0.0, 0.0, "EPFL"),
            "",
            false,
            0.0,
            0.0,
            20.0,
            emptyList())
  }

  @Test
  fun getNewUid() {
    Mockito.`when`(providerRepository.getNewUid()).thenReturn("test uid")
    MatcherAssert.assertThat(listProviderViewModel.getNewUid(), CoreMatchers.`is`("test uid"))
  }

  @Test
  fun addProvider() {
    val imageUri = mock(Uri::class.java)
    listProviderViewModel.addProvider(provider, imageUri)
    verify(providerRepository).addProvider(eq(provider), eq(imageUri), any(), any())
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
        listProviderViewModel.selectedService.value, CoreMatchers.`is`(Services.PLUMBER))
  }

  @Test
  fun getProviderById() {
    runBlocking {
      listProviderViewModel.fetchProviderById("1234")
      verify(providerRepository).returnProvider("1234")
    }
  }

  @Test
  fun filterStringFields() = runBlocking {
    val filterField = "Test Field"
    val filterAction: (Provider) -> Boolean = { it.service == Services.PLUMBER }
    val defaultFilterAction: (Provider) -> Boolean = { it.service != Services.PLUMBER }

    listProviderViewModel.filterStringFields(
        iconPressed = true,
        filterCondition = false,
        filterAction = filterAction,
        defaultFilterAction = defaultFilterAction,
        filterField = filterField)

    verify(providerRepository).filterProviders(any())
  }

  @Test
  fun updateSelectedLanguages() = runBlocking {
    val newLanguages = setOf(Language.ENGLISH, Language.FRENCH)
    val languagePressed = Language.ENGLISH

    listProviderViewModel.updateSelectedLanguages(newLanguages, languagePressed)

    MatcherAssert.assertThat(
        listProviderViewModel.selectedLanguages.value, CoreMatchers.`is`(newLanguages))
    verify(providerRepository).filterProviders(any())
  }

  @Test
  fun clearFilterFields() = runBlocking {
    listProviderViewModel.clearFilterFields()

    MatcherAssert.assertThat(
        listProviderViewModel.selectedLanguages.value, CoreMatchers.`is`(emptySet()))
    MatcherAssert.assertThat(
        listProviderViewModel.selectedRatings.value, CoreMatchers.`is`(emptySet()))
    MatcherAssert.assertThat(listProviderViewModel.minPrice.value, CoreMatchers.`is`(""))
    MatcherAssert.assertThat(listProviderViewModel.maxPrice.value, CoreMatchers.`is`(""))
  }

  @Test
  fun updateSelectedRatings() = runBlocking {
    val newRatings = setOf(4.0, 5.0)
    val ratingPressed = 5.0

    listProviderViewModel.updateSelectedRatings(newRatings, ratingPressed)

    MatcherAssert.assertThat(
        listProviderViewModel.selectedRatings.value, CoreMatchers.`is`(newRatings))
    verify(providerRepository).filterProviders(any())
  }

  @Test
  fun updateMinPrice_validInput() = runBlocking {
    val minPrice = "100"
    listProviderViewModel.updateMinPrice(minPrice)

    MatcherAssert.assertThat(listProviderViewModel.minPrice.value, CoreMatchers.`is`(minPrice))
    verify(providerRepository).filterProviders(any())
  }

  @Test
  fun updateMinPrice_invalidInput() = runBlocking {
    val minPrice = "invalid"
    listProviderViewModel.updateMinPrice(minPrice)

    MatcherAssert.assertThat(listProviderViewModel.minPrice.value, CoreMatchers.`is`(minPrice))
    verify(providerRepository).filterProviders(any())
  }

  @Test
  fun updateMaxPrice_validInput() = runBlocking {
    val maxPrice = "500"
    listProviderViewModel.updateMaxPrice(maxPrice)

    MatcherAssert.assertThat(listProviderViewModel.maxPrice.value, CoreMatchers.`is`(maxPrice))
    verify(providerRepository).filterProviders(any())
  }

  @Test
  fun updateMaxPrice_invalidInput() = runBlocking {
    val maxPrice = "invalid"
    listProviderViewModel.updateMaxPrice(maxPrice)

    MatcherAssert.assertThat(listProviderViewModel.maxPrice.value, CoreMatchers.`is`(maxPrice))
    verify(providerRepository).filterProviders(any())
  }
}
