import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.provider.ui.profile.JobsDoneSection
import com.android.solvit.provider.ui.profile.ProfileHeader
import com.android.solvit.provider.ui.profile.StatsSection
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.provider.Language
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class ProfessionalProfileScreenTest {

  private lateinit var mockNavigationActions: NavigationActions
  private lateinit var providerRepository: ProviderRepository
  private lateinit var providerViewModel: ListProviderViewModel

  private val provider =
      Provider(
          uid = "user123",
          name = "John Doe",
          companyName = "Company",
          phone = "1234567890",
          location = Location(0.0, 0.0, "Chemin des Triaudes"),
          description = "Description",
          rating = 4.5,
          price = 50.0,
          languages = listOf(Language.ENGLISH, Language.FRENCH))

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    mockNavigationActions = mock(NavigationActions::class.java)
    providerRepository = mock(ProviderRepository::class.java)
    providerViewModel = ListProviderViewModel(providerRepository)
  }

  @Test
  fun providerProfileScreen_profileHeader_displaysCorrectly() {

    composeTestRule.setContent { ProfileHeader(mockNavigationActions, provider) }

    composeTestRule.onNodeWithTag("backButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profileImage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("professionalName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("companyNameTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("companyName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("serviceTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("service").assertIsDisplayed()
    composeTestRule.onNodeWithTag("contactTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("contact").assertIsDisplayed()
    composeTestRule.onNodeWithTag("locationTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("location").assertIsDisplayed()
    composeTestRule.onNodeWithTag("logoutButton").assertIsDisplayed()
  }

  @Test
  fun providerProfileScreen_profileHeader_performClick() {

    composeTestRule.setContent { ProfileHeader(mockNavigationActions, provider) }

    composeTestRule.onNodeWithTag("backButton").performClick()
    verify(mockNavigationActions).goBack()
  }

  @Test
  fun providerProfileScreen_logoutButton_performClick() {

    composeTestRule.setContent { ProfileHeader(mockNavigationActions, provider) }

    composeTestRule.onNodeWithTag("logoutButton").performClick()
  }

  @Test
  fun providerProfileScreen_JobsDoneSection_displaysCorrectly() {

    composeTestRule.setContent { JobsDoneSection() }
    composeTestRule.onNodeWithTag("jobsDoneTitle").assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("jobItem").assertCountEquals(4)
  }

  @Test
  fun providerProfileScreen_StatsSection_displaysCorrectly() {

    composeTestRule.setContent { StatsSection(provider) }
    composeTestRule.onNodeWithTag("statsSection").assertIsDisplayed()
  }
}
