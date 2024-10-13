package com.android.solvit.seeker.ui.navigation

/*
@RunWith(Enclosed::class)
class SeekerBottomNavigationMenuTest {

  private lateinit var navController: NavController
  private lateinit var navigationActions: NavigationActions

  @Before
  fun setUp() {
    navController = Mockito.mock(NavController::class.java)
    navigationActions = NavigationActions(navController)
  }

  @RunWith(AndroidJUnit4::class)
  class MyComposeEspressoTest {

    @get:Rule val composeTestRule = createComposeRule()


    @Test
    fun testMyComposableWithEspresso() {
      // Set the Compose content for testing
      composeTestRule.setContent {
        SeekerBottomNavigationMenu(
            onTabSelect = {}, tabList = LIST_TOP_LEVEL_DESTINATION, selectedItem = "home")
      }

      val tab = TopLevelDestinations.SERVICES.textId

      // Verify the button is displayed
      composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
      composeTestRule.onNodeWithTag(tab).assertIsDisplayed()
    }

    @Test
    fun testBottomNavigationItemClick() {
      var selectedTab: TopLevelDestination? = null
      val tabList =
          listOf(
              TopLevelDestinations.SERVICES,
              TopLevelDestinations.CREATE_REQUEST,
              TopLevelDestinations.MESSAGE,
              TopLevelDestinations.PROFILE,
              TopLevelDestinations.ORDER)

      composeTestRule.setContent {
        SeekerBottomNavigationMenu(
            onTabSelect = { selectedTab = it },
            tabList = tabList,
            selectedItem = TopLevelDestinations.SERVICES.route)
      }

      // Simulate click on the "Request" tab
      composeTestRule.onNodeWithTag(TopLevelDestinations.SERVICES.textId).performClick()

      // Verify that the selected tab is "Request"
      assert(selectedTab == TopLevelDestinations.SERVICES)
    }
  }
}*/
