import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.NavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.provider.ui.profile.ProfessionalProfileScreen
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.seeker.ui.profile.EditSeekerProfileScreen
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.provider.Language
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.ui.navigation.NavigationActions
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

@RunWith(AndroidJUnit4::class)
class ProfessionalProfileScreenTest {

    private lateinit var navController: NavController
    private lateinit var navigationActions: NavigationActions
    private lateinit var providerRepository: ProviderRepository
    private lateinit var providerViewModel: ListProviderViewModel

    private val provider = Provider(
        uid = "user123",
        name = "John Doe",
        companyName = "Company",
        phone = "1234567890",
        location = Location(0.0, 0.0, "Chemin des Triaudes"),
        description = "Description",
        rating = 4.5,
        price = 50.0,
        languages = listOf(Language.ENGLISH, Language.FRENCH)
    )

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        navController = mock(NavController::class.java)
        navigationActions = NavigationActions(navController)
        providerRepository = mock(ProviderRepository::class.java)
        providerViewModel = ListProviderViewModel(providerRepository)

        composeTestRule.setContent {
            ProfessionalProfileScreen(
                listProviderViewModel = providerViewModel,
                userId = "user123",
                navigationActions = navigationActions
            )
        }

        `when`(providerRepository.getProviders(, any(), any())).thenAnswer { invocation ->
            val onSuccess = invocation.arguments[1] as (List<Provider>) -> Unit
            onSuccess(listOf(provider))
        }
    }

    @Test
    fun professionalProfileScreen_displaysCorrectly() {

        composeTestRule.onNodeWithTag("background").assertIsDisplayed()
        composeTestRule.onNodeWithTag("backButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profileImage").assertIsDisplayed()
        composeTestRule.onNodeWithTag("professionalName").assertIsDisplayed()
        composeTestRule.onNodeWithTag("companyNameTitle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("companyName").assertIsDisplayed()
        composeTestRule.onNodeWithTag("professionTitle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profession").assertIsDisplayed()
        composeTestRule.onNodeWithTag("contactTitle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("contact").assertIsDisplayed()
        composeTestRule.onNodeWithTag("locationTitle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("location").assertIsDisplayed()
        composeTestRule.onNodeWithTag("positionTitle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("position").assertIsDisplayed()
        composeTestRule.onNodeWithTag("jobsDoneTitle").assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("jobItem").assertCountEquals(4)
        composeTestRule.onNodeWithTag("statsSection").assertIsDisplayed()
    }
}

