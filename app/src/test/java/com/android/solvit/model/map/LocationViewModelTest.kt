package com.github.se.bootcamp.model.map

import com.android.solvit.model.map.LocationRepository
import com.android.solvit.model.map.LocationViewModel
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class LocationViewModelTest {
  private lateinit var locationRepository: LocationRepository
  private lateinit var locationViewModel: LocationViewModel

  @Before
  fun setUp() {
    locationRepository = mock(LocationRepository::class.java)
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
    verify(locationRepository).search(eq(""), any(), any())
  }

  @Test
  fun setQueryCallsRepository() {
    locationViewModel.setQuery("EPFL")
    verify(locationRepository).search(eq("EPFL"), any(), any())
  }
}
