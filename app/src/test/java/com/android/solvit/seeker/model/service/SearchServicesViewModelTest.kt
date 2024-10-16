package com.android.solvit.seeker.model.service

import com.android.solvit.seeker.ui.service.SERVICES_LIST
import com.android.solvit.shared.model.service.Services
import org.junit.Before
import org.junit.Test

class SearchServicesViewModelTest {

  private lateinit var viewModel: SearchServicesViewModel

  @Before
  fun setUp() {
    viewModel = SearchServicesViewModel()
  }

  @Test
  fun onSearchTextChange() {
    viewModel.onSearchTextChange("Plumber")
    assert(viewModel.searchText.value == "Plumber")
    viewModel.onSearchTextChange("")
    assert(viewModel.searchText.value == "")
  }

  @Test
  fun onToggleSearch() {
    viewModel.onToggleSearch()
    assert(viewModel.isSearching.value)
    viewModel.onToggleSearch()
    assert(!viewModel.isSearching.value)
  }

  @Test
  fun servicesList() {
    for (service in SERVICES_LIST) {
      assert(viewModel.servicesList.value.contains(service))
    }
    viewModel.onSearchTextChange("Plumber")
    assert(viewModel.servicesList.value[0].service == Services.PLUMBER)
  }
}
