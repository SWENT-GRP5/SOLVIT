package com.android.solvit.shared.model.map

import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class LocationViewModelTest {
  private lateinit var locationRepository: LocationRepository
  private lateinit var locationViewModel: LocationViewModel

  @Before
  fun setUp() {
    locationRepository = Mockito.mock(LocationRepository::class.java)
    locationViewModel = LocationViewModel(locationRepository)
  }

  @Test
  fun locationSuggestionsAreEmptyInitially() {
    assert(locationViewModel.locationSuggestions.value.isEmpty())
  }

  @Test
  fun queryIsEmptyInitially() {
    assert(locationViewModel.query.value.isEmpty())
  }

  @Test
  fun setQueryCallsRepositoryWithEmptyString() {
    locationViewModel.setQuery("")
    Mockito.verify(locationRepository).search(eq(""), any(), any())
  }

  @Test
  fun setQueryCallsRepository() {
    locationViewModel.setQuery("EPFL")
    Mockito.verify(locationRepository).search(eq("EPFL"), any(), any())
  }
}